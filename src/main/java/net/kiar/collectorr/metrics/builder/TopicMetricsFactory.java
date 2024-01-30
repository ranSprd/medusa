package net.kiar.collectorr.metrics.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import net.kiar.collectorr.config.model.TopicConfig;
import net.kiar.collectorr.config.model.TopicConfigMetric;
import net.kiar.collectorr.connector.mqtt.mapping.TopicStructure;
import net.kiar.collectorr.metrics.BuildInLabels;
import net.kiar.collectorr.metrics.FieldDescription;
import net.kiar.collectorr.metrics.MetricDefinition;
import net.kiar.collectorr.metrics.MetricDefinitionBuilder;
import net.kiar.collectorr.metrics.PlaceholderString;
import net.kiar.collectorr.payloads.FieldName;
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
        for(TopicConfigMetric givenMetricConfig : topicConfig.getMetrics()) {
            MetricDefinitionBuilder builder = MetricDefinitionBuilder
                    .metricFromFieldDescriptor(givenMetricConfig.getValueFieldName())
                    .name(nameBuilder.getName(givenMetricConfig.getName()))
                    .metricType(givenMetricConfig.getMetricType());

            labelBuilder.addLabelsToMetric(givenMetricConfig, builder, payloadResolver);
            
            // insert mappings - overwrite content or set new labels
            addMappings(topicConfig, builder, labelBuilder);
            
            
            if (givenMetricConfig.getDescription() == null || givenMetricConfig.getDescription().isBlank()) {
                builder.description("from " +topicConfig.getTopic());
            } else {
                builder.description( givenMetricConfig.getDescription());
            }

            result.add( builder.get());
        }
        return result;
    }
    
    private void addMappings(TopicConfig topicConfig, MetricDefinitionBuilder builder, LabelBuilder labelBuilder) {
        if (topicConfig.hasValueMappings()) {
            builder.insertMappings( topicConfig.getValueMappings(), (x) -> labelBuilder.sourceField(x));
        }
    }
        
    private List<MetricDefinition> buildAutoMetric(PayloadResolver payloadResolver, MetricNameBuilder nameBuilder, LabelBuilder labelBuilder) {
        if (payloadResolver.getValueNodes().isEmpty()) {
            log.warn("can't extract an payload. No value field found.");
            return List.of();
        } 
        // construct metrics (definition)
        return payloadResolver.getValueNodes().stream()
                    .map(valueNode -> constructDefaultMetricForValueField(valueNode.fieldName(), nameBuilder, payloadResolver, labelBuilder))
                    .collect(Collectors.toList());
    }
    
    private MetricDefinition constructDefaultMetricForValueField(FieldName valueFieldName, MetricNameBuilder nameBuilder, PayloadResolver payloadResolver, LabelBuilder labelBuilder) {
        MetricDefinitionBuilder builder = MetricDefinitionBuilder
                .metricForField( valueFieldName.getFullName())
                .name(nameBuilder.getDefaultName());
        
        labelBuilder.addAutoLabelsFromPayload(builder, payloadResolver);
        
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
        
        public PlaceholderString getDefaultName() {
            return new PlaceholderString(namePrefix +"_{" +BuildInLabels.VALUE_FIELD_NAME +"}");
        }
        
        /**
         * create a name based on the given preferedName. If that given name is
         * empty, then a default pattern for the name is used.
         * 
         * @param preferedName
         * @return 
         */
        public PlaceholderString getName(String preferedName) {
            if (preferedName == null || preferedName.isBlank()) {
                return getDefaultName();
            }
            return new PlaceholderString(preferedName);
        }
        
        
    }
    
}
