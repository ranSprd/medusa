package net.kiar.collectorr.config.model;

import java.util.List;

/**
 *
 * @author ranSprd
 */
public class TopicConfig {
    
    /** The path or topic name, it can contain wildcards and placeholders for fields  */
    private String topic;
    
    /** value mappings of labels. This allows adding new labels, 
     * e.g. a human readable name based on a sensor-id */
    private List<TopicConfigMappings> mappings;
    
    /** definition of a metric and its mapping to the fields of the payload */
    private List<TopicConfigMetric> metrics;
    

    public TopicConfig() {
    }

    public TopicConfig(String topic) {
        this.topic = topic;
    }
    

    /**
     * The topic definition, it can contain the mqtt wildcards # or +
     * @return 
     */
    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<TopicConfigMappings> getMappings() {
        return mappings;
    }

    public void setMappings(List<TopicConfigMappings> mappings) {
        this.mappings = mappings;
    }
    
    public boolean hasMappings() {
        return (mappings != null && !mappings.isEmpty());
    }
    
    public List<TopicConfigMetric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<TopicConfigMetric> metrics) {
        this.metrics = metrics;
    }
    
    /**
     * true if the metrics section is not present or empty
     * @return 
     */
    public boolean hasNoMetrics() {
        return (metrics == null || metrics.isEmpty());
    }
    
}
