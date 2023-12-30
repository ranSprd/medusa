package net.kiar.collectorr.metrics.builder;

import java.util.List;
import net.kiar.collectorr.config.MappingsConfigLoader;
import net.kiar.collectorr.config.model.TopicConfig;
import net.kiar.collectorr.connector.mqtt.mapping.TopicStructure;
import net.kiar.collectorr.metrics.FieldType;
import net.kiar.collectorr.metrics.MetricDefinition;
import net.kiar.collectorr.payloads.PayloadResolver;
import net.kiar.collectorr.payloads.json.JsonResolver;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author ranSprd
 */
public class TopicMetricsFactoryTest {
    
    private final String configContentForTest1 = """
topics:
- topic: topic/to/test
  metrics:
  - name: testMetric
    valueField: abc
    labels: [ort, device, field1]
  - name:
    valueField: x
""";
    
    @Test
    public void testMetricFromConfig() {
        String topicPath = "topic/to/test";
        MappingsConfigLoader conf = MappingsConfigLoader.readContent( configContentForTest1);
        
        TopicConfig testConfig = conf.findTopic(topicPath).get();
        List<MetricDefinition> metrics = TopicMetricsFactory.INSTANCE.buildMetric(null, topicPath, testConfig, TopicStructure.build("/home/{ort}/{device}/"));
        
        assertNotNull(metrics);
        assertEquals(2, metrics.size());
        
        MetricDefinition testMetric = metrics.stream()
                .filter(def -> "testMetric".equals(def.getName().getProcessed()))
                .findAny()
                .get();
        
        assertEquals("abc", testMetric.getFieldOfValue().getFieldName());
        assertTrue(testMetric.hasLabels());
    }
    
    @Test
    public void testAutoMetricWithBooleanLabel() {
        String topicPath = "topic/to/test";
        TopicConfig topicToTest = new TopicConfig();
        
        String jsonPayload = """ 
                             { "temp" : 12, "outdoor" : true, "place" : "roof" } 
        """;
        
        PayloadResolver payloadResolver = JsonResolver.consume(jsonPayload);
        
        List<MetricDefinition> metrics = TopicMetricsFactory.INSTANCE.buildMetric(payloadResolver, topicPath, topicToTest, TopicStructure.build(topicPath));
        
        assertEquals(1, metrics.size());
        assertTrue( metrics.get(0).getName().getProcessed().endsWith("temp"));
    }
    
    
//    private final String configContentForTest2 = """
//topics:
//- topic: topic/to/test
//  metrics:
//  - name: testMetric
//    valueField: outdoor
//""";
    
//    @Test
    // Labels sind nicht in der config definiert - 
    public void testMetricFromBoolean() {
        String topicPath = "topic/to/test";
        MappingsConfigLoader conf = MappingsConfigLoader.readContent( configContentForTest1);
        
        TopicConfig topicToTest = conf.findTopic(topicPath).get();
//        TopicConfig topicToTest = new TopicConfig();
        
        String jsonPayload = """ 
                             { "temp" : 12, "outdoor" : true, "place" : "roof" } 
        """;
        
        PayloadResolver payloadResolver = JsonResolver.consume(jsonPayload);
        
        List<MetricDefinition> metrics = TopicMetricsFactory.INSTANCE.buildMetric(payloadResolver, topicPath, topicToTest, TopicStructure.build(topicPath));
        
        assertEquals(1, metrics.size());
        assertTrue( metrics.get(0).getName().getProcessed().endsWith("temp"));
    }
    
    
    private final String configContentForTest3 = """
topics:
- topic: topic/to/test
  metrics:
  - name: testMetric
    valueField: outdoor
    labels: [label-1, label-2]
""";
    @Test
    public void testLabelsFromTopicConfiguration() {
        String topicPath = "topic/to/test";
        MappingsConfigLoader conf = MappingsConfigLoader.readContent( configContentForTest3);
        
        TopicConfig topicToTest = conf.findTopic(topicPath).get();
        
        List<MetricDefinition> metrics = TopicMetricsFactory.INSTANCE.buildMetric(null, topicPath, topicToTest, TopicStructure.build("topic/{label-1}/{label-2}"));
        
        assertNotNull(metrics);
        assertEquals(1, metrics.size());
        
        MetricDefinition def = metrics.get(0);
        assertEquals(2, def.getLabels().size());
        
        assertTrue( def.getLabels().stream().anyMatch(l -> l.getName().equals("label-1")), "expected Label 'label-1' not found");
        assertTrue( def.getLabels().stream().anyMatch(l -> l.getName().equals("label-2")), "expected Label 'label-2' not found");
        assertTrue( def.getLabels().stream().allMatch( l -> l.getType() == FieldType.TOPIC), "expected LabelType 'TOPIC' not found");
        
    }
    
    
    private final String configContentForTest4 = """
topics:
- topic: topic/to/test
  metrics:
  - name: testMetric
    valueField: outdoor
    labels: [label-1, label-2, label-3]
""";
    @Test
    public void testMultipleLabelsConfiguration() {
        String topicPath = "topic/to/test";
        MappingsConfigLoader conf = MappingsConfigLoader.readContent( configContentForTest4);
        
        TopicConfig topicToTest = conf.findTopic(topicPath).get();
        
        List<MetricDefinition> metrics = TopicMetricsFactory.INSTANCE.buildMetric(null, topicPath, topicToTest, TopicStructure.build("topic/{label-1}/{label-2}"));
        
        assertNotNull(metrics);
        assertEquals(1, metrics.size());
        
        MetricDefinition def = metrics.get(0);
        assertEquals(3, def.getLabels().size());
        
        assertTrue( def.getLabels().stream().anyMatch( l -> l.getType() == FieldType.TOPIC), "expected LabelType 'TOPIC' not found");
        assertTrue( def.getLabels().stream().anyMatch( l -> l.getType() == FieldType.PAYLOAD), "expected LabelType 'PAYLOAD' not found");
        
    }
}
