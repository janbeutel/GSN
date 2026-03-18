error id: file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/ConfigLoader.java:_empty_/XmlMapper#registerModule#
file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/ConfigLoader.java
empty definition using pc, found symbol in pc: _empty_/XmlMapper#registerModule#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 355
uri: file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/ConfigLoader.java
text:
```scala
package ch.epfl.gsn.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.io.File;

public class ConfigLoader {
    private static final XmlMapper mapper = new XmlMapper();

    static {
        // Crucial: This tells Jackson how to handle Optional
        mapper.registerModule@@(new Jdk8Module());
    }

    public static GsnConf load(String path) throws Exception {
        return mapper.readValue(new File(path), GsnConf.class);
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/XmlMapper#registerModule#