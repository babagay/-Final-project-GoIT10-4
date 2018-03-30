package YouTubeAnalizer.Request;

import YouTubeAnalizer.API.YoutubeInteractionService;
import YouTubeAnalizer.Cache.CacheService;
import YouTubeAnalizer.Entity.Channel;
import YouTubeAnalizer.Settings.SettingsService;
import com.gluonhq.particle.application.Particle;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static YouTubeAnalizer.API.YoutubeInteractionService.getYouTubeService;

public class RequestService
{
    private static YoutubeInteractionService youtubeInteractionService = YoutubeInteractionService.getInstance();

    public static Observable<ArrayList<Channel>> channelStream;

    public static ObservableEmitter<ArrayList<Channel>> channelStreamer;

    public static Particle application = null;

    private final static int POOL_CAPACITY = 10_000;

    private static ThreadPoolExecutor requestPool = new ThreadPoolExecutor( 100, POOL_CAPACITY, 60L, TimeUnit.MINUTES, new ArrayBlockingQueue<>( POOL_CAPACITY ), Executors.defaultThreadFactory() );

    // private static Executor requestPool = Executors.newFixedThreadPool( 4 );
    // private static Executor requestPool = ForkJoinPool.commonPool();

    public static void init(Particle app)
    {
        application = app;

        channelStream = Observable.create( emitter -> {
            // This code will be run when subscriber appears
            RequestService.channelStreamer = emitter;
        } );
    }

    /**
     * Get channels from cache
     */
    public static ArrayList<Channel> getCachedChannels(String request)
    {
        if ( SettingsService.getInstance().getSettings().isUsedCache() )
        {
            // если включен кеш , пробуем взять из кеша сперва
            ArrayList<Channel> channels = CacheService.get( request );

            if ( channels.size() > 0 )
            {
                return channels;
            }
        }
        return null;
    }

    public static void shutdownPool()
    {
        requestPool.shutdown();
    }

    /**
     * Get channels from cache (Optional wrapper)
     */
    public static Optional<ArrayList<Channel>> getCachedChannelsOpt(String request)
    {
        Optional<ArrayList<Channel>> o = Optional.ofNullable( getCachedChannels( request ) );
        return o;
    }

    private static CompletableFuture<Stream<Channel>> getChannels(String request)
    {
        CompletableFuture<Stream<Channel>> futureStream = CompletableFuture
                .supplyAsync( () -> {
                            // worker-1
                            return youtubeInteractionService.getChannels( request );
                        }, requestPool
                )
                // [?] Почему не работает код: return youtubeInteractionService.mapChannels( w );
                .thenComposeAsync( w -> {
                            // worker-1
                            return CompletableFuture.supplyAsync( () -> {
                                // worker-2
                                return youtubeInteractionService.mapChannels( w ).parallelStream();
                            } );
                        },
                        requestPool
                )
                .whenComplete( (result, throwable) -> {
                            // worker-2
                            if ( throwable == null ) {
                                // OK - result is got
                            }
                            else {
                                System.out.println( "error has been occurred during channel getting" );
                                throw new RuntimeException( throwable );
                            }
                        }
                )
                .exceptionally( throwable -> {
                    System.out.println( throwable.getMessage() );
                    return null;
                } );

        return futureStream;
    }
    
    /**
     * взять число каментов по видео
     */
    private static List<Video> getVideoInfo(String videoId)
    throws IOException
    {
        YouTube youtube = getYouTubeService();
        VideoListResponse response = null;
        java.util.List<Video>  result = null;

        try {
            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("part", "statistics");
            parameters.put("id", videoId);

            YouTube.Videos.List videosListByIdRequest = youtube.videos().list(parameters.get("part").toString());
            if (parameters.containsKey("id") && parameters.get("id") != "") {
                videosListByIdRequest.setId(parameters.get("id").toString());
            }

            videosListByIdRequest.setFields( "items(statistics/commentCount)" );

            videosListByIdRequest.setMaxResults( 50L );

             response = videosListByIdRequest.execute();

             result = response.getItems();

        } catch ( Exception e ){
            e.printStackTrace();
        }

        return result;
    }
    
    /**
     * Альтернативная версия метода getChannelsWide()
     */
    private static CompletableFuture<List<Channel>> getChannelsWideAlter(String request)
    {
        return getChannels( request ).thenComposeAsync( channelStream1 -> {

            ArrayList<Channel> list = channelStream1
                    .map( channel1 -> setChannelVideoIds( channel1 ) )
                    .map( channel2 -> channel2.splitVideoIdsOnBatches() )
                    .map( channel3 -> calculateChannelVideosCommentTotal( channel3 ) )
                    .collect( ArrayList<Channel>::new, ArrayList::add, ArrayList::addAll );

            return CompletableFuture.supplyAsync( () -> list );
        });
    }

    /**
     * todo
     * в отдельном потоке
     */
    private static Channel setChannelVideoIds(Channel channel)
    {
        try {
            return channel.setVideoIds( getVideoIdsByChannel(channel) );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        return channel;
    }

    /**
     * Общее количество комментариев всех видео канала
     */
    private static Channel calculateChannelVideosCommentTotal(Channel channel)
    {
        Integer totalComments = channel.getVideoIdBatchesList().parallelStream()
                .map( RequestService::calculateCommentNumberForSequence )
                .reduce( 0, (a, b) -> a + b );

        channel.setTotalCommentsNumber( totalComments );

        return channel;
    }

    /**
     * Взять пачку видео и сосчетать количество каментов в ней
     * todo
     * Если делать в отдельном потоке каждый запрос, можно накидать потоков, которые складывают результаты в список
     * и потом сделать reduce.
     * Потоки можно складывать в очередь и запомнить ее размер.
     * После этого отправить очередь на исполнение
     * и мониторить количество элементов в списке результатов. Когда оно будет равно размеру очереди, выполнить reduce.
     */
    private static int calculateCommentNumberForSequence(String sequence)
    {
        int sequenceCommentNumber = 0;

        try {
            sequenceCommentNumber = getVideoInfo(sequence).parallelStream()
                    .map( video -> video.getStatistics().getCommentCount() )
                    .map( number -> number.intValue() )
                    .reduce( 0, (a,b) -> a + b );

        } catch ( IOException e ) {
            e.printStackTrace();
        }

        return sequenceCommentNumber;
    }

    /**
     * Первая реализация метода вычисления общего числа комментариев под всеми видео канала
     */
    private static CompletableFuture<List<Channel>> getChannelsWide(String request)
    {
        final AtomicReference<CountDownLatch> latchChannelList = new AtomicReference<>();
        final AtomicReference<CountDownLatch> latchIdSequences = new AtomicReference<>();
        final AtomicReference<List<Channel>> resultChannels = new AtomicReference<>();

        resultChannels.set( new ArrayList<>(10) );

        return getChannels( request ).thenComposeAsync( channelStream1 -> {

            ArrayList<Channel> list = channelStream1.collect( ArrayList<Channel>::new, ArrayList::add, ArrayList::addAll );

            latchChannelList.set( new CountDownLatch( list.size() ) );

            list.forEach( channel -> {

                AtomicLong videoCommentNumber = new AtomicLong();

                requestPool.execute( () -> {

                    try {
                        List<SearchResult> videoList = youtubeInteractionService.getVideos( channel, null, null, null );
                        
                        ArrayList<String> batchList = splitOnBatches( videoList );

                        latchIdSequences.set( new CountDownLatch( batchList.size() ) );

                        batchList.stream().forEach( idSequence -> requestPool.execute( () -> {

                            try {
                                int sequenceCommentNumber = getVideoInfo( idSequence ).stream()
                                        .map( item -> item.getStatistics()
                                                .getCommentCount() )
                                        .map( number -> number.intValue() )
                                        .reduce( 0, (a, b) -> a + b );

                                videoCommentNumber.set( videoCommentNumber.get() + sequenceCommentNumber );

                                latchIdSequences.get().countDown();

                            } catch ( IOException e ) {
                                e.printStackTrace();
                            }
                        } ) );

                        latchIdSequences.get().await();

                        channel.setTotalCommentsNumber( videoCommentNumber.get() );

                        resultChannels.get().add( channel );

                        latchChannelList.get().countDown();

                    } catch ( Throwable e ) {
                        e.printStackTrace(); // в единый поток ошибок
                    }
                } );
            } );

            try {
                latchChannelList.get().await();
            } catch ( InterruptedException e ) {
                e.printStackTrace();
            }

            return CompletableFuture.supplyAsync( () -> resultChannels.get() );

        }, requestPool )
                .exceptionally( throwable -> {
                    System.out.println( throwable.getMessage() );
                    throwable.printStackTrace();
                    return null;
                } );
    }
    
    private static List<String> getVideoIdsByChannel(Channel channel) throws IOException
    {
        return getVideosByChannel( channel ).parallelStream().map( item -> item.getId().getVideoId() )
                                     .collect( ArrayList::new, ArrayList::add, ArrayList::addAll );
    }
    
    private static List<SearchResult> getVideosByChannel(Channel channel) throws IOException
    {
        return youtubeInteractionService.getVideos( channel, null, null, null );
    }
    
    private static ArrayList<String> splitOnBatches(List<SearchResult> videoList)
    {
        ArrayList<String> batchList = new ArrayList<>();
        StringJoiner joiner = new StringJoiner( "," );
        int u = 0;
        
        for ( int i = 0; i < videoList.size(); i++ ) {
            if ( u++ < 50 ) {
                joiner.add( videoList.get( i ).getId().getVideoId() );
            }
            else {
                batchList.add( joiner.toString() );
                joiner = new StringJoiner( "," );
                u = 0;
            }
        }
        
        return batchList;
    }

    // можно запускать в отдельном потоке
    public static void get(String request, Consumer<ArrayList<Channel>> callback)
    {
        Observable.create(
                (ObservableEmitter<Optional<ArrayList<Channel>>> emitter) -> {
                    // Fetch channels from cache
                    emitter.onNext( getCachedChannelsOpt( request ) );
                    emitter.onComplete();
                }
        )
                .map( channelListOptional -> {
                    if ( channelListOptional.isPresent() )
                    {
                        try {
                            callback.accept( channelListOptional.get() );
                            System.out.println( "Результат взят из кеша" );
                        } catch ( Throwable t ){
                            t.printStackTrace();
                        }
                    }

                    return channelListOptional;
                } )
                .filter( channels -> !channels.isPresent() )
                .subscribe(
                        optional -> {

                            //  Fetch channels from youtube service
                            getChannels( request )
                                    .thenApply( channelStream -> {

                                        ArrayList<Channel> list = channelStream.collect( ArrayList<Channel>::new, ArrayList::add, ArrayList::addAll );

                                        try
                                        {
                                            callback.accept( list );

                                        } catch ( Throwable t )
                                        {
                                            // todo
                                            // [?] можно ранее сформировать канал ошибок и здесь эмитить в него
                                            System.out.println("Error occurred during callback running. " + t);
                                            t.getStackTrace();
                                        }

                                        return null;
                                    } );
                        },
                        throwable -> {
                            // todo
                            // обработка ошибок
                            System.out.println("Throwable was thrown");
                        }
                );
    }
    

    public static void getWide (String request, Consumer<ArrayList<Channel>> callback)
    {
        Optional<ArrayList<Channel>> cachedChannels = getCachedChannelsOpt( request );
        try {
            if ( cachedChannels.isPresent() ) {
                // Взяли из кеша
            
                callback.accept( cachedChannels.get() );

            }
            else {
                getChannelsWide( request ).whenCompleteAsync( (channelList, throwable) -> {
                    if (throwable == null){
                        callback.accept( (ArrayList<Channel>) channelList );
                    }
                }, requestPool );
            }
        }
        catch ( Throwable t ) {
            System.out.println(t.getMessage());
            t.getStackTrace();
        }
    }


}
