package ch.epfl.gsn.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WrapperConf {

    @JacksonXmlProperty(isAttribute = true)
    private String wrapper;

    @JacksonXmlProperty(isAttribute = true, localName = "partial-order-key")
    private Optional<String> partialKey;

    // Maps (xml \ "predicate") to a list of Key/Value pairs
    @JsonProperty("predicate")
    private List<Predicate> predicates;

    // Maps (xml \ "output-structure" \ "field")
    @JsonProperty("output-structure")
    private OutputStructureContainer outputStructure;

    // --- Helper Classes ---

    @Data
    public static class Predicate {
        @JacksonXmlProperty(isAttribute = true)
        private String key;

        @JacksonXmlText
        private String value;
    }

    @Data
    public static class OutputStructureContainer {
        @JacksonXmlProperty(localName = "field")
        private List<FieldConf> fields;
    }
}