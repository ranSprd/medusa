package net.kiar.collectorr.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ranSprd
 */
public class PrometheusGauge {
    private static final Logger log = LoggerFactory.getLogger(PrometheusGauge.class);

    private final MetricDefinition metricDefinition;
    
    private double value;
    private long millisTimestamp;
        
    private final List<LabelValue> labels = new ArrayList<>();
    
    private String signature;
    

    public PrometheusGauge(MetricDefinition def) {
        this.metricDefinition = def;
    }

    public MetricDefinition getMetricDefinition() {
        return metricDefinition;
    }
    

    public double getValue() {
        return value;
    }

    /**
     * set the value sample
     * @param value 
     */
    public void setValue(double value) {
        this.value = value;
    }
    
    /**
     * set the value
     * @param stringValue
     * @return false if the given string can't be parsed as a double
     */
    public boolean setValue(String stringValue) {
        try {
            setValue( Double.parseDouble(stringValue));
            return true;
        } catch (Exception e) {
            log.warn("Error parsing double value. Input is[{}]", stringValue, e);
        }
        return false;
    }

    public long getMillisTimestamp() {
        return millisTimestamp;
    }

    public void setMillisTimestamp(long millisTimestamp) {
        this.millisTimestamp = millisTimestamp;
    }
    
    /**
     * update the timestamp smaple value with the current system time
     */
    public void updateMillisTimestamp() {
        this.millisTimestamp = System.currentTimeMillis();
    }
    

    /**
     * Add the given data as key value pair for labels. If this method is called
     * multiple times the pairs will not be overwritten. 
     *
     * @param labelName
     * @param labelValue 
     */
    public void addValueForLabel(String labelName, String labelValue) {
        labels.add(new LabelValue(labelName, labelValue));
    }
    
    /**
     * Adds (if entry not exist) or overwrites (if the entry is not present) the
     * pair of label and value. 
     * 
     * @param labelName
     * @param labelValue 
     */
    public void overwriteValueForLabel(String labelName, String labelValue) {
        Optional<LabelValue> item = labels.stream()
                .filter(entry -> entry.key.equals(labelName))
                .findAny();
        if (item.isPresent()) {
            item.get().setValue(labelValue);
        } else {
            addValueForLabel(labelName, labelValue);
        }
                
    }

    public int getNumberOfLabels() {
        return labels.size();
    }
    
    public boolean hasLabels() {
        return !labels.isEmpty();
    }
    
    public String getLabelValue(String labelKeyName) {
        return labels.stream()
                .filter(lv -> lv.getKey().equals(labelKeyName))
                .findAny()
                .map(lv -> lv.getValue())
                .orElse(null);
    }
    
    public String getSignature() {
        return signature;
    }
    
    public void buildSignature() {
        StringBuilder b = new StringBuilder();
        b.append(metricDefinition.getName());
        for(LabelValue lv : labels) {
            b.append(lv.getKey()).append(lv.getValue());
        }
        signature = b.toString();
    }
    
    public String toMetricString() {
        StringBuilder builder = new StringBuilder();
        builder.append("# HELP ").append(metricDefinition.getName()).append(" ").append(metricDefinition.getDescription()).append('\n');
        builder.append("# TYPE ").append(metricDefinition.getName()).append(" gauge\n");
        builder.append(metricDefinition.getName());
        if (!labels.isEmpty()) {
            builder.append("{");
            boolean addComma = false;
            for(LabelValue labelValue : labels) {
                if (addComma) {
                    builder.append(",");
                }
                
                builder.append(metricDefinition.resolveLabelName(labelValue.key)).append("=\"").append(labelValue.value).append("\"");
                addComma = true;
            }
            builder.append("}");
        }
        
        // add the samples
        builder.append(" ").append(value).append(" ").append(System.currentTimeMillis()).append('\n');
        return builder.toString();
    }
    
    
    
    private static class LabelValue {
        private final String key;
        private String value;

        public LabelValue(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
