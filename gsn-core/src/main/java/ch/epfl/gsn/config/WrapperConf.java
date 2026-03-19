package ch.epfl.gsn.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;

public record WrapperConf(
        @JacksonXmlProperty(isAttribute = true)
        String wrapper,

        @JacksonXmlProperty(isAttribute = true, localName = "partial-order-key")
        Optional<String> partialKey,

        // Maps (xml \ "predicate") to a list of Key/Value pairs
        @JsonProperty("predicate")
        List<Predicate> predicates,

        // Maps (xml \ "output-structure" \ "field")
        @JsonProperty("output-structure")
        OutputStructureContainer outputStructure
) {
    public Map<String, String> params() {
        if (predicates == null) {
            return Map.of();
        }
        HashMap<String, String> out = new HashMap<>();
        for (Predicate p : predicates) {
            out.put(p.key(), p.value());
        }
        return out;
    }

    public List<FieldConf> output() {
        return outputStructure == null || outputStructure.fields() == null ? List.of() : outputStructure.fields();
    }

    // --- Helper records ---

    public record Predicate(
            @JacksonXmlProperty(isAttribute = true)
            String key,

            @JacksonXmlText
            String value
    ) {}

    public record OutputStructureContainer(
            @JacksonXmlProperty(localName = "field")
            List<FieldConf> fields
    ) {}
}