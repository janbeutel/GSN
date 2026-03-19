package ch.epfl.gsn.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import java.util.List;

public record StreamConf(
        @JacksonXmlProperty(isAttribute = true, localName = "name")
        String name,

        // Jackson will use 0 if the attribute is missing.
        @JacksonXmlProperty(isAttribute = true, localName = "rate")
        int rate,

        @JacksonXmlProperty(isAttribute = true, localName = "count")
        int count,

        // Maps to <query>text</query>
        @JacksonXmlProperty(localName = "query")
        String query,

        // Tells Jackson to look for multiple <source> tags
        @JacksonXmlProperty(localName = "source")
        @JacksonXmlElementWrapper(useWrapping = false)
        List<SourceConf> sources
) {}