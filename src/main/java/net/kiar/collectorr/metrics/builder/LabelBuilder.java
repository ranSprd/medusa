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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.kiar.collectorr.config.model.TopicConfig;
import net.kiar.collectorr.config.model.TopicConfigMetric;
import net.kiar.collectorr.metrics.FieldDescription;
import net.kiar.collectorr.metrics.FieldType;
import net.kiar.collectorr.metrics.MetricDefinitionBuilder;
import net.kiar.collectorr.payloads.PayloadResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ranSprd
 */
public class LabelBuilder {
    private static final Logger log = LoggerFactory.getLogger(LabelBuilder.class);

    private final Map<String, FieldDescription> topicLabels;
    private final TopicConfig topicConfig;

    public LabelBuilder(Map<String, FieldDescription> topicLabels, TopicConfig topicConfig) {
        this.topicLabels = topicLabels;
        this.topicConfig = topicConfig;
    }
    
    
    public void addAutoLabels(MetricDefinitionBuilder builder, PayloadResolver payloadResolver) {
        
        // 1st fallback is looking into the topic wide label settings and use these
        if (topicConfig.hasConfiguredLabels()) {
            topicConfig.getLabels().stream()
                    .map(labelName -> toFieldDescription(labelName, topicLabels))
                    .filter(Objects::nonNull)
                    .forEach(fieldDesc -> builder.insertLabel(fieldDesc));
        } else {
            // no global label settings in topic config, try to generate 
            topicLabels.values().stream()
                    .forEach(labelNode -> builder.topicLabel(labelNode));
            if (payloadResolver != null) {
                payloadResolver.getLabelNodes().stream()
                        .forEach(labelNode -> builder.label(labelNode.name()));
            }
        }

    }
    
    public void addLabels(TopicConfigMetric configuredMetric, MetricDefinitionBuilder builder, PayloadResolver payloadResolver) {
            if (configuredMetric.hasConfiguredLabels()) {
                configuredMetric.getLabels().stream()
                        .map(labelName -> toFieldDescription(labelName, topicLabels))
                        .filter(Objects::nonNull)
                        .forEach(fieldDesc -> builder.insertLabel(fieldDesc));
            } else if (topicConfig.hasConfiguredLabels()) {
                // 1st fallback - use global label settings from topic 
                topicConfig.getLabels().stream()
                        .map(labelName -> toFieldDescription(labelName, topicLabels))
                        .filter(Objects::nonNull)
                        .forEach(fieldDesc -> builder.insertLabel(fieldDesc));
            } else {
                // put all known labels from topic, because other labels are not present
                addAutoLabels(builder, payloadResolver);
            }
        
    }
    
    /**
     * build a TOPIC field description - based on the exist configuration. 
     * This code search the field in config and creates a TOPIC field descriptor
     * @param fieldName
     * @param topicLabels
     * @return 
     */
    private FieldDescription toFieldDescription(String fieldName, Map<String, FieldDescription> topicLabels) {
        Optional<FieldDescription> parsed = FieldDescription.parseFieldDescriptor(fieldName);
        if (parsed.isPresent()) {
            // fix the label type if the name is present in the topic list
            FieldDescription result = parsed.get();
            FieldDescription topicLabelDef = topicLabels.get(result.getFieldName());
            if (topicLabelDef != null) {
                result.setType(FieldType.TOPIC);
                result.setFieldIndex( topicLabelDef.getFieldIndex());
            } else {
                result.setType(FieldType.PAYLOAD);
            }
            return result;
        }
        return null;
    }
    
    
    
    
}
