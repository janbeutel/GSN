package ch.epfl.gsn.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public record ProcessingConf(
        @JsonProperty("class-name")
        String className,

        @JsonProperty("unique-timestamps")
        boolean uniqueTimestamp,

        // Maps (xml \ "init-params" \ "param")
        @JsonProperty("init-params")
        InitParamsContainer initParamsContainer,

        // Maps (xml \ "output-specification") and its "rate" attribute
        @JsonProperty("output-specification")
        OutputSpec outputSpec,

        // Maps (xml \ "output-structure" \ "field")
        @JsonProperty("output-structure")
        OutputStructureContainer outputStructure,

        @JsonProperty("web-input")
        Optional<WebInputConf> webInput
) {
    public Optional<Integer> rate() {
        return outputSpec == null || outputSpec.rate() == null ? Optional.empty() : Optional.of(outputSpec.rate());
    }

    public List<FieldConf> output() {
        return outputStructure == null || outputStructure.fields() == null ? List.of() : outputStructure.fields();
    }

    public java.util.Map<String, String> initParams() {
        if (initParamsContainer == null || initParamsContainer.params() == null) {
            return java.util.Map.of();
        }
        HashMap<String, String> out = new HashMap<>();
        for (Param p : initParamsContainer.params()) {
            out.put(p.name(), p.value());
        }
        return out;
    }

    // --- Helper records to handle XML nesting ---

    public record InitParamsContainer(
            @JacksonXmlProperty(localName = "param")
            List<Param> params
    ) {}

    public record Param(
            @JacksonXmlProperty(isAttribute = true)
            String name,

            @JacksonXmlText
            String value
    ) {}

    public record OutputSpec(
            @JacksonXmlProperty(isAttribute = true)
            Integer rate
    ) {}

    public record OutputStructureContainer(
            @JacksonXmlProperty(isAttribute = true, localName = "partition-field")
            String partitionField,

            @JacksonXmlProperty(localName = "field")
            List<FieldConf> fields
    ) {}
}