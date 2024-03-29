package net.kiar.collectorr.metrics;

import java.util.List;
import java.util.Optional;
import net.kiar.collectorr.metrics.FieldValueMappings.FieldMappingContent;
import net.kiar.collectorr.payloads.FieldName;
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
    
    private final FieldName fieldName;
    private int fieldIndex = -1;
    private String name;
    private FieldSourceType type = FieldSourceType.PAYLOAD;
    
    private FieldValueMappings fieldValueMappings;
    
    private String fixedContent = null;
    
    private boolean included = false;
    private boolean mapped = false;
    
    public static FieldDescription topicField(int index, String fieldName) {
        FieldDescription result = new FieldDescription(index, fieldName, null);
        result.setType(FieldSourceType.TOPIC);
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
                        String fieldName = pair[0].trim();
                        if (fieldName.length() > 0) {
                            result = new FieldDescription(fieldName);
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
                    result.name = namePart.getName();
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
        this(fieldName, null);
    }

    public FieldDescription(String fieldName, String name) {
        this(-1, fieldName, name);
    }

    public FieldDescription(int fieldIndex, String fieldName, String name) {
        this.fieldName = new FieldName(fieldName);
        this.name = name;
        this.fieldIndex = fieldIndex;
    }
    
    public boolean isInValid() {
        if (type == FieldSourceType.PAYLOAD) {
            return (fieldName == null || fieldName.getFullName().isBlank());
        }
        
        // case Type = TOPIC
        return true;
    }

    public FieldName getFieldName() {
        return fieldName;
    }

    public String getName() {
        return name;
    }
    
    public boolean hasName() {
        return (name != null && !name.isBlank());
    }

    public String getFixedContent() {
        return fixedContent;
    }

    public void setFixedContent(String fixedContent) {
        this.fixedContent = fixedContent;
    }
    
    /** true if the value/content of the field is configured and not read from any payload */
    public boolean hasFixedContent() {
        return fixedContent != null && !fixedContent.isBlank();
    }

    public boolean isIncluded() {
        return included;
    }

    public void setIncluded(boolean included) {
        this.included = included;
    }

    /** if true then this field is referenced in value mapping section of configuration 
     * @return 
     */
    public boolean isMapped() {
        return mapped;
    }

    public void setMapped(boolean mapped) {
        this.mapped = mapped;
    }
    
    public FieldSourceType getType() {
        return type;
    }

    public void setType(FieldSourceType type) {
        this.type = type;
    }

    public int getFieldIndex() {
        return fieldIndex;
    }

    public void setFieldIndex(int fieldIndex) {
        this.fieldIndex = fieldIndex;
    }

    public FieldValueMappings getFieldValueMappings() {
        if (this.fieldValueMappings == null) {
            fieldValueMappings = new FieldValueMappings();
        }
        return fieldValueMappings;
    }

    
    /**
     * Resolve all mappings (targetField and value) for the given content of the 
     * source field.
     * 
     * @param sourceValueContent
     * @return a list of fields and values
     */
    public List<FieldMappingContent> resolveMappingsForSource(String sourceValueContent) {
        if (sourceValueContent == null || fieldValueMappings == null) {
            return List.of();
        }
        return fieldValueMappings.findMappingsForValue(sourceValueContent);
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
