package YouTubeAnalizer.Request;

import YouTubeAnalizer.API.YoutubeInteractionService;
import YouTubeAnalizer.Cache.CacheService;
import YouTubeAnalizer.Entity.Channel;
import YouTubeAnalizer.Settings.SettingsService;
import com.gluonhq.particle.application.Particle;
import io.reactivex.Emitter;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class RequestService
{
    private static YoutubeInteractionService youtubeInteractionService = YoutubeInteractionService.getInstance();

    public static Observable<ArrayList<Channel>> channelStream;

    public static ObservableEmitter<ArrayList<Channel>> channelStreamer;

    public static Particle application = null;

    // todo
    // создать свой пул.
    // Однако, здесь https://allegro.tech/2014/10/async-rest.html
    // рекомендуют юзать RxJersey https://jersey.github.io/#rx-client.java8
    // https://stackoverflow.com/questions/39469435/working-with-jersey-client-in-rx-style
    // pool = Executors.newFixedThreadPool( 4 );
    public static ForkJoinPool pool = ForkJoinPool.commonPool();


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
        if ( SettingsService.getInstance().getSettings().isUseCache() )
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
                        }, pool
                )
                // [?] Почему не работает код: return youtubeInteractionService.mapChannels( w );
                .thenComposeAsync( w -> {
                            // worker-1
                            return CompletableFuture.supplyAsync( () -> {
                                // worker-2
                                return youtubeInteractionService.mapChannels( w ).stream();
                            } );
                        }
                )
                .whenComplete( (result, throwable) -> {
                            // worker-2
                            if ( throwable == null ) {
                                // OK - result is got
                            }
                            else {
                                System.out.println("error has been occurred during channel getting");
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
     * todo
     * это потребительский код?
     * должен ли getChannels() отдавать стрим? Имеет ли смысл передавать в callback() поток?
     *
     * http://reactivex.io/documentation/operators.html
     *
     * Код внутри колбэка делает:
     * render
     * запоминает время окончания запроса
     * отдает результат в кеш
     */
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
                        callback.accept( channelListOptional.get() );
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


}
