package YouTubeAnalizer.Settings;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class SettingsService
{
    private String fileName = "settings.json";

    private Settings settings;

    private static class SettingsServiceHolder
    {
        private static final SettingsService instance = new SettingsService();
    }

    public static SettingsService getInstance()
    {
        return SettingsService.SettingsServiceHolder.instance;
    }

    public Settings getSettings()
    {
        return settings;
    }

    public void initSettings()
    {
//        boolean initSettingsFile = false;
        
        File storageFile = new File( fileName );
        InputStream targetStream = null;

        File file = new File( fileName );

        if ( !file.exists() )
        {
            try
            {
                file.createNewFile();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }

        try
        {
            targetStream = new FileInputStream( storageFile );
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }

        String json = null;

        try ( final InputStreamReader reader = new InputStreamReader( targetStream ) )
        {
            json = CharStreams.toString( reader );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        if ( json.equals( "" ) ){
            settings = new Settings();
            storeSettings();
        } else {
            Gson gson = new GsonBuilder().create();
            settings = gson.fromJson( json, Settings.class );
        }

        System.out.println( "Показывать время выполнения запроса: " + settings.isShowRequestDuration() );
        System.out.println( "Использовать кеш: " + settings.isUseCache() );
        System.out.println( "Время протухания, сек: " + settings.getExpirationTime() );
        System.out.println( "Путь к файлу кеша: " + settings.getCacheDirectory() );
        System.out.println( "файл кеша: " + settings.getCacheFilePath() );
    }

    public void storeSettings()
    {
        String s = new Gson().toJson( settings );

        BufferedWriter writer;

        try
        {
            FileWriter fileWriter = new FileWriter( fileName );

            writer = new BufferedWriter( fileWriter );

            writer.write( s );

            writer.flush();

            writer.close();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }
}
