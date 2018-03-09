package YouTubeAnalizer.Entity;

import java.time.LocalDateTime;
import java.util.Set;

public class Channel {

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
     * Время протухания. Для свежего объекта должно быть больще текущего значения
     */
    int expirationDate;
    
    public Channel (String channelId, int expirationDate)
    {
        this.channelId = channelId;
        this.expirationDate = expirationDate;
    }
}
