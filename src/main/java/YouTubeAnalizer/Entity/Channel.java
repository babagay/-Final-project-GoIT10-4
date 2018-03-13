package YouTubeAnalizer.Entity;

import java.time.LocalDateTime;
import java.util.Set;

public class Channel implements Comparable<Channel> {

    /**
     * Дата создания канала
     */
    LocalDateTime creationDate;

    int followersNumber;

    /**
     *  Кол-во видео на канале
     */
    int videosNumber;

    /**
     * Кол-во просмотров всех видео
     */
    int totalViewsNumber;

    /**
     * Имя канала
     */
    public String name;

    /**
     * [?] или channelID и есть имя канала
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
    
    public Channel (String channelId)
    {
        this.channelId = channelId;
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

    public int getFollowersNumber()
    {
        return followersNumber;
    }

    public int getVideosNumber()
    {
        return videosNumber;
    }

    public int getTotalViewsNumber()
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

    public void setFollowersNumber(int followersNumber)
    {
        this.followersNumber = followersNumber;
    }

    public void setVideosNumber(int videosNumber)
    {
        this.videosNumber = videosNumber;
    }

    public void setTotalViewsNumber(int totalViewsNumber)
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
