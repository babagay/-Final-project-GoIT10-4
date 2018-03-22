package YouTubeAnalizer.actions;

import YouTubeAnalizer.Request.RequestService;
import com.gluonhq.particle.annotation.ParticleActions;
import com.gluonhq.particle.application.ParticleApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

@ParticleActions
public class SearchActions implements Initializable
{
    @Inject
    ParticleApplication app;

    @FXML
    private TextField request;

    @FXML
    private ProgressIndicator requestProgress;

    @FXML
    private Label requestTime;

    private volatile double progress = 0.0;


    @FXML
    protected void doRequest(ActionEvent event)
    {
        restartProgress();

        // todo
        // в зависимости от выбранной опции, посылает опр запрос

        RequestService.get( request.getText(), channels -> {

            // todo
            // render
            // запоминает время окончания запроса
            // отдает результат в кеш
            // возможно, что-то делает с прогресс-баром

            // Либо можно передавать пачкой каналы
            channels.forEach( c -> {
                RequestService.channelStreamer.onNext( c.channelId );
                finishProgress();
            } );
        } );


    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        requestTime.setText( "" );

        // requestProgress.setVisible( false );
        setProgress( 0.0 );
    }

    private void finishProgress()
    {
        setProgress( 1.0 );
    }

    private void setProgress(double value)
    {
        progress = value;
        requestProgress.setProgress( value );
    }

    // thread pool
    private void restartProgress()
    {
        progress = 0.0;

        Thread thread = new Thread( () -> {

            while ( progress < 1.0 )
            {
                setProgress( progress );

                progress += 0.1;

                try
                {
                    Thread.sleep( 50 );
                }
                catch ( InterruptedException e )
                {
                }
            }
        } );
        thread.start();
    }
}
