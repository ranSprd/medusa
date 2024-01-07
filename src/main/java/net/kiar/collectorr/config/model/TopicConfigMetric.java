package net.kiar.collectorr.config.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;

/**
 * definition of a metric (values and labels) in a given payload
 * @author ranSprd
 */
public class TopicConfigMetric {
    
    /** name of the valueFieldName field in payload */
    @JsonAlias({"valueField", "valuefieldname"})
    private String valueFieldName;
    
    /**
     * 2 types are implemented
     * counter: 
     * gauge:
     * 
     */
    @JsonAlias({"type"})
    private String metricType = "gauge";
    
    /**
     * name of the metric in prometheus. The default setting is the name of the 
     * mqtt field (set in value). Normally it is not necessary to set it, it is here
     * to overwrite this logic.
     */
    private String name;
    
    /** 
     * A long description which is delivered with metric.
     */
    private String description;
    
    /** list of labels which should be added onto the metric. 
     *  If a label is not set (means empty) it will not be added to the metric. */
    private List<String> labels;

    /** 
     * Contains the value field of the metric. It can contain index informations like
     * 'value#2' which splitts the content of the field value by whitespace an use the
     * 3rd entry of the array.
     * 
     * @return the name of the field
     */
    public String getValueFieldName() {
        return valueFieldName;
    }

    public void setValueFieldName(String valueFieldName) {
        this.valueFieldName = valueFieldName;
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }
    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
