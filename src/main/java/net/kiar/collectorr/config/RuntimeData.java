package net.kiar.collectorr.config;

import java.util.HashMap;
import java.util.Map;

/**
 * @todo not perfect, please refactore
 * 
 * @author ranSprd
 */
public class RuntimeData {

    private static final Map<String,MappingsConfigLoader> configMap = new HashMap<>();
    
    public static MappingsConfigLoader findConfig(String configName) {
        return configMap.get(configName);
    }
    
    public static void registerConfig(String configName, MappingsConfigLoader mcl) {
        configMap.put(configName, mcl);
    }
}
