package YouTubeAnalizer;

import YouTubeAnalizer.Cache.CacheService;
import YouTubeAnalizer.Settings.SettingsService;
import com.gluonhq.particle.application.Particle;
import com.gluonhq.particle.application.ParticleApplication;
import javafx.scene.Scene;
import org.controlsfx.control.NotificationPane;

import java.lang.reflect.Field;

import static org.controlsfx.control.action.ActionMap.actions;

public class App extends ParticleApplication
{
    private static SettingsService settingsService = SettingsService.getInstance();

    public App()
    {
        super( "YouTube Analyzer" );
        
        disableNotificationPane();

        settingsService.initSettings();

        CacheService.initStorage();
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
