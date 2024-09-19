package net.kiar.collectorr.metrics.builder;

import java.util.List;
import java.util.Optional;
import net.kiar.collectorr.config.MappingsConfigLoader;
import net.kiar.collectorr.config.model.TopicConfig;
import net.kiar.collectorr.payloads.DataProvider;
import net.kiar.collectorr.connector.mqtt.mapping.TopicStructure;
import net.kiar.collectorr.metrics.BuildInLabels;
import net.kiar.collectorr.metrics.FieldSourceType;
import net.kiar.collectorr.metrics.MetricDefinition;
import net.kiar.collectorr.metrics.MetricType;
import net.kiar.collectorr.payloads.PayloadResolver;
import net.kiar.collectorr.payloads.PayloadResolverFactory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;

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
        
        assertEquals("abc", testMetric.getFieldOfValue().getFieldName().getFullName());
        assertTrue(testMetric.hasLabels());
    }
    
    @Test
    public void testAutoMetricWithBooleanLabel() {
        String topicPath = "topic/to/test";
        TopicConfig topicToTest = new TopicConfig();
        
        String jsonPayload = """ 
                             { "temp" : 12, "outdoor" : true, "place" : "roof" } 
        """;
        
        PayloadResolver payloadResolver = PayloadResolverFactory.build(jsonPayload);
        
        List<MetricDefinition> metrics = TopicMetricsFactory.INSTANCE.buildMetric(payloadResolver, topicPath, topicToTest, TopicStructure.build(topicPath));
        
        assertEquals(1, metrics.size());
        assertFalse( metrics.get(0).getName().getProcessed().isEmpty());
        assertTrue( metrics.get(0).hasLabels());
        assertEquals("temp", metrics.get(0).getFieldOfValue().getFieldName().getFullName());
        assertFalse(metrics.get(0).getFieldOfValue().hasName());
    }
    
    
    private final String configContentForTest2 = """
topics:
- topic: topic/to/test
  metrics:
  - name: testMetric
    valueField: outdoor
""";
    
    @Test
    // Labels sind nicht in der config definiert - 
    public void testMetricFromBoolean() {
        String topicPath = "topic/to/test";
        MappingsConfigLoader conf = MappingsConfigLoader.readContent( configContentForTest2);
        
        TopicConfig topicToTest = conf.findTopic(topicPath).get();
//        TopicConfig topicToTest = new TopicConfig();
        
        String jsonPayload = """ 
                             { "temp" : 12, "outdoor" : true, "place" : "roof" } 
        """;
        
        PayloadResolver payloadResolver = PayloadResolverFactory.build(jsonPayload);
        
        List<MetricDefinition> metrics = TopicMetricsFactory.INSTANCE.buildMetric(payloadResolver, topicPath, topicToTest, TopicStructure.build(topicPath));
        
        assertEquals(1, metrics.size());
        assertNotNull( metrics.get(0).getName());
        assertEquals(1, metrics.get(0).getLabels().size());
        assertTrue(metrics.get(0).findLabel("place").isPresent());
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
        
        assertTrue( def.getLabels().stream().anyMatch(label -> label.getFieldName().getFullName().equals("label-1")), "expected Label 'label-1' not found");
        assertTrue( def.getLabels().stream().anyMatch(label -> label.getFieldName().getFullName().equals("label-2")), "expected Label 'label-2' not found");
        assertTrue(def.getLabels().stream().allMatch(label -> label.getType() == FieldSourceType.TOPIC), "expected LabelType 'TOPIC' not found");
        
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
        
        assertTrue(def.getLabels().stream().anyMatch(l -> l.getType() == FieldSourceType.TOPIC), "expected LabelType 'TOPIC' not found");
        assertTrue(def.getLabels().stream().anyMatch(l -> l.getType() == FieldSourceType.PAYLOAD), "expected LabelType 'PAYLOAD' not found");
        
    }
    
    private final String configWithLabelFieldAsValue = """
topics:
- topic: topic/to/test
  metrics:
  - name: testMetric
    valueField: strField
""";
    @Test
    public void testValueFieldIsNotALabel() {
        String topicPath = "topic/to/test";
        MappingsConfigLoader conf = MappingsConfigLoader.readContent( configWithLabelFieldAsValue);
        
        TopicConfig topicToTest = conf.findTopic(topicPath).get();
        
        String jsonPayload = """ 
                             { "strField" : "12", "outdoor" : "yes", "place" : "roof" } 
        """;
        
        PayloadResolver payloadResolver = PayloadResolverFactory.build(jsonPayload);
        
        
        List<MetricDefinition> metrics = TopicMetricsFactory.INSTANCE.buildMetric(payloadResolver, topicPath, topicToTest, TopicStructure.build("topic/{label-1}"));
        
        assertNotNull(metrics);
        assertEquals(1, metrics.size());
        
        MetricDefinition def = metrics.get(0);
        assertEquals(3, def.getLabels().size());
        
        assertTrue( def.getLabels().stream().anyMatch( label -> label.getFieldName().getFullName().equalsIgnoreCase("outdoor")) );
        assertTrue( def.getLabels().stream().anyMatch( label -> label.getFieldName().getFullName().equalsIgnoreCase("place")) );
        assertTrue( def.getLabels().stream().anyMatch( label -> label.getFieldName().getFullName().equalsIgnoreCase("label-1")) );
    }
    
    private final String configWithCounter = """
topics:
- topic: topic/to/test
  metrics:
  - name: Metric1
    type:   CounteR
""";

    @Test
    public void testCounterMetric() {
        String topicPath = "topic/to/test";
        MappingsConfigLoader conf = MappingsConfigLoader.readContent( configWithCounter);
        
        TopicConfig topicToTest = conf.findTopic(topicPath).get();
        
        List<MetricDefinition> metrics = TopicMetricsFactory.INSTANCE.buildMetric(null, topicPath, topicToTest, TopicStructure.build(topicPath));
        assertFalse(metrics.isEmpty());
        
        MetricDefinition metric1 = find("Metric1", metrics).get();
        assertEquals(MetricType.COUNTER, metric1.getMetricType());
    }
    
    @Test
    public void testMetricNameContainsNoDots() {
        TopicMetricsFactory.MetricNameBuilder nameBuilder = new TopicMetricsFactory.MetricNameBuilder("topic/to/test");
        
        DataProvider dataProvider = Mockito.mock(DataProvider.class);
        Mockito.when(dataProvider.resolve( BuildInLabels.VALUE_FIELD_NAME, "")).thenReturn("FIELD-NAME");
        
        
        assertEquals("topic_to_test_FIELD-NAME", nameBuilder.getDefaultName().getProcessed(dataProvider));
    }
    
    
    private Optional<MetricDefinition> find(String metricName, List<MetricDefinition> list) {
        if (list == null || list.isEmpty()) {
            return Optional.empty();
        }
        return list.stream().filter(item -> item.getName().getRaw().equals(metricName)).findAny();
    }
}
