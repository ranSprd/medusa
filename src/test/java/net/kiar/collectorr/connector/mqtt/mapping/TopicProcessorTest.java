package net.kiar.collectorr.connector.mqtt.mapping;

import java.util.List;
import net.kiar.collectorr.config.MappingsConfigLoader;
import net.kiar.collectorr.config.model.TopicConfig;
import net.kiar.collectorr.metrics.FieldDescription;
import net.kiar.collectorr.metrics.MetricDefinition;
import net.kiar.collectorr.metrics.PrometheusGauge;
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
        
        List<PrometheusGauge> result = p.consumeMessage("{ \"temp\" : 12.0, \"unit\" : \"celcius\" }", "sensors/room1/a5226");
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        PrometheusGauge first = result.get(0);
        
        MetricDefinition def = first.getMetricDefinition();
        assertNotNull(def);
        assertNotNull(def.getLabels());
        assertEquals(1, def.getLabels().size());

        FieldDescription labelDef = def.getLabels().get(0);
        assertNotNull(labelDef);
        assertEquals("unit", labelDef.getFieldName());
        
        assertEquals(12.0, first.getValue());
        assertTrue(first.getMillisTimestamp() >= startTime);
        
        assertTrue(first.hasLabels());
        assertEquals(def.getLabels().size(), first.getNumberOfLabels());
    }
    
    
    @Test
    public void testTwoMetricsExpected() {
        TopicConfig topicConfig = new TopicConfig();
        TopicProcessor p = new TopicProcessor(topicConfig, TopicStructure.build("#"));
        
        List<PrometheusGauge> result = p.consumeMessage(" { \"freeheap\" : 45488, \"cpuSpeed\" : 80}", "/home/heizung/ESP8266-1074379");
        
        assertEquals(2, result.size());
        
        for(PrometheusGauge g : result) {
            if (g.getMetricDefinition().getName().getProcessed().endsWith("cpuSpeed")) {
                assertEquals(80, g.getValue(), 0.01);
            } else if (g.getMetricDefinition().getName().getProcessed().endsWith("freeheap")) {
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
        TopicCache topicCache = TopicCache.buildPattern(topicToTest).get();
        
        List<PrometheusGauge> result = topicCache.getTopicProcessor().consumeMessage(" { \"freeheap\" : 45488, \"foo\" : \"label-3-content\" }", topicPath);
        
        assertEquals(1, result.size());
        
        assertEquals(3, result.get(0).getNumberOfLabels(), "3 labels for metric are expected");
        PrometheusGauge gauge = result.get(0);
        assertEquals(3, gauge.getNumberOfLabels());
        assertEquals("heizung", gauge.getLabelValue("label-1"));
        assertEquals("ESP8266-1074379", gauge.getLabelValue("label-2"));
        assertEquals("label-3-content", gauge.getLabelValue("label-3"));
    }
    
}
