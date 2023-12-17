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

/**
 *
 * @author ranSprd
 */
public class MqttHeartbeatConfig {

    // e.g. "0/5 * * * * ?" for doing something important every 5 seconds
    private String cron;
    
    private String topic;
    private String message;

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    
    /**
     * Check is cron and topic parameters are set. 
     * @return true if all mandadory parameters are set
     */
    public boolean isValid() {
        if (topic == null || topic.isBlank()) {
            return false;
        }
        if (cron == null || cron.isBlank()) {
            return false;
        }
        
        return true;
    }
}
