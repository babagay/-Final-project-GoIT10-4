package YouTubeAnalizer.Entity;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Channel implements Comparable<Channel>, Serializable {

    /**
     * todo Дата создания канала
     */
    transient private final SimpleStringProperty creationDate = new SimpleStringProperty(  );

    private LocalDateTime creationDateCached;

    /**
     * Количество подписчиков
     */
    transient private final SimpleLongProperty followersNumber = new SimpleLongProperty(  );

    private long followersNumberCached;

    /**
     *  Кол-во видео на канале
     */
    transient private final SimpleLongProperty videosNumber = new SimpleLongProperty(  );

    long videosNumberCached;

    /**
     * Кол-во просмотров всех видео
     */
    transient private final SimpleLongProperty totalViewsNumber = new SimpleLongProperty(  );

    private long totalViewsNumberCached;

    /**
     * Количество комментариев всех видео
     */
    transient private final SimpleLongProperty totalCommentsNumber = new SimpleLongProperty(  );

    private long totalCommentsNumberCached;

    /**
     * Имя канала
     */
    transient private final SimpleStringProperty name = new SimpleStringProperty( "" );

    private String nameCached;

    /**
     * уникальный идентификатор канала
     */
    transient private final SimpleStringProperty channelId = new SimpleStringProperty("");

    private String channelIdCached;

    /**
     * Channel description
     */
    transient private final SimpleStringProperty description = new SimpleStringProperty( "" );

    private String descriptionCached;

    /**
     * Набор видео
     */
    transient Set<Video> videos;
    
    transient List<String> videoIds = new ArrayList<>( 100 );
    
    public void setVideoIds (List<String> videoIds)
    {
        this.videoIds = videoIds;
    }
    
    public List<String> getVideoIds ()
    {
        return videoIds;
    }
    
    boolean needToBeRestoredFromCahce = false;

    /**
     * Время протухания, сек.
     */
    long expirationDate;

    public Channel(boolean restoreFromCache)
    {
        needToBeRestoredFromCahce = restoreFromCache;
    }

    /**
     * Don't forget to set relevant expirationDate after object has been created
     */
    public Channel (String channelId)
    {
        this(channelId, 1);
    }

    public Channel (String channelId, int expirationDate)
    {
        setChannelId( channelId );
        this.expirationDate = expirationDate;
    }

    /**
     * Extra deserialization
     */
    public void restoreChannel()
    {
        // [?] if ( needToBeRestoredFromCahce )

        name.set(nameCached);
        channelId.set(channelIdCached);
        description.set(descriptionCached);
        followersNumber.set(followersNumberCached);
        totalViewsNumber.set(totalViewsNumberCached);
        totalCommentsNumber.set(totalCommentsNumberCached);
        videosNumber.set(videosNumberCached);
        creationDate.set(""); // todo

        needToBeRestoredFromCahce = false;
    }

    // todo
    public LocalDateTime getCreationDate()
    {
        return null;
//        return creationDate.get();
    }

    public long getFollowersNumber()
    {
        return followersNumber.get();
    }

    public SimpleLongProperty followersNumberProperty()
    {
        return followersNumber;
    }

    public long getVideosNumber ()
    {
        return videosNumber.get();
    }

    public long getTotalViewsNumber ()
    {
        return totalViewsNumber.get();
    }

    public String getName()
    {
        return name.get();
    }

    public String getChannelId()
    {
        return channelId.get();
    }

    private void setChannelId(String channelId)
    {
        channelIdCached = channelId;
         this.channelId.set( channelId );
    }

    public Set<Video> getVideos()
    {
        return videos;
    }

    public long getExpirationDate()
    {
        return expirationDate;
    }

    public void setCreationDate(LocalDateTime creationDate)
    {
        // todo
        this.creationDate.set("");
    }

    public void setFollowersNumber(long followersNumber)
    {
        followersNumberCached = followersNumber;
        this.followersNumber.set( followersNumber );
    }

    public void setTotalViewsNumber(long totalViewsNumber)
    {
        this.totalViewsNumberCached = totalViewsNumber;
        this.totalViewsNumber.set(totalViewsNumber);
    }

    public void setVideos(Set<Video> videos)
    {
        this.videos = videos;
    }

    public void setExpirationDate(long expirationDate)
    {
        this.expirationDate = expirationDate;
    }
    
    public void setDescription (String description)
    {
        descriptionCached = description;
        this.description.set( description );
    }

    public String getDescription()
    {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty()
    {
        return description;
    }

    public long getTotalCommentsNumber ()
    {
        return totalCommentsNumber.get();
    }
    
    public void setTotalCommentsNumber (long totalCommentsNumber)
    {
        this.totalCommentsNumberCached = totalCommentsNumber;
        this.totalCommentsNumber.set(totalCommentsNumber);
    }
    
    public void setName (String name)
    {
        nameCached = name;
        this.name.set( name );
    }
    
    public void setVideosNumber (long videosNumber)
    {
        this.videosNumberCached = videosNumber;
        this.videosNumber.set(videosNumber);
    }
    
    /**
     * true, если текущее время меньше expirationDate
     */
    public boolean isFresh()
    {
        return expirationDate > System.currentTimeMillis() /1000;
    }

    /**
     * true, если expirationDate < now
     */
    public boolean isExpired()
    {
        return !isFresh();
    }

    @Override
    public String toString()
    {
        return "Channel{" +
                "channelId='" + channelId.get() + '\'' +
                '}';
    }

    @Override
    public int compareTo(Channel channel)
    {
        if ( getChannelId().compareTo( channel.getChannelId() ) > 0 )
        {
            return 1;
        }

        if ( getChannelId().compareTo( channel.getChannelId() ) < 0 )
        {
            return -1;
        }

        if ( getChannelId().compareTo( channel.getChannelId() ) == 0 && !channelId.equals( channel.channelId ) )
        {
            return -1;
        }

        if ( getChannelId().compareTo( channel.getChannelId() ) == 0 && channelId.equals( channel.channelId ) )
        {
            if ( hashCode() > channel.hashCode() )
            {
                return 1;
            }
            else
            {
                return -1;
            }
        }

        return -1;
    }
}
