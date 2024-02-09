package net.kiar.collectorr.metrics;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.kiar.collectorr.config.model.FieldValueMap;
import net.kiar.collectorr.config.model.RootFieldMap;

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
    
    /**
     * Builder for a value field which is defined through the given string.
     * @param fieldDescriptor something like 'value#2'
     * @return 
     */
    public static MetricDefinitionBuilder metricFromFieldDescriptor(String fieldDescriptor) {
        Optional<FieldDescription> fieldDescription = FieldDescription.parseFieldDescriptor(fieldDescriptor);
        if (fieldDescription.isPresent()) {
            MetricDefinitionBuilder result = new MetricDefinitionBuilder(fieldDescription.get().getFieldName().getFullName());
            result.metric.setFieldOfValue(fieldDescription.get());
            return result;
        }
        return new MetricDefinitionBuilder("value");
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
    
    public MetricDefinitionBuilder metricType(String metricType) {
        metric.setMetricType(MetricType.resolve(metricType));
        return this;
    }
    
    /** insert the given instance
     * @param fieldDescription
     * @return  */
    public MetricDefinitionBuilder insertLabel(FieldDescription fieldDescription) {
        metric.registerLabel(fieldDescription);
        return this;
    }
    
    /** insert a label which should present as label
     * 
     * @param labelName
     * @return 
     */
    public MetricDefinitionBuilder includedLabel(String labelName) {
        FieldDescription fieldDescription = new FieldDescription(labelName);
        fieldDescription.setIncluded(true);
        metric.registerLabel(fieldDescription);
        return this;
    }
    
    public MetricDefinitionBuilder noDescription() {
        return this;
    }
    
    public MetricDefinitionBuilder description(String description) {
        metric.setDescription(description);
        return this;
    }
    
    public MetricDefinitionBuilder insertMappings(RootFieldMap mappings, Function<String, FieldDescription> fieldCreator) {
        if (mappings == null || mappings.isEmpty()) {
            return this;
        }
        
        // register for each source field targetField/value pairs based on sourceField value
        mappings.entrySet().stream()
                .forEach(entry -> registerMappingsForField(entry.getKey(), entry.getValue(), fieldCreator));
        return this;
    }
    
    private void registerMappingsForField(String sourceFieldName, FieldValueMap fieldValues, Function<String, FieldDescription> fieldCreator) {
        Optional<FieldDescription> sourceLabelField = metric.findLabel(sourceFieldName, fieldCreator);
        if (sourceLabelField.isPresent()) {
            for(Map.Entry<String, Map<String, String>> entry : fieldValues.entrySet()) {
                String sourceValue = entry.getKey();
                sourceLabelField.get().getFieldValueMappings().registerMappings(sourceValue, entry.getValue());
            }
        }
    }
    
    
    
    public MetricDefinition get() {
        return metric;
    }
    
    public String getFieldNameOfMetricValue() {
        return metric.getFieldOfValue().getFieldName().getFullName();
    }

    public FieldDescription getFieldOfMetricValue() {
        return metric.getFieldOfValue();
    }
    
    
}
