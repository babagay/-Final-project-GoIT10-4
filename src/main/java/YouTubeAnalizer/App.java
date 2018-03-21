package YouTubeAnalizer;

import YouTubeAnalizer.Cache.CacheService;
import YouTubeAnalizer.Entity.Channel;
import YouTubeAnalizer.Request.RequestService;
import YouTubeAnalizer.Settings.SettingsService;
import YouTubeAnalizer.view.SearchForm;
import com.gluonhq.particle.application.Particle;
import com.gluonhq.particle.application.ParticleApplication;
import com.gluonhq.particle.form.Form;
import com.gluonhq.particle.form.FormManager;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.controlsfx.control.NotificationPane;

import javax.inject.Inject;
import java.lang.reflect.Field;

import static org.controlsfx.control.action.ActionMap.actions;

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

        RequestService.init();

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

    // не работает, т.к., видимо setOnCloseRequest перетирается в другом месте
    void setOnCloseAction()
    {
        Stage stage = getPrimaryStage();

        stage.setOnCloseRequest( event -> {
            System.out.println("Close works!");
            //CacheService.saveStorage();
        } );
    }
}
