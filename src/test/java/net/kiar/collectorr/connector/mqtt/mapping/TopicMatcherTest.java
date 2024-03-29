/*
 * Copyright 2016 Red Hat Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.kiar.collectorr.connector.mqtt.mapping;

import java.util.List;
import net.kiar.collectorr.config.model.TopicConfig;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Tests related to topic wildcards matches
 */
public class TopicMatcherTest {

    @Test
    public void testFixedTopicMatch() {

        String subTopic = "mytopic/foo";
        String pubTopic = "mytopic/foo";

        assertTrue(isMatch(subTopic, pubTopic));
    }

    @Test
    public void testFixedTopicNotMatch() {

        String subTopic = "mytopic/foo";
        String pubTopic = "mytopic/bar";

        assertFalse(isMatch(subTopic, pubTopic));
    }

    @Test
    public void testTopicSharpWildcardMatch() {

        String subTopic = "mytopic/#";
        String pubTopic = "mytopic/foo";

        assertTrue(isMatch(subTopic, pubTopic));

        pubTopic = "mytopic/foo/bar";

        assertTrue(isMatch(subTopic, pubTopic));
    }

    @Test
    public void testTopicSharpWildcardNotMatch() {

        String subTopic = "mytopic/#";
        String pubTopic = "mytopic";

        assertFalse(isMatch(subTopic, pubTopic));
    }

    @Test
    public void testTopicPlusWildcardMatch() {

        String subTopic = "mytopic/+/bar";
        String pubTopic = "mytopic/foo/bar";

        assertTrue(isMatch(subTopic, pubTopic));

        pubTopic = "mytopic/another/bar";

        assertTrue(isMatch(subTopic, pubTopic));

        subTopic = "mytopic/foo/+";
        pubTopic = "mytopic/foo/bar";

        assertTrue(isMatch(subTopic, pubTopic));

        subTopic = "mytopic/+/bar/+/foo";
        pubTopic = "mytopic/a/bar/b/foo";

        assertTrue(isMatch(subTopic, pubTopic));
    }

    @Test
    public void testTopicPlusWildcardNotMatch() {

        String subTopic = "mytopic/+/bar";
        String pubTopic = "mytopic/bar";

        assertFalse(isMatch(subTopic, pubTopic));

        subTopic = "mytopic/foo/+";
        pubTopic = "mytopic/foo";

        assertFalse(isMatch(subTopic, pubTopic));

        subTopic = "mytopic/+/bar/+/foo";
        pubTopic = "mytopic/a/bar/b/foo/c";

        assertFalse(isMatch(subTopic, pubTopic));
    }
    
    public static boolean isMatch(String wildcardedTopic, String topic) {
        
        TopicConfig tc = new TopicConfig();
        tc.setTopic(wildcardedTopic);
        
        return TopicMatcher.getMatcherFor( List.of( tc)).find(topic).isPresent();
    }
}
