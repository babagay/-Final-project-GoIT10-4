package YouTubeAnalizer.Cache;

import YouTubeAnalizer.Entity.Channel;
import YouTubeAnalizer.Entity.ChannelCreator;
import YouTubeAnalizer.Settings.SettingsService;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.sun.javafx.binding.ExpressionHelper;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

/**
 * Хранилище данных кэша.
 * Единица хранилища - узел (Node), формируемый при разогреве кэша объект.
 * Новые ноды могут быть созданы в момент кеширования каналов, но на диске хранятся список запросов вместо списка нод.
 * Узлы хранятся в Storage.nodes - это поле представляет собой Level1-кеш, который не сбрасывается на диск.
 * Запросы в строковом виде, а также объекты каналов (Channel) хранятся в полях Storage.Repository.
 * Поле Storage.Repository.channels представляет собой Level2-кеш.
 * Объект класса Storage.Repository сбрасывается на диск. Например, при закрытии приложения.
 *
 * Запросы, сохраняемые в кеше, имеют вид строки: "channelA,channelB,channelC"
 *
 * fixme
 * [] не корректно работает с русской кодировкой
 * [] что-то не то с часовыми поясами - приходится задавать преувеличенные значения expirationTime
 */
final public class Storage {

    private Repository repository;

    private static class LazyHolder
    {
        public static final Storage StorageInstance = new Storage();
    }

    // todo
    // можно вычислять и запоминать при сохранении количество кнаалов и узлов,
    // чтобы при разогреве указывать мощность множества
    private Storage()
    {
        repository = new Repository();

        // nodes = Collections.synchronizedSortedSet( new TreeSet() );
        nodes = new ConcurrentSkipListSet();
    }
    
    public static Storage getInstance()
    {
        return LazyHolder.StorageInstance;
    }
    
    /**
     * кеш L1
     * не сохраняется на диск
     */
    Set<Node> nodes;

    public SortedSet<String> getRequests()
    {
        return repository.requests;
    }

    public SortedSet<Channel> getChannels()
    {
        return repository.channels;
    }
 
    private String cacheFileName;

    /**
     * Разогрев кеша
     */
    void init()
    {
        cacheFileName = getFilePath();
        String json = null;
        File storageFile = new File( cacheFileName );
        InputStream targetStream = null;

        File file = new File( cacheFileName );

        if ( !file.exists() )
        {
            if ( dirExists() ) {
                createFile();
                save();
            } else {
                createDir();
                createFile();
            }
        }

        try
        {
            targetStream = new FileInputStream( storageFile );
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }

        try ( final InputStreamReader reader = new InputStreamReader( targetStream, "UTF8" ) )
        {
            json = CharStreams.toString( reader );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        // init L2
        try {
            if ( !json.equals( "" ) ) {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter( Channel.class, new ChannelCreator() )
                        .create();
                repository = gson.fromJson( json, Repository.class );
            }
        } catch ( Throwable throwable ){
            throwable.printStackTrace();
        }

        repository.channels.parallelStream().forEach( Channel::restoreChannel );

        initLevel1();

        examine();

        CacheService.setWarmingIsFinished();

        processDeferedSetRequestsQueue();

        // todo
        // выводить сообщение, сколько узлов сгенерировано
        // сколько запросов закешировано
        // и сколько объектов
    }
    
    /**
     * Сохранить кеш в JSON-файл
     */
    void save()
    {
        String s = new Gson().toJson( repository );

        BufferedWriter writer;

        try
        {
            FileWriter fileWriter = new FileWriter( getFilePath() );

            writer = new BufferedWriter( fileWriter  );
    
            OutputStreamWriter
                    writer2 =
                    new OutputStreamWriter( new FileOutputStream( getFilePath() ), Charset.forName( "UTF-8" ) );

            writer2.write( s );

            writer2.flush();

            writer2.close();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }
    
    /**
     * поднять хранилище L1
     * восстановить список нод по списку requests
     */
    void initLevel1()
    {
        restoreNodes();
    }
    
    /**
     * @deprecated
     * поднять хранилище L2
     * восстановить список каналов
     */
    void initLevel2(){
    }

    /**
     * восстановить список узлов при разогреве кеша
     */
    void restoreNodes()
    {
        restoreNodesFromRequests();

        // if ( CacheService.shouldNodeBeCached() ) // [!] думаю, не очень хорошая практика
        restoreNodesFromL2();
    }

    /**
     * восстановить ноды на основании закешированных запросов
     */
    void restoreNodesFromRequests()
    {
        SortedSet<String> requests = getRequests();

        if ( requests != null && requests.size() > 0 )
        {
            getRequests().stream()
                    .map( Node::restoreNode )
                    .filter( Objects::nonNull )
                    .forEach( nodes::add );
        }
    }

    /**
     * восстановить ноды на основании закешированных каналов
     */
    void restoreNodesFromL2()
    {
        Storage.getInstance()
                .getChannels().stream()
                .forEach( channel -> {
                    Node node = Node.getFactory().create( channel.getChannelId(), channel );
                    putNode( node );
                } );
    }


    /**
     * Найти канал в L2 кеше
     * todo
     * похоже, нужно юзать Storage.getInstance().getChannels().parallelStream()
     */
    static Channel getChannelById(String channelId)
    {
         return Storage.getInstance().getChannels().stream()
                .filter( channel -> channel.getChannelId().equals( channelId ) )
                .findFirst().orElse( null );
    }

    /**
     * Взять все ноды, которые содержат канал с заданным id (названием)
     */
    static ArrayList<Node> getNodesByChannel(Channel channel)
    {
        return (ArrayList<Node>) getInstance().nodes.stream().filter( node -> node.containsChannel( channel ) ).collect( Collectors.toList() );
    }
  
    /**
     * удалить нерелевантные ноды
     */
    void cleanL1()
    {
        ArrayList<Node> nodesToDrop = nodes.stream().filter( Node::isNotRelevant ).collect( ArrayList::new, ArrayList::add, ArrayList::addAll );

        if ( nodesToDrop != null )
            nodesToDrop.parallelStream().forEach( Storage.getInstance()::removeNode );
    }

    /**
     * Удалить просроченные каналы
     */
    void cleanL2()
    {
        ArrayList<Channel> channelsToDrop = repository.channels.parallelStream().filter( Channel::isExpired ).collect( ArrayList::new, ArrayList::add, ArrayList::addAll );

        if ( channelsToDrop != null )
             channelsToDrop.stream().forEach( Storage.getInstance()::removeChannel );
    }

    /**
     * Удалить запросы, для которых нет узлов
     * todo если по данному реквесту нода восстановлена лишь частично, её надо удалить, а также удалить реквест
     */
    void cleanRequests()
    {
        ArrayList<String> remove = (ArrayList<String>) Storage.getInstance().repository.requests.stream()
                .filter( key -> {
                    try
                    {
                        if ( CacheService.getNode( key ) == null )
                        {
                            return true;
                        }
                        else
                        {
                            return false;
                        }
                    }
                    catch ( Exception e )
                    {
                        return true;
                    }
                } )
                .collect( Collectors.toList() );

        remove.stream().forEach( i -> Storage.getInstance().repository.requests.remove( i ) );
    }
    
    boolean isNodePresentedInCache(Node node)
    {
        if ( nodes == null )
            return false;

        if ( nodes.size() == 0 )
            return false;

        Node n = nodes.stream()
                .filter( node1 ->
                        node1.request.equals( node.request )
                )
                .findFirst()
                .orElse( null );
        
        return n != null;
    }
    
    boolean isRequestCached(String request)
    {
        String req = repository.requests.stream().filter( r -> r.equals( request ) ).findFirst().orElse( null );
        
        return req != null;
    }
    
    boolean isChannelCached(Channel channel)
    {
        Channel
                c =
                repository.channels.stream()
                        .filter( channel1 -> channel1.getChannelId().equals( channel.getChannelId() ) )
                        .findFirst()
                        .orElse( null );
        return c != null;
    }

    /**
     * проверить, нет ли уже этой ноды
     * добавить реквест в requests
     */
    void putNode(Node node)
    {
        if ( node != null )
        {
            if ( !isNodePresentedInCache( node ) )
            {
                nodes.add( node );

                if ( !isRequestCached( node.request ) )
                    repository.requests.add( node.request );
            }
        }
    }
 
    /**
     * Добавить канал, если канала с таким id нет в кеше
     */
    void putChannel(Channel channel)
    {
        if ( !isChannelCached( channel ) )
        repository.channels.add( channel );
    }

    /**
     * удалить из коллекции
     * удалить из реквестов
     */
    void removeNode (Node node)
    {
        String request = node.request;

        nodes.remove( node );

        //nodes.removeIf( node1 -> node1.request.equals( node.request ) );

//        try {
//            request =
//                    repository.requests.parallelStream()
//                                       .filter( request1 -> request1.equals( node.request ) )
//                                       .findFirst()
//                                       .orElseGet( null );
//        }
//        catch ( Throwable t ) {
//            t.printStackTrace();
//        }
//
//        if ( request != null ) { repository.requests.remove( request ); }

        repository.requests.remove( request );
    }

    void removeChannel(Channel channel)
    {
        repository.channels.removeIf( channel1 -> channel1.getChannelId().equals( channel.getChannelId() ) );
    }
    
    private class Repository
    {
        /**
         * Список запросов
         */
        @SerializedName("requests")
        SortedSet<String> requests;

        /**
         * кеш L2
         * список каналов
         */
        @SerializedName("channels")
        SortedSet<Channel> channels;

        public Repository()
        {
            if ( requests == null )
                requests = Collections.synchronizedSortedSet( new TreeSet() );

            if ( channels == null )
                channels =  Collections.synchronizedSortedSet( new TreeSet() );
        }
    }

    // todo
    // проверить на наличие битых реквестов, мертвых узлов и просроченных каналов
    // Возможно совместить с методом clean()
    private void examine()
    {

    }

    private void processDeferedSetRequestsQueue()
    {
        BlockingQueue<Runnable> queue = CacheService.getDeferedSetRequestsQueue();

        if ( queue.size() > 0 ){

            queue.stream().forEach( runnable -> {
                try {
                    queue.take();
                } catch ( InterruptedException e ) {
                }
                runnable.run();
            } );
        }
    }
   
    
    private boolean dirExists(){
    
        Path path = Paths.get( getDir() );
        return Files.exists( path );
        
    }
    
    private void createFile(){
        File file = new File( cacheFileName );
        try {
            file.createNewFile();
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }
    }
    private void createDir(){
        File theDir = new File(getDir());
        System.out.println("creating directory: " + theDir.getName());
        boolean result = false;
    
        try{
            theDir.mkdir();
            result = true;
        }
        catch(SecurityException se){
            //handle it
        }
        if(result) {
            System.out.println("DIR created");
        }
    }
    
    
    /**
     * путь к файлу кеша
     */
    private String getFilePath()
    {
        return SettingsService.getInstance().getSettings().getCacheFilePath();
    }
    
    /**
     * Папка кеша
     */
    private String getDir(){
        return SettingsService.getInstance().getSettings().getCacheDirectory();
    }
}
