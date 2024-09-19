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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.kiar.collectorr.metrics.FieldDescription;
import net.kiar.collectorr.payloads.PayloadDataNode;
import net.kiar.collectorr.payloads.PayloadDataNode;
import net.kiar.collectorr.payloads.PayloadResolver;
import net.kiar.collectorr.payloads.PayloadResolver;

/**
 *
 * @author ranSprd
 */
public abstract class ResolvedData implements PayloadResolver {
    
    protected static PayloadDataNode extractContent(PayloadDataNode node, FieldDescription nodeDesc) {
        if (nodeDesc.getFieldIndex() >= 0) {
            return new PayloadDataNode(node.fieldName(), extractPart(node.value(), nodeDesc.getFieldIndex()));
        }
        return node;
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
    
    
    protected final List<PayloadDataNode> valueNodes;
    protected final List<PayloadDataNode> labelNodes;

    public ResolvedData(List<PayloadDataNode> valueNodes, List<PayloadDataNode> labelNodes) {
        this.valueNodes = valueNodes;
        this.labelNodes = labelNodes;
    }
    
    @Override
    public Optional<PayloadDataNode> findNode(String nodeName) {
        if (nodeName == null) {
            return Optional.empty();
        }
        return valueNodes.stream().filter(node -> nodeName.equalsIgnoreCase(node.fieldName().getFullName())).findAny().or(() -> labelNodes.stream().filter(node -> nodeName.equalsIgnoreCase(node.fieldName().getFullName())).findAny());
    }

    @Override
    public List<PayloadDataNode> findNodes(FieldDescription valueNodeDesc) {
        String str = valueNodeDesc.getFieldName().getFullName().replaceAll("\\.\\*\\.", "\\\\.#[0-9]*\\\\.");
        Pattern pattern = Pattern.compile(str);
        List<PayloadDataNode> result = valueNodes.stream().filter(node -> pattern.matcher(node.fieldName().getFullName()).matches()).map(node -> extractContent(node, valueNodeDesc)).collect(Collectors.toList());
        if (result.isEmpty()) {
            return labelNodes.stream().filter(node -> pattern.matcher(node.fieldName().getFullName()).matches()).map(node -> extractContent(node, valueNodeDesc)).collect(Collectors.toList());
        }
        return result;
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
    
}
