package net.kiar.collectorr.mqtt;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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
        
    }
    
}
