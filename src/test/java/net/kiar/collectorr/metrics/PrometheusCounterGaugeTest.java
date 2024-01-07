package net.kiar.collectorr.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author ranSprd
 */
public class PrometheusCounterGaugeTest {
    

    @Test
    public void testOnlyMetricDefinitionGiven() {
        MetricDefinition def = MetricDefinitionBuilder.metricForField("value")
                .name( new PlaceholderString("exampleMetric"))
                .label("label1")
                .get();
        
        PrometheusCounterGauge gauge = new PrometheusCounterGauge(def);
        assertEquals(MetricType.GAUGE, gauge.getMetricType());
        
        String prometheusText = gauge.toMetricString();
        assertTrue(prometheusText.contains("exampleMetric gauge"), "Gauge metric expected"); 
        assertTrue(prometheusText.contains("exampleMetric 0.0"), "Metric without label expected"); 
        assertFalse(prometheusText.contains("label1"), "Metric without label expected"); 
        System.out.println( prometheusText);
    }
    
    
    @Test
    public void testCounterMetric() {
        MetricDefinition def = MetricDefinitionBuilder.metricForField("value")
                .name( new PlaceholderString("exampleMetric"))
                .metricType("counter")
                .get();
        
        PrometheusCounterGauge counter = new PrometheusCounterGauge(def);
        assertEquals(MetricType.COUNTER, counter.getMetricType());
        
        String prometheusText = counter.toMetricString();
        assertTrue(prometheusText.contains("exampleMetric counter"), "Counter metric expected"); 
//        System.out.println( counter.toMetricString());
    }
    
    
//    @Test
//    public void testLabelsAndNamesAreValid() {
//        
//        String label1 = "label.#1.content";
//        MetricDefinition def = MetricDefinitionBuilder.metricForField("value")
//                .name( new PlaceholderString("example.Metric"))
//                .label(label1)
//                .get();
//        
//        PrometheusCounterGauge gauge = new PrometheusCounterGauge(def);
//        gauge.addValueForLabel(label1, "values can contain #.");
//        
//        String prometheusText = gauge.toMetricString();
//        System.out.println( prometheusText);
//        
//        assertTrue(prometheusText.contains("# TYPE example_Metric gauge"), "replacement of forbidden characters expected"); 
//    }
//    
}
