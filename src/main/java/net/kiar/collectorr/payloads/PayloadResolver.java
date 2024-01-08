package net.kiar.collectorr.payloads;

import java.util.List;
import java.util.Optional;
import net.kiar.collectorr.metrics.FieldDescription;

/**
 *
 * @author ranSprd
 */
public interface PayloadResolver {

    
    public List<PayloadDataNode> findNodes(FieldDescription valueNodeDesc);
    
    /** value is not filtered as in findNodes (means something like val#1 from metric definition will not work) 
     * @param nodeName name of the field in source
     * @return raw unprocessed value
     * 
     */
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
