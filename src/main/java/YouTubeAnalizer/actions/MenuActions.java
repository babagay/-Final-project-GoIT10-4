package YouTubeAnalizer.actions;

import com.gluonhq.particle.annotation.ParticleActions;
import com.gluonhq.particle.application.ParticleApplication;
import com.gluonhq.particle.state.StateManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.action.ActionProxy;

import javax.inject.Inject;
import java.io.IOException;

@ParticleActions
public class MenuActions {

    @Inject
    ParticleApplication app;

    @Inject
    private StateManager stateManager;

    @ActionProxy(text="Exit", accelerator="alt+F4")
    private void exit() {
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


    @ActionProxy (text = "Settings")
    private void settings()
    {

        Stage stage = new Stage();
        Parent root = null;


        try {
            root = FXMLLoader.load(
                    SettingsActions.class.getResource( "Settings.fxml" ) );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        if ( root != null ) {

            stage.setScene( new Scene( root ) );
            stage.setTitle( "Settings" );
            stage.initModality( Modality.WINDOW_MODAL );
            stage.initOwner(
                    //((Node)event.getSource()).getScene().getWindow()
                    app.getPrimaryStage().getScene().getWindow()
            );
            stage.showAndWait();
        }
    }

}