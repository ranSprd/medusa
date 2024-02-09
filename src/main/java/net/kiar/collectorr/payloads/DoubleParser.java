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
package net.kiar.collectorr.payloads;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ranSprd
 */
public class DoubleParser {
    private static final Logger log = LoggerFactory.getLogger(DoubleParser.class);
    
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?");


    /**
     * This logic takes the first numerical value from input. 
     * 
     * @param str something like 3.4 or 5.6% or y=7
     * 
     * @return The first numerical value which is found in string or 0.0 if there is nothing
     */
    public static double parse(String str) {
        if (str == null || str.isBlank()) {
            return 0.0;
        }
        
        try {
            return Double.parseDouble(str.trim());
        } catch (Exception e) {
            log.debug("Error parsing double value. Input is[{}]  {}", str, e.getMessage());
        }

        Matcher matcher = DOUBLE_PATTERN.matcher(str);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(0));
            } catch (Exception e) {
                // ignore
            }
        }
        return 0.0;
    }
}
