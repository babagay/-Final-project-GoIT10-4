package YouTubeAnalizer;

import com.gluonhq.particle.application.Particle;
import com.gluonhq.particle.application.ParticleApplication;
import javafx.scene.Scene;

import static org.controlsfx.control.action.ActionMap.actions;

public class App extends ParticleApplication
{
//    public static void main(String[] args)
//    {
//        System.out.println("Implement me");
//    }


    public App()
    {
        super("YouTube Analyzer");
    }

    @Override
    public void postInit (Scene scene)
    {
        getApp().buildMenu( "File -> [signin,---, exit]", "Help -> [about]" );

        getApp().getToolBarActions().addAll( actions( "signin" ) );
    }

    private Particle getApp ()
    {
        return getParticle();
    }
}
