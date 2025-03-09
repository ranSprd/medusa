package net.kiar.collectorr.payloads.json;

import net.kiar.collectorr.payloads.plain.ResolvedData;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.kiar.collectorr.payloads.FieldName;
import net.kiar.collectorr.payloads.PayloadDataNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ranSprd
 */
public class JsonResolver extends ResolvedData {
    private static final Logger log = LoggerFactory.getLogger(JsonResolver.class);
    
    // @todo vielleicht die Factory verwenden?
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
    }

    private JsonResolver(List<PayloadDataNode> valueNodes, List<PayloadDataNode> labelNodes) {
        super(valueNodes, labelNodes);
    }

    
    
    /**
     * parse the given string payload and sort out values und labels
     * @param payload
     * @return 
     */
    public static Optional<JsonResolver> consume(String payload) {
        List<PayloadDataNode> values = new ArrayList<>();
        List<PayloadDataNode> labels = new ArrayList<>();
        if (payload != null && !payload.isBlank()) {
            try {
                JsonNode root = mapper.readTree( payload); 
                processNode(values, labels, "", root);
                return Optional.of(new JsonResolver(values, labels));
            } catch (Exception e) {
                log.debug("Processing as json payload failed {} \n {}", payload, e.getMessage());
            }
        }
        return Optional.empty();
    }

    private static void processNode(List<PayloadDataNode> values, List<PayloadDataNode> labels, String prefix, JsonNode node) {
        if (node == null) {
            return;
        }
        
        if (node.isNumber()) {
            values.add( new PayloadDataNode( new FieldName("value"), node.asText()));
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
            } else if (valueNode.isTextual() ) { // || valueNode.isBoolean()) {
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

    
}
