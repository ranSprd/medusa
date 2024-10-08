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

import java.util.Optional;
import net.kiar.collectorr.payloads.json.JsonResolver;
import net.kiar.collectorr.payloads.plain.PlainDataResolver;

/**
 *
 * @author ranSprd
 */
public class PayloadResolverFactory {
  
    /**
     * parse the given string payload and sort out values und labels
     * @param payload
     * @return 
     */
    public static PayloadResolver build(String payload) {
        Optional<JsonResolver> result = JsonResolver.consume(payload);
        if (result.isEmpty()) {
            return PlainDataResolver.construct(payload);
        }
        return result.get();
    }
    
}
