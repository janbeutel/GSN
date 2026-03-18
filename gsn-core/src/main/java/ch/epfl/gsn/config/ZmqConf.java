package ch.epfl.gsn.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZmqConf {
    @JacksonXmlProperty(localName = "zmq-enable")
    private boolean enabled;

    @JacksonXmlProperty(localName = "zmqproxy")
    private int proxyPort;

    @JacksonXmlProperty(localName = "zmqmeta")
    private int metaPort;
}