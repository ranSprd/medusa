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
package net.kiar.collectorr.payloads.json;

import java.util.Optional;
import net.kiar.collectorr.connector.mqtt.mapping.TopicStructure;
import net.kiar.collectorr.payloads.PayloadDataNode;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author ranSprd
 */
public class TopicPathResolverTest {
    
    @Test
    public void testSomeMethod() {
        TopicStructure struct = TopicStructure.build("/{field-a}/{field-b}/{field-c}");
        
        TopicPathResolver tpr = new TopicPathResolver("/a/b/c", struct);
        
        Optional<PayloadDataNode> x = tpr.findNode("field-c");
        assertTrue(tpr.findNode("field-a").isPresent());
        assertTrue(tpr.findNode("field-b").isPresent());
        assertTrue(tpr.findNode("field-c").isPresent());
        assertFalse(tpr.findNode("field").isPresent());
    }
    
}
