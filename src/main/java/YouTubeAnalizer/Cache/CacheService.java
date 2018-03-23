package YouTubeAnalizer.Cache;

import YouTubeAnalizer.Entity.Channel;
import YouTubeAnalizer.Settings.SettingsService;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.AsyncSubject;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * [!] ConcurrentSkipListSet - видимо, более эффективная структура (по сравнению с synchronizedSortedSet), из которой можно получить и Set
 *      или Map
 *
 * [?] надо ли после разогрева запустить рефреш кэша в отдельном потоке или встроить его в разогрев
 *
 * fixme
 * программа отвалилась на другом компе из-за отсутсвия файла кеша по нестандартному пути
 * Кодировка файла кеша д.б. UTF8
 *
 * todo
 * заюзать контрол Browse
 *
 * [!]
 * время работы апи-запроса д.б. 10 сек
 */
public enum CacheService
{
    CACHE_SERVICE;

    private CountDownLatch channelSetLatch;

    private AtomicBoolean isWarmingUp = new AtomicBoolean( false );

    private BlockingQueue<Runnable> deferedSetRequests = initDeferedSetRequestsQueue();

    private long warmingTimeStart;

    private long warmingTimeEnd;

    private boolean doNotCacheNode = false;

    CacheService()
    {
    }

    public static BlockingQueue<Runnable> getDeferedSetRequestsQueue()
    {
        return getInstance().deferedSetRequests;
    }

    public static final void setWarmingIsFinished()
    {
        getInstance().isWarmingUp.set( false );

        getInstance().warmingTimeEnd = System.currentTimeMillis() / 1000;

        long warmingUpTime =  getInstance().warmingTimeEnd -  getInstance().warmingTimeStart;

        System.out.println("Разогрев кэша завершен. Потраченное время, сек: " + warmingUpTime);
    }

    public static boolean shouldNodeBeCached()
    {
        return getInstance().doNotCacheNode;
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
    public final static ArrayList<Channel> get(String key)
    {
        ArrayList<Channel> channels = new ArrayList<>(  );

        try
        {
            channels = getNode( trimRequest( key ) ).channels.stream()
                    .filter( Channel::isFresh )
                    .collect( ArrayList::new, ArrayList::add, ArrayList::addAll );
        }
        catch ( Exception e )
        {
            // getNode( key ) м.б. null
        }
        finally
        {
            return channels;
        }
    }

    /**
     * Перегруженная версия основного метода, позволяющая принудительно не кешировать
     * созданную в случае успешного поиска ноду
     * использовать: get(key, false) - чтобы не кешировать ноду
     * [!] метд не имеет смысла, т.к. при разогреве все ноды восстанавливаются
     */
    public final static ArrayList<Channel> get(String key, boolean doCreateNode)
    {
        if ( doCreateNode == false )
            getInstance().doNotCacheNode = true;

        return get( key );
    }
    
    /**
     * Обёртка для set(Channel... channels).
     * Наиболее общий вариант кеширования запроса.
     * [!] метод set(Channel) скрыт для того, чтобы вынудить пользовательский код указать строку запроса
     */
    public final static void set(String request, Channel... channels)
    {
        if ( CacheService.getInstance().isWarmingUpNow() )
        {
            // если в данный момент идет разогрев, положить запрос на модификацию в потокобезопасную очередь, которую можно обработать после разогрева
            getInstance().deferedSetRequests.add( new Thread( () -> setTask( request, channels ) ) );
        }
        else
        {
            setTask( request, channels );
        }
    }

    /**
     * Разогрев кеша в отдельном потоке
     */
    public final static void initStorage()
    {
        Thread initThread = new Thread( () -> {

            getInstance().setWarming();

            // имитация продолжительного разогрева
            try {
                Thread.sleep( 1 );
            } catch ( InterruptedException e ) {
            }

            Storage.getInstance().init();
        } );
        initThread.start();
    }

    /**
     * сбросить кеш на диск
     */
    public final static void saveStorage()
    {
        if ( getInstance().isWarmingUp.get() ) {

            // если кеш в данный момент разогревается, подождать завершения
            while ( getInstance().isWarmingUp.get() ) {
            }

            save();
        }
        else {
            save();
        }
    }

    // todo
    // наверное, надо использовать parallelStream()
    Optional<Node> fetchNodeFromCacheL1(String key)
    {
        return Storage.getInstance().nodes.stream()
                .filter( node -> node.request.equals( key ) )
                .filter( Node::isRelevant )
                .findFirst();
    }

    /**
     * Если есть все требуемые каналы и они не просрочены,
     * создать ноду, добавить ее в сторадж и вернуть
     */
    Node fetchChannelsFromL2andCreateNode(String key)
    {
        Node restoredNode = Node.getFactory().reproduce( key );

        if ( restoredNode != null )
        {
            if ( getInstance().doNotCacheNode == false )
            Storage.getInstance().putNode( restoredNode );
        }

        return restoredNode;
    }

    /**
     * очистить от пробелов
     */
    String prepareKey(String key)
    {
        return key;
    }

    private static ArrayBlockingQueue<Runnable> initDeferedSetRequestsQueue()
    {
        return new ArrayBlockingQueue<>( 10 );
    }

    private final static CacheService getInstance()
    {
        return CacheService.valueOf( "CACHE_SERVICE" );
    }

    private final static void setTask(String request, Channel... channels)
    {
        getInstance().channelSetLatch = new CountDownLatch( channels.length );

        set( channels );

        try
        {
            getInstance().channelSetLatch.await();
        }
        catch ( InterruptedException e )
        {
        }

        Node node = Node.getFactory().create( trimRequest( request ), channels );

        Storage.getInstance().putNode( node );
    }

    /**
     * todo можно сделать в отдельном потоке
     */
    private static void save()
    {
        clean();

        Storage.getInstance().save();

        System.out.println( "Cache saved" );
    }

    private static void clean()
    {
        Storage.getInstance().cleanL1();
        Storage.getInstance().cleanL2();
        Storage.getInstance().cleanRequests();
    }

    /**
     * отвалидировать ключ
     */
    private boolean isKeyValid(String key)
    {
        if ( key.equals( "" ) )
            return false;

        String trimmed = trimRequest( key );

        if ( !trimmed.equals( key ) )
            return false;

        return true;
    }

    /**
     * true, если происходит разогрев кеша
     */
    private boolean isWarmingUpNow()
    {
        return getInstance().isWarmingUp.get();
    }

    private void setWarming()
    {
        getInstance().isWarmingUp.set( true );

        getInstance().warmingTimeStart = System.currentTimeMillis() / 1000;
    }

    /**
     * Допустимые знаки: A-Z 0-9 _ - , .
     */
    private static String trimRequest(String request)
    {
        return request.replaceAll( "[^a-zA-Z0-9 _\\-.,]","" );
    }

    /**
     * Время протухания от текущего момента
     */
    private static long generateExpirationTime()
    {
        long delta = SettingsService.getInstance().getSettings().getExpirationTime();

        long now = System.currentTimeMillis() / 1000;

        return now + delta;
    }

    /**
     * Закешировать объект[ы] канала
     */
    private final static void set(Channel... channels)
    {
        AsyncSubject.fromArray( channels ).subscribeOn( Schedulers.computation() )
                .subscribe(
                        channelToStore -> {

                            // найти объект с заданным channelId в L2. Если он есть, это обновление. Если нет - добавление.
                            Channel channel = Storage.getChannelById( channelToStore.getChannelId() );

                            if ( channel != null )
                            {
                                // Взять все ноды, в которые входит данный channelId.
                                ArrayList<Node> connectedNodes = Storage.getNodesByChannel( channel );

                                // Удалить канал channelId из кеша L2.
                                Storage.getInstance().removeChannel( channel );

                                channel.setExpirationDate( generateExpirationTime() );

                                // Выполнить добавление в L2.
                                Storage.getInstance().putChannel( channel );

                                connectedNodes.stream().forEach( node -> node.refresh() );

                                // если после рефреша появились мертвые ноды, их надо удалить
                                Storage.getInstance().cleanL1();
                            }
                            else
                            {
                                // Обновить время протухания канала.
                                channelToStore.setExpirationDate( generateExpirationTime() );

                                // положить в L2.
                                Storage.getInstance().putChannel( channelToStore );

                                // Создать ноду, обновить время протухания.
                                Node node = new Node( channelToStore.getChannelId(), Node.getFactory().generateChannelSet(channelToStore) );
                                node.recalcExpirationDate();

                                // положить ноду в L1.
                                Storage.getInstance().putNode( node );

                                // Debug
                                // System.out.println("канал " + channelToStore.getChannelId() + " добавлен");
                            }

                            getInstance().channelSetLatch.countDown();
                        },
                        e -> {
                            // System.out.println(e.getMessage());
                        },
                        () -> {
                            // System.out.println("complete");
                        }
                );
    }

}
