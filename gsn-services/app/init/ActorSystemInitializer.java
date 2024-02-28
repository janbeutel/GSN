package init;

import akka.actor.ActorSystem;
import akka.actor.Props;
import play.inject.ApplicationLifecycle;
import service.gsn.GSNConfigService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;

import ch.epfl.gsn.data.DataStore;
import ch.epfl.gsn.data.SensorStore;
import ch.epfl.gsn.config.GsnConf;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import play.Logger;

@Singleton
public class ActorSystemInitializer {



    @Inject
    public ActorSystemInitializer(ApplicationLifecycle lifecycle,ActorSystem actorSystem) {

        Logger.info("Application has started");

        Config config = ConfigFactory.load();
		GsnConf gsnConf = GsnConf.load(config.getString("gsn.config"));
		DataStore dataStore = new DataStore(gsnConf);
        GSNConfigService gsnConfigService = new GSNConfigService(config);

        actorSystem.actorOf(Props.create(SensorStore.class, dataStore), "gsnSensorStore");


        // Shut down the actor system when the application exits
        lifecycle.addStopHook(() -> {

            Logger.info("Application terminated");
            dataStore.close();
            actorSystem.terminate();
            return CompletableFuture.completedFuture(null);
        });
    }
}
