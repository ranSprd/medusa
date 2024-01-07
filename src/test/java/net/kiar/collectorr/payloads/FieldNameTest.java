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
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author ranSprd
 */
public class FieldNameTest {
    
    @Test
    public void testArrayNameOfUniqueField () {
        
        FieldName name = new FieldName("common_list.#2.id");
        System.out.println("" + name.getPrefix());
        
        assertEquals("common_list.#2.", new FieldName("common_list.#2.id").getPrefix());
        assertEquals("common_list.*.", new FieldName("common_list.*.id").getPrefix());
    }
    
    @Test
    public void testPrefixExtraction() {
        assertEquals("", new FieldName(null).getPrefix());
        assertEquals(".", new FieldName(".").getPrefix());
        assertEquals("foo.bar.", new FieldName("foo.bar.field").getPrefix());
        assertEquals("common_list.*.", new FieldName("common_list.*.val").getPrefix());
        assertEquals("common_list.#1.", new FieldName("common_list.#1.id").getPrefix());
    }
    
    @Test
    public void testSamePrefix() {
        FieldName fieldInPayload = new FieldName("common_list.#2.id");
        FieldName valueField = new FieldName("common_list.*.val");
        
        System.out.println("label " +fieldInPayload.getPrefix());
        System.out.println("value " +valueField.getPrefix());
        
        assertTrue( valueField.isSamePrefix(fieldInPayload));
    }
    
}
