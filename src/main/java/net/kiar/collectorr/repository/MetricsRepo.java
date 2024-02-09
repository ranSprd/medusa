package net.kiar.collectorr.repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import net.kiar.collectorr.metrics.PrometheusCounterGauge;

/**
 *
 * @author ranSprd
 */
public enum MetricsRepo {
    INSTANCE;
    
    // @todo
    // replace the hashmap and replace by a sorted list
    private final Map<String, PrometheusCounterGauge> data = new HashMap<>();
    
    public void add(PrometheusCounterGauge gauge) {
        if (gauge != null) {
            data.put(gauge.getSignature(), gauge);
        }
    }

    public void add(Collection<PrometheusCounterGauge> updatedMetrics) {
        for(PrometheusCounterGauge gauge : updatedMetrics) {
            data.put(gauge.getSignature(), gauge);
        }
    }
    
    public Collection<PrometheusCounterGauge> data() {
        return data.values();
    }
    
    public Map<String, List<PrometheusCounterGauge>> sortedData() {
        Map<String, List<PrometheusCounterGauge>> grouped = data.values().stream()
                .collect(Collectors.groupingBy(PrometheusCounterGauge::getName, Collectors.toList() ));
        
        return new TreeMap<>( grouped);
    }
    
}
