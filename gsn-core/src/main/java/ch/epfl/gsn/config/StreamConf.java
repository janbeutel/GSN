package ch.epfl.gsn.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
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