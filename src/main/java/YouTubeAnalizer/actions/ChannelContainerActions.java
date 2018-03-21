package YouTubeAnalizer.actions;

import YouTubeAnalizer.Request.RequestService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class ChannelContainerActions
{
    @FXML private Button btn;

    @FXML private TextField fieldOne;

    public Button getBtn()
    {
        return btn;
    }

    public ChannelContainerActions()
    {
        RequestService.channelStream.subscribe( r -> {

            // todo вставлять в таблицу
            fieldOne.setText( r );
        });
    }
}
