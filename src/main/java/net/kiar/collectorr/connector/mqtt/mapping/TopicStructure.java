/*
 * Copyright 2023 ranSprd.
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
package net.kiar.collectorr.connector.mqtt.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.kiar.collectorr.metrics.FieldDescription;

/**
 *
 * @author ranSprd
 */
public class TopicStructure {
    
    public static final String SEGMENT_WILDCARD = "[^/]+";
    public static final String TOPIC_WILDCARD = ".*";
    
    private String topicPatternStr;
    
    private final List<TopicSegment> segments = new ArrayList<>();
    
    public static TopicStructure build(String topicPath) {
        
        TopicStructure result = new TopicStructure();
        if (topicPath == null || topicPath.isBlank()) {
            return result;
        }
        
        int indexOfWildCard = topicPath.indexOf('#');
        
        final String path = indexOfWildCard > -1
                ? topicPath.substring(0, indexOfWildCard+1).trim()
                : topicPath.trim();
        
        String[] parts = path.split("/");
        if (parts == null) {
            // split was faulty
            return result;
        }
        
        StringBuilder pattern = new StringBuilder();
        for(int t = 0; t < parts.length; t++) {
            String part = parts[t].trim();
            TopicSegment segment = new TopicSegment(part, t);
            if (!result.segments.isEmpty()) {
                pattern.append('/');
            }
            pattern.append(segment.getSegmentPattern());
            result.segments.add( segment);
        }
        if (path.endsWith("/")) {
            pattern.append('/');
        }
        result.topicPatternStr = pattern.toString();
        
        
        // prepare the FieldDescriptions
        
        return result;
    }
    
    
    /**
     * Each topic, e.g. 'foo/bar/x' consists of several segements , separates by a single /.
     * This method returns the nuber of detected segements.
     * 
     * @return 
     */
    public int getNumberOfSegments() {
        return segments.size();
    }

    public TopicSegment getSegement(int index) {
        return segments.get(index);
    }

    public String getTopicPatternStr() {
        return topicPatternStr;
    }

    public List<TopicSegment> getSegments() {
        return segments;
    }
    

    /**
     * Generate a map with all fields found in topic pattern
     * @return 
     */
    public Map<String, FieldDescription> getFieldDescriptions() {
        return segments.stream()
                .filter(segment -> segment.hasField())
                .map(segment -> FieldDescription.topicField(segment.getSegementIndex(), segment.getFieldName()))
                .collect( Collectors.toUnmodifiableMap(keyMapper -> keyMapper.getFieldName(), value -> value));
    }


    public static class TopicSegment {
        
        private final String raw;
        private final int segementIndex;
        
        private String segmentPattern = "";
        private String fieldName = "";

        public TopicSegment(String raw, int index) {
            this.raw = raw;
            this.segementIndex = index;
            
            boolean inPrefix = true;
            boolean parserEnd = false;
            char last =' ';
            StringBuilder fieldNameStack = null;
            for(int t = 0, len = raw.length(); t < len; t++) {
                char c = raw.charAt(t);
                if (c == '#') {
                   segmentPattern = segmentPattern + TOPIC_WILDCARD; 
                   return;
                } else if (inPrefix) {
                    switch (c) {
                        case '{':
                            inPrefix = false;
                            if (last != '+') {
                                segmentPattern = segmentPattern + SEGMENT_WILDCARD;
                            } 
                            fieldNameStack = new StringBuilder();
                            break;
                        case '+':
                            segmentPattern = segmentPattern + SEGMENT_WILDCARD;
                            break;
                        default:
                            segmentPattern = segmentPattern + c;
                            break;
                    }
                } else if (!parserEnd) {
                    if (c == '}') {
                        parserEnd = true;
                        if (fieldNameStack != null) {
                            fieldName = fieldNameStack.toString();
                        }
                    } else {
                        fieldNameStack.append(c);
                    }
                }
                last = c;
            }
        }

        public String getSegmentPattern() {
            return segmentPattern;
        }
        
        public String getFieldName() {
            return fieldName;
        }

        public int getSegementIndex() {
            return segementIndex;
        }
        
        public boolean hasField() {
            return fieldName != null && !fieldName.isBlank();
        }
        
    }
    
    
}
