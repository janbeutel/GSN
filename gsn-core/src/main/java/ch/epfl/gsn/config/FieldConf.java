package ch.epfl.gsn.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import java.util.Optional;

public record FieldConf(
        @JacksonXmlProperty(isAttribute = true, localName = "name")
        String name,

        @JacksonXmlProperty(isAttribute = true, localName = "type")
        String dataType,

        // Captures the text between <field>...</field>
        @JacksonXmlText
        String description,

        @JacksonXmlProperty(isAttribute = true)
        Optional<String> unit,

        @JacksonXmlProperty(isAttribute = true)
        Optional<String> index
) {}