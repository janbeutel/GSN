package ch.epfl.gsn.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import java.util.Optional;
import java.util.List;

public record SourceConf(
        @JacksonXmlProperty(isAttribute = true, localName = "alias")
        String alias,

        // Maps to <query>Text Content</query>
        @JacksonXmlProperty(localName = "query")
        String query,

        @JacksonXmlProperty(isAttribute = true, localName = "storage-size")
        Optional<String> storageSize,

        @JacksonXmlProperty(isAttribute = true, localName = "slide")
        Optional<String> slide,

        @JacksonXmlProperty(isAttribute = true, localName = "disconnected-buffer-size")
        Optional<Integer> disconnectBufferSize,

        @JacksonXmlProperty(isAttribute = true, localName = "sampling-rate")
        Optional<Double> samplingRate,

        // Maps (xml \ "address").map(...)
        @JacksonXmlProperty(localName = "address")
        @JacksonXmlElementWrapper(useWrapping = false)
        List<WrapperConf> wrappers
) {}