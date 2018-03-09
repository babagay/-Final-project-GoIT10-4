package YouTubeAnalizer.Cache;

import YouTubeAnalizer.Entity.Channel;

import java.util.List;
import java.util.Map;

public class Node {

//    Map<String, List<Channel>>  responseSet
//    List<String> requestSet // “a,b,c”  “foo,bar”
    
    /**
     * “foo,bar”: [ChannelFoo, ChannelBar]
     */
    // Map<String,List<Channel>> response;
    
    
    String request;
    
    void addChannel(Channel channel)
    {
    
    }
    
    public void setRequest (String request)
    {
        this.request = request;
    }
    
    public int getChannelNumber(){
        return 0;
    }
}


