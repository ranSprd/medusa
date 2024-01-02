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
    private int fieldIndex = -1;
    private String name;
    private FieldType type = FieldType.PAYLOAD;
    
    private FieldMapping mappings;
    private String fixedContent = null;
    
    public static FieldDescription topicField(int index, String name) {
        FieldDescription result = new FieldDescription(index, name, name);
        result.setType(FieldType.TOPIC);
        return result;
    }
    /**
     *  A field descriptor is a string which contains the field name and an optional label name.
     *  both names are separated by |. For example field|label. Furthermore the descriptor can contain
     *  an index in the value. For instance, if you have a value like '5.6 m/s' then the interesting part 
     *  is '5.6'. For easy access to that part of the input data, each value is splitted by whitespaces 
     *  and each of the the resulting fields can be accessed by index. For our example '5.6 m/s' the 
     *  descriptor is 'value#0' (if the field is named value).
     * 
     * @param content
     * @return 
     */
    public static Optional<FieldDescription> parseFieldDescriptor(String content) {
        if (content == null || content.isBlank()) {
            return Optional.empty();
        }
        
        // ignore labels with leading -
        String rawContent = content.trim();
        if (rawContent.startsWith("-")) {
            return Optional.empty();
        }
        
        FieldDescription result = null;
        String parts[] = rawContent.split(DESCRIPTOR_DELIMITER);
        if (parts.length > 0) {
            if (parts[0].isBlank()) {
                log.warn("invalid field descriptor [{}] missing fieldname at index 0", content);
            } else {
                // first check format key=value
                if (parts[0].contains("=")) {
                    String[] pair = parts[0].split("=");
                    if (pair.length > 0) {
                        String name = pair[0].trim();
                        if (name.length() > 0) {
                            result = new FieldDescription(name);
                            if (pair.length > 1) {
                                // happy case
                                result.setFixedContent(pair[1].trim());
                            }
                        } else {
                            // Can't extract an valid fieldname (maybe something like '=foo' found
                            return Optional.empty();
                        }
                    } else {
                        // another failure, same result, can't extract an fieldname
                        return Optional.empty();
                    }
                    
                } else {
                    result = new FieldDescription( FieldIndexDescriptor.parse(parts[0]));
                }
                
                if (parts.length > 1) {
                    // here we know, that a name-section is present. Parse that part
                    FieldIndexDescriptor namePart = FieldIndexDescriptor.parse(parts[1]);
                    result.setName( namePart.getName());
                    if (result.getFieldIndex() < 0) {
                        // maybe the name part contains an index (field|name#1), so we add this info
                        result.setFieldIndex(namePart.getIndex());
                    }
                }
            }
        }
        return Optional.ofNullable( result);
    }
    
    private FieldDescription( FieldIndexDescriptor fieldIndexDescriptor) {
        this(fieldIndexDescriptor.getName());
        if (fieldIndexDescriptor.getIndex() >= 0) {
            this.fieldIndex = fieldIndexDescriptor.getIndex();
        }
    }
    
    public FieldDescription(String fieldName) {
        this.fieldName = fieldName;
        this.name = fieldName;
        this.fieldIndex = -1;
    }

    public FieldDescription(String fieldName, String name) {
        this.fieldName = fieldName;
        this.name = name;
        this.fieldIndex = -1;
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

    public String getFixedContent() {
        return fixedContent;
    }

    public void setFixedContent(String fixedContent) {
        this.fixedContent = fixedContent;
    }
    
    public boolean hasFixedContent() {
        return fixedContent != null && !fixedContent.isBlank();
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
    
    
    public static class FieldIndexDescriptor {
        private int index = -1;
        private String name;
        
        private static FieldIndexDescriptor parse(String descriptor) {
            FieldIndexDescriptor result = new FieldIndexDescriptor();
            
            if (descriptor == null) {
                return result;
            }
            
            String[] parts = descriptor.split("#");

            if (parts.length > 0) {
                result.name = parts[0];
                if (parts.length > 1) {
                    try {
                        result.index = Integer.parseInt(parts[1]);
                    } catch (Exception e) {
                    }
                }
            }
            return result;
        }

        private FieldIndexDescriptor() {
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
