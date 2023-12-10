package net.kiar.collectorr.metrics;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author ranSprd
 */
public class PrometheusGaugeTest {
    

    @Test
    public void testInvalid() {
        MetricDefinition def = MetricDefinitionBuilder.metricForField("value")
                .name("exampleMetric")
                .label("label1")
                .get();
        
        PrometheusGauge gauge = new PrometheusGauge(def);
        System.out.println( gauge.toMetricString());
    }
    
}
