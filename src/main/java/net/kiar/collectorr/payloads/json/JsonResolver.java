package net.kiar.collectorr.payloads.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.kiar.collectorr.metrics.FieldDescription;
import net.kiar.collectorr.payloads.FieldName;
import net.kiar.collectorr.payloads.PayloadDataNode;
import net.kiar.collectorr.payloads.PayloadResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ranSprd
 */
public class JsonResolver implements PayloadResolver {
    private static final Logger log = LoggerFactory.getLogger(JsonResolver.class);
    
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
    }

//    private final JsonNode root;
    private final List<PayloadDataNode> valueNodes;
    private final List<PayloadDataNode> labelNodes;
    
    
    private JsonResolver(JsonNode root) {
//        this.root = root;
        this.valueNodes = new ArrayList<>();
        this.labelNodes = new ArrayList<>();
        
        processNode(valueNodes, labelNodes, "", root);
    }
    
    /**
     * parse the given string payload and sort out values und labels
     * @param payload
     * @return 
     */
    public static JsonResolver consume(String payload) {
        if (payload != null && !payload.isBlank()) {
            try {
                JsonNode root = mapper.readTree( payload); 
                return new JsonResolver(root);
            } catch (Exception e) {
                log.error("Processing payload failed {} \n {}", payload, e.getMessage());
            }
        }
        return new JsonResolver(null);
    }

    private static void processNode(List<PayloadDataNode> values, List<PayloadDataNode> labels, String prefix, JsonNode node) {
        if (node == null) {
            return;
        }
        Iterator<Map.Entry<String, JsonNode>> it = node.fields();
        while(it.hasNext()) {
            Map.Entry<String, JsonNode> n = it.next();
            JsonNode valueNode = n.getValue();
            String nodeNameStr = prefix + n.getKey();
//                if (valueNode.isNumber() || valueNode.isBoolean()) {
            if (valueNode.isNumber()) {
                values.add( new PayloadDataNode( new FieldName(nodeNameStr), valueNode.asText()));
            } else if (valueNode.isTextual() || valueNode.isBoolean()) {
                labels.add( new PayloadDataNode(new FieldName(nodeNameStr), valueNode.asText()));
            } else if (valueNode.isObject()) {
                processNode(values, labels, nodeNameStr +".", valueNode);
            } else if (valueNode.isArray()) {
                Iterator<JsonNode> nodeIterator = valueNode.elements();
                int counter = 0;
                while (nodeIterator.hasNext()) {
                    JsonNode next = nodeIterator.next();                
                    processNode(values, labels, nodeNameStr +".#" +counter +".", next);
                    counter++;
                }
            }
        }
        
    }

    @Override
    public Optional<PayloadDataNode> findNode(String nodeName) {
        if (nodeName == null) {
            return Optional.empty();
        }
        return valueNodes.stream()
                .filter(node -> nodeName.equalsIgnoreCase(node.fieldName().getFullName()))
                .findAny()
                .or( () -> labelNodes.stream()
                        .filter(node -> nodeName.equalsIgnoreCase(node.fieldName().getFullName()))
                        .findAny()
                );
    }
    
    @Override
    public List<PayloadDataNode> findNodes(FieldDescription valueNodeDesc) {
        String str = valueNodeDesc.getFieldName().getFullName().replaceAll("\\.\\*\\.", "\\\\.#[0-9]*\\\\.");
        Pattern pattern = Pattern.compile(str);
        
        List<PayloadDataNode> result = valueNodes.stream()
                .filter(node -> pattern.matcher( node.fieldName().getFullName()).matches())
                .map(node -> extractContent(node, valueNodeDesc))
                .collect(Collectors.toList());
        if (result.isEmpty()) {
            return labelNodes.stream()
                    .filter(node -> pattern.matcher( node.fieldName().getFullName()).matches())
                    .map(node -> extractContent(node, valueNodeDesc))
                    .collect(Collectors.toList());
        }
        return result;
    }
    
    private static PayloadDataNode extractContent(PayloadDataNode node, FieldDescription nodeDesc) {
        if (nodeDesc.getFieldIndex() >= 0) {
            return new PayloadDataNode(node.fieldName(), extractPart(node.value(), nodeDesc.getFieldIndex()));
        }
        return node;
    }
    
    
    
    @Override
    public List<PayloadDataNode> getValueNodes() {
        return valueNodes;
    }
    
    @Override
    public String getValueNamesAsString() {
        return valueNodes.stream().map(o -> o.fieldName().getFullName()).collect(Collectors.joining(","));        
    }
    
    

    @Override
    public List<PayloadDataNode> getLabelNodes() {
        return labelNodes;
    }
    
    /**
     * comma separated list of label-names
     * @return 
     */
    @Override
    public String getLabelNamesAsString() {
        return labelNodes.stream().map(o -> o.fieldName().getFullName()).collect(Collectors.joining(", "));        
    }
    
    public static String extractPart(String value, int index) {
        if (value == null || value.isBlank() || index < 0) {
            return "";
        }
        
        String[] parts = value.split(" ");
        if (parts.length > 0 && index < parts.length) {
            return parts[index];
        }
        return "";
    }
    
}
