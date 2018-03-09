package YouTubeAnalizer.Cache;

import YouTubeAnalizer.Entity.Channel;

import java.util.*;

final public class Storage {
    
    private static class LazyHolder
    {
        public static final Storage StorageInstance = new Storage();
    }
    
    private Storage()
    {
        if ( nodes == null )
            nodes = Collections.synchronizedSortedSet( new TreeSet() );
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
     * список каналов
     * сохраняется на диск
     */
    Set<Channel> channels;
    
    /**
     * поднять хранилище L1
     * восстановить список нод по списку requests
     */
    void initLevel1(){
    
    }
    
    /**
     * поднять хранилище L2
     * восстановить список каналов
     */
    void initLevel2(){
    
    }
}
