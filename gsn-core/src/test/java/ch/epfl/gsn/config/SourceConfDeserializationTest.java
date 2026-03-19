package ch.epfl.gsn.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.util.List;
import org.junit.Test;

public class SourceConfDeserializationTest {

    @Test
    public void deserializesSourceWithAddressPredicates() throws Exception {
        XmlMapper mapper = new XmlMapper();
        mapper.registerModule(new Jdk8Module());

        String xml = """
            <source alias="source" storage-size="1" sampling-rate="1">
              <address wrapper="remote-rest">
                <predicate key="query">select * from AdM_BackLogConfig</predicate>
                <predicate key="remote-contact-point">http://example.invalid/streaming/</predicate>
                <predicate key="start-time">continue</predicate>
              </address>
              <query>select * from wrapper</query>
            </source>
            """;

        SourceConf src = mapper.readValue(xml, SourceConf.class);

        assertEquals("source", src.alias());
        assertNotNull(src.query());
        assertEquals("select * from wrapper", src.query());
        assertTrue(src.storageSize().isPresent());
        assertTrue(src.samplingRate().isPresent());

        List<WrapperConf> wrappers = src.wrappers();
        assertEquals(1, wrappers.size());

        WrapperConf w = wrappers.get(0);
        assertEquals("remote-rest", w.wrapper());
        assertEquals(
                "select * from AdM_BackLogConfig",
                w.params().get("query")
        );
        assertEquals("http://example.invalid/streaming/", w.params().get("remote-contact-point"));
        assertEquals("continue", w.params().get("start-time"));
    }
}

