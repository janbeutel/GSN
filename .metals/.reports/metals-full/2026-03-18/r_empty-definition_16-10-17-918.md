error id: file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/GsnConf.java:lombok/Data#
file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/GsnConf.java
empty definition using pc, found symbol in pc: lombok/Data#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 44
uri: file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/GsnConf.java
text:
```scala
package ch.epfl.gsn.config;

import lombok.D@@ata;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.Optional;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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
```


#### Short summary: 

empty definition using pc, found symbol in pc: lombok/Data#