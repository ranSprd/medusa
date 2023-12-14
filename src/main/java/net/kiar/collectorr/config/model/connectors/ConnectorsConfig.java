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
import java.util.List;

/**
 *
 * @author ranSprd
 */
public class ConnectorsConfig {
    
    @JsonProperty("mqtt-brokers")
    private List<MqttConnectorConfig> mqttBrokers;

    public List<MqttConnectorConfig> getMqttBrokers() {
        return mqttBrokers;
    }

    public void setMqttBrokers(List<MqttConnectorConfig> mqttBrokers) {
        this.mqttBrokers = mqttBrokers;
    }

    
}
