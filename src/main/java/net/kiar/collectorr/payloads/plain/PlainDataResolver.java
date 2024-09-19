/*
 * Copyright 2024 ranSprd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kiar.collectorr.payloads.plain;

import java.util.List;
import java.util.Optional;
import net.kiar.collectorr.payloads.FieldName;
import net.kiar.collectorr.payloads.PayloadDataNode;

/**
 *
 * @author ranSprd
 */
public class PlainDataResolver extends ResolvedData {
    
    private static final String RAW_NODE_NAME = "plainContent";
    

    public static PlainDataResolver construct(String raw) {
        if (raw == null || raw.isBlank()) {
            return new PlainDataResolver(List.of(), List.of());
        }
        
        try {
            String trimmed = raw.trim();
            double d = Double.parseDouble(trimmed);
            return new PlainDataResolver( List.of(new PayloadDataNode( new FieldName(RAW_NODE_NAME), trimmed)), 
                                        List.of());
        } catch (Exception e) {
            // ignore
        }
        
        return new PlainDataResolver(List.of(), 
                                   List.of(new PayloadDataNode( new FieldName(RAW_NODE_NAME), raw)));
    }
    
    
    public PlainDataResolver(List<PayloadDataNode> valueNodes, List<PayloadDataNode> labelNodes) {
        super(valueNodes, labelNodes);
    }

    @Override
    public Optional<PayloadDataNode> findNode(String nodeName) {
        if (RAW_NODE_NAME.equals(nodeName)) {
            if (!valueNodes.isEmpty()) {
                return Optional.of(valueNodes.getFirst());
            } else if (!labelNodes.isEmpty()) {
                return Optional.of(labelNodes.getFirst());
            }
        }
        return Optional.empty();
    }

    
}
