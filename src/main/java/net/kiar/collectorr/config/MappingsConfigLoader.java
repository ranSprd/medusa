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
import static java.util.stream.Collectors.toList;
import net.kiar.collectorr.config.model.MappingConfiguration;
import net.kiar.collectorr.config.model.TopicConfig;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ranSprd
 */
public class MappingsConfigLoader {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(MappingsConfigLoader.class);
    
    private final MappingConfiguration config;

    private MappingsConfigLoader() {
        this( new MappingConfiguration());
    }
    
    private MappingsConfigLoader(MappingConfiguration config) {
        this.config = config;
    }
    
    public static MappingsConfigLoader readFiles(List<String> configFileNames) {
        MappingsConfigLoader result = new MappingsConfigLoader();
        if (configFileNames != null && !configFileNames.isEmpty()) {
            for(String fileName : configFileNames) {
                try {
                    log.info("read topic mappings from {}", fileName);
                    String content = Files.readString( Path.of(fileName));
                    parseAsMappingConfiguration( content)
                            .ifPresent( loaded -> result.merge(loaded));
                } catch (Exception e) {
                    log.error("loading given config {} failed! \n{}", fileName, e.getMessage());
                }
            }
        }
        return result;
    }
    
    public static MappingsConfigLoader readFromFile(String connectorName, String configFileName) {
        if (configFileName != null && !configFileName.isBlank()) {
            try {
                log.info("read topic mappings from {}", configFileName);
                String content = Files.readString( Path.of(configFileName));
                return readContent(content);
            } catch (Exception e) {
                log.error("loading given config {} failed! {}", configFileName, e.getMessage());
            }
        }

        // fall through - print a warning and return an empty config
        log.warn(" {} - no configuration file for mapping 'topic to metric' defined. No topics will be observed.", connectorName);
        return new MappingsConfigLoader();
    }
    
    /**
     * Create a configuration from a single string configuration
     * @param content
     * @return 
     */
    public static MappingsConfigLoader readContent(String content) {
        return parseAsMappingConfiguration(content)
                    .map(loaded -> new MappingsConfigLoader(loaded))
                    .orElseGet( () -> new MappingsConfigLoader());
    }
    
    private static Optional<MappingConfiguration> parseAsMappingConfiguration(String content) {
        if (content == null || content.isBlank()) {
            log.warn("mapping configuration is empty or not readable");
        } else {
            try {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory()); 
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                mapper.findAndRegisterModules();

                return Optional.ofNullable(mapper.readValue(content, MappingConfiguration.class));
            } catch (Exception e) {
                log.error("can not parse given payload {}", e.getMessage());
            }
        }
        return Optional.empty();
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
    
    private void merge(MappingConfiguration otherConfig) {
        if (otherConfig == null || otherConfig.getTopics() == null || otherConfig.getTopics().isEmpty()) {
            return;
        }
        
        // merge both lists based on the topics
        // 1st remove all entry which are already in our root list
        List<TopicConfig> disjunkt = otherConfig.getTopics().stream()
                .filter( newEntry -> config.findTopic( newEntry.getTopic()).isEmpty())
                .collect(toList());
        // 2nd add that list
        if (!disjunkt.isEmpty()) {
            config.getTopics().addAll( disjunkt);
        }
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
        return config.findTopic(topic);
    }
    
    
    
}
