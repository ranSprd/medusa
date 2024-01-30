/*
 * Copyright 2024 ranSprd.
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
package net.kiar.collectorr.metrics.builder;

import java.util.Collection;
import java.util.Map;
import net.kiar.collectorr.config.MappingsConfigLoader;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author ranSprd
 */
public class LabelBuilderTest {
    


    private final String manyLabels = """
topics:
- topic: /topic/{to}/test
  mappings:
    field1:
      A:
        newLabel1: label1Content-forA
        newLabel2: label2Content-forA
        newLabel3: label3Content-forA
      B:
        newLabel1: label1Content-forB
        newLabel4: label4Content-forB
""";

    @Test
    public void testTargetFields() {
        MappingsConfigLoader conf = MappingsConfigLoader.readContent(manyLabels);
        LabelBuilder lb = new LabelBuilder(Map.of(), conf.getTopicsToObserve().get(0));
        
        Collection<String> mappedFields = lb.getMappedTargetFields();
        assertNotNull(mappedFields);
        assertFalse(mappedFields.isEmpty());
        assertEquals(4, mappedFields.size());
        
        assertTrue(mappedFields.contains("newLabel1"));
        assertTrue(mappedFields.contains("newLabel2"));
        assertTrue(mappedFields.contains("newLabel3"));
        assertTrue(mappedFields.contains("newLabel4"));
        
        
        
    }
    
}
