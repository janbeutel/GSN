package ch.epfl.gsn.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;

public record WebInputCommand(
        @JacksonXmlProperty(isAttribute = true)
        String name,

        // Maps to a sequence of <field> tags inside the XML
        @JacksonXmlProperty(localName = "field")
        List<FieldConf> params
) {}