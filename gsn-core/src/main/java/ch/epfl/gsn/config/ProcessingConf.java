package ch.epfl.gsn.config;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import java.util.List;
import java.util.Optional;

@Data
public class ProcessingConf {

    @JsonProperty("class-name")
    private String className;

    @JsonProperty("unique-timestamps")
    private boolean uniqueTimestamp;

    // Maps (xml \ "init-params" \ "param")
    @JsonProperty("init-params")
    private InitParamsContainer initParamsContainer;

    // Maps (xml \ "output-specification") and its "rate" attribute
    @JsonProperty("output-specification")
    private OutputSpec outputSpec;

    // Maps (xml \ "output-structure" \ "field")
    @JsonProperty("output-structure")
    private OutputStructureContainer outputStructure;

    @JsonProperty("web-input")
    private Optional<WebInputConf> webInput;

    // --- Helper classes to handle XML nesting ---

    @Data
    public static class InitParamsContainer {
        @JacksonXmlProperty(localName = "param")
        private List<Param> params;
    }

    @Data
    public static class Param {
        @JacksonXmlProperty(isAttribute = true)
        private String name;
        @JacksonXmlText
        private String value;
    }

    @Data
    public static class OutputSpec {
        @JacksonXmlProperty(isAttribute = true)
        private Integer rate;
    }

    @Data
    public static class OutputStructureContainer {
        @JacksonXmlProperty(isAttribute = true, localName = "partition-field")
        private String partitionField;

        @JacksonXmlProperty(localName = "field")
        private List<FieldConf> fields;
    }
}