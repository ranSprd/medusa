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
import net.kiar.collectorr.metrics.BuildInLabels;
import net.kiar.collectorr.metrics.FieldDescription;
import static net.kiar.collectorr.metrics.FieldType.PAYLOAD;
import static net.kiar.collectorr.metrics.FieldType.TOPIC;
import net.kiar.collectorr.payloads.PayloadDataNode;
import net.kiar.collectorr.payloads.PayloadResolver;
import net.kiar.collectorr.payloads.json.TopicPathResolver;

/**
 *
 * @author ranSprd
 */
public class DataProvider {

    private final PayloadResolver payloadResolver;
    private final TopicPathResolver topicResolver;
    private final BuildInLabels buildInLabels;

    public DataProvider(PayloadResolver payloadResolver, TopicPathResolver topicResolver, BuildInLabels buildInLabels) {
        this.payloadResolver = payloadResolver;
        this.topicResolver = topicResolver;
        this.buildInLabels = buildInLabels;
    }

    
    public Optional<PayloadDataNode> getData(FieldDescription field) {
        
        // some fields define fixed content
        if (field.hasFixedContent()) {
            return Optional.of( new PayloadDataNode(field.getName(), field.getFixedContent()));
        }
        
        Optional<PayloadDataNode> result = Optional.empty();
        switch (field.getType()) {
            case PAYLOAD ->
                result = payloadResolver.findLabelNode(field);
            case TOPIC ->
                result = topicResolver.findLabelNode(field);
        }

        return result;
    }
    
    public String resolve(String fieldName, String fallbackValue) {
        Optional<PayloadDataNode> found = payloadResolver.findNode(fieldName);
        if (found.isPresent()) {
            return found.get().value();
        }
        found = topicResolver.findNode(fieldName);
        if (found.isPresent()) {
            return found.get().value();
        }
        return buildInLabels.find(fieldName, fallbackValue);
    }

}
