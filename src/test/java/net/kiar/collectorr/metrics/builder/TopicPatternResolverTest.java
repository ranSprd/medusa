package net.kiar.collectorr.metrics.builder;

import java.util.Map;
import net.kiar.collectorr.config.model.TopicConfigPattern;
import net.kiar.collectorr.metrics.FieldDescription;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author ranSprd
 */
public class TopicPatternResolverTest {
    
    private Map<String, FieldDescription>  forPattern(String topicPattern) {
        return TopicPatternResolver.extractFieldsFromPattern( new TopicConfigPattern(topicPattern));
    }
    
    @Test
    public void testWithInvalidInput() {
        assertTrue( TopicPatternResolver.extractFieldsFromPattern(null).isEmpty());
        assertTrue( forPattern(null).isEmpty());
        assertTrue( forPattern("").isEmpty());
        assertTrue( forPattern("    ").isEmpty());
    }
    
    @Test
    public void testInvalidPatternsPresent() {
        assertTrue( forPattern("foo/{bar/xy").isEmpty());
        assertTrue( forPattern("foo}/bar/xy").isEmpty());
        assertTrue( forPattern("foo/{}/bar/xy").isEmpty());
        assertTrue( forPattern("foo/{pref}suf/bar/xy").isEmpty());
    }
    
    @Test
    public void testWithPatterns() {
        assertEquals(1, forPattern("foo/{bar}/xy").size());
        assertEquals(1, forPattern("{foo}").size());
        assertEquals(2, forPattern("foo/{1}/bar/{2}/xy").size());
    }
    
}
