error id: file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/StorageConf.java:com/fasterxml/jackson/dataformat/xml/annotation/JacksonXmlProperty#
file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/StorageConf.java
empty definition using pc, found symbol in pc: com/fasterxml/jackson/dataformat/xml/annotation/JacksonXmlProperty#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 177
uri: file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/StorageConf.java
text:
```scala
package ch.epfl.gsn.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.dataformat.xml.annotation.Jackso@@nXmlProperty;

import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageConf {
    @JacksonXmlProperty(isAttribute = true, localName = "driver")
    private String driver;

    @JacksonXmlProperty(isAttribute = true, localName = "url")
    private String url;

    @JacksonXmlProperty(isAttribute = true, localName = "user")
    private String user;

    @JacksonXmlProperty(isAttribute = true, localName = "password")
    private String pass;

    @JacksonXmlProperty(isAttribute = true, localName = "identifier")
    private Optional<String> identifier;
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: com/fasterxml/jackson/dataformat/xml/annotation/JacksonXmlProperty#