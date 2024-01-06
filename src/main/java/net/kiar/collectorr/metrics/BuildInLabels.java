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
package net.kiar.collectorr.metrics;

import java.util.HashMap;
import java.util.Map;
import net.kiar.collectorr.payloads.PayloadDataNode;

/**
 *
 * @author ranSprd
 */
public class BuildInLabels {
    public static final String
            VALUE_FIELD_NAME = "valueFieldName",
            TOPIC_NAME = "topicName";

    private Map<String, String> buildInPairs = new HashMap<>();

    private BuildInLabels() {
    }
    
    public static BuildInLabels getBuildInData(PayloadDataNode valueField, String topicPath) {
        BuildInLabels result = new BuildInLabels();
        result.buildInPairs.put(VALUE_FIELD_NAME, valueField.getFieldName().getFullName().replaceAll("\\.", "_"));
        result.buildInPairs.put(TOPIC_NAME, topicPath.replaceAll("/", "_"));
        return result;
    }
    
    public String find(String key, String fallbackValue) {
        return buildInPairs.getOrDefault(key, fallbackValue);
    }
    
}
