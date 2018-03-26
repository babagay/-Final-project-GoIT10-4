package YouTubeAnalizer.view;

import YouTubeAnalizer.Entity.Channel;
import com.gluonhq.particle.annotation.ParticleForm;
import com.gluonhq.particle.form.Form;
import javafx.scene.Node;

/**
 *     getApp().getFormManager().registerForm( SearchForm.class );
 http://docs.gluonhq.com/particle/1.1.3/#_singleton_injections
 getApp().getFormManager().getForm( SearchForm.class, Form.UpdateMode.UPDATE_NEW_INSTANCE )
 .ifPresent( form -> form.configure( new Channel( "Foo" ) )
 .resizable( false )
 .showAndWait() );
 */

/**
 * @deprecated
 */
@ParticleForm( name = "search" )
public class SearchForm extends Form<Channel> {
    @Override
    protected String getTitle ()
    {
        return null;
    }
    
    @Override
    protected String getMessage ()
    {
        return null;
    }
    
    @Override
    protected Node getView ()
    {
        return null;
    }

    @Override
    protected void importModel(Channel channel)
    {

    }

    @Override
    protected void exportModel(Channel channel)
    {

    }

}
