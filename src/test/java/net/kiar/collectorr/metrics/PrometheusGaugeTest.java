package net.kiar.collectorr.metrics;

import org.junit.jupiter.api.Test;

/**
 *
 * @author ranSprd
 */
public class PrometheusGaugeTest {
    

    @Test
    public void testInvalid() {
        MetricDefinition def = MetricDefinitionBuilder.metricForField("value")
                .name( new PlaceholderString("exampleMetric"))
                .label("label1")
                .get();
        
        PrometheusGauge gauge = new PrometheusGauge(def);
        System.out.println( gauge.toMetricString());
    }
    
}
