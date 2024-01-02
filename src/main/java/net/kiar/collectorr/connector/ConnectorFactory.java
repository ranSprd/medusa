package net.kiar.collectorr.connector;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import net.kiar.collectorr.config.ConnectorsConfigLoader;
import net.kiar.collectorr.config.MappingsConfigLoader;
import net.kiar.collectorr.config.RuntimeData;
import net.kiar.collectorr.config.model.connectors.MqttConnectorConfig;
import net.kiar.collectorr.config.model.connectors.MqttHeartbeatConfig;
import net.kiar.collectorr.connector.mqtt.consumer.MqttMessageConsumer;
import net.kiar.collectorr.repository.MqttTopicStats;
import net.redhogs.cronparser.CronExpressionDescriptor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
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
    
    @Inject
    private Scheduler scheduler;    
    
    public void onStart(@Observes StartupEvent ev) {
        log.info("Initialize Connectors...");
        initializeConnectors();
    }
    
    
    private void initializeConnectors() {
        if (ConnectorsConfigLoader.INSTANCE.readFromFile(connectorsConfigFile)) {
            data.map.putAll(ConnectorsConfigLoader.INSTANCE.getMqttConnectors().orElse(List.of())
                    .stream()
                    .map(connectorConfig -> new ConnectorHandler(connectorConfig))
                    .map(connector -> connector.startIncomingMessageConsumer())
                    .map(connector -> connector.initializeCron(scheduler))
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
        
        public Map<String, MqttTopicStats.UnknownTopicStatistic> getUnknownStatsForConnector(String connectorName) {
            if (connectorName == null || connectorName.isBlank()) {
                return Map.of();
            }
            
            ConnectorHandler handler = map.get(connectorName);
            if (handler == null) {
                return Map.of();
            }
            
            return handler.consumer.getStats().getUnknowTopicsStatistics();
        }
    }
    
    
    private static class ConnectorHandler {
        
        private boolean failed = false;
        
        private String conncetorName;
        
        private final MqttConnectorConfig mqttConnectorConfig;
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
        
        public void sendMessage(String topic, String message) {
            try {
                if (!mqttClient.isConnected()) {
                    mqttClient.reconnect();
                }
//                log.info("send message to broker");
                MqttMessage msg = new MqttMessage(message.getBytes());
                mqttClient.publish(topic, msg);
            } catch (Exception e) {
                log.warn(" {} - sending heartbeat failed", conncetorName, e.getMessage());
            }
        }
        
        public ConnectorHandler startIncomingMessageConsumer() {
            log.info("Initialize MQTT Connector {}...", mqttConnectorConfig.getName());
            
            conncetorName = mqttConnectorConfig.getName();
            if (conncetorName == null || conncetorName.isBlank()) {
                conncetorName = "MQTT_" +UUID.randomUUID();
                log.info("MQTT connector name invalid, generated name is {}", conncetorName);
            }

            String mqttHost = mqttConnectorConfig.getUrl();
            if (mqttHost == null || mqttHost.isBlank()) {
                log.error(" {} - no MQTT broker url configured. Can't establish connection.", 
                        conncetorName);
                failed = true;
                return this;
            }

            String mappingFile = mqttConnectorConfig.getMappingConfigFile();
            if (mappingFile == null || mappingFile.isBlank()) {
                log.warn(" {} - no configuration file for mapping 'topic to metric' defined. No topics will be observed.", conncetorName);
            }
    //        ConfigLoader conf = ConfigLoader.readFromFile("src/test/resources/example-config01.yaml");
            mappingConf = MappingsConfigLoader.readFromFile(mappingFile);
            RuntimeData.registerConfig(conncetorName, mappingConf);

            log.info(" {} - file [{}] with mqtt procssing config read, found {} configured topics", 
                    conncetorName, mappingFile, mappingConf.getNumberOfTopics());

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
                connOpts.setAutomaticReconnect(true);

                // Connect the client
                log.info("{} - Connecting to MQTT Broker at {}", conncetorName, mqttHost);
                mqttClient.connect(connOpts);
                log.info(" {} - Connected to broker with version {}", conncetorName, connOpts.getMqttVersion());

                // Topic filter the client will subscribe to
                String subTopic = mqttConnectorConfig.getRootTopic();
                if (subTopic == null || subTopic.isBlank()) {
                    subTopic = "#";
                }

                // Callback - Anonymous inner-class for receiving messages
                consumer = new MqttMessageConsumer(mappingConf, new MqttTopicStats());
                mqttClient.setCallback(consumer);

                // Subscribe client to the topic filter and a QoS level of 0
                log.info(" {} - Subscribing client to topic: {}", conncetorName, subTopic);
                mqttClient.subscribe(subTopic, 0);
    //
            } catch (MqttException me) {
                failed = true;
                log.error("{} Exception:   " , conncetorName, me);
                log.error("Reason Code: {}", me.getReasonCode());
                log.error("Message:     {}", me.getMessage());
                if (me.getCause() != null) {
                    log.error("Cause:       {}", me.getCause());
                }
            }
            return this;
        }
        
        public ConnectorHandler initializeCron(Scheduler scheduler)  {
            if (failed) {
                return this;
            }
            
            MqttHeartbeatConfig heartbeat = mqttConnectorConfig.getHeartbeat();
            if (heartbeat != null && heartbeat.isValid()) {
                log.info(" {} - found cron expression [{}]", conncetorName, heartbeat.getCron());
                log.info(" {} - fire a heartbeat {}", 
                        conncetorName, makeHumanReadable(heartbeat.getCron()));
                final String message = (heartbeat.getMessage()==null)?"":heartbeat.getMessage();
                scheduler.newJob(conncetorName +"_heartbeat")
                            .setCron(heartbeat.getCron())
                            .setTask(executionContext -> { 
                                log.debug(" {} - send heartbeat", conncetorName );
                                sendMessage(heartbeat.getTopic(), message);
                            })
                            .schedule();                
            }
            return this;
        }
        
        public String makeHumanReadable(String cronExpr) {
            try {
                return CronExpressionDescriptor.getDescription( cronExpr);
            } catch (ParseException ex) {
                log.warn("can't evaluate cron expression {}", cronExpr);
            }
            return cronExpr;
        }
        
    }
    
}
