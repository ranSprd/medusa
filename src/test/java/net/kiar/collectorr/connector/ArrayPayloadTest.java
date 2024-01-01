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
import net.kiar.collectorr.config.MappingsConfigLoader;
import net.kiar.collectorr.config.model.TopicConfig;
import net.kiar.collectorr.connector.mqtt.mapping.TopicCache;
import net.kiar.collectorr.metrics.PrometheusGauge;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author ranSprd
 */
public class ArrayPayloadTest {
    
    private final String configContentForTest1 = """
topics:
- topic: /get_livedata_info
  metrics:
  - valueField: common_list.val
      
""";
    

//    @Test
    public void testMetricWithLabels() throws IOException {
        String topicPath = "/get_livedata_info";
//        MappingsConfigLoader conf = MappingsConfigLoader.readFromFile("src/test/resources/http/ecowitt-mappings.yaml");
        MappingsConfigLoader conf = MappingsConfigLoader.readContent(configContentForTest1);
        
        TopicConfig topicToTest = conf.getTopicsToObserve().get(0);
        TopicCache topicCache = TopicCache.buildTopicPattern(topicToTest).get();
        
        String message = Files.readString( Paths.get("src/test/resources/http/payloads/ecowitt.json"));
        
        List<PrometheusGauge> result = topicCache.getTopicProcessor().consumeMessage(message, topicPath);
        
        System.out.println("metrics generated " +result.size());
        assertNotNull(result);
        
    }
    
    
}
