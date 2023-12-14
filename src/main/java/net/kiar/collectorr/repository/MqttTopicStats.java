package net.kiar.collectorr.repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.kiar.collectorr.metrics.PrometheusGauge;
import net.kiar.collectorr.payloads.PayloadResolver;
import net.kiar.collectorr.payloads.json.JsonResolver;

/**
 *
 * @author ranSprd
 */
public class MqttTopicStats {
    
    private final Map<String, UnknownTopicStatistic> unknownData = new ConcurrentHashMap<>();
    
    public static MqttTopicStats getInstance() {
        return MqttTopicStatsHolder.INSTANCE;
    }
    
    private static class MqttTopicStatsHolder {
        private static final MqttTopicStats INSTANCE = new MqttTopicStats();
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
    }

    public Map<String, UnknownTopicStatistic> getUnknowTopicsStatistics() {
        return unknownData;
    }
    
    public void registerProcessed(String topic, List<PrometheusGauge> results) {
//        UnknownTopicStatistic stat = unknownData.computeIfAbsent(Integer.toHexString(topic.hashCode()), x -> new UnknownTopicStatistic(topic, payload));
//        stat.incReceivedCount();
    }
    
    
    public static class ProcessedTopicStatistic {
        private int receivedCount = 0;
        private int processedCount = 0;
        private final String topic;

        public ProcessedTopicStatistic(String topic, List<PrometheusGauge> results) {
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
