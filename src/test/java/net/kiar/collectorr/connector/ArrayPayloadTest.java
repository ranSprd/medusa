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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import net.kiar.collectorr.config.MappingsConfigLoader;
import net.kiar.collectorr.config.model.TopicConfig;
import net.kiar.collectorr.connector.mqtt.mapping.TopicCache;
import net.kiar.collectorr.metrics.MetricDefinition;
import net.kiar.collectorr.metrics.PrometheusCounterGauge;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author ranSprd
 */
public class ArrayPayloadTest {
    
    private final String configContentForTest1 = """
topics:
- topic: /get_livedata_info
  metrics:
  - valueField: common_list.*.val
      
""";
    

    @Test
    public void testMetricWithLabels() throws IOException {
        String topicPath = "/get_livedata_info";
//        MappingsConfigLoader conf = MappingsConfigLoader.readFromFile("src/test/resources/http/ecowitt-mappings.yaml");
        MappingsConfigLoader conf = MappingsConfigLoader.readContent(configContentForTest1);
        
        TopicConfig topicToTest = conf.getTopicsToObserve().get(0);
        TopicCache topicCache = TopicCache.buildTopicPattern(topicToTest).get();
        
        String message = Files.readString( Paths.get("src/test/resources/http/payloads/ecowitt.json"));
        
        List<PrometheusCounterGauge> result = topicCache.getTopicProcessor().consumeMessage(message, topicPath);
        assertNotNull(result);
        assertEquals(12, result.size());
        
        Optional<PrometheusCounterGauge> anyGauge = result.stream()
                .filter(gauge -> gauge.getName().endsWith("list.#0.val"))
                .findAny();
        assertTrue(anyGauge.isPresent());
        assertEquals(anyGauge.get().getNumberOfLabels(), 3);
        assertEquals( "0x02", anyGauge.get().getLabelValue("common_list.#0.id"));
        assertEquals( "C", anyGauge.get().getLabelValue("common_list.#0.unit"));
        assertEquals( "ecowitt", anyGauge.get().getLabelValue("device"));
                
        
        List<MetricDefinition> foundMetrics = topicCache.getTopicProcessor().getDefinedMetrics();
        assertEquals(1, foundMetrics.size());
        System.out.println( result.get(0).toMetricString());
        
        MetricDefinition metric = foundMetrics.get(0);
        assertEquals(11, metric.getLabels().size());
        
    }
    
    
}
