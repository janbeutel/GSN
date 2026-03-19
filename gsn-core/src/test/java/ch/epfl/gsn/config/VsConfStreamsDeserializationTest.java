package ch.epfl.gsn.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.util.List;
import org.junit.Test;

public class VsConfStreamsDeserializationTest {

    @Test
    public void deserializesStreamsOnlyVirtualSensor() throws Exception {
        XmlMapper mapper = new XmlMapper();
        mapper.registerModule(new Jdk8Module());

        String xml = """
            <virtual-sensor name="n" protected="false" priority="1" initPriority="false">
              <description>d</description>
              <streams>
                <stream name="data" rate="1" count="0">
                  <source alias="source" storage-size="1" sampling-rate="1">
                    <address wrapper="remote-rest">
                      <predicate key="query">select * from AdM_BackLogConfig</predicate>
                    </address>
                    <query>select * from wrapper</query>
                  </source>
                  <query>select * from source</query>
                </stream>
              </streams>
            </virtual-sensor>
            """;

        VsConf conf = mapper.readValue(xml, VsConf.class);
        assertNotNull(conf);

        List<StreamConf> streams = conf.streams();
        assertEquals(1, streams.size());
        StreamConf s = streams.get(0);
        assertEquals("data", s.name());
        assertEquals("select * from source", s.query());
        assertEquals(1, s.sources().size());
        assertEquals("source", s.sources().get(0).alias());
    }
}

