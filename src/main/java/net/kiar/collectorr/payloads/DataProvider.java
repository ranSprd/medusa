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
package net.kiar.collectorr.payloads;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.kiar.collectorr.metrics.BuildInLabels;
import net.kiar.collectorr.metrics.FieldDescription;
import static net.kiar.collectorr.metrics.FieldType.PAYLOAD;
import static net.kiar.collectorr.metrics.FieldType.TOPIC;
import net.kiar.collectorr.metrics.MetricDefinition;
import net.kiar.collectorr.payloads.json.TopicPathResolver;

/**
 *
 * @author ranSprd
 */
public class DataProvider {
    
    public static class DataProviderFactory {

        private final PayloadResolver payloadResolver;
        private final TopicPathResolver topicResolver;

        private DataProviderFactory(PayloadResolver payloadResolver, TopicPathResolver topicResolver) {
            this.payloadResolver = payloadResolver;
            this.topicResolver = topicResolver;
        }


        public List<DataProvider> dataProvider(MetricDefinition metric) {
            
            return payloadResolver.findNodes(metric.getFieldOfValue()).stream()
                        .map(valueInPayload -> new DataProvider(
                                        valueInPayload,
                                                    payloadResolver, 
                                                    topicResolver,
                                                    metric))
                        .collect(Collectors.toList());
        }

    }
    
    public static DataProviderFactory getFactory(PayloadResolver payloadResolver, TopicPathResolver topicResolver) {
        return new DataProviderFactory(payloadResolver, topicResolver);
    }
    

    private final PayloadDataNode fieldOfValueData;
    private final PayloadResolver payloadResolver;
    private final TopicPathResolver topicResolver;
    private final BuildInLabels buildInLabels;
    private final MetricDefinition metric;
    
    private final List<FieldDescription.FieldMappingValue> usedMappings = new ArrayList<>();


    private DataProvider(PayloadDataNode valueFieldData, PayloadResolver payloadResolver, TopicPathResolver topicResolver, MetricDefinition metric) {
        this.fieldOfValueData = valueFieldData;
        this.payloadResolver = payloadResolver;
        this.topicResolver = topicResolver;
        this.buildInLabels = BuildInLabels.getBuildInData(valueFieldData, topicResolver.getTopicPath());
        this.metric = metric;
    }

    /** value for metric = source field */
    public PayloadDataNode getFieldOfValueData() {
        return fieldOfValueData;
    }

    public MetricDefinition getMetric() {
        return metric;
    }

    public FieldDescription getFieldOfValue() {
        return metric.getFieldOfValue();
    }
    
    public void registerMapping(FieldDescription.FieldMappingValue mappingValue) {
        usedMappings.add(mappingValue);
    }

    public List<FieldDescription.FieldMappingValue> getUsedMappings() {
        return usedMappings;
    }

    public Optional<PayloadDataNode> getData(FieldDescription field) {
        
        // some fields define fixed content
        if (field.hasFixedContent()) {
            return Optional.of( new PayloadDataNode(field.getFieldName(), field.getName(), field.getFixedContent()));
        }
        
        Optional<PayloadDataNode> result = Optional.empty();
        switch (field.getType()) {
            case PAYLOAD -> 
                result = filter(payloadResolver.findNodes(field));
            case TOPIC ->
                result = filter(topicResolver.findNodes(field));
        }

        return result;
    }
    
    public String resolve(String fieldName, String fallbackValue) {
        Optional<String> mappedValue = usedMappings.stream()
                .filter(mapping -> mapping.targetFieldName().equalsIgnoreCase(fieldName))
                .map(mapping -> mapping.targetValue())
                .findAny();
        if (mappedValue.isPresent()) {
            return mappedValue.get();
        }
        
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
    
    private Optional<PayloadDataNode> filter(List<PayloadDataNode> payloadNodes) {
        if (payloadNodes.isEmpty()) {
            return Optional.empty();
//        } else if (payloadNodes.size() == 1) {
//            PayloadDataNode singleNode = payloadNodes.get(0);
////            if (singleNode.name().)
//            return Optional.of( payloadNodes.get(0));
        }
        
        String prefix = this.fieldOfValueData.getFieldName().getPrefix();
        return payloadNodes.stream()
                // either the field is part of an array, then the prefix should match or the field is not an array, then it should be used
                .filter(node -> (node.getFieldName().getPrefix().equalsIgnoreCase( prefix)) || !node.getFieldName().isArrayItem())
                .findAny();

//        return Optional.empty();
        
    }

}
