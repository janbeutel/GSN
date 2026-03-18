error id: file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/GsnConf.java:_empty_/JsonProperty#
file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/GsnConf.java
empty definition using pc, found symbol in pc: _empty_/JsonProperty#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 687
uri: file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/GsnConf.java
text:
```scala
package ch.epfl.gsn.config;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.Optional;

@Data
public class GsnConf {
    private final int monitorPort;
    private final String timeFormat;
    private final ZmqConf zmqConf;
    private final StorageConf storageConf;
    private final Optional<StorageConf> slidingConf;
    private final int maxDBConnections;
    private final int maxSlidingDBConnections;
    private final BacklogCommandsConf backlogCommandsConf;

    @JsonCreator
    public GsnConf(
        @Jso@@nProperty("monitor-port") int monitorPort,
        @JsonProperty("time-format") String timeFormat,
        @JsonProperty("zmq-conf") ZmqConf zmqConf, // Assuming tag name
        @JsonProperty("storage") StorageConf storageConf,
        @JsonProperty("sliding") Optional<StorageConf> slidingConf,
        @JsonProperty("max-db-connections") int maxDBConnections,
        @JsonProperty("max-sliding-db-connections") int maxSlidingDBConnections,
        @JsonProperty("backlog-commands") BacklogCommandsConf backlogCommandsConf
    ) {
        this.monitorPort = monitorPort;
        this.timeFormat = timeFormat;
        this.zmqConf = zmqConf;
        this.storageConf = storageConf;
        this.slidingConf = slidingConf;
        this.maxDBConnections = maxDBConnections;
        this.maxSlidingDBConnections = maxSlidingDBConnections;
        this.backlogCommandsConf = backlogCommandsConf;
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/JsonProperty#