package net.kiar.collectorr.connector.mqtt.mapping;

import net.kiar.collectorr.payloads.DataProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.kiar.collectorr.config.model.TopicConfig;
import net.kiar.collectorr.metrics.FieldDescription;
import net.kiar.collectorr.metrics.FieldValueMappings;
import net.kiar.collectorr.metrics.MetricDefinition;
import net.kiar.collectorr.metrics.PrometheusCounterGauge;
import net.kiar.collectorr.metrics.builder.TopicMetricsFactory;
import net.kiar.collectorr.payloads.PayloadDataNode;
import net.kiar.collectorr.payloads.PayloadResolver;
import net.kiar.collectorr.payloads.PayloadResolverFactory;
import net.kiar.collectorr.payloads.plain.TopicPathResolver;
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
    
    private boolean verbose = false;
    private boolean discovery = false;

    public TopicProcessor(TopicConfig topicConfig, TopicStructure topicStructure) {
        this.topicConfig = topicConfig;
        this.topicStructure = topicStructure;
        resolveBehaviourFlagsFromMode();
    }

    
    private void resolveBehaviourFlagsFromMode() {
        if ("discover".equalsIgnoreCase(topicConfig.getMode())) {
            verbose = true;
            discovery = true;
        } else if ("verbose".equalsIgnoreCase(topicConfig.getMode())) {
            verbose = true;
            discovery = false;
        }
    }
    /**
     * process the given payload and update all available metrics based on that data
     * 
     * @param messagePayload payload data as raw string (expected is json)
     * @param topic the full topic name
     * 
     * @return a list of metrics (including labels and samples)
     */
    public List<PrometheusCounterGauge> consumeMessage(String messagePayload, String topic) {
        PayloadResolver payloadResolver = PayloadResolverFactory.build(messagePayload);

        if (invalid) {
            log.debug("topic is marked as invalid, skip processing");
            return List.of();
        }
        
        if (definedMetrics.isEmpty()) {
            definedMetrics.addAll(
                TopicMetricsFactory.INSTANCE.buildMetric(payloadResolver, topic, topicConfig, topicStructure)
            );
            if (definedMetrics.isEmpty() && !discovery) {
                invalid = true;
            }
            if (verbose) {
                log.info("found {} metrics in payload.", definedMetrics.size());
            }
            if (!definedMetrics.isEmpty()) {
                log.info("\n use {} as source ", topic);
            }
        }
        
        TopicPathResolver pathResolver = new TopicPathResolver(topic, topicStructure);
        if (pathResolver.isExcluded()) {
            return List.of();
        }
        // consume values
        DataProvider.DataProviderFactory dataFactory = DataProvider.getFactory(payloadResolver, pathResolver);
        return definedMetrics.stream()
                .map( metric -> createValueEntriesForMetric(metric, dataFactory))
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
    private List<PrometheusCounterGauge> createValueEntriesForMetric(MetricDefinition metric, DataProvider.DataProviderFactory dataFactory) {
        if (!metric.isValid()) {
            return List.of();
        }
        
        return dataFactory.dataProvider(metric).stream()
                    .map(dataProvider -> createMetricEntryForValue(dataProvider))
                    .collect(Collectors.toList());
    }

    private PrometheusCounterGauge createMetricEntryForValue(DataProvider dataProvider) {
        // we support only 1 type of metrics...
        final MetricDefinition metric = dataProvider.getMetric();
        PrometheusCounterGauge gauge = new PrometheusCounterGauge(metric);
        gauge.setValue( dataProvider.getFieldOfValueData().value());
        gauge.updateMillisTimestamp();
//        System.out.println("Metric: " +dataProvider.getFieldOfValue().getName());
        
        if (metric.hasLabels()) {
            for(FieldDescription field : metric.getLabels()) {
                
                Optional<PayloadDataNode> input = dataProvider.getDataForField(field);
                if (input.isPresent()) {
                    String fieldValue = input.get().value();
                    if (field.isIncluded()) {
                        if (field.hasName()) {
                            gauge.addValueForLabel(field.getName(), fieldValue);
                        } else {
                            gauge.addValueForLabel(input.get().fieldName().getFullName(), fieldValue);
                        }
                    }
                    dataProvider.registerMappings( field.resolveMappingsForSource(fieldValue));
                }
            }
            
            // after regular label handling we process the mapping logic now
            // this allows overwriting or adding values
            for(FieldValueMappings.FieldMappingContent targetMapping : dataProvider.getUsedMappings()) {
                Optional<FieldDescription> targetField = dataProvider.getMetric().findLabel( targetMapping.targetFieldName());
                if (targetField.isPresent() && targetField.get().isIncluded()) {
                    gauge.overwriteValueForLabel(targetMapping.targetFieldName(), targetMapping.targetValue());
                }
            }
        }
        gauge.setName( metric.getName().getProcessed(dataProvider));
        gauge.buildSignature();
        return gauge;
    }

    public List<MetricDefinition> getDefinedMetrics() {
        return definedMetrics;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public boolean isDiscovery() {
        return discovery;
    }
    
}
