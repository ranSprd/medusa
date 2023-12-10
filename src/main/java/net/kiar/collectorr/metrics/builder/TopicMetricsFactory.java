package net.kiar.collectorr.metrics.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import net.kiar.collectorr.config.model.TopicConfig;
import net.kiar.collectorr.config.model.TopicConfigMappings;
import net.kiar.collectorr.config.model.TopicConfigMetric;
import net.kiar.collectorr.metrics.FieldDescription;
import net.kiar.collectorr.metrics.FieldType;
import net.kiar.collectorr.metrics.MetricDefinition;
import net.kiar.collectorr.metrics.MetricDefinitionBuilder;
import net.kiar.collectorr.payloads.PayloadResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build all metrics for a topic. There is a automatic mode. That means, if no configuration
 * for the topic is given it constructs a metric from the given payload
 * @author ranSprd
 */
public enum TopicMetricsFactory {
    
    INSTANCE;
    
    private static final Logger log = LoggerFactory.getLogger(TopicMetricsFactory.class);
    
    public List<MetricDefinition> buildMetric(PayloadResolver payloadResolver, String topic, TopicConfig topicConfig) {
        MetricNameBuilder nameBuilder = new MetricNameBuilder(topic);
        Map<String, FieldDescription> topicLabels = TopicPatternResolver.extractFieldsFromPattern(topicConfig.getPattern());
        
        // start AUTOMATIC construction of metrics based on the given nodes
        if (topicConfig.hasNoMetrics()) {
            return buildAutoMetric(payloadResolver, nameBuilder, topicLabels);
        } 
        
        List<MetricDefinition> result = new ArrayList<>();
        for(TopicConfigMetric configuredMetric : topicConfig.getMetrics()) {
            MetricDefinitionBuilder builder = MetricDefinitionBuilder
                    .metricForField( configuredMetric.getValueField())
                    .name(nameBuilder.getName(configuredMetric.getName(), configuredMetric.getValueField()));

            if (configuredMetric.hasConfiguredLabels()) {
                configuredMetric.getLabels().stream()
                        .map(raw -> toFieldDescription(raw, topicLabels))
                        .forEach(fieldDesc -> builder.insertLabel(fieldDesc));
            } else {
                // put all known labels from topic, because other labels are not present
                addAutoLabels(topicLabels, builder, payloadResolver);
            }
            
            // insert mappings - overwrite content or set new labels
            addMappings(topicConfig, builder);
            
            
            if (configuredMetric.getDescription() == null || configuredMetric.getDescription().isBlank()) {
                builder.description("from " +topicConfig.getTopic());
            } else {
                builder.description( configuredMetric.getDescription());
            }

            result.add( builder.get());
        }
        return result;
    }
    
    
    private void addMappings(TopicConfig topicConfig, MetricDefinitionBuilder builder) {
        
        if (!topicConfig.hasMappings()) {
            return;
        }
        
        for(TopicConfigMappings mapping : topicConfig.getMappings()) {
            builder.insertMappings(mapping.getSource(), mapping.getTarget(), mapping.getMap());
        }
        
    }
        
    private List<MetricDefinition> buildAutoMetric(PayloadResolver payloadResolver, MetricNameBuilder nameBuilder, Map<String, FieldDescription>  topicLabels) {
        if (payloadResolver.getValueNodes().isEmpty()) {
            log.warn("can't extract an payload. No value field found.");
            return List.of();
        } 
            // construct metrics (definition)
        return payloadResolver.getValueNodes().stream()
                    .map(valueNode -> constructMetricForValue(valueNode.name(), nameBuilder, payloadResolver, topicLabels))
                    .collect(Collectors.toList());
    }
    
    private MetricDefinition constructMetricForValue(String valueFieldName, MetricNameBuilder nameBuilder, PayloadResolver payloadResolver, Map<String, FieldDescription>  topicLabels) {
        MetricDefinitionBuilder builder = MetricDefinitionBuilder
                .metricForField( valueFieldName)
                .name(nameBuilder.getNameFromField(valueFieldName));
        
        addAutoLabels(topicLabels, builder, payloadResolver);
        
        return builder.get();
    }

    private void addAutoLabels(Map<String, FieldDescription>  topicLabels, MetricDefinitionBuilder builder, PayloadResolver payloadResolver) {
        topicLabels.values().stream()
                .forEach(labelNode -> builder.topicLabel(labelNode));
        if (payloadResolver != null) {
            payloadResolver.getLabelNodes().stream()
                    .forEach(labelNode -> builder.label(labelNode.name()));
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
    
    
    // kann vielleicht in den MetricDefinitionbuilder...
    public static class MetricNameBuilder {

        private final String namePrefix;
        public MetricNameBuilder(String topic) {
            if (topic == null || topic.isBlank()) {
                namePrefix = UUID.randomUUID().toString();
            } else {
                namePrefix = topic.replaceAll("/", "_");
            }
        }
        
        public String getNameFromField(String fieldName) {
            return getName(null, fieldName);
        }
        
        public String getName(String preferedName, String fieldName) {
            if (preferedName == null || preferedName.isBlank()) {
                if (fieldName == null || fieldName.isBlank()) {
                    return namePrefix +"_" +UUID.randomUUID().toString();
                }
                return namePrefix +"_" +fieldName;
            }
            return preferedName;
        }
    }
    
}
