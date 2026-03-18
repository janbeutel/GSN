package ch.epfl.gsn.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebInputCommand {

    @JacksonXmlProperty(isAttribute = true)
    private String name;

    // Maps to a sequence of <field> tags inside the XML
    // Jackson will use the FieldConf class we created earlier to parse each one
    @JsonProperty("field")
    private List<FieldConf> params;
}