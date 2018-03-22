package YouTubeAnalizer.actions;

import YouTubeAnalizer.Settings.SettingsService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsActions implements Initializable{

    @FXML private Text actiontarget;

    @FXML private TextField cacheFilePath;
    @FXML private TextField expirationTime;

    @FXML private CheckBox useCache;
    @FXML private CheckBox showRequestDuration;

    @FXML
    SettingsService settingsService = SettingsService.getInstance();

    @FXML
    protected void handleSubmitButtonAction(ActionEvent event) {

        settingsService.getSettings().setUseCache( useCache.isSelected() );
        settingsService.getSettings().setShowRequestDuration( showRequestDuration.isSelected() );
        settingsService.getSettings().setCacheFilePath( cacheFilePath.getText() );
        settingsService.getSettings().setExpirationTime( Long.parseLong( expirationTime.getText()) );
    
        File cacheDir = new File( cacheFilePath.getText()).getParentFile();
        
        settingsService.getSettings().setCacheDirectory( cacheDir.toString() );

        settingsService.storeSettings();
    }
    
    @Override
    public void initialize (URL location, ResourceBundle resources)
    {
        useCache.setSelected( true );
        cacheFilePath.setText( "foo" );
    }
}
