package net.kiar.collectorr.metrics.builder;

import java.util.HashMap;
import java.util.Map;
import net.kiar.collectorr.config.model.TopicConfigPattern;
import net.kiar.collectorr.metrics.FieldDescription;

/**
 *
 * @author ranSprd
 */
public class TopicPatternResolver {

    public static Map<String, FieldDescription> extractFieldsFromPattern(TopicConfigPattern pattern) {
        if (pattern == null || pattern.getPath() == null || pattern.getPath().isBlank()) {
            return Map.of();
        }
        
        String[] parts = pattern.getPath().split("/");
        if (parts == null) {
            // split was faulty
            return Map.of();
        }
        
        Map<String, FieldDescription> result = new HashMap<>();
        for(int t = 0; t < parts.length; t++) {
            String part = parts[t].trim();
            if (part.startsWith("{") && part.endsWith("}")) {
                int nameLen = part.length();
                if (nameLen > 2) {
                    String name = part.substring(1, nameLen-1);
                    result.put(name, FieldDescription.topicField(t, name));
                }
            }
        }
        return result;
    }
}
