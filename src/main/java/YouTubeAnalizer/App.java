package YouTubeAnalizer;

import YouTubeAnalizer.Cache.CacheService;
import YouTubeAnalizer.Request.RequestService;
import YouTubeAnalizer.Settings.SettingsService;
import com.gluonhq.particle.application.Particle;
import com.gluonhq.particle.application.ParticleApplication;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.controlsfx.control.NotificationPane;

import java.lang.reflect.Field;

/**
 * https://developers.google.com/youtube/v3/code_samples/
 * https://developers.google.com/youtube/v3/code_samples/java#search_by_location
 * https://developers.google.com/youtube/v3/guides/auth/server-side-web-apps
 * https://developers.google.com/youtube/v3/docs/channels

 */
public class App extends ParticleApplication
{
    private static SettingsService settingsService = SettingsService.getInstance();
    
    private static final String APPLICATION_NAME = "YouTube Analyzer";

    public App()
    {
        super( APPLICATION_NAME );

        RequestService.init(getApp());

        settingsService.initSettings();
    }

    @Override
    public void init() throws Exception {
        CacheService.initStorage();
    }

    @Override
    public void postInit(Scene scene)
    {
        getApp().buildMenu( "File -> [settings,---, exit]", "Help -> [about]" );

        // getApp().getToolBarActions().addAll( actions( "settings" ) );

        disableNotificationPane();

        setOnCloseAction();
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

    void setOnCloseAction()
    {
        Stage stage = getPrimaryStage();

        stage.setOnHiding( event -> {
            System.out.println("Application is closing...");

            CacheService.saveStorage();

            RequestService.shutdownPool();
        } );
    }

    @Override
    public void stop(){

        try
        {
            super.stop();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        System.out.println("Application stopped");
    }
}
