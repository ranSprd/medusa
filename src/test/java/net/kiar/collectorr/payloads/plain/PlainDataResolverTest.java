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
package net.kiar.collectorr.payloads.plain;

import net.kiar.collectorr.payloads.PayloadDataNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

/**
 *
 * @author ranSprd
 */
public class PlainDataResolverTest {
    
    @Test
    public void testValueData() {
        PayloadDataNode vn1 = PlainDataResolver.construct("33.7").getValueNodes().getFirst();
        assertNotNull(vn1);
        assertEquals("33.7", vn1.value());
    }
    
}
