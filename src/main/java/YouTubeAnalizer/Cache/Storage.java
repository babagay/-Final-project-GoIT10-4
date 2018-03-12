package YouTubeAnalizer.Cache;

import YouTubeAnalizer.Entity.Channel;
import YouTubeAnalizer.Settings.SettingsService;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

final public class Storage {
    
    private static class LazyHolder
    {
        public static final Storage StorageInstance = new Storage();
    }
    
    private Storage()
    {
        if ( nodes == null )
            nodes = Collections.synchronizedSortedSet( new TreeSet() );

        if ( channels == null )
            channels =  Collections.synchronizedSortedSet( new TreeSet() );
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
    
    /**
     * список запросов
     * сохранять на диск
     */
     
    Set<String> requests;
    
    /**
     * кеш L2
     * список каналов
     * сохраняется на диск
     */
    
    SortedSet<Channel> channels;
    
    // todo в каком классе д.б. поле channels и requests
    
    // todo создать геттеры
    
    // todo переестить в сервис
    // isWarmingUp = true
    void init()
    {
    
    }
    
    /**
     * todo
     * поднять хранилище L1
     * восстановить список нод по списку requests
     */
    void initLevel1(){
    
    }
    
    /**
     * todo
     * поднять хранилище L2
     * восстановить список каналов
     */
    void initLevel2(){
    
    }
    
    /**
     * путь к файлу кеша
     */
    String getFilePath()
    {
        return SettingsService.getInstance().getSettings().getCacheFilePath();
    }

    /**
     * Найти канал в L2 кеше
     */
    static Channel getChannelById(String channelId)
    {
        return Storage.getInstance().channels.stream().filter( channel -> channel.channelId.equals( channelId ) ).findFirst().orElse( null );
    }

    /**
     * Взять все ноды, которые содержат канал с заданным id (названием)
     */
    static ArrayList<Node> getNodesByChannel(Channel channel)
    {
        return (ArrayList<Node>) getInstance().nodes.stream().filter( node -> node.containsChannel( channel ) ).collect( Collectors.toList() );
    }
  
    /**
     * удалить ноды с флагом isDead = true
     */
    void clean()
    {
        nodes.parallelStream().filter( Node::isDead ).forEach( Storage.getInstance()::removeNode );
    }
    
    boolean isNodePresentedInCache(Node node)
    {
        Node n = nodes.stream().filter( node1 -> node1.request.equals( node.request ) ).findFirst().orElse( null );
        
        return n != null;
    }
    
    boolean isRequestCached(String request)
    {
        String req = requests.stream().filter( r -> r.equals( request ) ).findFirst().orElse( null );
        
        return req != null;
    }
    
    boolean isChannelCached(Channel channel)
    {
        Channel
                c =
                channels.stream()
                        .filter( channel1 -> channel1.getChannelId().equals( channel.getChannelId() ) )
                        .findFirst()
                        .orElse( null );
        return c != null;
    }
    
    // проверить, нет лли уже этой ноды
    // добавить реквест в requests
    void putNode(Node node)
    {
        if ( !isNodePresentedInCache( node ) ) {
            nodes.add( node );
            
            if ( !isRequestCached( node.request ) )
                requests.add( node.request );
        }
    }
 
    /**
     * Добавить канал, если канала с таким id нет в кеше
     */
    void putChannel(Channel channel)
    {
        if ( !isChannelCached( channel ) )
        channels.add( channel );
    }
    
    // удалить из коллекции
    // удалить из реквестов
    void removeNode(Node node)
    {
        nodes.removeIf( node1 -> node1.request.equals( node.request ) );
        String
                request =
                requests.parallelStream()
                        .filter( request1 -> request1.equals( node.request ) )
                        .findFirst()
                        .orElseGet( null );
        
        if ( request != null )
        requests.remove( request );
    }

    void removeChannel(Channel channel)
    {
        channels.removeIf( channel1 -> channel1.getChannelId().equals( channel.getChannelId() ) );
    }
    
    private class Repository {
    
    }
}
