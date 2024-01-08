package net.kiar.collectorr.payloads;

/**
 *
 * @author ranSprd
 */
public class PayloadDataNode {
    private final FieldName fieldName;
    private final String value;
    
    public PayloadDataNode(FieldName fieldName, String value) {
        this.fieldName = fieldName;
        this.value = value;
    }

    public FieldName fieldName() {
        return fieldName;
    }

    public String value() {
        return value;
    }

}
