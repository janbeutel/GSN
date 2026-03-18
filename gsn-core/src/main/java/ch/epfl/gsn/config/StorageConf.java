package ch.epfl.gsn.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageConf {
    @JacksonXmlProperty(isAttribute = true, localName = "driver")
    private String driver;

    @JacksonXmlProperty(isAttribute = true, localName = "url")
    private String url;

    @JacksonXmlProperty(isAttribute = true, localName = "user")
    private String user;

    @JacksonXmlProperty(isAttribute = true, localName = "password")
    private String pass;

    @JacksonXmlProperty(isAttribute = true, localName = "identifier")
    private Optional<String> identifier;
}