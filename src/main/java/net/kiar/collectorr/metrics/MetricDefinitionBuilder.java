package net.kiar.collectorr.metrics;

import java.util.Map;
import java.util.Optional;

/**
 *
 * @author ranSprd
 */
public class MetricDefinitionBuilder {

    /**
     * start a builder with the given fieldname 
     * @param fieldNameOfMetricValue
     * @return 
     */
    public static MetricDefinitionBuilder metricForField(String fieldNameOfMetricValue) {
        return new MetricDefinitionBuilder(fieldNameOfMetricValue);
    }
    
    private final MetricDefinition metric;
    
    private MetricDefinitionBuilder(String valueName) {
        metric = new MetricDefinition( new PlaceholderString("metric_" +valueName));
        metric.setFieldOfValue(valueName);
    }
    
    public MetricDefinitionBuilder name(PlaceholderString metricName) {
        metric.setName( metricName);
        return this;
    }
    
    /** insert the given instance */
    public MetricDefinitionBuilder insertLabel(FieldDescription fieldDescription) {
        metric.getLabels().add(fieldDescription);
        return this;
    }
    
    public MetricDefinitionBuilder label(String labelFieldName, String labelName) {
        metric.registerPayloadLabel(labelFieldName, labelName);
        return this;
    }
    
    // labelname = fieldname
    public MetricDefinitionBuilder label(String labelName) {
        return label(labelName, labelName);
    }
    
    /** insert a new instance as topic label */
    public MetricDefinitionBuilder topicLabel(FieldDescription topicFieldDesc) {
        return topicLabel(topicFieldDesc.getFieldIndex(), topicFieldDesc.getName());
    }
    
    public MetricDefinitionBuilder topicLabel(int index, String labelName) {
        metric.registerTopicLabel(index, labelName);
        return this;
    }
    
    public MetricDefinitionBuilder noDescription() {
        return this;
    }
    public MetricDefinitionBuilder description(String description) {
        metric.setDescription(description);
        return this;
    }
    
    public MetricDefinitionBuilder insertMappings(String source, String target, Map<String, String> valueMappings) {
        if (valueMappings == null || valueMappings.isEmpty()) {
            return this;
        }
        Optional<FieldDescription> sourceLabel = metric.findLabel(source);
        if (sourceLabel.isEmpty()) {
            return this;
        }
        Optional<FieldDescription> targetLabel = metric.findLabel(target);
        if (targetLabel.isPresent()) {
            FieldMapping fieldMapping = new FieldMapping(target);
            fieldMapping.setMapping(valueMappings);
            sourceLabel.get().setMappings(fieldMapping);
        }
            
        return this;
    }
    
    public MetricDefinition get() {
        return metric;
    }
}
