package net.kiar.collectorr.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ranSprd
 */
public class MetricDefinition {
    private static final Logger log = LoggerFactory.getLogger(MetricDefinition.class);

    private PlaceholderString name;
    private String description = "";
    private MetricType metricType = MetricType.GAUGE;
 
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

    public MetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
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
    
//    public void registerPayloadLabel(String fieldName, String labelName) {
//        boolean alreadyInList = labelNames.stream()
//                    .filter(node -> node.getType() == FieldSourceType.PAYLOAD)
//                    .anyMatch(node -> node.getFieldName().getFullName().equalsIgnoreCase(fieldName));
//        if (!alreadyInList) {
//            labelNames.add( new FieldDescription(fieldName, labelName));
//        }
//    }
    
    public void registerLabel(FieldDescription fieldDescription) {
        Optional<FieldDescription> alreadyInList = labelNames.stream()
                .filter(node -> node.getFieldName().getFullName().equalsIgnoreCase( fieldDescription.getFieldName().getFullName()))
                .findAny();
        if (alreadyInList.isPresent()) {
            if (alreadyInList.get().getType() != fieldDescription.getType()) {
                log.warn("found field {} sevaral times with different types [{}, {}] which can lead to unpredictable behavior",
                        fieldDescription.getFieldName().getFullName(), alreadyInList.get().getType(), fieldDescription.getType());
            }
        } else {
            labelNames.add( fieldDescription);
        }
    }
    
    public List<FieldDescription> getLabels() {
        return labelNames;
    }
    
    public boolean hasLabels() {
        return !labelNames.isEmpty();
    }
    
    public Optional<FieldDescription> findLabel(String fieldName) {
        return labelNames.stream()
                    .filter(desc -> fieldName.equalsIgnoreCase( desc.getFieldName().getFullName()))
                    .findAny();
    }
    
    public Optional<FieldDescription> findLabel(String fieldName, Function<String, FieldDescription> fieldCreator) {
        Optional<FieldDescription> result = findLabel(fieldName);
        if (result.isEmpty()) {
            FieldDescription field = fieldCreator.apply(fieldName);
            if (field != null) {
                labelNames.add(field);
                result = Optional.of(field);
            }
        }
        return result;
    }
    
    public String resolveLabelName(String fieldName) {
        Optional<FieldDescription> fieldDesc = findLabel(fieldName);
        if (fieldDesc.isPresent()) {
            return fieldDesc.get().getFieldName().getFullName();
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
