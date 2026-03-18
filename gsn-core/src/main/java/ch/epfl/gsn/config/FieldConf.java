package ch.epfl.gsn.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldConf {

    @JacksonXmlProperty(isAttribute = true, localName = "name")
    private String name;

    @JacksonXmlProperty(isAttribute = true, localName = "type")
    private String dataType;

    // This captures the text between <field>...</field>
    @JacksonXmlText
    private String description;

    @JacksonXmlProperty(isAttribute = true)
    private Optional<String> unit;

    @JacksonXmlProperty(isAttribute = true)
    private Optional<String> index;
}