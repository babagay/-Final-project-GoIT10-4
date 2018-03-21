package YouTubeAnalizer.Entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

public class Channel implements Comparable<Channel>, Serializable {

    /**
     * @deprecated
     * Дата создания канала
     */
    LocalDateTime creationDate;

    long followersNumber;

    /**
     *  Кол-во видео на канале
     */
    long videosNumber;

    /**
     * Кол-во просмотров всех видео
     */
    long totalViewsNumber;
    
    long totalCommentsNumber;
    

    /**
     * Имя канала
     */
    public String name;

    /**
     * уникальный идентификатор канала
     */
    public String channelId;

    /**
     * Набор видео
     */
    Set<Video> videos;

    /**
     * Время протухания, сек.
     */
    long expirationDate;
    
    String description;
    
    public Channel (String channelId)
    {
        this.channelId = channelId;
        
        // todo сгенерить expirationDate
    }

    public Channel (String channelId, int expirationDate)
    {
        this.channelId = channelId;
        this.expirationDate = expirationDate;
    }

    public LocalDateTime getCreationDate()
    {
        return creationDate;
    }

    public long getFollowersNumber()
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
        return name;
    }

    public String getChannelId()
    {
        return channelId;
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
        this.followersNumber = followersNumber;
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
        this.description = description;
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
        this.name = name;
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
        if ( channelId.compareTo( channel.channelId ) > 0 )
        {
            return 1;
        }

        if ( channelId.compareTo( channel.channelId ) < 0 )
        {
            return -1;
        }

        if ( channelId.compareTo( channel.channelId ) == 0 && !channelId.equals( channel.channelId ) )
        {
            return -1;
        }

        if ( channelId.compareTo( channel.channelId ) == 0 && channelId.equals( channel.channelId ) )
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
