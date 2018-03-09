package YouTubeAnalizer;

import YouTubeAnalizer.Cache.CacheService;
import YouTubeAnalizer.Entity.Channel;
import com.gluonhq.particle.application.Particle;
import com.gluonhq.particle.application.ParticleApplication;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.NotificationPane;

import java.lang.reflect.Field;

import static org.controlsfx.control.action.ActionMap.actions;

public class App extends ParticleApplication
{

    public App()
    {
        super( "YouTube Analyzer" );
        
        disableNotificationPane();
    
        Channel c1 = new Channel( "a", 1 );
        Channel c2 = new Channel( "b", 1 );
        Channel c3 = new Channel( "c", 1 );
        
        CacheService.set( c1,c2, c3 );
    
        int t = 0;
    }

    @Override
    public void postInit(Scene scene)
    {
        getApp().buildMenu( "File -> [settings,---, exit]", "Help -> [about]" );

        getApp().getToolBarActions().addAll( actions( "settings" ) );
    }

    /**
     * Hack to disable pesky advertisement
     */
    private void disableNotificationPane()
    {
        Particle particle = getParticle();

        Field notificationPaneField = null;
        try
        {
            notificationPaneField = particle.getClass().getDeclaredField( "notificationPane" );
        }
        catch ( NoSuchFieldException e )
        {
            e.printStackTrace();
        }

        notificationPaneField.setAccessible( true );

        try
        {
            notificationPaneField.set( particle,  new NotificationPane() );
        }
        catch ( IllegalAccessException e )
        {
            e.printStackTrace();
        }
    }

    private Particle getApp()
    {
        return getParticle();
    }
}
