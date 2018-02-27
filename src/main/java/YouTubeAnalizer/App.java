package YouTubeAnalizer;

import com.gluonhq.particle.application.Particle;
import com.gluonhq.particle.application.ParticleApplication;
import javafx.scene.Scene;

import java.lang.reflect.Field;

import static org.controlsfx.control.action.ActionMap.actions;

public class App extends ParticleApplication
{

    public App()
    {
        super( "YouTube Analyzer" );

        disableNotificationPane();
    }

    @Override
    public void postInit(Scene scene)
    {
        getApp().buildMenu( "File -> [signin,---, exit]", "Help -> [about]" );

        getApp().getToolBarActions().addAll( actions( "signin" ) );
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
            notificationPaneField.set( particle, null );
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
