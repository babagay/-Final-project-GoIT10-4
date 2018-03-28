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
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

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
    
    private static YouTube youtube;
    
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
        try {
            youtube = getYouTubeService();
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }
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

    // При слабом инете: connect timed out
    public List<Channel> getChannels(String request)
    {
        List<Channel> list = new ArrayList<>();

        try {

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
    
    private final static int MAX_RESULTS = 50;
    private final static int INIT_CAPACITY = 1000;
    
    private static HashMap<String, String> getVideoParams = new HashMap<>();
    
    static {
        getVideoParams.put("part", "id,snippet");
        getVideoParams.put("maxResults", MAX_RESULTS + "");
        getVideoParams.put("type", "video");
        // parameters.put("order", "date");
        // parameters.put("q", channel.getChannelId());
    }
    
    /**
     * Get video IDs for single channel
     */
    public List<SearchResult> getVideos (
            YouTubeAnalizer.Entity.Channel channel,
            String pageToken,
            Integer step,
            ArrayList<SearchResult> acc)
    throws IOException
    {
        List<SearchResult> results;
        Integer totalItems = 0;
    
        if ( acc == null ) { acc = new ArrayList<>( INIT_CAPACITY ); }
    
        if ( step == null ) { step = 0; }
    
        getVideoParams.put( "pageToken", pageToken );
        getVideoParams.put( "channelId", channel.getChannelId() );
        
        YouTube.Search.List
                searchListByKeywordRequest =
                youtube.search().list( getVideoParams.get( "part" ).toString() );
    
        if ( getVideoParams.containsKey( "maxResults" ) ) {
            searchListByKeywordRequest.setMaxResults( Long.parseLong( getVideoParams.get( "maxResults" ).toString() ) );
        }
    
        if ( getVideoParams.containsKey( "q" ) && getVideoParams.get( "q" ) != "" ) {
            searchListByKeywordRequest.setQ( getVideoParams.get( "q" ).toString() );
        }
    
        if ( getVideoParams.containsKey( "order" ) && getVideoParams.get( "order" ) != "" ) {
            searchListByKeywordRequest.setOrder( getVideoParams.get( "order" ).toString() );
        }
    
        if ( getVideoParams.containsKey( "channelId" ) && getVideoParams.get( "channelId" ) != "" ) {
            searchListByKeywordRequest.setChannelId( getVideoParams.get( "channelId" ).toString() );
        }
    
        if ( getVideoParams.containsKey( "pageToken" ) && getVideoParams.get( "pageToken" ) != "" &&
             getVideoParams.get( "pageToken" ) != null )
        {
            searchListByKeywordRequest.setPageToken( getVideoParams.get( "pageToken" ).toString() );
        }
    
        // Restrict the search results to only include videos. See:
        // https://developers.google.com/youtube/v3/docs/search/list#type
        if ( getVideoParams.containsKey( "type" ) && getVideoParams.get( "type" ) != "" ) {
            searchListByKeywordRequest.setType( getVideoParams.get( "type" ).toString() );
        }
    
        // To increase efficiency, only retrieve the fields that the application uses.
        // "items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)"
        searchListByKeywordRequest.setFields( "items(id/videoId),nextPageToken,pageInfo(totalResults)" );
    
        try {
            SearchListResponse response = searchListByKeywordRequest.execute();
         
            totalItems = response.getPageInfo().getTotalResults();
            
            results = response.getItems();
            acc.addAll( results );
        
            if ( MAX_RESULTS * ++step < totalItems ) {
                getVideos( channel, response.getNextPageToken(), step, acc );
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    
        return acc;
    }


    public ArrayList<YouTubeAnalizer.Entity.Channel> mapChannels(List<Channel> channels)
    {
        return channels.stream().filter( Objects::nonNull )
                .map( this::mapChannel )
                .collect( ArrayList::new, ArrayList::add, ArrayList::addAll );
    }

    /**
     * Map com.google.api.services.youtube.model.Channel to YouTubeAnalizer.Entity.Channel
     */
    public YouTubeAnalizer.Entity.Channel mapChannel(Channel channel) {

        YouTubeAnalizer.Entity.Channel channel1 = new YouTubeAnalizer.Entity.Channel(channel.getId());
        channel1.setFollowersNumber(channel.getStatistics().getSubscriberCount().longValueExact());
        channel1.setName(channel.getBrandingSettings().getChannel().getTitle());
        channel1.setDescription(channel.getBrandingSettings().getChannel().getDescription());
        channel1.setTotalCommentsNumber(channel.getStatistics().getCommentCount().longValueExact());
        channel1.setVideosNumber(channel.getStatistics().getVideoCount().longValueExact());
        channel1.setTotalViewsNumber(channel.getStatistics().getViewCount().longValueExact());
        // channel1.setCreationDate(); // todo
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
