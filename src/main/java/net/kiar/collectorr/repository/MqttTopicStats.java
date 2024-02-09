package net.kiar.collectorr.repository;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import net.kiar.collectorr.metrics.PrometheusCounterGauge;
import net.kiar.collectorr.payloads.PayloadResolver;
import net.kiar.collectorr.payloads.json.JsonResolver;

/**
 *
 * @author ranSprd
 */
public class MqttTopicStats {
    
    private MeterRegistry registry = Metrics.globalRegistry;    
    
    private final Map<String, UnknownTopicStatistic> unknownData = new ConcurrentHashMap<>();
    
    private long lastProcessed = System.currentTimeMillis();
    private long lastTimeStamp = 0;
    
    private AtomicLong gauge;

    public MqttTopicStats(String connectorName) {
        
        gauge = registry.gauge("updated_metrics_" +connectorName, new AtomicLong(0));        
//        Gauge.builder("jvm.threads.peak", threadBean, ThreadMXBean::getPeakThreadCount) 
//    .baseUnit(BaseUnits.THREADS) // optional 
//    .description("The peak live thread count...") // optional 
//    .tags("key", "value") // optional 
//    .register(registry);         
    }
    
    
    private void markAction() {
        lastTimeStamp = System.currentTimeMillis();
    }
    
    
    /** 
     * 
     * a not configured topic arrived 
     * 
     * @param topic
     * @param payload 
     */
    public void registerUnknown(String topic, String payload) {
        UnknownTopicStatistic stat = unknownData.computeIfAbsent(
                Integer.toHexString(topic.hashCode()), 
                x -> new UnknownTopicStatistic(topic, payload));
        stat.incReceivedCount();
        markAction();
    }

    public Map<String, UnknownTopicStatistic> getUnknowTopicsStatistics() {
        return unknownData;
    }
    
    public void registerProcessed(String topic, List<PrometheusCounterGauge> results) {
        if (results != null && !results.isEmpty()) {
            lastProcessed = System.currentTimeMillis();
            gauge.set( gauge.get() + results.size());
        }
        markAction();
    }

    public long getLastProcessed() {
        return lastProcessed;
    }

    /**
     * last time of a processed or unpressed event 
     * 
     * @return 
     */    
    public long getLastTimeStamp() {
        return lastTimeStamp;
    }

    public static class ProcessedTopicStatistic {
        private int receivedCount = 0;
        private int processedCount = 0;
        private final String topic;

        public ProcessedTopicStatistic(String topic, List<PrometheusCounterGauge> results) {
            this.topic = topic;
        }
        
        public void markProcessed() {
            receivedCount++;
        }

        public int getReceivedCount() {
            return receivedCount;
        }

        public int getProcessedCount() {
            return processedCount;
        }

        public String getTopic() {
            return topic;
        }
    }
    
    public static class UnknownTopicStatistic {
        private int receivedCount = 0;
//        private int processedCount = 0;
        private final String topic;
        private String detectedValues;
        private String detectedLabels;

        public UnknownTopicStatistic(String topic, String messagePayload) {
            this.topic = topic;
            PayloadResolver payloadResolver = JsonResolver.consume(messagePayload);
            detectedLabels = payloadResolver.getLabelNamesAsString();
            detectedValues = payloadResolver.getValueNamesAsString();
        }

        public String getTopic() {
            return topic;
        }
        
        public void incReceivedCount() {
            receivedCount++;
        }

        public int getReceivedCount() {
            return receivedCount;
        }

//        public void incProcessedCount() {
//            processedCount++;
//        }
//
//        public int getProcessedCount() {
//            return processedCount;
//        }
        
        public String getDetectedValues() {
            return detectedValues;
        }

        public void setDetectedValues(String detectedValues) {
            this.detectedValues = detectedValues;
        }

        public String getDetectedLabels() {
            return detectedLabels;
        }

        public void setDetectedLabels(String detectedLabels) {
            this.detectedLabels = detectedLabels;
        }
        
        
        
    }
}
