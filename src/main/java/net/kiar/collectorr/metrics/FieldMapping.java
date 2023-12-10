package net.kiar.collectorr.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author ranSprd
 */
public class FieldMapping {
    
    private final String targetFieldName;
    
    private final Map<String, String> mapping = new HashMap<>();

    public FieldMapping(String targetFieldName) {
        this.targetFieldName = targetFieldName;
    }
    

    public String getTargetFieldName() {
        return targetFieldName;
    }

    public Map<String, String> getMapping() {
        return mapping;
    }

    public void setMapping(Map<String, String> mappingToAdd) {
        this.mapping.putAll(mappingToAdd);
    }
    
    public Optional<String> findMappingValueFor(String sourceValue) {
        if (sourceValue != null) {
            return Optional.ofNullable(mapping.get(sourceValue));
        }
        return Optional.empty();
    }
    
    
    
}
