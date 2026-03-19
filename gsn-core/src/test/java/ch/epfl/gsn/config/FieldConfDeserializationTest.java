package ch.epfl.gsn.config;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.util.Optional;
import org.junit.Test;

public class FieldConfDeserializationTest {

    @Test
    public void deserializesSingleFieldConf() throws Exception {
        XmlMapper mapper = new XmlMapper();
        mapper.registerModule(new Jdk8Module());

        String xml = "<field name=\"POSITION\" type=\"INTEGER\" index=\"true\">hello</field>";
        FieldConf fc = mapper.readValue(xml, FieldConf.class);

        assertEquals("POSITION", fc.name());
        assertEquals("INTEGER", fc.dataType());
        assertEquals(Optional.of("true"), fc.index());
        assertEquals("hello", fc.description());
    }
}

