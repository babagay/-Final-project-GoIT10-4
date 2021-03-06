package YouTubeAnalizer.view;

import YouTubeAnalizer.Entity.Channel;
import com.gluonhq.particle.annotation.ParticleView;
import com.gluonhq.particle.application.Particle;
import com.gluonhq.particle.state.StateManager;
import com.gluonhq.particle.view.View;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;



@ParticleView(name = "basic", isDefault = true)
public class BasicView implements View
{
    
    @Inject
    private StateManager stateManager;
 
    private final VBox rootContainer = new VBox(0);

    private Pane requestContainer = new HBox( 0 );

    private Pane channelInfoBox = new VBox(  );
 
    
    

    @Override
    public void init() {
 
        makeSearchForm();

        makeChannelContainer();

        stateManager.setPersistenceMode(StateManager.PersistenceMode.USER);
    }




    @Override
    public Node getContent() {
        return rootContainer;
    }

    

    private void makeChannelContainer(){

        try
        {
            rootContainer.getChildren().addAll( new ScrollPane( FXMLLoader.load(YouTubeAnalizer.App.class.getResource("ui-channels-grid.fxml")) ));
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    private void makeSearchForm()
    {
        //rootContainer.setAlignment( Pos.TOP_LEFT );
        try
        {
            rootContainer.getChildren().addAll( new BorderPane( FXMLLoader.load(YouTubeAnalizer.App.class.getResource("ui-search.form.fxml")) ));
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        
       
    }

/*
    private CompletableFuture<Stream<Channel>> getChannels(String request)
    {
        CompletableFuture<Stream<Channel>> futureStream = CompletableFuture
                .supplyAsync( () -> {
                            // worker-1
                            return youtubeInteractionService.getChannels( request );
                        }, ForkJoinPool.commonPool()
                        // todo создать свой пул.
                        // Однако, здесь https://allegro.tech/2014/10/async-rest.html
                        // рекомендуют юзать RxJersey https://jersey.github.io/#rx-client.java8
                        // https://stackoverflow.com/questions/39469435/working-with-jersey-client-in-rx-style
                )
                // [?] Почему не работает код: return youtubeInteractionService.mapChannels( w );
                .thenComposeAsync( w -> {
                            // worker-1
                            return CompletableFuture.supplyAsync( () -> {
                                // worker-2
                                return youtubeInteractionService.mapChannels( w ).stream();
                            } );
                        }
                )
                .whenComplete( (r, throwable) -> {
                            // worker-2
                            if ( throwable == null ) {
                                // OK
                            }
                            else {
                                throw new RuntimeException( throwable );
                            }
                        }
                )
                .exceptionally( throwable -> {
                    System.out.println( throwable.getMessage() );
                    return null;
                } );

        return futureStream;
    }
*/

    /**
     * [?] Откуда брать request
     * Как обрабатывать ошибки - передавать второй колбэк?
     */
    private void handleGetChannel (ActionEvent event)
    {
//        p2.setProgress(0.0F);
//        //serviceExample.restart();
//        System.out.println("serviceExample restart()");
//
//
//
//        String request = "UCb6roUNSl5kXdSMkcyoxfOg,UCs6Agc6DvG7dZ8X4wZiGR1A";
//
//        RequestService.get( request, channels -> {
//
//            // todo
//            // render
//            // запоминает время окончания запроса
//            // отдает результат в кеш
//            // возможно, что-то делает с прогресс-баром
//
//            // test
//            channels.forEach( c -> System.out.println( c.channelId + " :)") );
//        });




        
        // todo положить в отдельный поток, сперва создав пул (может, заюзать ForkJoinPool.commonPool()?)
        // сделать FutureTsk, всегда возвращающий ArrayList<Channel>



        // todo как написать это красиво, в одну строчку? if channel == null then getChannelsFromApi
        // CompletableFuture.getChannelsFromCache()[если каналы есть, бросить исключение].ifNull().getChannelsFromApi()
        // [?] действительно ли, getChannelsFromCache и getChannelsFromApi выполняются параллельно
        ArrayList<Channel> channels = null; //getCachedChannels( request );

//        getCachedChannels( request ).stream().map( chan -> /* сохранить каналы. если что-то не так, вернуть null */chan ).findAny()
//                .orElseGet( () ->
//                        (Channel) youtubeInteractionService.getChannels( request )
//                );
    
//        Optional<ArrayList<Channel>> f = getCachedChannelsOpt( request );
        

        
        
        
        if ( channels == null ) {
    

            try {

                // Можно сделать в духе, как в кеш-сервисе:
                // channels = fetchFromCache().orElse( fetchFromYoutube() )

                // [?] Стоит ли создавать отдельный пул

                // [!] В этом месте код разветвляется
                // и то, что внутри thenApply() отработает позже того, что в конце метода handleGetChannel()
                // Решение: создать метод, возвращающий CompletableFuture и присоединить к нему then() или whenComplete

                // https://www.youtube.com/watch?v=aMQJnigGvfY
                // https://dzone.com/articles/20-examples-of-using-javas-completablefuture
                // http://www.deadcoderising.com/java8-writing-asynchronous-code-with-completablefuture/
                // CompletableFuture.supplyAsync( this::getRndInt, ForkJoinPool.commonPool() );
                // List<com.google.api.services.youtube.model.Channel> Y_channels = youtubeInteractionService.getChannels( request );
//                CompletableFuture<List<com.google.api.services.youtube.model.Channel>> one = CompletableFuture.supplyAsync( () -> youtubeInteractionService.getChannels( request ) ); // OK
//                Function<CompletableFuture<List<com.google.api.services.youtube.model.Channel>>, CompletableFuture<ArrayList<Channel>>> function =
//                        channelList -> CompletableFuture.supplyAsync( () -> youtubeInteractionService.mapChannels(channelList.getNow( null )) );
                /*
                getChannels( request )
                        .thenApply( channelArrayListStream -> {

                            channelArrayListStream.forEach( c -> {
                                System.out.println( c.channelId );
                            } );
                            return null;
                        } );
*/

                //                    com.google.api.services.youtube.model.Channel channel = response.getItems().get( 0 );
//                    System.out.printf(
//                            "This channel's ID is %s. Its title is '%s', and it has %s views.\n",
//                            channel.getId(),
//                            channel.getSnippet().getTitle(),
//                            channel.getStatistics().getViewCount());

          
        
                // todo сложить каналы в ArrayList<Channel>
                // Если его размер меньше количества каналов в запросе, выйти с ошибкой
        

        
                // todo закешировать запрос и проверить, что кешируются все каналы
//                    CacheService.set( channel1.channelId, channel1 );
        
        
            }
//            catch ( GoogleJsonResponseException err ) {
//                err.printStackTrace();
//                System.err.println( "There was a service error: " +
//                                    err.getDetails().getCode() + " : " + err.getDetails().getMessage() );
//            }
            catch ( Throwable t ) {
                t.printStackTrace();
            }
        }
    }
}


