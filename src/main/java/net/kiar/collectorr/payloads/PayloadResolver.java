package net.kiar.collectorr.payloads;

import java.util.List;
import java.util.Optional;
import net.kiar.collectorr.metrics.FieldDescription;

/**
 *
 * @author ranSprd
 */
public interface PayloadResolver {

    public Optional<PayloadDataNode> findNode(FieldDescription fieldDesc);
    public Optional<PayloadDataNode> findNode(String nodeName);
    
    /**
     * get all nodes classified as value (normally numeric data)
     * @return 
     */
    public List<PayloadDataNode> getValueNodes();
    public String getValueNamesAsString();
    
    /**
     * all nodes classified as label (normally textual and boolean data)
     * @return 
     */
    public List<PayloadDataNode> getLabelNodes();
    public String getLabelNamesAsString();
    
}
