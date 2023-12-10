package net.kiar.collectorr.mqtt;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.util.UUID;
import net.kiar.collectorr.config.ConfigLoader;
import net.kiar.collectorr.config.RuntimeData;
import net.kiar.collectorr.mqtt.consumer.MqttMessageConsumer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ranSprd
 */
@ApplicationScoped
public class MQTTObserver {
    private static final Logger log = LoggerFactory.getLogger(MQTTObserver.class);
    
    @ConfigProperty(name = "mqtt.broker.config") 
    private String configFileName;
    
    @ConfigProperty(name = "mqtt.broker.url") 
    private String mqttHostUrl; // "tcp://192.168.2.145:1883";
    

    private MqttClient mqttClient = null;

    public void onStart(@Observes StartupEvent ev) {
        log.info("The application is starting...");
        run();
    }
    
    public void onStop(@Observes ShutdownEvent ev) {               
        log.info("The application is stopping...");
        if (mqttClient != null) {
            try {
                // Disconnect the client
                mqttClient.disconnect();
            } catch (Exception e) {
                log.error("Can not close connection to broker.", e);
            }
        }
    }
    

    public void run()  {
        
        log.info("TopicSubscriber initializing...");

//        ConfigLoader conf = ConfigLoader.readFromFile("src/test/resources/example-config01.yaml");
        ConfigLoader conf = ConfigLoader.readFromFile(configFileName);
        RuntimeData.config = conf;
        log.info("file [{}] with mqtt procssing config read, found {} configured topics", configFileName, conf.getNumberOfTopics());

        try {
            // Create an Mqtt client
            mqttClient = new MqttClient(mqttHostUrl, "MQTT_Prometheus_Mapper_" + UUID.randomUUID().toString().substring(0, 8));
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            // Connect the client
            log.info("Connecting to MQTT Broker at {}", mqttHostUrl);
            mqttClient.connect(connOpts);
            log.info("Connected to broker with version {}", connOpts.getMqttVersion());

            // Topic filter the client will subscribe to
//            final String subTopic = "shellies/+/info";
            final String subTopic = "#";
//            final String subTopic = "\\#";
//shellyplus1-4855199c5cf4/events/rpc


            // Callback - Anonymous inner-class for receiving messages
            mqttClient.setCallback(new MqttMessageConsumer(conf));
                
            // Subscribe client to the topic filter and a QoS level of 0
            log.info("Subscribing client to topic: {}", subTopic);
            mqttClient.subscribe(subTopic, 0);
//
        } catch (MqttException me) {
            log.error("Exception:   " + me);
            log.error("Reason Code: " + me.getReasonCode());
            log.error("Message:     " + me.getMessage());
            if (me.getCause() != null) {
                log.error("Cause:       " + me.getCause());
            }
        }
    }

}
