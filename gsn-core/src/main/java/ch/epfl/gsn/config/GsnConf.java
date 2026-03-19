package ch.epfl.gsn.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.Optional;
import java.io.IOException;

public record GsnConf(
        @JacksonXmlProperty(localName = "monitor-port")
        int monitorPort,

        @JacksonXmlProperty(localName = "time-format")
        String timeFormat,

        @JacksonXmlProperty(localName = "zmq-conf")
        ZmqConf zmqConf,

        @JacksonXmlProperty(localName = "storage")
        StorageConf storageConf,

        @JacksonXmlProperty(localName = "sliding")
        Optional<StorageConf> slidingConf,

        @JacksonXmlProperty(localName = "max-db-connections")
        int maxDBConnections,

        @JacksonXmlProperty(localName = "max-sliding-db-connections")
        int maxSlidingDBConnections,

        @JacksonXmlProperty(localName = "backlog-commands")
        BacklogCommandsConf backlogCommandsConf
) {
    public static GsnConf load(String path) throws IOException {
        return GsnConfigLoader.load(path);
    }
}