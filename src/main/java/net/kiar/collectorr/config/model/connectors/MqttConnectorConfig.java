/*
 * Copyright 2023 ranSprd.
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
package net.kiar.collectorr.config.model.connectors;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author ranSprd
 */
public class MqttConnectorConfig {
    
    private String name;
    private String url;
    
    /** maximum time between 2 received and valid processed messages. Used as indicator for healthy state. */
    private long maxIdleTimeMillis = -1;
    
    @JsonProperty("mapping-file")
    private String mappingConfigFile;
    
    /**
     * Optional: subscripe to the given topic. If empty the topic is set to #
     */
    private String rootTopic;
    
    private MqttHeartbeatConfig heartbeat;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMappingConfigFile() {
        return mappingConfigFile;
    }

    public void setMappingConfigFile(String mappingConfigFile) {
        this.mappingConfigFile = mappingConfigFile;
    }

    public String getRootTopic() {
        return rootTopic;
    }

    public void setRootTopic(String rootTopic) {
        this.rootTopic = rootTopic;
    }

    public MqttHeartbeatConfig getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(MqttHeartbeatConfig heartbeat) {
        this.heartbeat = heartbeat;
    }

    /** maximum time between 2 received and valid processed messages. Used as indicator for healthy state. */
    public long getMaxIdleTimeMillis() {
        return maxIdleTimeMillis;
    }

    public void setMaxIdleTimeMillis(long maxIdleTimeMillis) {
        this.maxIdleTimeMillis = maxIdleTimeMillis;
    }
    
    
    
}
