package YouTubeAnalizer.Entity;

public class Channel {
    
    public String channelId;
    
    int expirationDate;
    
    public Channel (String channelId, int expirationDate)
    {
        this.channelId = channelId;
        this.expirationDate = expirationDate;
    }
}
