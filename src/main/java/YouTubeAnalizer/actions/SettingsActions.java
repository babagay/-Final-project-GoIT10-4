package YouTubeAnalizer.actions;

import YouTubeAnalizer.Settings.Settings;
import YouTubeAnalizer.Settings.SettingsService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsActions implements Initializable{

    private Settings settings;
    
    @FXML private Button storeSettingsButton;

    @FXML private TextField cacheFilePath;
    @FXML private TextField expirationTime;

    @FXML private CheckBox useCache;
    @FXML private CheckBox showRequestDuration;
 
    SettingsService settingsService = SettingsService.getInstance();
    
    public SettingsActions ()
    {
        settings = SettingsService.getInstance().getSettings();
    }
    
    @FXML
    protected void onStoreSettingsAction (ActionEvent event) {

        settingsService.getSettings().setUseCache( useCache.isSelected() );
        settingsService.getSettings().setShowRequestDuration( showRequestDuration.isSelected() );
        settingsService.getSettings().setCacheFilePath( cacheFilePath.getText() );
        settingsService.getSettings().setExpirationTime( Long.parseLong( expirationTime.getText()) );
    
        File cacheDir = new File( cacheFilePath.getText()).getParentFile();
        
        if ( cacheDir ==  null ){
            settingsService.getSettings().setCacheDirectory( System.getProperty("user.dir") );
        } else {
            settingsService.getSettings().setCacheDirectory( cacheDir.toString() );
        }

        settingsService.storeSettings();
    
        Stage stage = (Stage) storeSettingsButton.getScene().getWindow();
        stage.close();
    }
    
    @Override
    public void initialize (URL location, ResourceBundle resources)
    {
        useCache.setSelected( settings.isUsedCache() );
        showRequestDuration.setSelected( settings.isShownRequestDuration() );
        cacheFilePath.setText( settings.getCacheFilePath() );
        expirationTime.setText( settings.getExpirationTime() + "" );
    }
}
