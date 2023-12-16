package net.kiar.collectorr.connector.mqtt.mapping;

import java.util.Optional;
import java.util.regex.Pattern;
import net.kiar.collectorr.config.model.TopicConfig;

/**
 *
 * @author ranSprd
 */
public class TopicCache {
    
    private static final String PLUS_WILDCARD = "\\+";
    private static final String SHARP_WILDCARD = "#";

    private static final String PLUS_WILDCARD_REPLACE = "[^/]\\+";
    private static final String SHARP_WILDCARD_REPLACE = ".*";
    
    

    private final Pattern preCompiledPattern;
    private final TopicProcessor topicProcessor;

    public TopicCache(Pattern preCompiledPattern, TopicProcessor topicProcessor) {
        this.preCompiledPattern = preCompiledPattern;
        this.topicProcessor = topicProcessor;
    }
    
    public static Optional<TopicCache> buildPattern(TopicConfig topicConfig) {
        String wildcardedTopic = topicConfig.getTopic();
        String topicReplaced =
                wildcardedTopic.replaceAll(PLUS_WILDCARD, PLUS_WILDCARD_REPLACE).replaceAll(SHARP_WILDCARD, SHARP_WILDCARD_REPLACE);

        Pattern pattern = Pattern.compile(topicReplaced);
        
        return Optional.of(new TopicCache(pattern, new TopicProcessor(topicConfig)));
    }
    
    

    public Pattern getPreCompiledPattern() {
        return preCompiledPattern;
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
