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
public class WebInputConf {

    @JacksonXmlProperty(isAttribute = true)
    private String password;

    // Maps (xml \ "command")
    // Jackson will recursively use the WebInputCommand class
    @JsonProperty("command")
    private List<WebInputCommand> commands;
}