package net.kiar.collectorr.connector.mqtt.mapping;

import net.kiar.collectorr.payloads.DataProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.kiar.collectorr.config.model.TopicConfig;
import net.kiar.collectorr.metrics.FieldDescription;
import net.kiar.collectorr.metrics.MetricDefinition;
import net.kiar.collectorr.metrics.PrometheusGauge;
import net.kiar.collectorr.metrics.builder.TopicMetricsFactory;
import net.kiar.collectorr.payloads.PayloadDataNode;
import net.kiar.collectorr.payloads.PayloadResolver;
import net.kiar.collectorr.payloads.json.JsonResolver;
import net.kiar.collectorr.payloads.json.TopicPathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ranSprd
 */
public class TopicProcessor {

    private static final Logger log = LoggerFactory.getLogger(TopicProcessor.class);

    private final TopicConfig topicConfig;
    private final TopicStructure topicStructure;
    private final List<MetricDefinition> definedMetrics = new ArrayList<>();
    
    private boolean invalid = false;

    public TopicProcessor(TopicConfig topicConfig, TopicStructure topicStructure) {
        this.topicConfig = topicConfig;
        this.topicStructure = topicStructure;
    }

    /**
     * process the given payload and update all available metrics based on that data
     * 
     * @param messagePayload payload data as raw string (expected is json)
     * @param topic the full topic name
     * 
     * @return a list of metrics (including labels and samples)
     */
    public List<PrometheusGauge> consumeMessage(String messagePayload, String topic) {
        PayloadResolver payloadResolver = JsonResolver.consume(messagePayload);

        if (invalid) {
            log.debug("topic is marked as invalid, skip processing");
            return List.of();
        }
        
        if (definedMetrics.isEmpty()) {
            definedMetrics.addAll(
                TopicMetricsFactory.INSTANCE.buildMetric(payloadResolver, topic, topicConfig, topicStructure)
            );
            if (definedMetrics.isEmpty()) {
                invalid = true;
            }
        }
        
        // consume values
        return definedMetrics.stream()
                .map( metric -> createValueEntriesForMetric(metric, topic, payloadResolver))
                .flatMap(list -> list.stream())
                .collect(Collectors.toList());
    }

    /**
     * Enrich given metric with data (value & labels).
     * 
     * @param metric
     * @param topic
     * @param payloadResolver
     * @return 
     */
    private List<PrometheusGauge> createValueEntriesForMetric(MetricDefinition metric, String topic, PayloadResolver payloadResolver) {
        if (!metric.isValid()) {
            return List.of();
        }
        Optional<PayloadDataNode> valueField = payloadResolver.findNode(metric.getFieldOfValue());
        if (valueField.isEmpty()) {
            return List.of();
        }
        DataProvider.DataProviderFactory dataFactory = DataProvider.getFactory(payloadResolver, new TopicPathResolver(topic, topicStructure));
        
        // we support only 1 type of metrics...
        PrometheusGauge gauge = new PrometheusGauge(metric);
        gauge.setValue( valueField.get().value());
        gauge.updateMillisTimestamp();
        
        DataProvider dataProvider = dataFactory.dataProvider(valueField.get());
        if (metric.hasLabels()) {
            List<FieldDescription.FieldMappingValue> foundMappings = new ArrayList<>();
            for(FieldDescription field : metric.getLabels()) {
                
                Optional<PayloadDataNode> input = dataProvider.getData(field);
                if (input.isPresent()) {
                    String fieldValue = input.get().value();
                    gauge.addValueForLabel(field.getName(), fieldValue);
                    field.resolveMapping(fieldValue)
                            .ifPresent(target -> foundMappings.add(target));
                }
            }
            
            // after regular label handling we process the mapping logic now
            // this allows overwriting or adding values
            for(FieldDescription.FieldMappingValue targetMapping : foundMappings) {
                gauge.overwriteValueForLabel(targetMapping.targetFieldName(), targetMapping.targetValue());
            }
            
        }
        gauge.setName( metric.getName().getProcessed(dataProvider));
        gauge.buildSignature();
        
        return List.of(gauge);
    }

    public List<MetricDefinition> getDefinedMetrics() {
        return definedMetrics;
    }

}
