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
package net.kiar.collectorr.connector;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import net.kiar.TestHelper.Result;
import net.kiar.TestHelper;
import net.kiar.collectorr.metrics.MetricDefinition;
import net.kiar.collectorr.metrics.PrometheusCounterGauge;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author ranSprd
 */
public class ArrayPayloadTest {
    
    
    private final String configMinimalSetup = """
topics:
- topic: /get_livedata_info
  metrics:
  - valueField: common_list.*.val
""";
    @Test
    public void testAutoGeneratedLabels() throws IOException {
        Result result = TestHelper.buildAndProcessFirst(configMinimalSetup, "/get_livedata_info", "src/test/resources/http/payloads/ecowitt.json");
        assertNotNull(result.metrics());
        assertEquals(12, result.metrics().size());
        
        Optional<PrometheusCounterGauge> anyGauge = result.metrics().stream()
                .filter(gauge -> gauge.getName().endsWith("list.#0.val"))
                .findAny();
        assertTrue(anyGauge.isPresent());
        System.out.println( anyGauge.get().toMetricString());
        assertEquals(2, anyGauge.get().getNumberOfLabels());
        assertEquals( "0x02", anyGauge.get().getLabelValue("id"));
        assertEquals( "C", anyGauge.get().getLabelValue("unit"));
//        assertEquals( "0x02", anyGauge.get().getLabelValue("common_list.#0.id"));
//        assertEquals( "C", anyGauge.get().getLabelValue("common_list.#0.unit"));
//        assertEquals( "ecowitt", anyGauge.get().getLabelValue("device"));
                
        
        List<MetricDefinition> metricDefs = result.topicCache().getTopicProcessor().getDefinedMetrics();
        assertEquals(1, metricDefs.size());
        
        MetricDefinition firstMetric = anyGauge.get().getMetricDefinition();
        assertEquals(2, firstMetric.getLabels().size());
    }
    
    private final String configWithSomeLabels = """
topics:
- topic: /get_livedata_info
  metrics:
  - valueField: common_list.*.val
    labels: [common_list.*.id, common_list.*.unit|unit]                                            
""";
    @Test
    public void testWithLabelSettings() throws IOException {
    
        Result result = TestHelper.buildAndProcessFirst(configWithSomeLabels, "/get_livedata_info", "src/test/resources/http/payloads/ecowitt.json");
        Optional<PrometheusCounterGauge> anyGauge = result.metrics().stream()
                .filter(gauge -> gauge.getName().endsWith("list.#0.val"))
                .findAny();
        assertTrue(anyGauge.isPresent());
//        System.out.println( anyGauge.get().toMetricString());

        assertEquals(anyGauge.get().getNumberOfLabels(), 2);
        assertEquals( "0x02", anyGauge.get().getLabelValue("common_list.#0.id"));
        assertEquals( "C", anyGauge.get().getLabelValue("unit"));
    }
    
    
    
    private final String configWithMapping = """
topics:
- topic: /get_livedata_info
  mappings:
    common_list.*.id:
      0x02:
        detailedName: temperature
        ignoreable: "not shown"
      0x01:
        detailedName: unknown
      0x07:
        detailedName: humidity
      0x15:
        detailedName: solar
                                                                                          
  metrics:
  - name: "weather_{detailedName}"
    valueField: common_list.*.val
    labels: [common_list.*.id|id, detailedName]                                            
""";
    @Test
    public void testInsertedMappings() throws IOException {
    
        Result result = TestHelper.buildAndProcessFirst(configWithMapping, "/get_livedata_info", "src/test/resources/http/payloads/ecowitt.json");
        Optional<PrometheusCounterGauge> anyGauge = result.metrics().stream()
                .filter(gauge -> gauge.getMetricDefinition().getFieldOfValue().getFieldName().getFullName().endsWith("*.val"))
                .findAny();
        assertTrue(anyGauge.isPresent());
        System.out.println( anyGauge.get().toMetricString());

        assertEquals(2, anyGauge.get().getNumberOfLabels());
        assertEquals("weather_temperature", anyGauge.get().getName());
        assertEquals( "0x02", anyGauge.get().getLabelValue("id"));
        assertEquals( "temperature", anyGauge.get().getLabelValue("detailedName"));
        assertNull(anyGauge.get().getLabelValue("ignoreable"));
    }
    
    private final String multiplus = """
topics:
- topic: /get_livedata_info
  mappings:
    value.*.name:
      "Pylontech battery":
        detailedName: Battery
      "MultiPlus-II 48/3000/35-32":
        detailedName: Multiplus
                                     
  metrics:
  - name: "soc"
    valueField: value.*.soc
    labels: [detailedName|name]                                            
    
""";
    @Test
    public void testHandleMultipleArrayItems() throws IOException {
    
        Result result = TestHelper.buildAndProcessFirst(multiplus, "/get_livedata_info", "src/test/resources/mqtt/payloads/multiplus-array.json");
        
        
        for( PrometheusCounterGauge m : result.metrics()) {
            System.out.println( m.toMetricString());
        }
//        Optional<PrometheusCounterGauge> anyGauge = result.metrics().stream()
//                .filter(gauge -> gauge.getMetricDefinition().getFieldOfValue().getFieldName().getFullName().endsWith("*.val"))
//                .findAny();
//        assertTrue(anyGauge.isPresent());
//        System.out.println( anyGauge.get().toMetricString());

    }
    
    
    
    
    
}
