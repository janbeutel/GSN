package ch.epfl.gsn.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BacklogCommandsConf {
    @JacksonXmlProperty(localName = "backlog-commands-enable")
    private boolean enabled;

    @JacksonXmlProperty(localName = "backlog-commands-port")
    private int backlogCommandsPort;
}