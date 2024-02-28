package service.gsn;

import com.typesafe.config.Config;
import javax.inject.Inject;
import javax.inject.Singleton;

import ch.epfl.gsn.config.GsnConf;
import ch.epfl.gsn.data.DataStore;
import ch.epfl.gsn.data.SensorStore;
import org.zeromq.ZMQ;

@Singleton
public class GSNConfigService {

    private final Config config;
    private final GsnConf gsnConf;
    private final int pageLength;
    private final ZMQ.Context context;
    private final int backlogCommandsPort;


    @Inject
    public GSNConfigService(Config config) {
        this.config = config;
        this.gsnConf = GsnConf.load(config.getString("gsn.config"));
        //this.pageLength = config.getInt("gsn.ui.pagination.length");
        this.pageLength = 10;
        this.context = ZMQ.context(1);
        this.backlogCommandsPort = gsnConf.backlogCommandsConf().backlogCommandsPort();
    }

    public Config getConfig() {
        return config;
    }

    public GsnConf getGsnConf() {
        return gsnConf;
    }

    public int getPageLength() {
        return pageLength;
    }

    public ZMQ.Context getContext() {
        return context;
    }

    public int getBacklogCommandsPort(){
        return backlogCommandsPort;
    }

}
