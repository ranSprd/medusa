package net.kiar.collectorr.connector.mqtt.consumer;

import java.util.List;
import java.util.Optional;
import net.kiar.collectorr.config.MappingsConfigLoader;
import net.kiar.collectorr.metrics.PrometheusCounterGauge;
import net.kiar.collectorr.connector.mqtt.mapping.TopicMatcher;
import net.kiar.collectorr.connector.mqtt.mapping.TopicProcessor;
import net.kiar.collectorr.repository.MetricsRepo;
import net.kiar.collectorr.repository.MqttTopicStats;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for incoming MQTT messages
 * 
 * @author ranSprd
 */
public class MqttMessageConsumer implements MqttCallback {

    private static final Logger log = LoggerFactory.getLogger(MqttMessageConsumer.class);

    private final TopicMatcher topicMatcher;
    private final MqttTopicStats stats;

    private final HealthThresholds healthThresholds;
    
    public MqttMessageConsumer(MappingsConfigLoader watchingConfig, HealthThresholds healthThresholds, MqttTopicStats stats) {
        this.topicMatcher = TopicMatcher.getMatcherFor( watchingConfig.getTopicsToObserve());
        this.healthThresholds = healthThresholds;
        this.stats = stats;
    }
    
    /**
     * Called when a message arrives from the server that matches any subscription 
     * made by the client.
     * 
     * @param topic
     * @param message
     * @throws Exception 
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        try {
            Optional<TopicProcessor> processor = topicMatcher.find(topic);
            String payload = new String(message.getPayload());

            if (processor.isPresent()) {
                if (processor.get().isVerbose()) {
                    log.info("\n\tTopic:   " + topic
    //                        + "\n\tMessage: " + payload
                            + "\n");
                }
                List<PrometheusCounterGauge> results = processor.get().consumeMessage(payload, topic);
                MetricsRepo.INSTANCE.add( results);
                stats.registerProcessed(topic, results);

            } else {
                stats.registerUnknown(topic, payload);
            }
        } catch (Exception e) {
            // collect metrics...
            log.error("Processing of incoming message at topic [{}] failed", topic, e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("Connection to MQTT broker lost! {}", cause.getMessage(), cause);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            MqttMessage msg = (token == null)? null : token.getMessage();
            if (msg != null) {
                log.info("complete {}", msg);
            }
        } catch (Exception e) {
            log.warn(" MQTT complete processing error {}", e.getMessage());
        }
    }

    public MqttTopicStats getStats() {
        return stats;
    }
    
    /**
     * timestamp of the last valid incoming message
     * @return 
     */
    public boolean healthState() {
        if (healthThresholds.getMaxIdleTimeMillis() > 1) {
            long timeStamp = stats.getLastTimeStamp();
            return (timeStamp == 0 || (System.currentTimeMillis() - timeStamp) < healthThresholds.getMaxIdleTimeMillis());
        }
        return true;
    }

}
