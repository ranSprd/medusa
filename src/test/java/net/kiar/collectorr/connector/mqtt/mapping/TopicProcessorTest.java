package net.kiar.collectorr.connector.mqtt.mapping;

import java.util.List;
import net.kiar.collectorr.config.MappingsConfigLoader;
import net.kiar.collectorr.config.model.TopicConfig;
import net.kiar.collectorr.metrics.FieldDescription;
import net.kiar.collectorr.metrics.MetricDefinition;
import net.kiar.collectorr.metrics.PrometheusCounterGauge;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author ranSprd
 */
public class TopicProcessorTest {
    
    @Test
    public void testConstructDefaultMetricFromInput() {
        
        long startTime = System.currentTimeMillis();
        
        TopicConfig topicConfig = new TopicConfig();
        TopicProcessor p = new TopicProcessor(topicConfig, TopicStructure.build("#"));
        
        List<PrometheusCounterGauge> result = p.consumeMessage("{ \"temp\" : 12.0, \"unit\" : \"celcius\" }", "sensors/room1/a5226");
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        PrometheusCounterGauge gauge = result.get(0);
        
        MetricDefinition def = gauge.getMetricDefinition();
        assertNotNull(def);
        assertNotNull(def.getLabels());
        assertEquals(1, def.getLabels().size());
        assertEquals("temp", def.getFieldOfValue().getFieldName().getFullName());

        FieldDescription labelFieldDesc = def.getLabels().get(0);
        assertNotNull(labelFieldDesc);
        assertEquals("unit", labelFieldDesc.getFieldName().getFullName());
        
        assertEquals(12.0, gauge.getValue());
        assertTrue(gauge.getMillisTimestamp() >= startTime);
        
        assertTrue(gauge.hasLabels());
        assertEquals(def.getLabels().size(), gauge.getNumberOfLabels());
    }
    
    
    @Test
    public void testTwoMetricsExpected() {
        TopicConfig topicConfig = new TopicConfig();
        TopicProcessor p = new TopicProcessor(topicConfig, TopicStructure.build("#"));
        
        List<PrometheusCounterGauge> result = p.consumeMessage(" { \"freeheap\" : 45488, \"cpuSpeed\" : 80}", "/home/heizung/ESP8266-1074379");
        
        assertEquals(2, result.size());
        
        for(PrometheusCounterGauge g : result) {
            if (g.getName().endsWith("cpuSpeed")) {
                assertEquals(80, g.getValue(), 0.01);
            } else if (g.getName().endsWith("freeheap")) {
                assertEquals(45488, g.getValue(), 0.01);
            } else {
                assertTrue(false, "unexpected case");
            }
        }
    }
    
 
    private final String configContentForTest3 = """
topics:
- topic: topic/{label-1}/{label-2}
  metrics:
  - name: testMetric
    valueField: freeheap
    labels: [label-1, label-2, foo|label-3]
""";
    
    @Test
    public void testMetricWithLabels() {
        String topicPath = "topic/heizung/ESP8266-1074379";
        MappingsConfigLoader conf = MappingsConfigLoader.readContent( configContentForTest3);
        
        TopicConfig topicToTest = conf.getTopicsToObserve().get(0);
        TopicCache topicCache = TopicCache.buildTopicPattern(topicToTest).get();
        
        List<PrometheusCounterGauge> result = topicCache.getTopicProcessor().consumeMessage(" { \"freeheap\" : 45488, \"foo\" : \"label-3-content\" }", topicPath);
        
        assertEquals(1, result.size());
        
        assertEquals(3, result.get(0).getNumberOfLabels(), "3 labels for metric are expected");
        PrometheusCounterGauge gauge = result.get(0);
        assertEquals(3, gauge.getNumberOfLabels());
        assertEquals("heizung", gauge.getLabelValue("label-1"));
        assertEquals("ESP8266-1074379", gauge.getLabelValue("label-2"));
        assertEquals("label-3-content", gauge.getLabelValue("label-3"));
    }
    
    
    private final String configContentForTest4 = """
topics:
- topic: topic/wheater
  metrics:
  - valueField: val#0
    labels: [val|unit#1]
                                                 """;
    @Test
    public void testSplittedFields() {
        String topicPath = "topic/wheater";
        MappingsConfigLoader conf = MappingsConfigLoader.readContent( configContentForTest4);
        
        TopicConfig topicToTest = conf.getTopicsToObserve().get(0);
        TopicCache topicCache = TopicCache.buildTopicPattern(topicToTest).get();
        
        List<PrometheusCounterGauge> result = topicCache.getTopicProcessor().consumeMessage("{\"id\": \"0x13\",\"val\": \"350.1 mm\",\"battery\": \"0\"}", topicPath);
        
        assertEquals(1, result.size());
        
        PrometheusCounterGauge gauge = result.get(0);
        assertEquals(1, gauge.getNumberOfLabels(), "one label (unit) for metric is expected");
        assertEquals("mm", gauge.getLabelValue("unit"));
        assertEquals(350.1, gauge.getValue());
    }
    
    
    
    private final String configContentForTest5 = """
topics:
- topic: topic/test
  metrics:
  - valueField: val
                                                 """;
    @Test
    public void testRobustValueParsing() {
        String topicPath = "topic/test";
        MappingsConfigLoader conf = MappingsConfigLoader.readContent( configContentForTest6);
        
        TopicConfig topicToTest = conf.getTopicsToObserve().get(0);
        TopicCache topicCache = TopicCache.buildTopicPattern(topicToTest).get();
        
        List<PrometheusCounterGauge> result = topicCache.getTopicProcessor().consumeMessage("{\"val\": \"71%\"}", topicPath);
        
        assertEquals(1, result.size());
        
        PrometheusCounterGauge gauge = result.get(0);

        assertEquals(71, gauge.getValue());
    }
    
    
    private final String configContentForTest6 = """
topics:
- topic: topic/test
  metrics:
  - valueField: val
    labels: [label-1=fixed, label-2, label-3=overwritten]
                                                 """;
    @Test
    public void testFixedLabelContent() {
        String topicPath = "topic/test";
        MappingsConfigLoader conf = MappingsConfigLoader.readContent( configContentForTest6);
        
        TopicConfig topicToTest = conf.getTopicsToObserve().get(0);
        TopicCache topicCache = TopicCache.buildTopicPattern(topicToTest).get();
        
        List<PrometheusCounterGauge> result = topicCache.getTopicProcessor().consumeMessage("{\"val\": \"71%\", \"label-2\": \"payload\", \"label-3\": \"payload\"}", topicPath);
        
        assertEquals(1, result.size());
        
        PrometheusCounterGauge gauge = result.get(0);
        assertEquals(3, gauge.getNumberOfLabels());
        assertEquals("fixed", gauge.getLabelValue("label-1"));
        assertEquals("payload", gauge.getLabelValue("label-2"));
        assertEquals("overwritten", gauge.getLabelValue("label-3"));

        assertEquals(71, gauge.getValue());
    }
    
}
