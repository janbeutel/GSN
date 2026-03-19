package ch.epfl.gsn.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;

public class VsConfigLoaderTest {

    private static File writeTempXml(String xml) throws Exception {
        var path = Files.createTempFile("gsn-vs-", ".xml");
        Files.write(path, xml.getBytes(StandardCharsets.UTF_8));
        return path.toFile();
    }

    @Test
    public void deserializesRepresentativeVirtualSensor() throws Exception {
        // Anonymized + minimized XML fixture based on a typical <virtual-sensor> config.
        String xml = """
            <virtual-sensor name="AdM_BackLogConfig__mapped" protected="false" priority="10" initPriority="false" time-zone="Europe/Zurich">
              <life-cycle pool-size="10" />
              <description>BackLog configuration for AdM deployment</description>
              <addressing />
              <storage user="sa" password="" driver="org.h2.Driver" url="jdbc:h2:mem:testdb" history-size="5" storage-directory="/tmp/gsn" timescale-chunk-size="60" />
              <processing-class>
                <class-name>gsn.vsensor.BridgeVirtualSensorPermasense</class-name>
                <unique-timestamps>false</unique-timestamps>
                <init-params>
                  <param name="position_mapping" />
                </init-params>
                <output-structure>
                  <field name="POSITION" type="INTEGER" index="true" />
                  <field name="DEVICE_ID" type="INTEGER" />
                  <field name="GENERATION_TIME" type="BIGINT" unit="unixtime" index="true" />
                  <field name="MESSAGE" type="VARCHAR(256)" />
                  <field name="CONFIGURATION" type="binary" />
                </output-structure>
              </processing-class>
              <streams>
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
              </streams>
            </virtual-sensor>
            """;

        File f = writeTempXml(xml);
        VsConf conf = VsConfigLoader.load(f.getAbsolutePath());

        assertNotNull(conf);
        assertEquals("AdM_BackLogConfig__mapped", conf.name());
        assertEquals(10, conf.priority());
        assertEquals(Optional.of(10), conf.poolSize());
        assertTrue(conf.address().isEmpty());
        assertEquals(Optional.of("5"), conf.storageSize());
        assertEquals(Optional.of("/tmp/gsn"), conf.storageDirectory());
        assertEquals(Optional.of("60"), conf.chunkSize());

        ProcessingConf processing = conf.processing();
        assertEquals("gsn.vsensor.BridgeVirtualSensorPermasense", processing.className());
        assertEquals(false, processing.uniqueTimestamp());
        assertTrue(processing.initParams().containsKey("position_mapping"));
        assertNull(processing.initParams().get("position_mapping"));

        List<FieldConf> output = processing.output();
        assertEquals(5, output.size());
        Map<String, FieldConf> byName = output.stream().collect(Collectors.toMap(FieldConf::name, x -> x));

        FieldConf pos = byName.get("POSITION");
        assertNotNull(pos);
        assertEquals("INTEGER", pos.dataType());
        assertEquals(Optional.of("true"), pos.index());
        assertNull(pos.description());

        FieldConf genTime = byName.get("GENERATION_TIME");
        assertNotNull(genTime);
        assertEquals("BIGINT", genTime.dataType());
        assertEquals(Optional.of("unixtime"), genTime.unit());
        assertEquals(Optional.of("true"), genTime.index());

        List<StreamConf> streams = conf.streams();
        assertEquals(1, streams.size());
        StreamConf stream = streams.get(0);
        assertEquals("data", stream.name());
        assertEquals(1, stream.rate());
        assertEquals(0, stream.count());
        assertEquals("select * from source", stream.query());
        assertEquals(1, stream.sources().size());

        SourceConf source = stream.sources().get(0);
        assertEquals("source", source.alias());
        assertEquals(Optional.of("1"), source.storageSize());
        assertTrue(source.samplingRate().isPresent());
        assertEquals(1.0, source.samplingRate().get(), 0.0001);
        assertEquals("select * from wrapper", source.query());

        assertEquals(1, source.wrappers().size());
        WrapperConf wrapper = source.wrappers().get(0);
        assertEquals("remote-rest", wrapper.wrapper());

        Map<String, String> params = wrapper.params();
        assertEquals("select * from AdM_BackLogConfig", params.get("query"));
        assertEquals("http://example.invalid/streaming/", params.get("remote-contact-point"));
        assertEquals("continue", params.get("start-time"));
    }
}

