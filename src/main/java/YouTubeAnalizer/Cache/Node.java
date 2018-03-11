package YouTubeAnalizer.Cache;

import YouTubeAnalizer.Entity.Channel;

import java.util.*;

public class Node
{
    /**
     * "ChannelFoo,ChannelBar"
     */
    String request;

    Set<Channel> channels;

    ArrayList<String> channelNames;

    boolean isDead;
    
    private static Factory factory = new Factory();
    
    /**
     * Время в секундах
     */
    int expirationDate = 0;

    public Node()
    {
        resetChannelNames();
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
    
    public static Factory getFactory ()
    {
        return factory;
    }
    
    public void setRequest (String request)
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
        
        setChannelNames( channels );
        
        recalcExpirationDate();
    }
    
    void setChannelNames(Set<Channel> channels)
    {
        resetChannelNames();
        channels.stream().forEach( channel -> channelNames.add( channel.getChannelId() ) );
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
    
    boolean isDead()
    {
        return isDead == true;
    }
    
    boolean isExpired()
    {
        return expirationDate <= (int) System.currentTimeMillis()/1000;
    }

    boolean isRelevant()
    {
        return !isDead() &&  !isExpired();
    }
    
    /**
     * восстановить ссылки на каналы, проверить их время протухания, пересчитать время протухания ноды
     */
    void refresh()
    {
        resetChannels();
        channelNames.stream().map( Storage::getChannelById ).filter( Channel::isFresh ).forEach( this::addChannel );
        // todo сбросить requests и засетить

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
    
    private void resetChannelNames()
    {
        channelNames = new ArrayList<>( 2 );
    }

    /**
     * Выставить ExpirationDate в соответствии с минимальным ExpirationDate среди всех каналов
     */
    void recalcExpirationDate()
    {
        expirationDate = channels.stream().min( Comparator.comparingInt( Channel::getExpirationDate ) ).get().getExpirationDate();
    }
    
    static class Factory {
        
        Node get(String request, Channel... channels)
        {
            TreeSet<Channel> channelSet = generateChannelSet( channels );
            Node node = new Node( request, channelSet );
            node.setChannelNames( channelSet );
            node.refresh();
            
            return node;
        }
        
        TreeSet<Channel> generateChannelSet(Channel... channels)
        {
            return Arrays.stream( channels ).collect( TreeSet<Channel>::new, TreeSet::add, TreeSet::addAll );
        }
    }

}


