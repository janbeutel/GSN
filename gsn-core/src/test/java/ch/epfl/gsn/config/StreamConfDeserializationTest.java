package ch.epfl.gsn.config;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.junit.Test;

public class StreamConfDeserializationTest {

    @Test
    public void deserializesStreamWithSources() throws Exception {
        XmlMapper mapper = new XmlMapper();
        mapper.registerModule(new Jdk8Module());

        String xml = """
            <stream name="data" rate="1" count="0">
              <source alias="source" storage-size="1" sampling-rate="1">
                <address wrapper="remote-rest">
                  <predicate key="query">select * from AdM_BackLogConfig</predicate>
                  <predicate key="remote-contact-point">http://example.invalid/streaming/</predicate>
                  <predicate key="start-time">continue</predicate>
                </address>
                <query>select * from wrapper</query>
              </source>
              <query>select * from source</query>
            </stream>
            """;

        StreamConf s = mapper.readValue(xml, StreamConf.class);

        assertEquals("data", s.name());
        assertEquals(1, s.rate());
        assertEquals(0, s.count());
        assertEquals("select * from source", s.query());
        assertEquals(1, s.sources().size());
        assertEquals("source", s.sources().get(0).alias());
    }
}

