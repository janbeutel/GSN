error id: file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/ConfigLoader.java:java/lang/Exception#
file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/ConfigLoader.java
empty definition using pc, found symbol in pc: java/lang/Exception#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 436
uri: file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/ConfigLoader.java
text:
```scala
package ch.epfl.gsn.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.io.File;

public class GsnConfigLoader {
    private static final XmlMapper mapper = new XmlMapper();

    static {
        // Crucial: This tells Jackson how to handle Optional
        mapper.registerModule(new Jdk8Module());
    }

    public static GsnConf load(String path) throws @@Exception {
        return mapper.readValue(new File(path), GsnConf.class);
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: java/lang/Exception#