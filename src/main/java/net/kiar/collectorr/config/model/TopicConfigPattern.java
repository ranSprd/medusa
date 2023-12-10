package net.kiar.collectorr.config.model;

/**
 *
 * @author ranSprd
 */
public class TopicConfigPattern {

    private String path;
    private String name;

    public TopicConfigPattern() {
    }
    

    public TopicConfigPattern(String pattern) {
        this.path = pattern;
    }
    
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
