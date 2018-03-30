package YouTubeAnalizer.Request;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class ServiceExample extends Service<String>
{
    protected Task<String> createTask()
    {
        return new Task<String>()
        {
            @Override
            protected String call() throws Exception
            {
                //DO YOU HARD STUFF HERE
                String res = "toto";

                System.out.println( "ServiceExample started in " + Thread.currentThread().getName() );

                Thread.sleep( 5000 );
                return res;
            }
        };
    }

}