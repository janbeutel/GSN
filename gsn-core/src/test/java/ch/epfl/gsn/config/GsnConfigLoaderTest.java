package ch.epfl.gsn.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

import org.junit.Test;

public class GsnConfigLoaderTest {

    private static File writeTempXml(String xml) throws Exception {
        var path = Files.createTempFile("gsn-gsn-", ".xml");
        Files.write(path, xml.getBytes(StandardCharsets.UTF_8));
        return path.toFile();
    }

    @Test
    public void deserializesRepresentativeSensorServerConfig() throws Exception {
        // Shape adapted to the current Jackson annotations in GsnConf / nested *Conf records.
        String xml = """
            <sensor-server>
              <monitor-port>22001</monitor-port>
              <time-format>HH:mm:ss</time-format>

              <zmq-conf>
                <zmq-enable>true</zmq-enable>
                <zmqproxy>22022</zmqproxy>
                <zmqmeta>22023</zmqmeta>
              </zmq-conf>

              <storage user="sa" password="" driver="org.h2.Driver" url="jdbc:h2:mem:gsn_mem_db" identifier="main" history-size="10" storage-directory="/tmp/gsn" timescale-chunk-size="60" />
              <sliding user="sa" password="" driver="org.h2.Driver" url="jdbc:h2:mem:sliding" identifier="sliding" history-size="20" storage-directory="/tmp/sliding" timescale-chunk-size="120" />

              <max-db-connections>8</max-db-connections>
              <max-sliding-db-connections>9</max-sliding-db-connections>

              <backlog-commands>
                <backlog-commands-enable>false</backlog-commands-enable>
                <backlog-commands-port>55555</backlog-commands-port>
              </backlog-commands>
            </sensor-server>
            """;

        File f = writeTempXml(xml);
        GsnConf conf = GsnConfigLoader.load(f.getAbsolutePath());

        assertNotNull(conf);
        assertEquals(22001, conf.monitorPort());
        assertEquals("HH:mm:ss", conf.timeFormat());

        ZmqConf zmq = conf.zmqConf();
        assertNotNull(zmq);
        assertTrue(zmq.enabled());
        assertEquals(22022, zmq.proxyPort());
        assertEquals(22023, zmq.metaPort());

        StorageConf storage = conf.storageConf();
        assertEquals("org.h2.Driver", storage.driver());
        assertEquals("jdbc:h2:mem:gsn_mem_db", storage.url());
        assertEquals("sa", storage.user());
        assertEquals("", storage.pass());
        assertEquals(Optional.of("main"), storage.identifier());
        assertEquals(Optional.of("10"), storage.historySize());
        assertEquals(Optional.of("/tmp/gsn"), storage.storageDirectory());
        assertEquals(Optional.of("60"), storage.chunkSize());

        assertTrue(conf.slidingConf().isPresent());
        StorageConf sliding = conf.slidingConf().get();
        assertEquals("jdbc:h2:mem:sliding", sliding.url());
        assertEquals(Optional.of("sliding"), sliding.identifier());
        assertEquals(Optional.of("20"), sliding.historySize());
        assertEquals(Optional.of("120"), sliding.chunkSize());

        BacklogCommandsConf backlog = conf.backlogCommandsConf();
        assertNotNull(backlog);
        assertEquals(false, backlog.enabled());
        assertEquals(55555, backlog.backlogCommandsPort());
    }
}

