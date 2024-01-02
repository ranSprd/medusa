package net.kiar.collectorr.metrics.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import net.kiar.collectorr.config.model.TopicConfig;
import net.kiar.collectorr.config.model.TopicConfigMappings;
import net.kiar.collectorr.config.model.TopicConfigMetric;
import net.kiar.collectorr.connector.mqtt.mapping.TopicStructure;
import net.kiar.collectorr.metrics.FieldDescription;
import net.kiar.collectorr.metrics.MetricDefinition;
import net.kiar.collectorr.metrics.MetricDefinitionBuilder;
import net.kiar.collectorr.metrics.PlaceholderString;
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
    
    public List<MetricDefinition> buildMetric(PayloadResolver payloadResolver, String topic, TopicConfig topicConfig, TopicStructure topicStructure) {
        MetricNameBuilder nameBuilder = new MetricNameBuilder(topic);
        Map<String, FieldDescription> detectedLabelsInTopic = topicStructure.getFieldDescriptions();
        
        LabelBuilder labelBuilder = new LabelBuilder(detectedLabelsInTopic, topicConfig);
        
        // start AUTOMATIC construction of metrics based on the given nodes
        if (topicConfig.hasNoMetrics()) {
            return buildAutoMetric(payloadResolver, nameBuilder, labelBuilder);
        } 
        
        List<MetricDefinition> result = new ArrayList<>();
        for(TopicConfigMetric configuredMetric : topicConfig.getMetrics()) {
            MetricDefinitionBuilder builder = MetricDefinitionBuilder
                    .metricFromFieldDescriptor(configuredMetric.getValueField())
                    .name(nameBuilder.getName(configuredMetric.getName(), configuredMetric.getValueField()));

            labelBuilder.addLabels(configuredMetric, builder, payloadResolver);
            
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
        
    private List<MetricDefinition> buildAutoMetric(PayloadResolver payloadResolver, MetricNameBuilder nameBuilder, LabelBuilder labelBuilder) {
        if (payloadResolver.getValueNodes().isEmpty()) {
            log.warn("can't extract an payload. No value field found.");
            return List.of();
        } 
            // construct metrics (definition)
        return payloadResolver.getValueNodes().stream()
                    .map(valueNode -> constructDefaultMetricForValueField(valueNode.name(), nameBuilder, payloadResolver, labelBuilder))
                    .collect(Collectors.toList());
    }
    
    private MetricDefinition constructDefaultMetricForValueField(String valueFieldName, MetricNameBuilder nameBuilder, PayloadResolver payloadResolver, LabelBuilder labelBuilder) {
        MetricDefinitionBuilder builder = MetricDefinitionBuilder
                .metricForField( valueFieldName)
                .name(nameBuilder.getNameFromField(valueFieldName));
        
        labelBuilder.addAutoLabels(builder, payloadResolver);
        
        return builder.get();
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
        
        public PlaceholderString getNameFromField(String fieldName) {
            return getName(null, fieldName);
        }
        
        public PlaceholderString getName(String preferedName, String fieldName) {
            if (preferedName == null || preferedName.isBlank()) {
                if (fieldName == null || fieldName.isBlank()) {
                    return new PlaceholderString(namePrefix +"_" +UUID.randomUUID().toString());
                }
                return new PlaceholderString(namePrefix +"_" +fieldName.replaceAll("\\.", "_"));
            }
            return new PlaceholderString(preferedName);
        }
        
        
    }
    
}
