package net.kiar.collectorr.payloads.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.kiar.collectorr.metrics.FieldDescription;
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

    private final JsonNode root;
    private final List<PayloadDataNode> valueNodes;
    private final List<PayloadDataNode> labelNodes;
    
    
    private JsonResolver(JsonNode root) {
        this.root = root;
        this.valueNodes = new ArrayList<>();
        this.labelNodes = new ArrayList<>();
        
        processNode(valueNodes, labelNodes, "", root);
    }
    
    private static void processNode(List<PayloadDataNode> values, List<PayloadDataNode> labels, String prefix, JsonNode node) {
        if (node == null) {
            return;
        }
        Iterator<Map.Entry<String, JsonNode>> it = node.fields();
        while(it.hasNext()) {
            Map.Entry<String, JsonNode> n = it.next();
            JsonNode valueNode = n.getValue();
            String nodeName = prefix + n.getKey();
//                if (valueNode.isNumber() || valueNode.isBoolean()) {
            if (valueNode.isNumber()) {
                values.add( new PayloadDataNode(nodeName, valueNode.asText()));
            } else if (valueNode.isTextual() || valueNode.isBoolean()) {
                labels.add( new PayloadDataNode(nodeName, valueNode.asText()));
            } else if (valueNode.isObject()) {
                processNode(values, labels, nodeName +".", valueNode);
            }
        }
        
    }

    private Optional<PayloadDataNode> findNode(List<PayloadDataNode> nodes, String nodeName) {
        if (nodeName == null) {
            return Optional.empty();
        }
        return nodes.stream()
                .filter(node -> nodeName.equalsIgnoreCase(node.name()))
                .findAny();
    }
    
    public Optional<PayloadDataNode> findNode(String nodeName) {
        if (nodeName == null) {
            return Optional.empty();
        }
        return valueNodes.stream()
                .filter(node -> nodeName.equalsIgnoreCase(node.name()))
                .findAny()
                .or( () -> labelNodes.stream()
                        .filter(node -> nodeName.equalsIgnoreCase(node.name()))
                        .findAny()
                );
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

    @Override
    public List<PayloadDataNode> getValueNodes() {
        return valueNodes;
    }
    
    @Override
    public String getValueNamesAsString() {
        return valueNodes.stream().map(o -> o.name()).collect(Collectors.joining(","));        
    }
    
    @Override
    public Optional<PayloadDataNode> findValueNode(String nodeName) {
        return findNode(valueNodes, nodeName);
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
        return labelNodes.stream().map(o -> o.name()).collect(Collectors.joining(", "));        
    }
    
    @Override
    public Optional<PayloadDataNode> findLabelNode(FieldDescription fieldDescription) {
        return findNode( labelNodes, fieldDescription.getFieldName());
    }
    
}
