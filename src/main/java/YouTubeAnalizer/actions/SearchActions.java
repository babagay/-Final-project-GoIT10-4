package YouTubeAnalizer.actions;

import YouTubeAnalizer.Cache.CacheService;
import YouTubeAnalizer.Entity.Channel;
import YouTubeAnalizer.Request.RequestService;
import com.gluonhq.particle.annotation.ParticleActions;
import com.gluonhq.particle.state.StateManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

@ParticleActions
public class SearchActions implements Initializable
{

    private StateManager stateManager;

    @FXML
    private ComboBox<String> requestType;

    @FXML
    private TextField request;

    @FXML
    private ProgressIndicator requestProgress;

    @FXML
    private Label requestTime;

    private volatile double progress = 0.0;

    public SearchActions()
    {
        stateManager = RequestService.application.getStateManager();
    }

    private final static int GET_SINGLE_CHANNEL_SHORT_INFO_REQUEST = 0;
    private final static int    GET_TWO_CHANNEL_SHORT_INFO_REQUEST = 1;
    private final static int  GET_MULTI_CHANNEL_SHORT_INFO_REQUEST = 2;
    private final static int  GET_SINGLE_CHANNEL_WIDE_INFO_REQUEST = 3;
    private final static int     GET_TWO_CHANNEL_WIDE_INFO_REQUEST = 4;
    private final static int   GET_MULTI_CHANNEL_WIDE_INFO_REQUEST = 5;

    @FXML
    protected void doRequest(ActionEvent event)
    {
        restartProgress();

        switch ( requestType.getSelectionModel().getSelectedIndex() ){
            case GET_SINGLE_CHANNEL_SHORT_INFO_REQUEST:
                System.out.println("sing");
                break;
            case GET_TWO_CHANNEL_SHORT_INFO_REQUEST:
                System.out.println("tw");
                break;
            case GET_MULTI_CHANNEL_SHORT_INFO_REQUEST:
                System.out.println("m");
                break;
            case GET_SINGLE_CHANNEL_WIDE_INFO_REQUEST:
                System.out.println("sing");
                break;
            case GET_TWO_CHANNEL_WIDE_INFO_REQUEST:
                System.out.println("tw");
                break;
            case GET_MULTI_CHANNEL_WIDE_INFO_REQUEST:
                System.out.println("m");
                break;
        }

        // todo
        // сформировать нужный запрос
        // В зависимости от запроса, своя валидация

        RequestService.get( request.getText(), channels -> {

            // todo

            // вычисляет время окончания запроса

            // отдает результат в кеш
            CacheService.set( request.getText(), channels );


            RequestService.channelStreamer.onNext( channels );


            storeRequest( request.getText() );

            finishProgress();

            setTime( "10 sec" ); // todo вермя
        } );


    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        initRequestString();

        initProgress();

        requestType.getSelectionModel().selectFirst();
    }

    private void storeRequest(String request)
    {
        RequestService.application.getStateManager().setProperty( "request", request );
    }

    private void finishProgress()
    {
        setProgress( 1.0 );
    }

    private void initProgress()
    {
        requestProgress.setVisible( false );

        setProgress( 0.0 );
    }

    private void initRequestString()
    {
        Task<Void> task = new Task<Void>() {

            @Override protected Void call()
            {
                Platform.runLater( () -> request.setText( RequestService.application.getStateManager().getProperty( "request" ).orElse( "" ).toString() ) );

                return null;
            }
        };

        new Thread( task ).start();
    }

    private void setTime(String value)
    {
        Task<Void> task = new Task<Void>() {

            @Override protected Void call() throws Exception {

                    Platform.runLater( () -> {
                        if ( true )
                        { // включено в настройках
                            requestTime.setVisible( true );
                            requestTime.setText( value );
                        }
                        requestProgress.setVisible( false );
                    } );

                return null;
            }
        };

        new Thread( task ).start();

    }

    private void setProgress(double value)
    {
        progress = value;
        requestProgress.setProgress( value );
    }

    // use thread pool
    private void restartProgress()
    {
        requestProgress.setVisible( true );

        requestTime.setVisible( false );

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
