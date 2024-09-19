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
package net.kiar.collectorr.metrics;

import java.io.IOException;
import java.util.Optional;
import net.kiar.TestHelper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author ranSprd
 */
public class FieldMappingsTest {

    private final String configWithMapping = """
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
        newLabel2: label2Content-forB
                                                                                          
  metrics:
  - name: "metricName"
    valueField: v
    labels: [newLabel1, detailedName, field1, to]                                            
""";
    @Test
    public void testCorrectLabelType() throws IOException {
        
        TestHelper.Result result = TestHelper.buildAndProcessFirstFromFile(configWithMapping, "/topic/to/test", "src/test/resources/mqtt/payloads/fieldMappingPayload1.json");
        assertNotNull(result.topicCache());
        
        Optional<PrometheusCounterGauge> anyGauge = result.metrics().stream()
                .filter(gauge -> gauge.getName().equals("metricName"))
                .findAny();
        assertTrue(anyGauge.isPresent());
        
        assertEquals(4, anyGauge.get().getMetricDefinition().getLabels().size());
        FieldDescription newLabel1Field = anyGauge.get().getMetricDefinition().findLabel("newLabel1").get(); 
        assertEquals( FieldSourceType.PAYLOAD, newLabel1Field.getType());
        assertTrue(newLabel1Field.isMapped());
        
        FieldDescription detailedNameField = anyGauge.get().getMetricDefinition().findLabel("detailedName").get();
        assertEquals( FieldSourceType.PAYLOAD, detailedNameField.getType());
        assertFalse(detailedNameField.isMapped());
        
        assertEquals( FieldSourceType.PAYLOAD, anyGauge.get().getMetricDefinition().findLabel("field1").get().getType());
        assertEquals( FieldSourceType.TOPIC, anyGauge.get().getMetricDefinition().findLabel("to").get().getType());
        
        
    }
    
    private final String configWithMapping2 = """
topics:
- topic: /topic/to/test
  mappings:
    field1:
      A:
        newLabel1: label1Content-forA
        newLabel2: label2Content-forA
        newLabel3: label3Content-forA
      B:
        newLabel1: label1Content-forB
        newLabel2: label2Content-forB
                                                                                          
  metrics:
  - name: "metricName"
    valueField: v
    labels: [newLabel1, newLabel2, detailedName]                                            
""";
    
    @Test
    public void testSourceFieldIsNotPartOfMetricLabels() throws IOException {
        TestHelper.Result result = TestHelper.buildAndProcessFirstFromFile(configWithMapping2, "/topic/to/test", "src/test/resources/mqtt/payloads/fieldMappingPayload1.json");
        assertNotNull(result.topicCache());
        
        Optional<PrometheusCounterGauge> anyGauge = result.metrics().stream()
                .filter(gauge -> gauge.getName().equals("metricName"))
                .findAny();
        assertTrue(anyGauge.isPresent());
        
        assertEquals(4, anyGauge.get().getMetricDefinition().getLabels().size());
        assertEquals(2, anyGauge.get().getNumberOfLabels());
    }
}