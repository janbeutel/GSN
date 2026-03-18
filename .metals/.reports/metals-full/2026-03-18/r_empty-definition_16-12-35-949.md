error id: file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/ZmqConf.java:_empty_/NoArgsConstructor#
file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/ZmqConf.java
empty definition using pc, found symbol in pc: _empty_/NoArgsConstructor#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 215
uri: file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/ZmqConf.java
text:
```scala
package ch.epfl.gsn.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@Data
@NoArgsConstructo@@r
@AllArgsConstructor
public class ZmqConf {
    @JacksonXmlProperty(localName = "zmq-enable")
    private boolean enabled;

    @JacksonXmlProperty(localName = "zmqproxy")
    private int proxyPort;

    @JacksonXmlProperty(localName = "zmqmeta")
    private int metaPort;
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/NoArgsConstructor#