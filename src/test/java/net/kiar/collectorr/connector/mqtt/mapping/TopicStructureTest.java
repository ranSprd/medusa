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

import java.util.Map;
import java.util.regex.Pattern;
import net.kiar.collectorr.metrics.FieldDescription;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author ranSprd
 */
public class TopicStructureTest {
    
    private static final String PLUS_WILDCARD = "\\+";
    private static final String SHARP_WILDCARD = "#";

    private static final String PLUS_WILDCARD_REPLACE = "[^/]\\+";
    private static final String SHARP_WILDCARD_REPLACE = ".*";
    
    @Test
    public void testInvalidInput() {
        TopicStructure.build(null);
        TopicStructure.build("");
    }
    
    public void testHappyCase() {
        TopicStructure result = TopicStructure.build("first/sec{Field1}/other/{field2}");
        assertNotNull(result);
        assertEquals(4, result.getNumberOfSegments());
        
        TopicStructure.TopicSegment segment1 = result.getSegment(0);
        assertNotNull(segment1);
        assertEquals("first", segment1.getSegmentPattern());

        TopicStructure.TopicSegment segment2 = result.getSegment(1);
        assertNotNull(segment2);
        assertEquals("sec[^/]\\+" , segment2.getSegmentPattern());
        assertEquals("Field1", segment2.getFieldName());

        TopicStructure.TopicSegment segment3 = result.getSegment(2);
        assertNotNull(segment3);
        assertEquals("other", segment3.getSegmentPattern());

        TopicStructure.TopicSegment segment4 = result.getSegment(3);
        assertNotNull(segment4);
        assertEquals("[^/]\\+", segment4.getSegmentPattern());
        assertEquals("field2", segment4.getFieldName());
        
    }
    
    @Test
    public void testPatternWithEndingSlash() {
        TopicStructure result = TopicStructure.build("first/second/");
        assertEquals(2, result.getNumberOfSegments());
    }
    
    @Test
    public void testWildcard() {
        TopicStructure result = TopicStructure.build("#");
        assertEquals(1, result.getNumberOfSegments());
        assertEquals(".*", result.getTopicPatternStr());
    }
    
    @Test
    public void testSegmentNumbers() {
        assertEquals(2, TopicStructure.build("first/second/").getNumberOfSegments());
        
    }
    @Test
    public void testTopicPattern() {
        assertEquals("first/second/", TopicStructure.build("first/second/").getTopicPatternStr());
        assertEquals("/first/second/", TopicStructure.build("/first/second/").getTopicPatternStr());
        assertEquals("/first/second", TopicStructure.build("/first/second").getTopicPatternStr());
        assertEquals("first/second/.*", TopicStructure.build("first/second/#").getTopicPatternStr());
        assertEquals("first.*", TopicStructure.build("first#").getTopicPatternStr());
        assertEquals("first/.*", TopicStructure.build("first/#/everything_is_ignored").getTopicPatternStr());
        assertEquals("first/[^/]+/3rd", TopicStructure.build("first/{second}/3rd").getTopicPatternStr());
        assertEquals("first/prefix-[^/]+/3rd", TopicStructure.build("first/prefix-{second}/3rd").getTopicPatternStr());
        assertEquals("first/[^/]+/2nd", TopicStructure.build("first/+/2nd").getTopicPatternStr());
        assertEquals("first/[^/]+/2nd", TopicStructure.build("first/{}/2nd").getTopicPatternStr());
    }
    @Test
    public void testPatternMatching() {
        String topicPattern = "first/f+/2nd";
        String topic = "first/foo/2nd";
        
        
        String wildcardedTopic = topicPattern;
        String topicReplaced =
                wildcardedTopic.replaceAll(PLUS_WILDCARD, PLUS_WILDCARD_REPLACE).replaceAll(SHARP_WILDCARD, SHARP_WILDCARD_REPLACE);        
        Pattern patternLegacy = Pattern.compile(topicReplaced);
        assertTrue( patternLegacy.matcher(topic).matches(), "straight forward code is broken");

        
        String str = TopicStructure.build(topicPattern).getTopicPatternStr();
        assertEquals(topicReplaced, str);
        Pattern pattern = Pattern.compile(str);
        
        boolean result = pattern.matcher(topic).matches();
        assertTrue(result);
    }
    
    @Test
    public void testFieldParsing() {
        assertTrue(TopicStructure.build("    ").getFieldDescriptions().isEmpty());
        assertTrue(TopicStructure.build(null).getFieldDescriptions().isEmpty());
        assertTrue(TopicStructure.build("").getFieldDescriptions().isEmpty());
        assertTrue(TopicStructure.build("{}").getFieldDescriptions().isEmpty());
        assertTrue(TopicStructure.build("/a/b{unclosed-field/c").getFieldDescriptions().isEmpty());
        assertTrue(TopicStructure.build("/a/b-invalid-field}/c").getFieldDescriptions().isEmpty());
        
        assertEquals(1, TopicStructure.build("/foo/{field1}/{field2/bar").getFieldDescriptions().size());
        assertEquals(1, TopicStructure.build("/foo/{field1}/bar").getFieldDescriptions().size());
        assertEquals(1, TopicStructure.build("{1}/").getFieldDescriptions().size());
        assertEquals(2, TopicStructure.build("{1}/{2}").getFieldDescriptions().size());
        
        Map<String, FieldDescription> fields = TopicStructure.build("{field-1}{ignored-2}").getFieldDescriptions();
        assertEquals(1, fields.size());
        assertTrue(fields.containsKey("field-1"));
    }
    
    @Test
    public void testFieldName() {
        TopicStructure struct = TopicStructure.build("a/b/c/{field-x}/");
        assertEquals( "#0", struct.getFieldNameOfSegment(0, true));
        assertEquals( "#7", struct.getFieldNameOfSegment(7, true));
        assertEquals( "field-x", struct.getFieldNameOfSegment(3, true));
    }
    
}
