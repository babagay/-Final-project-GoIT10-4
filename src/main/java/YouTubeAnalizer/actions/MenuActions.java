package YouTubeAnalizer.actions;

import YouTubeAnalizer.Cache.CacheService;
import YouTubeAnalizer.view.BasicView;
import com.gluonhq.particle.annotation.ParticleActions;
import com.gluonhq.particle.application.ParticleApplication;
import com.gluonhq.particle.state.StateManager;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import org.controlsfx.control.action.ActionProxy;

import javax.inject.Inject;

@ParticleActions
public class MenuActions {

    @Inject
    ParticleApplication app;

    @Inject
    private StateManager stateManager;

    @ActionProxy(text="Exit", accelerator="alt+F4")
    private void exit() {
        CacheService.saveStorage();
        app.exit();
    }

    @ActionProxy(text="About")
    private void about() {
        Alert alert = new Alert( Alert.AlertType.INFORMATION);
        alert.setTitle("Desktop App");
        alert.setHeaderText("About App");
        //alert.setGraphic(new ImageView(new Image(MenuActions.class.getResource("/icon.png").toExternalForm(), 48, 48, true, true)));
        alert.setContentText("This is a basic Desktop Application");
        alert.showAndWait();

    }

    /**
     * [!] Just example
     */
    @ActionProxy(text="Settings")
    private void settings() {
        TextInputDialog input = new TextInputDialog(stateManager.getProperty("CacheFilePath").orElse("").toString());
        input.setTitle("Path");
        input.setHeaderText(null);
        input.setContentText("Input file path:");
        input
                .showAndWait()
                .ifPresent(user -> {
                    BasicView homeView = (BasicView) app.getParticle().getViewManager().getCurrentView();
                    homeView.addFilePath(user);
                });
    }

}