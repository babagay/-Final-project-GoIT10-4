package YouTubeAnalizer.Entity;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

public class Channel implements Comparable<Channel>, Serializable {

    /**
     * Дата создания канала
     */
    LocalDateTime creationDate;

    /**
     * Количество подписчиков
     */
    private final SimpleLongProperty followersNumber = new SimpleLongProperty(  );

    /**
     *  Кол-во видео на канале
     */
    long videosNumber;

    /**
     * Кол-во просмотров всех видео
     */
    long totalViewsNumber;

    /**
     * Количество комментариев всех видео
     */
    long totalCommentsNumber;

    /**
     * Имя канала
     */
    private final SimpleStringProperty name = new SimpleStringProperty( "" );

    /**
     * уникальный идентификатор канала
     */
     private final SimpleStringProperty channelId = new SimpleStringProperty("");

    private final SimpleStringProperty description = new SimpleStringProperty( "" );

    /**
     * Набор видео
     */
    Set<Video> videos;

    /**
     * Время протухания, сек.
     */
    long expirationDate;
    


    /**
     * Dont forget to set relevant expirationDate after object has been created
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

    public LocalDateTime getCreationDate()
    {
        return creationDate;
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
        return videosNumber;
    }

    public long getTotalViewsNumber ()
    {
        return totalViewsNumber;
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
        this.creationDate = creationDate;
    }

    public void setFollowersNumber(long followersNumber)
    {
        this.followersNumber.set( followersNumber );
    }

    public void setTotalViewsNumber(long totalViewsNumber)
    {
        this.totalViewsNumber = totalViewsNumber;
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
        return totalCommentsNumber;
    }
    
    public void setTotalCommentsNumber (long totalCommentsNumber)
    {
        this.totalCommentsNumber = totalCommentsNumber;
    }
    
    public void setName (String name)
    {
        this.name.set( name );
    }
    
    public void setVideosNumber (long videosNumber)
    {
        this.videosNumber = videosNumber;
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
                "channelId='" + channelId + '\'' +
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
