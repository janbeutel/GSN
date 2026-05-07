package ch.epfl.gsn.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;

public record WrapperConf(
        @JacksonXmlProperty(isAttribute = true, localName = "wrapper")
        String wrapper,

        @JacksonXmlProperty(isAttribute = true, localName = "partial-order-key")
        Optional<String> partialKey,

        // Maps (xml \ "predicate") to a list of Key/Value pairs
        @JacksonXmlProperty(localName = "predicate")
        @JacksonXmlElementWrapper(useWrapping = false)
        List<Predicate> predicates,

        // Maps (xml \ "output-structure" \ "field")
        @JacksonXmlProperty(localName = "output-structure")
        OutputStructureContainer outputStructure
) {
    public WrapperConf {
        partialKey = partialKey == null ? Optional.empty() : partialKey;
    }

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
            @JacksonXmlProperty(isAttribute = true, localName = "key")
            String key,

            @JacksonXmlProperty(localName = "")
            @JacksonXmlText
            String value
    ) {}

    public record OutputStructureContainer(
            @JacksonXmlElementWrapper(useWrapping = false)
            @JacksonXmlProperty(localName = "field")
            List<FieldConf> fields
    ) {}
}