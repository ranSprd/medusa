package net.kiar.collectorr.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import net.kiar.collectorr.config.model.connectors.ConnectorsConfig;
import net.kiar.collectorr.config.model.connectors.MqttConnectorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load connectors (mqtt, http) configuration from file.
 * 
 * @author ranSprd
 */
public class ConnectorsConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(ConnectorsConfigLoader.class);
    
    public static final ConnectorsConfigLoader INSTANCE = new ConnectorsConfigLoader();
    
    private final static ConnectorsConfig EMPTY = new ConnectorsConfig();
    
    private ConnectorsConfig config = EMPTY;
    
    public boolean readFromFile(String configFileName) {
        try {
            log.info("read topic mappings from {}", configFileName);
            String content = Files.readString( Path.of(configFileName));
            return readContent(content);
        } catch (Exception e) {
            log.error("loading given config {} failed! {}", configFileName, e.getMessage());
        }
        return false;
    }

    /**
     * 
     * @param content
     * @return 
     */
    public boolean readContent(String content) {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory()); 
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.findAndRegisterModules();

            config = mapper.readValue(content, ConnectorsConfig.class);
            if (config != null) {
                return true;
            }
        } catch (Exception e) {
            log.error("can not parse given payload {}", e.getMessage());
        }
        config = EMPTY;
        return false;
    }
    
    public Optional<List<MqttConnectorConfig>> getMqttConnectors() {
        if (config != null) {
            return Optional.ofNullable(config.getMqttBrokers());
        }
        return Optional.empty();
    }
    
}
