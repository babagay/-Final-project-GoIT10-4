package YouTubeAnalizer.actions;

import YouTubeAnalizer.Cache.CacheService;
import YouTubeAnalizer.Entity.Channel;
import YouTubeAnalizer.Request.RequestService;
import YouTubeAnalizer.Settings.Settings;
import YouTubeAnalizer.Settings.SettingsService;
import com.gluonhq.particle.annotation.ParticleActions;
import com.gluonhq.particle.state.StateManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

@ParticleActions
public class SearchActions implements Initializable
{
    private static Settings settings = SettingsService.getInstance().getSettings();
    
    private StateManager stateManager;

    @FXML
    private Button goButton;
    
    @FXML
    private ComboBox<String> requestType;

    @FXML
    private TextField request;

    @FXML
    private ProgressIndicator requestProgress;

    @FXML
    private Label requestTime;

    private volatile double progress = 0.0;
    
    private long startRequestTime;
    
    private boolean shortInfoRequest = true;
    
    private String requestFiltered;
    
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
    
    /**
     * todo Валидация
     */
    @FXML
    protected void onRequestAction (ActionEvent event) throws Exception
    {
        goButton.setDisable( true );
        
        restartProgress();
    
        String[] textArr;
        
        // todo
        // отрефакторить switch
        switch ( requestType.getSelectionModel().getSelectedIndex() ){
            case GET_SINGLE_CHANNEL_SHORT_INFO_REQUEST:
                shortInfoRequest = true;
                requestFiltered = request.getText().split( "," )[0];
                break;
            case GET_TWO_CHANNEL_SHORT_INFO_REQUEST:
                shortInfoRequest = true;
                textArr = request.getText().split( "," );
                try {
                    requestFiltered = textArr[0] + "," + textArr[1];
                } catch ( Throwable e ){
                    // todo
                    // бросать в канал ошибок
                    // throw new Exception( "invalid input" );
                }
                break;
            case GET_MULTI_CHANNEL_SHORT_INFO_REQUEST:
                requestFiltered = request.getText();
                shortInfoRequest = true;
                break;
            case GET_SINGLE_CHANNEL_WIDE_INFO_REQUEST:
                requestFiltered = request.getText();
                shortInfoRequest = false;
                break;
            case GET_TWO_CHANNEL_WIDE_INFO_REQUEST:
                requestFiltered = request.getText();
                shortInfoRequest = false;
                break;
            case GET_MULTI_CHANNEL_WIDE_INFO_REQUEST:
                requestFiltered = request.getText();
                shortInfoRequest = false;
                break;
        }
    
        startRequestTime = System.currentTimeMillis();
    
        if ( shortInfoRequest ) {
            RequestService.get( requestFiltered, this::callback );
        }
        else {
            RequestService.getWide( requestFiltered, this::callback );
        }
    }
    
    // not used
    private void filter()
    {
       // requestFiltered = request.getText()
    }
    
    private void callback(ArrayList<Channel> channels)
    {
        CacheService.set( request.getText(), channels );
    
        RequestService.channelStreamer.onNext( channels );
    
        storeRequest( requestFiltered );
    
        finishProgress();
    
        sightRequestDuration();
        
        goButton.setDisable( false );
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
        
        this.request.setText( request );
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
    
    private void sightRequestDuration ()
    {
        if ( settings.isShownRequestDuration() ) {
    
            long endRequestTime = System.currentTimeMillis();
            
            Task<Void> task = new Task<Void>() {
                
                @Override
                protected Void call () throws Exception
                {
                    Platform.runLater( () -> {
                        
                        requestTime.setVisible( true );
                        requestTime.setText(  (endRequestTime - startRequestTime)/1000 + " sec"  );
                        
                        requestProgress.setVisible( false );
                    } );
                    
                    return null;
                }
            };
            
            new Thread( task ).start();
        }
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
                    Thread.sleep( 100 );
                }
                catch ( InterruptedException e )
                {
                }
            }
        } );
        thread.start();
    }
}
