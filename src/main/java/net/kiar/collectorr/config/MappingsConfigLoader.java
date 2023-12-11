package net.kiar.collectorr.config;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import net.kiar.collectorr.config.model.MappingConfiguration;
import net.kiar.collectorr.config.model.TopicConfig;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ranSprd
 */
public class MappingsConfigLoader {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(MappingsConfigLoader.class);
    
    private static final MappingConfiguration EMPTY = new MappingConfiguration();
    
    private final MappingConfiguration config;

    private MappingsConfigLoader(MappingConfiguration config) {
        this.config = config;
    }
    
    
    public static MappingsConfigLoader readFromFile(String configFileName) {
        try {
            log.info("read topic mappings from {}", configFileName);
            String content = Files.readString( Path.of(configFileName));
            return readContent(content);
        } catch (Exception e) {
            log.error("loading given config {} failed!", configFileName, e);
        }
        return new MappingsConfigLoader(EMPTY);
    }
    
    public static MappingsConfigLoader readContent(String content) {
        MappingConfiguration cc = EMPTY;
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory()); 
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.findAndRegisterModules();

            cc = mapper.readValue(content, MappingConfiguration.class);
        } catch (Exception e) {
            log.error("can not parse given payload", e);
        }
        return new MappingsConfigLoader(cc);
    }
    
    public static String toYAML(MappingsConfigLoader loader) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory()
                .disable(Feature.WRITE_DOC_START_MARKER)
        );
        mapper.setSerializationInclusion(Include.NON_NULL);
        try {
            return mapper.writeValueAsString(loader.config);
        } catch (JsonProcessingException ex) {
            log.error("can't convert to YAML ", ex);
        }
        return "";
    }
    
    public int getNumberOfTopics() {
        return config.getTopics().size();
    }

    /**
     * get the list of configured topics which should be monitored.
     * 
     * @return 
     */
    public List<TopicConfig> getTopicsToObserve() {
        return config.getTopics();
    }
    
    /** 
     * Find a topic whereby the mqtt wildcards (+, #) are ignored. 
     * @param topic
     * @return 
     */
    public Optional<TopicConfig> findTopic(String topic) {
        return config.getTopics().stream()
                .filter(t -> topic.equals(t.getTopic()))
                .findAny();
    }
    
    
    
}
