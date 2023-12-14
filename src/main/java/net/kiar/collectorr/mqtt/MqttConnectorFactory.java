package net.kiar.collectorr.mqtt;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.util.List;
import java.util.UUID;
import net.kiar.collectorr.config.ConnectorsConfigLoader;
import net.kiar.collectorr.config.MappingsConfigLoader;
import net.kiar.collectorr.config.RuntimeData;
import net.kiar.collectorr.config.model.connectors.MqttConnectorConfig;
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
public class MqttConnectorFactory {
    private static final Logger log = LoggerFactory.getLogger(MqttConnectorFactory.class);

    @ConfigProperty(name = "connector.config.file") 
    private String connectorsConfigFile;
    
    public void onStart(@Observes StartupEvent ev) {
        log.info("Initialize Connectors...");
        initializeConnectors();
    }
    
    
    private void initializeConnectors() {
        if (ConnectorsConfigLoader.INSTANCE.readFromFile(connectorsConfigFile)) {
            ConnectorsConfigLoader.INSTANCE.getMqttConnectors().orElse(List.of())
                    .stream().forEach(connectorConfig -> initializeConnector(connectorConfig));
        } else {
            log.warn("Can't read config from file [{}]. No MQTT Connectors started.", connectorsConfigFile);
        }
    }
    
    
    
    private void initializeConnector(MqttConnectorConfig mqttConnectorConfig) {
        log.info("Initialize MQTT Connector {}...", mqttConnectorConfig.getName());
        
        String name = mqttConnectorConfig.getName();
        if (name == null || name.isBlank()) {
            name = "MQTT_" +UUID.randomUUID();
            log.info("MQTT connector name invalid, generated name is {}", name);
        }
        
        String mqttHost = mqttConnectorConfig.getUrl();
        if (mqttHost == null || mqttHost.isBlank()) {
            log.error("MQTT connector [{}] has no broker url configured. Can't establish connection.", name);
            return;
        }

        String mappingFile = mqttConnectorConfig.getMappingConfigFile();
        if (mappingFile == null || mappingFile.isBlank()) {
            log.warn("no configuration file for mapping 'topic to metric' defined. No topics will be observed.");
        }
//        ConfigLoader conf = ConfigLoader.readFromFile("src/test/resources/example-config01.yaml");
        MappingsConfigLoader conf = MappingsConfigLoader.readFromFile(mappingFile);
        RuntimeData.registerConfig(name, conf);
        
        log.info("file [{}] with mqtt procssing config read, found {} configured topics", mappingFile, conf.getNumberOfTopics());

        try {
            // Create an Mqtt client
            // In a Java project I'm using the Eclipse Paho MQTT library. Into the 
            // project root I have a lot of folders like c32adeb3-f556-4563-afbe-8417b1de74ea-tcp1270018883 
            // cointaining a .lck file.
            // The reason is simple, this is because when you don't pass the persistence type 
            // explicitly MqttClient will use the MqttDefaultFilePersistence by default
            // We can use null or MemoryPersistence as 3rd parameter
            MqttClient mqttClient = new MqttClient(mqttHost, 
                    "MQTT_Prometheus_Mapper_" + name +"_"+ UUID.randomUUID().toString().substring(0, 8), null);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            // Connect the client
            log.info("Connecting to MQTT Broker at {}", mqttHost);
            mqttClient.connect(connOpts);
            log.info("Connected to broker with version {}", connOpts.getMqttVersion());

            // Topic filter the client will subscribe to
            String subTopic = mqttConnectorConfig.getRootTopic();
            if (subTopic == null || subTopic.isBlank()) {
                subTopic = "#";
            }

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
