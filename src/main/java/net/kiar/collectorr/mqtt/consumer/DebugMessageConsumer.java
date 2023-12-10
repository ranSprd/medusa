package net.kiar.collectorr.mqtt.consumer;

import java.sql.Timestamp;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ranSprd
 */
public class DebugMessageConsumer implements MqttCallback {

    private static final Logger log = LoggerFactory.getLogger(DebugMessageConsumer.class);


    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // Called when a message arrives from the server that
        // matches any subscription made by the client
        String time = new Timestamp(System.currentTimeMillis()).toString();
        String payload = new String(message.getPayload());
        log.info("\nReceived a Message!"
                + "\n\tTime:    " + time
                + "\n\tTopic:   " + topic
                //                            + "\n\tMessage: " + payload
                + "\n\tQoS:     " + message.getQos() + "\n");
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("Connection to messaging lost!" + cause.getMessage());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        log.info("complete");
    }

}
