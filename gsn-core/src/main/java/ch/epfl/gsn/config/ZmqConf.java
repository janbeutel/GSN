package ch.epfl.gsn.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public record ZmqConf(
        @JacksonXmlProperty(localName = "zmq-enable")
        boolean enabled,

        @JacksonXmlProperty(localName = "zmqproxy")
        int proxyPort,

        @JacksonXmlProperty(localName = "zmqmeta")
        int metaPort
) {}