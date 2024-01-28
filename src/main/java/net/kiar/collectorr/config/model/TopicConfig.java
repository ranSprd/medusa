package net.kiar.collectorr.config.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;

/**
 *
 * @author ranSprd
 */
public class TopicConfig {
    
    /** The path or topic name, it can contain wildcards and placeholders for fields  */
    private String topic;
    
    /** value mappings mostly for labels. This allows adding new labels, 
     * e.g. a human readable name based on a sensor-id */
    @JsonAlias({"mappings"})
    private RootFieldMap valueMappings;
    
    /** definition of a metric and its mapping to the fields of the payload */
    private List<TopicConfigMetric> metrics;
    
    /** default label list for all metrics of topic, if a sinlge metric has no label setting, 
     * then this configuration is used */
    private List<String> labels;
    

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

    public RootFieldMap getValueMappings() {
        return valueMappings;
    }

    public void setValueMappings(RootFieldMap valueMappings) {
        this.valueMappings = valueMappings;
    }

    public boolean hasValueMappings() {
        return (valueMappings != null && !valueMappings.isEmpty());
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

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }
    
    public boolean hasConfiguredLabels() {
        return this.labels != null && !this.labels.isEmpty();
    }
    
}
