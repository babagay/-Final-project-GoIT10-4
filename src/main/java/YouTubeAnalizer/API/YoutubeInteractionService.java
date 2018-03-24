package YouTubeAnalizer.API;

import YouTubeAnalizer.App;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

//import static YouTubeAnalizer.App.authorize;

public final class YoutubeInteractionService {
    
    private static final String APPLICATION_NAME = "YouTube Analyzer";
    
    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.dir"), "src/main/resources");
    
    private static final String FILE_SEPRATOR =  System.getProperty("file.separator");
    
    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    
    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();
    
    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;
    
    private static final List<String> SCOPES =
            Arrays.asList( YouTubeScopes.YOUTUBE_READONLY );

    /**
     * Parts of general data info set which can be fetched from Youtube service
     */
    private String part = "brandingSettings,snippet,contentDetails,statistics";
    
    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
    private YoutubeInteractionService ()
    {
    }
    
    private static class YoutubeInteractionServiceHolder {
        private static final YoutubeInteractionService instance = new YoutubeInteractionService();
    }
    
    public static YoutubeInteractionService getInstance ()
    {
        return YoutubeInteractionService.YoutubeInteractionServiceHolder.instance;
    }
    
    /**
     * Build and return an authorized API client service, such as a YouTube
     * Data API client service.
     * @return an authorized API client service
     * @throws IOException
     */
    public static YouTube getYouTubeService () throws IOException {
        Credential credential = authorize();
        return new YouTube.Builder( HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    // При слабом инете бывает: connect timed out
    public List<Channel> getChannels(String request)
    {
        YouTube youtube;
        List<Channel> list = new ArrayList<>();

        try {
            youtube = getYouTubeService();

            YouTube.Channels.List
                    channelsListByUsernameRequest =
                    youtube.channels().list( part );
            channelsListByUsernameRequest.setId( request );
            ChannelListResponse response = channelsListByUsernameRequest.execute();
            list = response.getItems();
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            return list;
        }
    }

    public YouTubeAnalizer.Entity.Channel test(String s){
        return new YouTubeAnalizer.Entity.Channel( "" );
    }

    public ArrayList<YouTubeAnalizer.Entity.Channel> mapChannels(List<Channel> channels)
    {
        return channels.stream().filter( Objects::nonNull )
                .map( this::mapChannel )
                .collect( ArrayList::new, ArrayList::add, ArrayList::addAll );
    }

    // todo
    public YouTubeAnalizer.Entity.Channel mapChannel(Channel channel){
        YouTubeAnalizer.Entity.Channel channel1 = new YouTubeAnalizer.Entity.Channel( channel.getId() );
                            channel1.setFollowersNumber( channel.getStatistics().getSubscriberCount().longValueExact() );
                            channel1.setName( channel.getBrandingSettings().getChannel().getTitle() );
                            channel1.setDescription( channel.getBrandingSettings().getChannel().getDescription() );
        //                    channel1.setTotalCommentsNumber( channel.getStatistics().getCommentCount().longValueExact() );
        //                    channel1.setVideosNumber( channel.getStatistics().getVideoCount().longValueExact() );
        //                    channel1.setTotalViewsNumber( channel.getStatistics().getViewCount().longValueExact() );
        return channel1;
    }
    
    /**
     * Create an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException
    {
        // Load client secrets.
        InputStream in =
                App.class.getResourceAsStream( "/client_secret_2.json" );
        
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load( JSON_FACTORY, new InputStreamReader( in ) );
        
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow
                        .Builder(
                        HTTP_TRANSPORT,
                        JSON_FACTORY,
                        clientSecrets,
                        SCOPES )
                        .setDataStoreFactory( DATA_STORE_FACTORY )
                        .setAccessType( "offline" )
                        .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver() ).authorize( "user" );
        return credential;
    }
}
