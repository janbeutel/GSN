package ch.epfl.gsn.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public record ProcessingConf(
        @JacksonXmlProperty(localName = "class-name")
        String className,

        @JacksonXmlProperty(localName = "unique-timestamps")
        boolean uniqueTimestamp,

        // Maps (xml \ "init-params" \ "param")
        @JacksonXmlProperty(localName = "init-params")
        InitParamsContainer initParamsContainer,

        // Maps (xml \ "output-specification") and its "rate" attribute
        @JacksonXmlProperty(localName = "output-specification")
        OutputSpec outputSpec,

        // Maps (xml \ "output-structure" \ "field")
        @JacksonXmlProperty(localName = "output-structure")
        OutputStructureContainer outputStructure,

        @JacksonXmlProperty(localName = "web-input")
        Optional<WebInputConf> webInput
) {
    public ProcessingConf {
        webInput = webInput == null ? Optional.empty() : webInput;
    }

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
            @JacksonXmlElementWrapper(useWrapping = false)
            @JacksonXmlProperty(localName = "param")
            List<Param> params
    ) {}

    public record Param(
            @JacksonXmlProperty(isAttribute = true, localName = "name")
            String name,

            @JacksonXmlProperty(localName = "")
            @JacksonXmlText
            String value
    ) {}

    public record OutputSpec(
            @JacksonXmlProperty(isAttribute = true, localName = "rate")
            Integer rate
    ) {}

    public record OutputStructureContainer(
            @JacksonXmlProperty(isAttribute = true, localName = "partition-field")
            String partitionField,

            @JacksonXmlElementWrapper(useWrapping = false)
            @JacksonXmlProperty(localName = "field")
            List<FieldConf> fields
    ) {}
}