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
package net.kiar.collectorr.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author ranSprd
 */
public class ConnectorsConfigLoaderTest {
    
        private final static String content = """
mqtt-brokers:
- name: central
  url: tcp://192.168.2.145:1883
  mapping-file:
- name: comfoair
  url: tcp://192.168.2.228:1883
 """;
        
    @Test
    public void testAll() {
        // The Loader is a singleton, for that reason all tests must be serialized
        
        // invalid file loading
        assertFalse(ConnectorsConfigLoader.INSTANCE.readFromFile(null));
        assertFalse(ConnectorsConfigLoader.INSTANCE.readFromFile("src/test/file"));

        // invalid content
        assertFalse(ConnectorsConfigLoader.INSTANCE.readContent(null));
        assertFalse(ConnectorsConfigLoader.INSTANCE.readContent(""));
        assertFalse(ConnectorsConfigLoader.INSTANCE.readContent(" {"));
        
        // should have content
        assertTrue(ConnectorsConfigLoader.INSTANCE.readContent(content));
        
    }
    
    
    
}
