package net.kiar.collectorr.repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.kiar.collectorr.metrics.PrometheusGauge;

/**
 *
 * @author ranSprd
 */
public enum MetricsRepo {
    INSTANCE;
    
    // @todo
    // replace the hashmap and replace by a sorted list
    private Map<String, PrometheusGauge> data = new HashMap<>();
    
    public void add(PrometheusGauge gauge) {
        if (gauge != null) {
            data.put(gauge.getSignature(), gauge);
        }
    }

    public void add(Collection<PrometheusGauge> updatedMetrics) {
        for(PrometheusGauge gauge : updatedMetrics) {
            data.put(gauge.getSignature(), gauge);
        }
    }
    
    public Collection<PrometheusGauge> data() {
        return data.values();
    }
    
}
