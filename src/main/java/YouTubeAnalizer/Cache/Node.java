package YouTubeAnalizer.Cache;

import YouTubeAnalizer.Entity.Channel;

import java.util.*;

public class Node implements Comparable<Node>
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
    long expirationDate = 0;

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

    @Override
    public int compareTo(Node node)
    {
        if ( request.compareTo( node.request ) > 0 )
        {
            return 1;
        }

        return -1;
    }
    
    void setChannelNames(Set<Channel> channels)
    {
        resetChannelNames();
        channels.stream().forEach( channel -> channelNames.add( channel.getChannelId() ) );
    }

    void addChannel(Channel channel)
    {
        if ( channel != null && channel.isFresh() )
        {
            channels.add( channel );

            if ( !channelNames.stream().anyMatch( name -> name.equals( channel.getChannelId() ) ) )
            {
                channelNames.add( channel.getChannelId() );
            }
        }
        else
        {
            // при попытке добавть нерелевантный канал, пометить ноду мертвой
            isDead = true;
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
        return expirationDate <= (long) System.currentTimeMillis()/1000;
    }

    boolean isRelevant()
    {
        return !isDead() && !isExpired();
    }

    /**
     * True if node is NOT relevant
     */
    boolean isNotRelevant()
    {
        return !isRelevant();
    }

    static Node restoreNode(String key)
    {
        return Node.getFactory().reproduce( key );
    }

    /**
     * восстановить ссылки на каналы, проверить их время протухания, пересчитать время протухания ноды
     */
    void refresh()
    {
        resetChannels();
        channelNames.stream().
                map( Storage::getChannelById ).
                forEach( this::addChannel );

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
        expirationDate = channels.stream().min( Comparator.comparingLong( Channel::getExpirationDate ) ).get().getExpirationDate();
    }

    static class Factory {

        /**
         * Собрать новую ноду
         */
        Node create(String request, Channel... channels)
        {
            TreeSet<Channel> channelSet = generateChannelSet( channels );
            Node node = new Node( request, channelSet );
            node.setChannelNames( channelSet );
            node.refresh();
            
            return node;
        }

        /**
         * Собрать новую ноду
         */
        Node create(String request, TreeSet<Channel> channelSet)
        {
            Node node = new Node( request, channelSet );
            node.setChannelNames( channelSet );
            node.refresh();

            return node;
        }

        /**
         * Собрать ноду на основании ранее закешированных объектов Channel
         */
        Node reproduce(String request)
        {
            Node node = new Node();
            node.setRequest( request );

            Arrays.stream( request.split( "," ) )
                    .map( Storage::getChannelById )
                    .filter( Objects::nonNull )
                    .filter( Channel::isFresh )
                    .forEach( node::addChannel );

            if ( node.getChannelNumber() < request.split( "," ).length )
            {
                node = null;
            }

            return node;
        }
        
        TreeSet<Channel> generateChannelSet(Channel... channels)
        {
            return Arrays.stream( channels ).collect( TreeSet<Channel>::new, TreeSet::add, TreeSet::addAll );
        }
    }

}


