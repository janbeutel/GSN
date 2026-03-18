error id: file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/StreamConf.java:lombok/AllArgsConstructor#
file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/StreamConf.java
empty definition using pc, found symbol in pc: lombok/AllArgsConstructor#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 103
uri: file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/StreamConf.java
text:
```scala
package ch.epfl.gsn.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgs@@Constructor;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreamConf {

    @JacksonXmlProperty(isAttribute = true)
    private String name;

    // We use int (primitive) here because your Scala code uses 0 as a default.
    // Jackson will use 0 if the attribute is missing.
    @JacksonXmlProperty(isAttribute = true)
    private int rate;

    @JacksonXmlProperty(isAttribute = true)
    private int count;

    // Maps to <query>text</query>
    private String query;

    // Maps (xml \ "source").map(...)
    // Tells Jackson to look for multiple <source> tags
    @JsonProperty("source")
    private List<SourceConf> sources;
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: lombok/AllArgsConstructor#