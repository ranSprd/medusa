package net.kiar.collectorr.metrics;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a mapping class. Data are loaded from a source and are available as a Key/Value pair called field.
 * Each field has a name (or key). But the name of that source field can be different from the name in the final
 * metric.
 * This class maps the source field name to the target metric name
 * 
 * @author ranSprd
 */
public class FieldDescription {
    private static final Logger log = LoggerFactory.getLogger(FieldDescription.class);
    
    public static final String DESCRIPTOR_DELIMITER = "\\|";
    
    private final String fieldName;
    private int fieldIndex = 0;
    private String name;
    private FieldType type = FieldType.PAYLOAD;
    
    private FieldMapping mappings;
    
    public static FieldDescription topicField(int index, String name) {
        FieldDescription result = new FieldDescription(index, name, name);
        result.setType(FieldType.TOPIC);
        return result;
    }
    /**
     *  A field descriptor is a string which contains the field name and an optional label name.
     *  both names are separated by :. For example field:label 
     * @param content
     * @return 
     */
    public static Optional<FieldDescription> parseFieldDescriptor(String content) {
        if (content == null || content.isBlank()) {
            return Optional.empty();
        }
        
        FieldDescription result = null;
        String parts[] = content.split(DESCRIPTOR_DELIMITER);
        if (parts.length > 0) {
            if (parts[0].isBlank()) {
                log.warn("invalid field descriptor [{}] missing fieldname at index 0", content);
            } else {
                result = new FieldDescription( parts[0]);
                
                if (parts.length > 1) {
                    result.setName(parts[1]);
                }
            }
        }
        return Optional.ofNullable( result);
    }

    public FieldDescription(String fieldName) {
        this.fieldName = fieldName;
        this.name = fieldName;
        this.fieldIndex = 0;
    }

    public FieldDescription(String fieldName, String name) {
        this.fieldName = fieldName;
        this.name = name;
        this.fieldIndex = 0;
    }

    public FieldDescription(int fieldIndex, String fieldName, String name) {
        this.fieldName = fieldName;
        this.name = name;
        this.fieldIndex = fieldIndex;
    }
    
    public boolean isInValid() {
        if (type == FieldType.PAYLOAD) {
            return (fieldName == null || fieldName.isBlank());
        }
        
        // case Type = TOPIC
        return true;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FieldType getType() {
        return type;
    }

    public void setType(FieldType type) {
        this.type = type;
    }

    public int getFieldIndex() {
        return fieldIndex;
    }

    public void setFieldIndex(int fieldIndex) {
        this.fieldIndex = fieldIndex;
    }

    /** return the full mapping data. That menas the target field and the whole
     *  mappings (if content x then return y).
     */
    public FieldMapping getMappings() {
        return mappings;
    }

    public void setMappings(FieldMapping mappings) {
        this.mappings = mappings;
    }
    
    /**
     * lookup in mapping data and return the target data if a mapping exists 
     * for the given source value.
     * 
     * @param sourceValueContent
     * @return 
     */
    public Optional<FieldMappingValue> resolveMapping(String sourceValueContent) {
        if (sourceValueContent == null || mappings == null) {
            return Optional.empty();
        }
        return mappings.findMappingValueFor(sourceValueContent)
                       .map(targetValue -> new FieldMappingValue(mappings.getTargetFieldName(), targetValue));
    }
    
    public static record FieldMappingValue(String targetFieldName, String targetValue) {};
}
