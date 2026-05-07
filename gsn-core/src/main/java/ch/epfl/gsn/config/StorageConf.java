package ch.epfl.gsn.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.Optional;

public record StorageConf(
        @JacksonXmlProperty(isAttribute = true, localName = "driver")
        String driver,

        @JacksonXmlProperty(isAttribute = true, localName = "url")
        String url,

        @JacksonXmlProperty(isAttribute = true, localName = "user")
        String user,

        @JacksonXmlProperty(isAttribute = true, localName = "password")
        String pass,

        @JacksonXmlProperty(isAttribute = true, localName = "identifier")
        Optional<String> identifier,

        // TODO check the below attributes
        // Optional attributes used in per-VS configuration (<storage history-size="..." storage-directory="..." timescale-chunk-size="..."/>)
        @JacksonXmlProperty(isAttribute = true, localName = "history-size")
        Optional<String> historySize,

        @JacksonXmlProperty(isAttribute = true, localName = "storage-directory")
        Optional<String> storageDirectory,

        @JacksonXmlProperty(isAttribute = true, localName = "timescale-chunk-size")
        Optional<String> chunkSize
) {
    public StorageConf {
        identifier = identifier == null ? Optional.empty() : identifier;
        historySize = historySize == null ? Optional.empty() : historySize;
        storageDirectory = storageDirectory == null ? Optional.empty() : storageDirectory;
        chunkSize = chunkSize == null ? Optional.empty() : chunkSize;
    }
}