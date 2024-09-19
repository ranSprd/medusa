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
package net.kiar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import net.kiar.collectorr.config.MappingsConfigLoader;
import net.kiar.collectorr.config.model.TopicConfig;
import net.kiar.collectorr.connector.mqtt.mapping.TopicCache;
import net.kiar.collectorr.metrics.PrometheusCounterGauge;

/**
 *
 * @author ranSprd
 */
public class TestHelper {
    
    public record Result(List<PrometheusCounterGauge> metrics, TopicCache topicCache) {};
    
    /**
     * take the first definiton from config, generate a metric description and process with payload
     * 
     * @param mappingConfiguration
     * @param topicPath
     * @param payloadfile
     * @return
     * @throws IOException 
     */
    public static Result buildAndProcessFirstFromFile(String mappingConfiguration, String topicPath, String payloadfile) throws IOException {

        String message = Files.readString( Paths.get( payloadfile));
        return buildAndProcessFirstWithPayload(mappingConfiguration, topicPath, message);
    }
    
    public static Result buildAndProcessFirstWithPayload(String mappingConfiguration, String topicPath, String payload) throws IOException {
        MappingsConfigLoader conf = MappingsConfigLoader.readContent(mappingConfiguration);
        
        TopicConfig topicToTest = conf.getTopicsToObserve().get(0);
        TopicCache topicCache = TopicCache.buildTopicPattern(topicToTest).get();
        
        List<PrometheusCounterGauge> result = topicCache.getTopicProcessor().consumeMessage(payload, topicPath);
        return new Result(result, topicCache);
    }
    
    
}
