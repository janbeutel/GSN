package ch.epfl.gsn.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import java.util.List;

public record SourceConf(
        @JacksonXmlProperty(isAttribute = true)
        String alias,

        // Maps to <query>Text Content</query>
        @JacksonXmlProperty
        String query,

        @JacksonXmlProperty(isAttribute = true, localName = "storage-size")
        Optional<String> storageSize,

        @JacksonXmlProperty(isAttribute = true)
        Optional<String> slide,

        @JacksonXmlProperty(isAttribute = true, localName = "disconnected-buffer-size")
        Optional<Integer> disconnectBufferSize,

        @JacksonXmlProperty(isAttribute = true, localName = "sampling-rate")
        Optional<Double> samplingRate,

        // Maps (xml \ "address").map(...)
        @JsonProperty("address")
        List<WrapperConf> wrappers
) {}