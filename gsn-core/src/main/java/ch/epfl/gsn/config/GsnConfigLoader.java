package ch.epfl.gsn.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.io.File;
import java.io.IOException;

public class GsnConfigLoader {
    private static final XmlMapper mapper = new XmlMapper();

    static {
        // Crucial: This tells Jackson how to handle Optional
        mapper.registerModule(new Jdk8Module());
    }

    public static GsnConf load(String path) throws IOException {
        return mapper.readValue(new File(path), GsnConf.class);
    }
}