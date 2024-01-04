package net.kiar.collectorr.payloads.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.kiar.collectorr.connector.mqtt.mapping.TopicStructure;
import net.kiar.collectorr.metrics.FieldDescription;
import net.kiar.collectorr.payloads.PayloadDataNode;
import net.kiar.collectorr.payloads.PayloadResolver;

/**
 *
 * @author ranSprd
 */
public class TopicPathResolver implements PayloadResolver {

    private final String topicPath;
    private final List<PayloadDataNode> data = new ArrayList<>();

    public TopicPathResolver(String topicPath, TopicStructure topicStructure) {
        this.topicPath = topicPath;
        if (topicPath != null) {
            int t = 0;
            String parts[] = topicPath.split("/");
            for(String p : parts) {
                if (p != null) {
                    String fieldName = topicStructure.getFieldNameOfSegment(t, true);
                    data.add(new PayloadDataNode(fieldName, p));
                }
                t++;
            }
        }
    }

    public String getTopicPath() {
        return topicPath;
    }

    @Override
    public List<PayloadDataNode> getValueNodes() {
        return List.of();
    }

    @Override
    public String getValueNamesAsString() {
        // a topic should not contain any value for a metric....
        return "";
    }

    public List<PayloadDataNode> getLabelNodes() {
        return List.of();
    }

    @Override
    public String getLabelNamesAsString() {
        return data.stream().map(o -> o.name()).collect(Collectors.joining(","));        
    }

    @Override
    public Optional<PayloadDataNode> findNode(FieldDescription fieldDescription) {
        if (fieldDescription.getFieldIndex() < data.size()) {
            if (fieldDescription.getFieldIndex() >= 0) {
                return Optional.of( data.get( fieldDescription.getFieldIndex()));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<PayloadDataNode> findNode(String nodeName) {
        return data.stream()
                .filter(node -> nodeName.equals(node.name()))
                .findAny();
    }
    
}
