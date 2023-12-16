package net.kiar.collectorr.connector;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import net.kiar.collectorr.config.ConnectorsConfigLoader;
import net.kiar.collectorr.config.MappingsConfigLoader;
import net.kiar.collectorr.config.RuntimeData;
import net.kiar.collectorr.config.model.connectors.MqttConnectorConfig;
import net.kiar.collectorr.connector.mqtt.consumer.MqttMessageConsumer;
import net.kiar.collectorr.repository.MqttTopicStats;
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
public class ConnectorFactory {
    private static final Logger log = LoggerFactory.getLogger(ConnectorFactory.class);
    
    public static final ConnectorData data = new ConnectorData();
    
    @ConfigProperty(name = "connector.config.file") 
    private String connectorsConfigFile;
    
    public void onStart(@Observes StartupEvent ev) {
        log.info("Initialize Connectors...");
        initializeConnectors();
    }
    
    
    private void initializeConnectors() {
        if (ConnectorsConfigLoader.INSTANCE.readFromFile(connectorsConfigFile)) {
            data.map.putAll(ConnectorsConfigLoader.INSTANCE.getMqttConnectors().orElse(List.of())
                    .stream()
                    .map(connectorConfig -> new ConnectorHandler(connectorConfig))
                    .map(connector -> connector.start())
                    .collect(Collectors.toMap(p -> p.conncetorName, p -> p)) );
        } else {
            log.warn("Can't read config from file [{}]. No MQTT Connectors started.", connectorsConfigFile);
        }
    }
    
    /** public interface to all connectors */
    public static class ConnectorData {
        private Map<String, ConnectorHandler> map = new HashMap<>();
        
        
        
        public List<ConnectorSummaryStatistics> getConnectorsOverview() {
            return map.values().stream()
                    .map(entry -> entry.getStatsSnapshot())
                    .collect(Collectors.toList());
        }
    }
    
//    public static record ConnectorInfo(String name, int )
    
    
    private static class ConnectorHandler {
        
        private boolean failed = false;
        
        private String conncetorName;
        
        private MqttConnectorConfig mqttConnectorConfig;
        private MappingsConfigLoader mappingConf;

        private MqttClient mqttClient;
        private MqttMessageConsumer consumer;

        public ConnectorHandler(MqttConnectorConfig mqttConnectorConfig) {
            this.mqttConnectorConfig = mqttConnectorConfig;
        }
        
        public ConnectorSummaryStatistics getStatsSnapshot() {
            ConnectorSummaryStatistics summary = new ConnectorSummaryStatistics(conncetorName);

            summary.setUnprocessed(
                    consumer.getStats().getUnknowTopicsStatistics().size()
            );
            
            return summary;
        }
        

        
        public ConnectorHandler start() {
            log.info("Initialize MQTT Connector {}...", mqttConnectorConfig.getName());

            conncetorName = mqttConnectorConfig.getName();
            if (conncetorName == null || conncetorName.isBlank()) {
                conncetorName = "MQTT_" +UUID.randomUUID();
                log.info("MQTT connector name invalid, generated name is {}", conncetorName);
            }

            String mqttHost = mqttConnectorConfig.getUrl();
            if (mqttHost == null || mqttHost.isBlank()) {
                log.error("MQTT connector [{}] has no broker url configured. Can't establish connection.", conncetorName);
                failed = true;
                return this;
            }

            String mappingFile = mqttConnectorConfig.getMappingConfigFile();
            if (mappingFile == null || mappingFile.isBlank()) {
                log.warn("no configuration file for mapping 'topic to metric' defined. No topics will be observed.");
            }
    //        ConfigLoader conf = ConfigLoader.readFromFile("src/test/resources/example-config01.yaml");
            mappingConf = MappingsConfigLoader.readFromFile(mappingFile);
            RuntimeData.registerConfig(conncetorName, mappingConf);

            log.info("file [{}] with mqtt procssing config read, found {} configured topics", mappingFile, mappingConf.getNumberOfTopics());

            try {
                // Create an Mqtt client
                // In a Java project I'm using the Eclipse Paho MQTT library. Into the 
                // project root I have a lot of folders like c32adeb3-f556-4563-afbe-8417b1de74ea-tcp1270018883 
                // cointaining a .lck file.
                // The reason is simple, this is because when you don't pass the persistence type 
                // explicitly MqttClient will use the MqttDefaultFilePersistence by default
                // We can use null or MemoryPersistence as 3rd parameter
                mqttClient = new MqttClient(mqttHost, 
                        "MQTT_Prometheus_Mapper_" + conncetorName +"_"+ UUID.randomUUID().toString().substring(0, 8), null);
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
                mqttClient.setCallback(new MqttMessageConsumer(mappingConf, new MqttTopicStats()));

                // Subscribe client to the topic filter and a QoS level of 0
                log.info("Subscribing client to topic: {}", subTopic);
                mqttClient.subscribe(subTopic, 0);
    //
            } catch (MqttException me) {
                failed = true;
                log.error("Exception:   " + me);
                log.error("Reason Code: " + me.getReasonCode());
                log.error("Message:     " + me.getMessage());
                if (me.getCause() != null) {
                    log.error("Cause:       " + me.getCause());
                }
            }
            return this;
        }
        
    }
    
}
