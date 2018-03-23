package YouTubeAnalizer.actions;

import YouTubeAnalizer.Entity.Channel;
import YouTubeAnalizer.Request.RequestService;
import YouTubeAnalizer.view.ChannelGrid;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.ArrayList;

/**
 * https://stackoverflow.com/questions/43216716/javafx-how-to-set-string-values-to-tableview
 * <p>
 * https://docs.oracle.com/javase/8/javafx/fxml-tutorial/fxml_tutorial_intermediate.htm - example
 * <p>
 * https://docs.oracle.com/javase/8/javafx/properties-binding-tutorial/binding.htm#JFXBD107
 */
public class ChannelContainerActions
{

    @FXML
    private ChannelGrid grid;

    @FXML
    private TableColumn channelIdColumn;


    public ChannelContainerActions()
    {
        RequestService.channelStream
                .subscribe(
                        channels -> grid.setChannels( channels ),
                        throwable -> throwable.printStackTrace()
                );
    }
}
