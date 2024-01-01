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
package net.kiar.collectorr.payloads;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author ranSprd
 */
public class DoubleParserTest {
    
    @Test
    public void testInvalidInput() {
        assertEquals(0.0, DoubleParser.parse(null));
        assertEquals(0.0, DoubleParser.parse(""));
        assertEquals(0.0, DoubleParser.parse("   "));
        assertEquals(0.0, DoubleParser.parse("five"));
    }
    
    @Test
    public void testNormal() {
        assertEquals(7, DoubleParser.parse("7"));
        assertEquals(1.7, DoubleParser.parse("1.7"));
    }
    
    @Test
    public void testExtendedDoubleParsing() {
        assertEquals(3.4, DoubleParser.parse("3.4%"));
        assertEquals(3.4, DoubleParser.parse("y=3.4"));
        assertEquals(1.2, DoubleParser.parse("1.2 5.6"));
        assertEquals(1.3e10, DoubleParser.parse("1.3e10#"));
        assertEquals(10., DoubleParser.parse("10.#"));
    }
    
}
