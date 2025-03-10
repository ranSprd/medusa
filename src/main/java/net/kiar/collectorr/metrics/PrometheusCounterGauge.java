package net.kiar.collectorr.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.kiar.collectorr.metrics.prometheus.PrometheusDataModelValidator;
import net.kiar.collectorr.payloads.DoubleParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ranSprd
 */
public class PrometheusCounterGauge {
    private static final Logger log = LoggerFactory.getLogger(PrometheusCounterGauge.class);

    private final MetricDefinition metricDefinition;
    
    private double value;
    private long millisTimestamp;
    
    private String name;
        
    private final List<LabelValue> labels = new ArrayList<>();
    
    private String signature;
    private MetricType metricType = MetricType.GAUGE;
    

    public PrometheusCounterGauge(MetricDefinition def) {
        this.metricDefinition = def;
        this.name = def.getName().getProcessed();
        setMetricType(def.getMetricType());
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

    public MetricType getMetricType() {
        return metricType;
    }

    public final void setMetricType(MetricType metricType) {
        if (metricType != null) {
            this.metricType = metricType;
        }
    }
    
    /**
     * Prüft die Metrik und gibt true zurück, wenn diese reportfähig, d.h. 
     * von Prometheus konsumiert werden kann.
     * Es gibt folgende Möglichkeiten:
     * - Metrik wurde "in letzter Zeit" geupdated, happy path, dann ist die Rückgabe true
     * - Metrik wurde lange nicht geändert:
     * -- entweder die Methode liefert false zurück, damit gibt es keine Daten für Prometheus
     * -- oder die Methode liefert true aber der Wert der Metrik wird z.B. auf 0 gesetzt
     * @return 
     */
    public boolean validateAndAdjust() {
        long notUpdatedMinutes = (System.currentTimeMillis() - millisTimestamp) / 60000;
        if (notUpdatedMinutes > 15) {
            return false;
        }
        return true;        
    }
    
    /**
     * set the value. The logic tries to get the first numerical value from that string
     * @param stringValue
     */
    public void setValue(String stringValue) {
        setValue( DoubleParser.parse(stringValue));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Metric names may contain ASCII letters, digits, underscores, and colons. It must match the regex [a-zA-Z_:][a-zA-Z0-9_:]*.
     * @param input  
     * @return 
     */
    public static String getValidMetricName(String input) {
        return  PrometheusDataModelValidator.fixMetricName(input);
    }
    /**
     * Labels may contain ASCII letters, numbers, as well as underscores. They must match the regex [a-zA-Z_][a-zA-Z0-9_]*
     * @param input
     * @return 
     */
    public static String getValidLabelName(String input) {
        return  PrometheusDataModelValidator.fixLabelName(input);
    }

    public long getMillisTimestamp() {
        return millisTimestamp;
    }

    public void setMillisTimestamp(long millisTimestamp) {
        this.millisTimestamp = millisTimestamp;
    }
    
    /**
     * update the timestamp sample value with the current system time
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
        b.append(name);
        for(LabelValue lv : labels) {
            b.append(lv.getKey()).append(lv.getValue());
        }
        signature = b.toString();
    }
    
    public String toMetricString() {
        return toMetricString(true);
    }
    
    public String toMetricString(boolean withHeader) {
        StringBuilder builder = new StringBuilder();
        String validName = getValidMetricName(name);
        if (withHeader) {
            builder.append("# HELP ").append(validName).append(" ").append(metricDefinition.getDescription()).append('\n');
            builder.append("# TYPE ").append(validName).append(" ").append(metricType.getName()).append("\n");
        }
        builder.append(validName);
        if (!labels.isEmpty()) {
            builder.append("{");
            boolean addComma = false;
            Collections.sort(labels, (a, b) -> a.key.compareTo(b.key));
            for(LabelValue labelValue : labels) {
                if (addComma) {
                    builder.append(",");
                }
                
                builder.append(getValidLabelName(metricDefinition.resolveLabelName(labelValue.key)))
                       .append("=\"").append(labelValue.value).append("\"");
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
            this.value = PrometheusDataModelValidator.fixLabelValue(value);
        }
    }
}
