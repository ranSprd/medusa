/*
 * Copyright 2023 ranSprd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kiar.collectorr.connector.mqtt.mapping;

import java.util.Optional;
import net.kiar.collectorr.config.model.TopicConfig;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author ranSprd
 */
public class TopicCacheTest {
    
    @Test
    public void testSomeMethod() {
        TopicConfig tc = new TopicConfig();
        tc.setTopic("foo/+/bar");
        
        Optional<TopicCache> topicCache = TopicCache.buildTopicPattern(tc);
        assertTrue(topicCache.isPresent());
        
        boolean result = topicCache.get().isMatch("foo/blub/bar");
        assertTrue(result);
        
    }
    
}
