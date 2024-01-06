package net.kiar.collectorr.payloads;

/**
 *
 * @author ranSprd
 */
public class PayloadDataNode {
    private final FieldName fieldName;
    private final String value;
    
    private String name;

    public PayloadDataNode(FieldName fieldName, String value) {
        this(fieldName, null, value);
    }

    public PayloadDataNode(FieldName fieldName, String name, String value) {
        this.fieldName = fieldName;
        this.value = value;
        this.name = name;
    }
    
    

    public FieldName getFieldName() {
        return fieldName;
    }

    public String value() {
        return value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
