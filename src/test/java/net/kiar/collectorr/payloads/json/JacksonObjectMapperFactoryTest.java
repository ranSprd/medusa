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
import com.fasterxml.jackson.core.JsonToken;
import java.io.IOException;
import java.nio.file.Path;

/**
 *
 * @author ranSprd
 */
public class JacksonObjectMapperFactoryTest {
    
//    @Test
    public void testPythonBooleans() throws IOException {
//        JsonResolver instance = JsonResolver.consume(
//                Files.readString( Path.of("src/test/resources/json/brokenBooleanShort.json")));
//        assertNotNull(instance);
        JsonParser parser = JacksonObjectMapperFactory.JSON.getMapper().createParser(Path.of("src/test/resources/json/brokenBooleanShort.json").toFile());
        while (!parser.isClosed()) {
            JsonToken jsonToken = parser.nextToken();

            if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                String fieldName = parser.getCurrentName();
                System.out.println(fieldName);

                jsonToken = parser.nextToken();

                if ("brand".equals(fieldName)) {
//            car.brand = parser.getValueAsString();
                } else if ("doors".equals(fieldName)) {
//            car.doors = parser.getValueAsInt();
                }
            }
        }

    }
    
}
