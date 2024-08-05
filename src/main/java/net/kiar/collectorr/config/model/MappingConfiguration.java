package net.kiar.collectorr.config.model;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * root class of configuration of all topics and its payload mapping to metrics
 * 
 * @author ranSprd
 */
public class MappingConfiguration {
    private static final Logger log = LoggerFactory.getLogger(MappingConfiguration.class);
    
    private List<TopicConfig> topics = new ArrayList<>();

    public List<TopicConfig> getTopics() {
        return topics;
    }

    public void setTopics(List<TopicConfig> topics) {
        if (topics == null) {
            log.warn("given list of Topic configurations is empty (null), this can lead to unexpected behaviour.");
            this.topics = List.of();
        } else {
            this.topics = topics;
        }
    }
    
    
    
}
