error id: file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/GsnConf.java:_empty_/Data#
file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/GsnConf.java
empty definition using pc, found symbol in pc: _empty_/Data#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 129
uri: file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/GsnConf.java
text:
```scala
package ch.epfl.gsn.config;

import lombok.Data;
import java.util.Optional;
import xml.Elem;
import xml.Node;
import xml.XML;

@D@@ata
public class GsnConf {
  int monitorPort;
  String timeFormat;
  ZmqConf zmqConf;
  StorageConf storageConf;
  Optional<StorageConf> slidingConf;
  int maxDBConnections;
  int maxSlidingDBConnections;
  BacklogCommandsConf backlogCommandsConf;

  public GsnConf(int monitorPort, String timeFormat, ZmqConf zmqConf, StorageConf storageConf, Optional<StorageConf> slidingConf, int maxDBConnections, int maxSlidingDBConnections, BacklogCommandsConf backlogCommandsConf) {
    this.monitorPort = monitorPort;
    this.timeFormat = timeFormat;
    this.zmqConf = zmqConf;
    this.storageConf = storageConf;
    this.slidingConf = slidingConf;
    this.maxDBConnections = maxDBConnections;
    this.maxSlidingDBConnections = maxSlidingDBConnections;
    this.backlogCommandsConf = backlogCommandsConf;
  }

  public static GsnConf load(String path) {

    Elem xml = XML.load(path);

    
    return new GsnConf(
      monitorPort,
      timeFormat,
      zmqConf,
      storageConf,
      slidingConf,
      maxDBConnections,
      maxSlidingDBConnections,
      backlogCommandsConf);
  }

}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/Data#