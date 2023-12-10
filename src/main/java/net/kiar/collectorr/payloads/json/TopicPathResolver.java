package net.kiar.collectorr.payloads.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    public TopicPathResolver(String topicPath) {
        this.topicPath = topicPath;
        if (topicPath != null) {
            String parts[] = topicPath.split("/");
            for(String p : parts) {
                if (p != null) {
                    data.add(new PayloadDataNode("", p));
                }
            }
        }
    }
    
    

    @Override
    public List<PayloadDataNode> getValueNodes() {
        return List.of();
    }

    @Override
    public String getValueNamesAsString() {
        return "";
    }

    @Override
    public Optional<PayloadDataNode> findValueNode(String nodeName) {
        return Optional.empty();
    }

    @Override
    public List<PayloadDataNode> getLabelNodes() {
//        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        return List.of();
    }

    @Override
    public String getLabelNamesAsString() {
//        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        return "";
    }

    @Override
    public Optional<PayloadDataNode> findLabelNode(FieldDescription fieldDescription) {
        if (fieldDescription.getFieldIndex() < data.size()) {
            if (fieldDescription.getFieldIndex() >= 0) {
                return Optional.of( data.get( fieldDescription.getFieldIndex()));
            }
        }
        return Optional.empty();
    }
    
}
