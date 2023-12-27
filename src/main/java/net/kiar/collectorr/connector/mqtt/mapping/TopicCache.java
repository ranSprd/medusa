package net.kiar.collectorr.connector.mqtt.mapping;

import java.util.Optional;
import java.util.regex.Pattern;
import net.kiar.collectorr.config.model.TopicConfig;

/**
 *
 * @author ranSprd
 */
public class TopicCache {
    
    private final Pattern preCompiledPattern;
    private final TopicProcessor topicProcessor;

    public TopicCache(Pattern preCompiledPattern, TopicProcessor topicProcessor) {
        this.preCompiledPattern = preCompiledPattern;
        this.topicProcessor = topicProcessor;
    }
    
    public static Optional<TopicCache> buildPattern(TopicConfig topicConfig) {

        TopicStructure ts = TopicStructure.build(topicConfig.getTopic());
        Pattern pattern = Pattern.compile(ts.getTopicPatternStr());
        
        return Optional.of(new TopicCache(pattern, new TopicProcessor(topicConfig, ts)));
    }
    
    

    public TopicProcessor getTopicProcessor() {
        return topicProcessor;
    }

    /**
     * 
     * @param givenTopic
     * @return  true if there is a configuration for the given topic
     */
    public boolean isMatch(String givenTopic) {
        return preCompiledPattern.matcher(givenTopic).matches();
    }
    
}
