package YouTubeAnalizer.Settings;

import com.google.gson.annotations.SerializedName;


public final class Settings {

    /**
     * Время протухания закешированного объекта канала по умолчанию, сек
     */
    private static final int EXPIRATION_TIME = 8000;
    
    /**
     * Путь к кешу по умолчанию
     */
    private static String CACHE_DIRECTORY = System.getProperty("user.dir");
    
    private static final String FS = System.getProperty("file.separator");
    
    /**
     * Файл кеша по умолчанию
     */
    private static final String CACHE_FILE = "storage.json";

    @SerializedName ("expirationTime")
    private long expirationTime;

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
     * папка кеша
     */
    @SerializedName ("cacheDirectory")
    private String cacheDirectory;

    /**
     * путь к файлу кэша:
     * имя файла либо
     * полный путь + имя файла
     */
    @SerializedName ("cacheFilePath")
    String cacheFilePath;

    Settings()
    {
        expirationTime = EXPIRATION_TIME;
        useCache = true;
        showRequestDuration = false;
        cacheFilePath = CACHE_DIRECTORY + FS + CACHE_FILE;
        cacheDirectory = CACHE_DIRECTORY;
    }
    
    public String getCacheDirectory ()
    {
        return cacheDirectory;
    }
    
    /**
     * Exp time shift in seconds
     */
    public long getExpirationTime()
    {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime)
    {
        this.expirationTime = expirationTime;
    }
    
    public void setCacheDirectory (String cacheDirectory)
    {
        this.cacheDirectory = cacheDirectory;
    }
    
    public boolean isUsedCache ()
    {
        return useCache;
    }

    public boolean isShownRequestDuration ()
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

    // todo валидация
    public void setCacheFilePath (String cacheFilePath)
    {
        this.cacheFilePath = cacheFilePath;
    }
}
