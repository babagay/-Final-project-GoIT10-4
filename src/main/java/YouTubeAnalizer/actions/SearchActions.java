package YouTubeAnalizer.actions;

import YouTubeAnalizer.Request.RequestService;
import com.gluonhq.particle.annotation.ParticleActions;
import com.gluonhq.particle.application.ParticleApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import javax.inject.Inject;

@ParticleActions
public class SearchActions
{
    @Inject
    ParticleApplication app;

    @FXML
    private TextField request;

    @FXML
    protected void doRequest(ActionEvent event)
    {
        RequestService.get( request.getText(), channels -> {

            // todo
            // render
            // запоминает время окончания запроса
            // отдает результат в кеш
            // возможно, что-то делает с прогресс-баром

            // Либо можно передавать пачкой каналы
            channels.forEach( c -> {
                RequestService.channelStreamer.onNext( c.channelId );
            } );
        } );



    }
}
