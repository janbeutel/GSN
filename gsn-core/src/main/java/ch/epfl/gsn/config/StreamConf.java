package ch.epfl.gsn.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record StreamConf(
        @JacksonXmlProperty(isAttribute = true)
        String name,

        // Jackson will use 0 if the attribute is missing.
        @JacksonXmlProperty(isAttribute = true)
        int rate,

        @JacksonXmlProperty(isAttribute = true)
        int count,

        // Maps to <query>text</query>
        @JacksonXmlProperty
        String query,

        // Tells Jackson to look for multiple <source> tags
        @JsonProperty("source")
        List<SourceConf> sources
) {}