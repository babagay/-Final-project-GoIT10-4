package YouTubeAnalizer.Entity;

import com.google.gson.InstanceCreator;

import java.lang.reflect.Type;

public class ChannelCreator implements InstanceCreator<Channel>
{
    @Override
    public Channel createInstance(Type type)
    {
        Channel channel = new Channel( true );

        return channel;
    }
}
