package net.kiar.collectorr.config;

import net.kiar.collectorr.config.builder.TopicConfBuilder;
import net.kiar.collectorr.config.model.BaseConfig;
import net.kiar.collectorr.config.model.MappingConfiguration;
import net.kiar.collectorr.config.model.TopicConfig;

/**
 *
 * @author ranSprd
 */
public class ConfigBuilder {
    
    
    private final MappingConfiguration mappingConf = new MappingConfiguration();
    
    public static ConfigBuilder name(String name) {
        BaseConfig bc = new BaseConfig();
        bc.setName(name);
        
        ConfigBuilder configBuilder = new ConfigBuilder();
        configBuilder.mappingConf.setGeneric( bc);
        
        return configBuilder;
    }
    
    public TopicConfBuilder topic(String topicName) {
        TopicConfig top = mappingConf.getTopics().stream()
                .filter( topicConf -> topicConf.getTopic().equals(topicName))
                .findAny()
                .orElseGet(() -> new TopicConfig(topicName));
        
        return new TopicConfBuilder(top);
    }
    
}
