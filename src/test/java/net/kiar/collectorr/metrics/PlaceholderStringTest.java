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
package net.kiar.collectorr.metrics;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author ranSprd
 */
public class PlaceholderStringTest {
    
    @Test
    public void testInvalidInput() {
      assertTrue(new PlaceholderString("").getPlaceholderNames().isEmpty());
      assertTrue(new PlaceholderString(null).getPlaceholderNames().isEmpty());
      assertTrue(new PlaceholderString(" {").getPlaceholderNames().isEmpty());
        
    }
    
    @Test
    public void testNoPlaceholderInInput() {
        PlaceholderString pstr = new PlaceholderString("This is a string");
        
        List<String> knownPlaceholders = pstr.getPlaceholderNames();
        assertTrue(knownPlaceholders.isEmpty());
    }
    
    @Test
    public void testWithPlaceholderInInput() {
        PlaceholderString pstr = new PlaceholderString("This is a {placeholder-1} string with a {2nd}.");
        
        List<String> knownPlaceholders = pstr.getPlaceholderNames();
        assertEquals(2, knownPlaceholders.size());
        
        assertTrue(knownPlaceholders.contains("placeholder-1"));
        assertTrue(knownPlaceholders.contains("2nd"));
    }
    
    
    @Test
    public void testString() {
        assertEquals("", new PlaceholderString("{foo}").getProcessed());
        assertEquals("bar", new PlaceholderString("{foo}").getProcessed("bar"));
        assertEquals("something", new PlaceholderString("something").getProcessed("bar"));
    }
    
}
