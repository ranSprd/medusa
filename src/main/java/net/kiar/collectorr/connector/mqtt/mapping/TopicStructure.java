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
        
        // ignore everthing behind #
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

    public TopicSegment getSegment(int index) {
        if (index < segments.size()) {
            return segments.get(index);
        }
        return null;
    }

    /**
     * The topic with wildcards (for pattern matching) but without field definitions
     * @return 
     */
    public String getTopicPatternStr() {
        return topicPatternStr;
    }

    public List<TopicSegment> getSegments() {
        return segments;
    }
    
    /**
     * 
     * @param segmentIndex
     * @param createEmptyFieldNames if true and there is no fieldName (null or blank)
     * the a fieldname in form of '#segment-Index' is created
     * @return 
     */
    public String getFieldNameOfSegment(int segmentIndex, boolean createEmptyFieldNames) {
        String result = null;
        if (segmentIndex < segments.size()) {
            result = segments.get(segmentIndex).getFieldName();
        } 
        
        if ((result == null || result.isBlank()) && createEmptyFieldNames) {
            result = "#" +segmentIndex;
        }
        
        return result;
    }

    /**
     * Generate a map with all fields found in topic pattern
     * @return 
     */
    public Map<String, FieldDescription> getFieldDescriptions() {
        return segments.stream()
                .filter(segment -> segment.hasField())
                .map(segment -> FieldDescription.topicField(segment.getSegementIndex(), segment.getFieldName()))
                .collect( Collectors.toUnmodifiableMap(keyMapper -> keyMapper.getFieldName().getFullName(), value -> value));
    }


    public static class TopicSegment {
        
        private final String raw;
        private final int segementIndex;
        
        private String segmentPattern = "";
        private String fieldName = "";
        
        private List<String> allowedContent = null;

        public TopicSegment(String raw, int index) {
            this.raw = raw;
            this.segementIndex = index;
            
            boolean beforeBrace = true;
            boolean inBrace = false;
            boolean afterBrace = false;
            char last =' ';
            StringBuilder fieldNameStack = null;
            
            PatternParser patternParser = new PatternParser();
            
            for(int t = 0, len = raw.length(); t < len; t++) {
                char c = raw.charAt(t);
                if (c == '#') {
                   segmentPattern = segmentPattern + TOPIC_WILDCARD; 
                   return;
                } else if (beforeBrace) {
                    switch (c) {
                        case '{' -> {
                            beforeBrace = false;
                            inBrace = true;
                            if (last != '+') {
                                segmentPattern = segmentPattern + SEGMENT_WILDCARD;
                            } 
                            fieldNameStack = new StringBuilder();
                        }
                        case '+' -> segmentPattern = segmentPattern + SEGMENT_WILDCARD;
                        default -> segmentPattern = segmentPattern + c;
                    }
                } else if (inBrace) {
                    if (c == '}') {
                        afterBrace = true;
                        inBrace= false;
                        if (fieldNameStack != null) {
                            fieldName = fieldNameStack.toString();
                        }
                    } else {
                        fieldNameStack.append(c);
                    }
                } else {
                    patternParser.consume(c);
                }
                last = c;
            }
            
            allowedContent = patternParser.getData();
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
        
        public boolean isSegmentNameAllowed(String name) {
            if (allowedContent == null || allowedContent.isEmpty()) {
                return true;
            }
            return allowedContent.stream()
                    .anyMatch(item -> item.equals(name));
        }
        
    }
    
    
    private static class PatternParser {
        private boolean inSquareBraket = false;
        private StringBuilder pattern = null;
        private final List<String> data = new ArrayList<>();

        public List<String> getData() {
            return data;
        }
        
        public void consume(char c) {
            if (inSquareBraket) {
                if (c == ']') {
                    inSquareBraket = false;
                    nextPattern();
                } else if (c == ',') {
                    nextPattern();
                    pattern = new StringBuilder();
                } else if (c != ' ') {
                    pattern.append(c);
                }
            } else if (c == '[') {
                inSquareBraket = true;
                pattern = new StringBuilder();
            }
        }
        
        private void nextPattern() {
            if (!pattern.isEmpty()) {
                data.add(pattern.toString().trim());
            } 
        }
    }
    
}
