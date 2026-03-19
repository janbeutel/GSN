package ch.epfl.gsn.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public record BacklogCommandsConf(
        @JacksonXmlProperty(localName = "backlog-commands-enable")
        boolean enabled,

        @JacksonXmlProperty(localName = "backlog-commands-port")
        int backlogCommandsPort
) {}