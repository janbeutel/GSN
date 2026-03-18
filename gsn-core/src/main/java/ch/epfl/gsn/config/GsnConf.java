package ch.epfl.gsn.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.Optional;


@Data
@NoArgsConstructor // Required for Jackson "Merging" and most DB libraries
@AllArgsConstructor // Generates the big constructor for you
public class GsnConf {
    @JacksonXmlProperty(localName = "monitor-port")
    private int monitorPort;

    @JacksonXmlProperty(localName = "time-format")
    private String timeFormat;

    @JacksonXmlProperty(localName = "zmq-conf")
    private ZmqConf zmqConf;

    @JacksonXmlProperty(localName = "storage")
    private StorageConf storageConf;

    @JacksonXmlProperty(localName = "sliding")
    private Optional<StorageConf> slidingConf;

    @JacksonXmlProperty(localName = "max-db-connections")
    private int maxDBConnections;

    @JacksonXmlProperty(localName = "max-sliding-db-connections")
    private int maxSlidingDBConnections;

    @JacksonXmlProperty(localName = "backlog-commands")
    private BacklogCommandsConf backlogCommandsConf;
}