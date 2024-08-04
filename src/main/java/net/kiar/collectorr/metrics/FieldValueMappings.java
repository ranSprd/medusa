/*
 * Copyright 2024 ranSprd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kiar.collectorr.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FieldValueMappins allow to overwrite or set (new) labels with a pre-defined content 
 * based on a value of a source field. This means, if you have a field 'id' so you 
 * can introduce a new label 'name' which can contain a more humand friendly
 * description based on the 'id' field.
 * 
 * @author ranSprd
 * @todo find another name
 */
public class FieldValueMappings {

    private final Map<String, List<FieldMappingContent>> targetFieldValues = new HashMap<>(); 
    
    
    /**
     * Resolves mappings (means targetField/value pairs) based on the given value of a source field.
     * This enables something like: if sourceField has value "A" (sourceValue) then overwrite the content 
     * of targetField with "B" (return value)
     * 
     * @param sourceValue content of the source field
     * 
     * @return
     * 
     */    
    public List<FieldMappingContent> findMappingsForValue(String sourceValue) {
        List<FieldMappingContent> list = targetFieldValues.get(sourceValue);
        if (list == null) {
            return List.of();
        }
        return list;
    }
    
    /**
     * register the mapping (tagetfield and its value) for a given value of source field.
     * 
     * @param sourceValue
     * @param targetMapping as a map of targetFieldName and fieldValue pairs
     */
    public void registerMappings(String sourceValue, Map<String, String> targetMapping) {
        List<FieldMappingContent> list = targetFieldValues.get(sourceValue);
        if (list == null) {
            list = new ArrayList<>();
            targetFieldValues.put(sourceValue, list);
        }
        if (targetMapping != null) {
            for(Map.Entry<String, String> entry : targetMapping.entrySet()) {
                list.add( new FieldMappingContent(entry.getKey(), entry.getValue()));
            }
        }
    }
    
    
    public static record FieldMappingContent(String targetFieldName, String targetValue) {};


}
