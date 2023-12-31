package net.kiar.collectorr.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author ranSprd
 */
public class MetricDefinition {

    private PlaceholderString name;
    private String description = "";
 
    private FieldDescription fieldOfValue;
    private final List<FieldDescription> labelNames = new ArrayList<>();

    public MetricDefinition(PlaceholderString name) {
        this.name = name;
    }
    
    public boolean isValid() {
        // todo - check if all necessary data are there
        if (fieldOfValue == null || fieldOfValue.isInValid()) {
            // ivalid because no field for the value is defined
            return false;
        }
        return true;
    }

    public void setFieldOfValue(String fieldName) {
        this.fieldOfValue = new FieldDescription(fieldName);
    }
    
    public void setFieldOfValue(FieldDescription fieldDescription) {
        this.fieldOfValue = fieldDescription;
    }

    public FieldDescription getFieldOfValue() {
        return fieldOfValue;
    }
    
    public void registerPayloadLabel(String fieldName, String labelName) {
        labelNames.add( new FieldDescription(fieldName, labelName));
    }
    
    public void registerTopicLabel(int indexInTopic, String labelName) {
        labelNames.add( FieldDescription.topicField(indexInTopic, labelName));
    }

    public List<FieldDescription> getLabels() {
        return labelNames;
    }
    
    public boolean hasLabels() {
        return !labelNames.isEmpty();
    }
    
    public Optional<FieldDescription> findLabel(String fieldName) {
        return labelNames.stream()
                    .filter(desc -> fieldName.equalsIgnoreCase( desc.getFieldName()))
                    .findAny();
    }
    
    public String resolveLabelName(String fieldName) {
        Optional<FieldDescription> fieldDesc = findLabel(fieldName);
        if (fieldDesc.isPresent()) {
            return fieldDesc.get().getName();
        }
        return fieldName;
    }

    public PlaceholderString getName() {
        return name;
    }

    public void setName(PlaceholderString name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    

    @Override
    public String toString() {
        return "MetricDefinition{" + "name=" + name + ", keyOfValue=" + fieldOfValue + ", labelNames=" + labelNames + '}';
    }
    
    
    
    
}
