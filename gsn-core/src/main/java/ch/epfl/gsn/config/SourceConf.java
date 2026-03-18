package ch.epfl.gsn.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SourceConf {

    @JacksonXmlProperty(isAttribute = true)
    private String alias;

    // Maps to <query>Text Content</query>
    private String query;

    @JacksonXmlProperty(isAttribute = true, localName = "storage-size")
    private Optional<String> storageSize;

    @JacksonXmlProperty(isAttribute = true)
    private Optional<String> slide;

    @JacksonXmlProperty(isAttribute = true, localName = "disconnected-buffer-size")
    private Optional<Integer> disconnectBufferSize;

    @JacksonXmlProperty(isAttribute = true, localName = "sampling-rate")
    private Optional<Double> samplingRate;

    // Maps (xml \ "address").map(...)
    // We use @JsonProperty to tell Jackson the tag name is "address" 
    // even though the Java field is called "wrappers"
    @JsonProperty("address")
    private List<WrapperConf> wrappers;
}