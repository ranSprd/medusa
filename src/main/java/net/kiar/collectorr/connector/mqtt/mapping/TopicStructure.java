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
                .map(segment -> FieldDescription.topicField(segment.getSegmentIndex(), segment.getFieldName()))
                .collect( Collectors.toUnmodifiableMap(keyMapper -> keyMapper.getFieldName().getFullName(), value -> value));
    }


    public static class TopicSegment {
        
        private final String raw;
        private final int segementIndex;
        
        private String segmentPattern = "";
        private String fieldName = "";
        
        private final List<SegmentRestriction> allowedSegmentNames = new ArrayList<>();

        public TopicSegment(String raw, int index) {
            this.raw = raw;
            this.segementIndex = index;
            
            boolean beforeBrace = true;
            boolean inBrace = false;
            boolean afterBrace = false;
            char last =' ';
            StringBuilder fieldNameStack = null;
            
            SegmentPatternParser patternParser = new SegmentPatternParser(allowedSegmentNames);
            
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
        }

        public String getSegmentPattern() {
            return segmentPattern;
        }
        
        public String getFieldName() {
            return fieldName;
        }

        public int getSegmentIndex() {
            return segementIndex;
        }
        
        public boolean hasField() {
            return fieldName != null && !fieldName.isBlank();
        }
        
        public boolean isSegmentNameAllowed(String name) {
            if (allowedSegmentNames == null || allowedSegmentNames.isEmpty()) {
                return true;
            }
            return allowedSegmentNames.stream()
                    .anyMatch(item -> item.getPattern().equals(name));
        }
        
        public String getOverwrittenSegmentName(String origin) {
            return allowedSegmentNames.stream()
                    .filter(entry -> entry.hasOverwrittenName())
                    .filter(entry -> entry.hasPattern())
                    .filter(entry -> entry.getPattern().equals(origin))
                    .findAny()
                    .map(selected -> selected.getOverwrittenName().toString())
                    .orElse(origin);
        }
        
    }
    
    
    private static class SegmentPatternParser {

        private boolean inSquareBraket = false;
        private boolean afterEqualSign = false;
        private final List<SegmentRestriction> data;
        
        private StringBuilder patternBuffer = new StringBuilder();
        private StringBuilder overwrittenNameBuffer = new StringBuilder();
        
        public SegmentPatternParser(java.util.List<SegmentRestriction> data) {
            this.data = data;
        }

        public List<SegmentRestriction> getData() {
            return data;
        }

        public void consume(char c) {
            if (inSquareBraket) {
                if (c == ']') {
                    inSquareBraket = false;
                    nextPattern();
                } else if (c == ',') {
                    nextPattern();
                    patternBuffer = new StringBuilder();
                } else if (afterEqualSign) {
                    overwrittenNameBuffer.append(c);
                } else if (c == '=') {
                    afterEqualSign = true;
                    overwrittenNameBuffer = new StringBuilder();
                } else if (c != ' ') {
                    patternBuffer.append(c);
                }
            } else if (c == '[') {
                inSquareBraket = true;
                patternBuffer = new StringBuilder();
            }
        }
        
        private void nextPattern() {
            if (patternBuffer != null && !patternBuffer.isEmpty()) {
                if (overwrittenNameBuffer != null && !overwrittenNameBuffer.isEmpty()) {
                    data.add( new SegmentRestriction(patternBuffer.toString(), overwrittenNameBuffer.toString().trim()));
                } else {
                    data.add( new SegmentRestriction(patternBuffer.toString()));
                }
            } 
            afterEqualSign = false;
            overwrittenNameBuffer = null;
        }
    }
    
    private static class SegmentRestriction {
        private final String pattern;
        private final String overwrittenName;

        public SegmentRestriction(String pattern) {
            this(pattern, "");
        }
        
        public SegmentRestriction(String pattern, String overwrittenName) {
            this.pattern = pattern;
            this.overwrittenName = overwrittenName;
        }
        
        public String getPattern() {
            return pattern;
        }

        public String getOverwrittenName() {
            return overwrittenName;
        }

        public boolean hasPattern() {
            return !pattern.isEmpty();
        }
        
        public boolean hasOverwrittenName() {
            return !overwrittenName.isEmpty();
        }
    }
    
}
