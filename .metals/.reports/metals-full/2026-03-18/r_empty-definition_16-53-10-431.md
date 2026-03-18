error id: file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/WebInputCommand.java:lombok/NoArgsConstructor#
file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/WebInputCommand.java
empty definition using pc, found symbol in pc: lombok/NoArgsConstructor#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 63
uri: file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/WebInputCommand.java
text:
```scala
package ch.epfl.gsn.config;

import lombok.Data;
import lombok.@@NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebInputCommand {

    @JacksonXmlProperty(isAttribute = true)
    private String name;

    // Maps to a sequence of <field> tags inside the XML
    // Jackson will use the FieldConf class we created earlier to parse each one
    @JsonProperty("field")
    private List<FieldConf> params;
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: lombok/NoArgsConstructor#