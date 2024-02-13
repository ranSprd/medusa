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
package net.kiar.collectorr.payloads.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.util.List;

/**
 * experimental class for dealing with parser errors
 * @author ranSprd
 */
public enum JacksonObjectMapperFactory {
    
    JSON;

    private final ObjectMapper mapper = new ObjectMapper();

    private JacksonObjectMapperFactory() {
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        mapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);

        mapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public boolean handleUnknownProperty(DeserializationContext dc, JsonParser jp, JsonDeserializer<?> jd, Object bean, String property) throws IOException, JsonProcessingException {
                System.out.println("Handling unknown property: " + property);
                return false;
            }
        });

        SimpleModule module = new SimpleModule();
        module.addDeserializer(boolean.class, new CustomBooleanDeserializer());
        mapper.registerModule(module);
    }

    public ObjectMapper getMapper() {
        return mapper;
    }
    

    class CustomBooleanDeserializer extends JsonDeserializer<Boolean> {

        @Override
        public Boolean deserialize(JsonParser p, DeserializationContext ctx) throws IOException {

            final String normed = (p.getText() == null) ? null : p.getText().trim().toLowerCase();
            if (List.of("1", "active", "true", "enabled").contains(normed)) {
                return Boolean.TRUE;
            } else if (List.of("0", "inactive", "false", "disabled").contains(normed)) {
                return Boolean.FALSE;
            }
            return false;
        }
    }

}
