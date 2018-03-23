package YouTubeAnalizer.view;

import YouTubeAnalizer.Entity.Channel;
import javafx.scene.control.TableView;

import java.util.ArrayList;

public class ChannelGrid extends TableView<Channel>
{
    private ArrayList<Channel> list;

    public void setChannels(ArrayList<Channel> list)
    {
        if ( getItems().size() > 0 )
        getItems().remove( 0, this.list.size() );

        this.list = list;

        getItems().addAll( list );
    }
}
