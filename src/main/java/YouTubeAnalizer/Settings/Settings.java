package YouTubeAnalizer.Settings;

import com.google.gson.annotations.SerializedName;


public final class Settings {

    /**
     * Время протухания закешированного объекта канала по умолчанию, сек
     */
    public static final int EXPIRATION_TIME = 1000;
    
    /**
     * Путь к кешу по умолчанию
     */
    public static final String CACHE_FILE = "storage.json";

    @SerializedName ("expirationTime")
    private int expirationTime;

    /**
     * использовать ли кеш
     */
    @SerializedName ("useCache")
    private boolean useCache;

    /**
     * отображать ли время запроса
     */
    @SerializedName ("showRequestDuration")
    private boolean showRequestDuration;

    /**
     * путь к файлу кэша
     */
    @SerializedName ("cacheFilePath")
    String cacheFilePath;

    Settings()
    {
        expirationTime = EXPIRATION_TIME;
        useCache = true;
        showRequestDuration = false;
        cacheFilePath = CACHE_FILE;
    }
    
    /**
     * Exp time shift in seconds
     */
    public int getExpirationTime()
    {
        return expirationTime;
    }

    public void setExpirationTime(int expirationTime)
    {
        this.expirationTime = expirationTime;
    }

    public boolean isUseCache()
    {
        return useCache;
    }

    public boolean isShowRequestDuration()
    {
        return showRequestDuration;
    }

    public String getCacheFilePath ()
    {
        return cacheFilePath;
    }

    public void setUseCache(boolean useCache)
    {
        this.useCache = useCache;
    }

    public void setShowRequestDuration(boolean showRequestDuration)
    {
        this.showRequestDuration = showRequestDuration;
    }

    public void setCacheFilePath (String cacheFilePath)
    {
        this.cacheFilePath = cacheFilePath;
    }
}
