package YouTubeAnalizer.view;

import YouTubeAnalizer.Cache.CacheService;
import YouTubeAnalizer.Entity.Channel;
import com.gluonhq.particle.annotation.ParticleView;
import com.gluonhq.particle.state.StateManager;
import com.gluonhq.particle.view.View;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

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

                        Channel c1 = new Channel( "Channel D" );
                        CacheService.set( c1.getChannelId(), c1 );


                        Channel c2 = new Channel( "Channel E" );
                        CacheService.set( c2.getChannelId(), c2 );

                        Channel c3 = new Channel( "Channel G" );
                        CacheService.set( c3.getChannelId(), c3 );
        });

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

}


