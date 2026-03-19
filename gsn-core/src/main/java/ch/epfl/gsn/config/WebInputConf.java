package ch.epfl.gsn.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record WebInputConf(
        @JacksonXmlProperty(isAttribute = true)
        String password,

        // Maps (xml \ "command")
        @JsonProperty("command")
        List<WebInputCommand> commands
) {}