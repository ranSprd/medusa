package net.kiar.collectorr.config.model;

import java.util.Map;

/**
 * Mapping of labels
 * 
 * @author ranSprd
 */
public class TopicConfigMappings {

    private String source;
    private String target;
    
    private Map<String, String> map;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }
    
    
    
}
