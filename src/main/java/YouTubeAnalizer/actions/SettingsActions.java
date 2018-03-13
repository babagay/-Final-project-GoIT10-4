package YouTubeAnalizer.actions;

import YouTubeAnalizer.Settings.SettingsService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class SettingsActions {

    @FXML private Text actiontarget;

    @FXML private TextField cacheFilePath;
    @FXML private TextField expirationTime;

    @FXML private CheckBox useCache;
    @FXML private CheckBox showRequestDuration;

    SettingsService settingsService = SettingsService.getInstance();

    @FXML
    protected void handleSubmitButtonAction(ActionEvent event) {

        settingsService.getSettings().setUseCache( useCache.isSelected() );
        settingsService.getSettings().setShowRequestDuration( showRequestDuration.isSelected() );
        settingsService.getSettings().setCacheFilePath( cacheFilePath.getText() );
        settingsService.getSettings().setExpirationTime( Long.parseLong( expirationTime.getText()) );

        settingsService.storeSettings();
    }
}
