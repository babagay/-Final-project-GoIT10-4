package YouTubeAnalizer.view;

import YouTubeAnalizer.Cache.CacheService;
import YouTubeAnalizer.Cache.Storage;
import YouTubeAnalizer.Entity.Channel;
import com.gluonhq.particle.annotation.ParticleView;
import com.gluonhq.particle.state.StateManager;
import com.gluonhq.particle.view.View;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.util.Random;
import java.util.StringJoiner;

@ParticleView(name = "basic", isDefault = true)
public class BasicView implements View
{

    @Inject
    private StateManager stateManager;

    private final VBox controls = new VBox(15);

    Button storeCacheButton, addChannelButton, getNodeButton;

    @Override
    public void init() {

        // testing
        storeCacheButton = new Button();
        storeCacheButton.setText( "store Cache" );
        storeCacheButton.setOnAction( e -> CacheService.saveStorage() );



        getNodeButton = new Button();
        getNodeButton.setText( "get Channel" );
        getNodeButton.setOnAction( e -> {
            Channel channel = null;
            try {
                channel = CacheService.get( "Channel D2" ).get( 0 );
                System.out.println("Got " + channel.getChannelId() + ", expDate " + channel.getExpirationDate());
            } catch ( Exception e1 ) {
                System.out.println("not found");
            }
        } );


        addChannelButton = new Button();
        addChannelButton.setText( "+ Channels" );
        addChannelButton.setOnAction( e -> {

            final long[] start = new long[1];

            final Channel[] c1 = new Channel[1];

            /**
             * [нагрузочное тестирование] в главном потоке
             * Добавление и чтение 10 тыс объектов в пустой кеш:
             * Добавление и чтение 10 тыс объектов повторно: 37 сек
             * Разогрев кэша с 20 тыс объектов: 28 сек
             *
             * [нагрузочное тестирование] запись в отдельном потоке
             * Добавление и чтение 10 тыс объектов в пустой кеш: 8 - 10 cек
             * Добавление в непустой кеш с одновременным чтением в отдельном потоке с печатью в консоль: 44 сек
             *
             * [!] при большом количестве каналов нужно подождать завершения разогрева кеша,
             *      после чего можно запускать тест. Видимо, происходит переполнения очереди
             */

            // write channels
            Thread threadWrite = new Thread( () -> {
                start[0] = System.currentTimeMillis() / 1000;

                for ( int i = 0; i < 10_000; i++ ) {
                    c1[0] = new Channel( getRndChannelName() );
                    CacheService.set( c1[0].getChannelId(), c1[0] );
                }

                long end = System.currentTimeMillis() / 1000;
                long res = end - start[0];
                System.out.println( "Writing time: " + res );
            } );
            threadWrite.start();


            // read channels
            Thread threadRead = new Thread( () -> {
                // [!] поток читает из ранее сохраненных данных, а не из тех, которые кешируются в данный момент
                Storage.getInstance().getRequests().parallelStream().map( key -> CacheService.get( key ) ); //.filter( Objects::nonNull ).forEach( item -> System.out.println(item) );
            } );
            threadRead.start();

        } );

        controls.getChildren().addAll( addChannelButton, storeCacheButton, getNodeButton );
        controls.setAlignment( Pos.CENTER );

        stateManager.setPersistenceMode(StateManager.PersistenceMode.USER);

        addFilePath(stateManager.getProperty("CacheFilePath").orElse("").toString());
    }


    @Override
    public Node getContent() {
        return controls;
    }

    /**
     * [!] Just example
     */
    public void addFilePath(String cacheFilePath) {
        // storeCacheButton.setText(cacheFilePath.isEmpty() ? "..." : cacheFilePath);

        stateManager.setProperty("CacheFilePath", cacheFilePath);
    }

    private static char[] alfa = new char[]{'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
    private static Random random = new Random(  );

    private String getRndChannelName()
    {
        StringJoiner joiner = new StringJoiner( "" );

        for ( int i = 0; i < 10; i++ ) {
            joiner.add( Character.toString( alfa[getRndInt()] ) );
        }

        joiner.add( Integer.toString( getRndInt() ) );

        return joiner.toString();
    }

    private int getRndInt()
    {
        return random.nextInt( 26 - 0 );
    }

}


