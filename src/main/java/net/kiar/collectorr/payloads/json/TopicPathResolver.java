package net.kiar.collectorr.payloads.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.kiar.collectorr.connector.mqtt.mapping.TopicStructure;
import net.kiar.collectorr.metrics.FieldDescription;
import net.kiar.collectorr.payloads.FieldName;
import net.kiar.collectorr.payloads.PayloadDataNode;
import net.kiar.collectorr.payloads.PayloadResolver;

/**
 *
 * @author ranSprd
 */
public class TopicPathResolver implements PayloadResolver {

    private final String topicPath;
    private final List<PayloadDataNode> data = new ArrayList<>();
    private boolean excluded = false;

    public TopicPathResolver(String topicPath, TopicStructure topicStructure) {
        this.topicPath = topicPath;
        if (topicPath != null) {
            int t = 0;
            String parts[] = topicPath.split("/");
            for(String p : parts) {
                if (p != null) {
                    TopicStructure.TopicSegment segment = topicStructure.getSegment(t);
                    if (segment != null) {
                        if (!segment.isSegmentNameAllowed(p)) {
                            excluded = true;
                        } 
                        String fieldName = segment.getFieldName();
                        data.add(new PayloadDataNode( new FieldName(fieldName), p));
                    } else {
                        data.add(new PayloadDataNode( new FieldName(""), p));
                    }
                }
                t++;
            }
        }
    }

    /**
     * some segments of a topic can have restricted content 
     * @return 
     */
    public boolean isExcluded() {
        return excluded;
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

    @Override
    public List<PayloadDataNode> findNodes(FieldDescription fieldDescription) {
        if (fieldDescription.getFieldIndex() < data.size()) {
            if (fieldDescription.getFieldIndex() >= 0) {
                return List.of( data.get( fieldDescription.getFieldIndex()));
            }
        }
        return List.of();
    }

    @Override
    public String getLabelNamesAsString() {
        return data.stream().map(o -> o.fieldName().getFullName()).collect(Collectors.joining(","));        
    }

//    @Override
//    public Optional<PayloadDataNode> findNode(FieldDescription fieldDescription) {
//        return Optional.empty();
//    }

    @Override
    public Optional<PayloadDataNode> findNode(String nodeName) {
        return data.stream()
                .filter(node -> nodeName.equals(node.fieldName().getFullName()))
                .findAny();
    }

    @Override
    public List<PayloadDataNode> getLabelNodes() {
        return List.of();
    }
    
}
