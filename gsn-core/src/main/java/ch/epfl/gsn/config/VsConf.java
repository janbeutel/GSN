package ch.epfl.gsn.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import java.util.HashMap;
import java.util.Optional;
import java.util.List;
import java.util.Map;

public record VsConf(
        @JacksonXmlProperty(isAttribute = true, localName = "name")
        String name,

        @JacksonXmlProperty(isAttribute = true, localName = "protected")
        boolean isProtected,

        @JacksonXmlProperty(isAttribute = true, localName = "priority")
        int priority,

        @JacksonXmlProperty(isAttribute = true, localName = "initPriority")
        boolean initPriority,

        @JacksonXmlProperty(isAttribute = true, localName = "time-zone")
        String timeZone,

        @JacksonXmlProperty(localName = "description")
        String description,

        // Maps to (xml \ "life-cycle") and its attribute "pool-size"
        @JacksonXmlProperty(localName = "life-cycle")
        LifeCycle lifeCycle,

        // If your XML is <predicate key="k">value</predicate>, use the helper class.
        @JacksonXmlProperty(localName = "addressing")
        Addressing addressing,

        @JacksonXmlProperty(localName = "storage")
        Optional<StorageConf> storage,

        @JacksonXmlProperty(localName = "processing-class")
        ProcessingConf processingClass,

        @JacksonXmlProperty(localName = "streams")
        StreamsContainer streamsContainer
) {
    public VsConf {
        storage = storage == null ? Optional.empty() : storage;
    }

    public static VsConf load(String path) throws Exception {
        return VsConfigLoader.load(path);
    }

    public ProcessingConf processing() {
        return processingClass;
    }

    public Optional<Integer> poolSize() {
        if (lifeCycle == null || lifeCycle.poolSize() == null) {
            return Optional.empty();
        }
        return Optional.of(lifeCycle.poolSize());
    }

    public Map<String, String> address() {
        if (addressing == null || addressing.predicates() == null) {
            return Map.of();
        }
        HashMap<String, String> out = new HashMap<>();
        for (Predicate p : addressing.predicates()) {
            out.put(p.key(), p.value());
        }
        return out;
    }

    public Optional<String> storageSize() {
        if (storage == null || storage.isEmpty()) {
            return Optional.empty();
        }
        Optional<String> historySize = storage.get().historySize();
        return historySize == null ? Optional.empty() : historySize;
    }

    public Optional<String> storageDirectory() {
        if (storage == null || storage.isEmpty()) {
            return Optional.empty();
        }
        Optional<String> storageDirectory = storage.get().storageDirectory();
        return storageDirectory == null ? Optional.empty() : storageDirectory;
    }

    public Optional<String> chunkSize() {
        if (storage == null || storage.isEmpty()) {
            return Optional.empty();
        }
        Optional<String> chunkSize = storage.get().chunkSize();
        return chunkSize == null ? Optional.empty() : chunkSize;
    }

    public List<StreamConf> streams() {
        if (streamsContainer == null || streamsContainer.streamList() == null) {
            return List.of();
        }
        return streamsContainer.streamList();
    }

    // --- Helper Inner Records for Nested XML Structure ---

    public record LifeCycle(
            @JacksonXmlProperty(isAttribute = true, localName = "pool-size")
            Integer poolSize
    ) {}

    public record Addressing(
            @JacksonXmlElementWrapper(useWrapping = false)
            @JacksonXmlProperty(localName = "predicate")
            List<Predicate> predicates
    ) {}

    public record Predicate(
            @JacksonXmlProperty(isAttribute = true, localName = "key")
            String key,

            @JacksonXmlProperty(localName = "")
            @JacksonXmlText
            String value
    ) {}

    public record StreamsContainer(
            @JacksonXmlElementWrapper(useWrapping = false)
            @JacksonXmlProperty(localName = "stream")
            List<StreamConf> streamList
    ) {}
}