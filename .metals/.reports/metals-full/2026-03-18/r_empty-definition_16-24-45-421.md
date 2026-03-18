error id: file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/VsConf.java:_empty_/ProcessingConf#
file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/VsConf.java
empty definition using pc, found symbol in pc: _empty_/ProcessingConf#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 1388
uri: file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/VsConf.java
text:
```scala
package ch.epfl.gsn.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VsConf {

    @JacksonXmlProperty(isAttribute = true)
    private String name;

    @JacksonXmlProperty(isAttribute = true, localName = "protected")
    private boolean isProtected;

    @JacksonXmlProperty(isAttribute = true)
    private int priority;

    @JacksonXmlProperty(isAttribute = true)
    private boolean initPriority;

    @JacksonXmlProperty(isAttribute = true, localName = "time-zone")
    private String timeZone;

    private String description;

    // Maps to (xml \ "life-cycle") and its attribute "pool-size"
    private LifeCycle lifeCycle;

    // Jackson can handle Map conversion, but requires a specific structure or custom deserializer
    // If your XML is <predicate key="k">value</predicate>, use a helper class
    @JsonProperty("addressing")
    private Addressing addressing;

    @JsonProperty("storage")
    private Optional<StorageConf> storage;

    @JsonProperty("processing-class")
    private Process@@ingConf processingClass;

    @JsonProperty("streams")
    private StreamsContainer streams;

    // --- Helper Inner Classes for Nested XML Structure ---

    @Data
    public static class LifeCycle {
        @JacksonXmlProperty(isAttribute = true, localName = "pool-size")
        private Integer poolSize;
    }

    @Data
    public static class Addressing {
        @JacksonXmlProperty(localName = "predicate")
        private List<Predicate> predicates;
    }

    @Data
    public static class Predicate {
        @JacksonXmlProperty(isAttribute = true)
        private String key;
        @JacksonXmlText
        private String value;
    }

    @Data
    public static class StreamsContainer {
        @JacksonXmlProperty(localName = "stream")
        private List<StreamConf> streamList;
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/ProcessingConf#