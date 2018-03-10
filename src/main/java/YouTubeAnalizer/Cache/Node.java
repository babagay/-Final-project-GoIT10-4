package YouTubeAnalizer.Cache;

import YouTubeAnalizer.Entity.Channel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class Node
{

    /**
     * "ChannelFoo,ChannelBar"
     */
    String request;

    Set<Channel> channels;

    ArrayList<String> channelNames;

    boolean isDead;

    int expirationDate = 0;

    public Node()
    {
        channelNames = new ArrayList<>( 2 );
        resetChannels();
        request = "";
        isDead = false;
    }

    public Node(String request, Set<Channel> channels)
    {
        this();
        setRequest( request );
        setChannels( channels );
    }

    public void setRequest(String request)
    {
        this.request = request;
    }

    public int getChannelNumber()
    {
        return channels.size();
    }

    public Set<Channel> getChannels()
    {
        return channels;
    }

    public void setChannels(Set<Channel> channels)
    {
        this.channels = channels;
        channels.stream().forEach( channel -> channelNames.add( channel.getChannelId() ) );
        recalcExpirationDate();
    }

    void addChannel(Channel channel)
    {
        channels.add( channel );

        if ( !channelNames.stream().anyMatch( name -> name.equals( channel.getChannelId() ) ) )
        {
            channelNames.add( channel.getChannelId() );
        }
    }

    boolean containsChannel(Channel channel)
    {
        return channels.stream().anyMatch( channel1 -> channel1.channelId.equals( channel.channelId ) );
    }

    /**
     * восстановить ссылки на каналы, проверить их время протухания, пересчитать время протухания ноды
     */
    void refresh()
    {
        resetChannels();
        channelNames.stream().map( Storage::getChannelById ).filter( Channel::isFresh ).forEach( this::addChannel );

        if ( channels.size() < channelNames.size() )
        {
            isDead = true;
        }
        else
        {
            recalcExpirationDate();
        }
    }

    private void resetChannels()
    {
        channels = new TreeSet<>();
    }

    /**
     * Выставить ExpirationDate в соответствии с минимальным ExpirationDate среди всех каналов
     */
    private void recalcExpirationDate()
    {
        expirationDate = channels.stream().min( Comparator.comparingInt( Channel::getExpirationDate ) ).get().getExpirationDate();
    }

}


