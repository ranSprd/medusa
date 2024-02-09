package net.kiar.collectorr.config.model;

import java.util.ArrayList;
import java.util.List;

/**
 * root class of configuration of all topics and its payload mapping to metrics
 * 
 * @author ranSprd
 */
public class MappingConfiguration {
    
    private BaseConfig generic;
    private List<TopicConfig> topics = new ArrayList<>();

    public BaseConfig getGeneric() {
        return generic;
    }

    public void setGeneric(BaseConfig generic) {
        this.generic = generic;
    }

    public List<TopicConfig> getTopics() {
        return topics;
    }

    public void setTopics(List<TopicConfig> topics) {
        this.topics = topics;
    }
    
    
    
}
