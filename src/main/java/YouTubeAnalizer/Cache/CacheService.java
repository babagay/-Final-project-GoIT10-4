package YouTubeAnalizer.Cache;

import YouTubeAnalizer.Entity.Channel;
import io.reactivex.Observable;
import io.reactivex.internal.schedulers.IoScheduler;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.AsyncSubject;


import javax.security.auth.Subject;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public enum CacheService {
    
    CACHE_SERVICE;
    
    CacheService ()
    {
    
    }
    
    private final static CacheService getInstance()
    {
        return CacheService.valueOf( "CACHE_SERVICE" );
    }
    
    /**
     * Поискать в L1
     * если нет, поискать в L2. Если есть, создать ноду, добавить в сторадж и вернуть её.
     * если в L2 тоже нет, вернуть null
     */
    public final static Node get (String key) throws Exception
    {
        CacheService cacheService = getInstance();
    
        key = cacheService.prepareKey( key );
    
        if ( !cacheService.isKeyValid( key ) ) { throw new Exception( "ключ не корректный" ); }
        
        String finalKey = key;
        return cacheService.fetchNodeFromCacheL1( key )
                           .orElseGet( () ->
                                               cacheService.fetchChannelsFromL2andCreateNode( finalKey ) );
    }
    
    /**
     * Закешировать канал
     * перезаписать объекты, в т.ч. обновить время протухания
     * можно в отдельном потоке
     * сохранить объект в L2
     * сформировать ноду и сохранить её в L1
     */
    public final static void set(Channel ... channels)
    {
    
        AsyncSubject.fromArray(channels).subscribeOn( Schedulers.computation() )
        .subscribe(
                c -> {
                    System.out.println(c.channelId);
                },
                e -> {
                    //System.out.println(e.getMessage());
                },
                () -> {
                    //System.out.println("A");
                }
                  );
        
         
    }
    
    Optional<Node> fetchNodeFromCacheL1 (String key)
    {
        return Storage.getInstance().nodes.stream()
                                          .filter( node -> node.request.equals( key ) )
                                          .findFirst();
    }
    
    /**
     * todo Если есть все требуемые каналы, создать ноду, добавить в сторадж и вернуть её.
     */
    Node fetchChannelsFromL2andCreateNode(String key)
    {
        Node node = new Node();
        node.setRequest( key );
        
        Arrays.stream( key.split( "," ) )
              .map( CacheService::getChannelById )
              .forEach( node::addChannel );
        
        if ( node.getChannelNumber() < key.split( "," ).length )
            node = null;
        
        return node;
    }
    
    static Channel getChannelById(String channelId)
    {
        return null;
    }
    
    /**
     * очистить от пробелов
     */
    String prepareKey(String key)
    {
        return key;
    }
    
    /**
     * отвалидировать ключ
     */
    private boolean isKeyValid(String key)
    {
        return true;
    }
    
    /**
     * разогрев кеша
     * в отделном потоке
     */
    void initStorage(){
    
        Storage.getInstance().initLevel2();
        Storage.getInstance().initLevel1();
    }
    
    /**
     * сбросить на диск
     */
    void saveStorage()
    {
    
    }
}
