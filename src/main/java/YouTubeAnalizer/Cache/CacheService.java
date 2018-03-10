package YouTubeAnalizer.Cache;

import YouTubeAnalizer.Entity.Channel;
import YouTubeAnalizer.Settings.SettingsService;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.AsyncSubject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * todo
 * когда запрашиваем ноду, проверяем, чтобы она была свежая и не мертвая
 */
public enum CacheService
{

    CACHE_SERVICE;

    CacheService()
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
     * Возвращать можно массив или список вместо ноды
     */
    public final static Node getNode(String key) throws Exception
    {
        CacheService cacheService = getInstance();

        key = cacheService.prepareKey( key );

        if ( !cacheService.isKeyValid( key ) )
        {
            throw new Exception( "ключ не корректный" );
        }

        String finalKey = key;
        return cacheService.fetchNodeFromCacheL1( key )
                .orElseGet( () ->
                        cacheService.fetchChannelsFromL2andCreateNode( finalKey ) );
    }

    /**
     * Обёртка для getNode()
     */
    public final static Set<Channel> get(String key)
    {
        Set<Channel> channels = null;

        try
        {
            channels = getNode( key ).channels;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        finally
        {
            return channels;
        }
    }

    /**
     * todo
     */
    public final static void set(String request, Channel... channels)
    {

    }

    /**
     * todo
     * Закешировать объект канала
     */
    public final static void set(Channel... channels)
    {
        AsyncSubject.fromArray( channels ).subscribeOn( Schedulers.computation() )
                .subscribe(
                        channelToStore -> {
                            //System.out.println(c.channelId);

                            // найти объект с заданным channelId в L2. Если он есть, это обновление. Если нет - добавление.
                            Channel channel = Storage.getChannelById( channelToStore.channelId );

                            if ( channel != null )
                            {
                                // Обновление: Взять все ноды, в которые входит данный channelId.
                                ArrayList<Node> connectedNodes = Storage.getNodesByChannel( channel );
                                //             Удалить канал channelId из кеша L2.
                                Storage.getInstance().removeChannel( channel );

                                channel.setExpirationDate( (int) System.currentTimeMillis() / 1000 + SettingsService.getInstance().getSettings().getExpirationTime() );

                                //             Выполнить добавление в L2.
                                Storage.getInstance().putChannel( channel );

                                connectedNodes.stream().forEach( node -> node.refresh() );

                                // если после рефреша есть мертвые ноды, их надо удалить
                                Storage.getInstance().clean();
                            }
                            else
                            {
                                // todo
                                // Добавление: Обновить время протухания канала. положить в L2. Создать ноду, обновить время протухания. положить ноду в L1.
                            }


                        },
                        e -> {
                            // System.out.println(e.getMessage());
                        },
                        () -> { // complete
                        }
                );
    }

    Optional<Node> fetchNodeFromCacheL1(String key)
    {
        return Storage.getInstance().nodes.stream()
                .filter( node -> node.request.equals( key ) )
                .findFirst();
    }

    /**
     * Если есть все требуемые каналы, создать ноду, добавить ее в сторадж и вернуть
     */
    Node fetchChannelsFromL2andCreateNode(String key)
    {
        Node node = new Node();
        node.setRequest( key );

        Arrays.stream( key.split( "," ) )
                .map( CacheService::getChannelById )
                .forEach( node::addChannel );

        if ( node.getChannelNumber() < key.split( "," ).length )
        {
            node = null;
        }

        Storage.getInstance().putNode( node );

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
    public final static void initStorage()
    {

        Storage.getInstance().initLevel2();
        Storage.getInstance().initLevel1();
    }

    /**
     * сбросить на диск
     */
    public final static void saveStorage()
    {

    }

    /**
     * Должны сохраняться при запуске
     */
    private static class Settings
    {
    }
}
