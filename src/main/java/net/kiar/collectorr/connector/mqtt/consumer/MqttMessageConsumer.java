package net.kiar.collectorr.connector.mqtt.consumer;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import net.kiar.collectorr.config.MappingsConfigLoader;
import net.kiar.collectorr.metrics.PrometheusGauge;
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

    public MqttMessageConsumer(MappingsConfigLoader watchingConfig, MqttTopicStats stats) {
        this.topicMatcher = TopicMatcher.getMatcherFor( watchingConfig.getTopicsToObserve());
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
                String time = new Timestamp(System.currentTimeMillis()).toString();
                log.info("\n\tTopic:   " + topic
                        + "\n\tMessage: " + payload
    //                    + "\n\tQoS:     " + message.getQos()
                        + "\n");
                List<PrometheusGauge> results = processor.get().consumeMessage(payload, topic);
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
        log.info("complete");
    }

    public MqttTopicStats getStats() {
        return stats;
    }

}
