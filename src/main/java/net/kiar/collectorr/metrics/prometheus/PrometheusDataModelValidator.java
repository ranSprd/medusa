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
package net.kiar.collectorr.metrics.prometheus;

/**
 * based on definitions of data model (label & metric names)
 * https://prometheus.io/docs/concepts/data_model/#metric-names-and-labels
 * 
 * @author ranSprd
 */
public class PrometheusDataModelValidator {
    
    /**
     * Metric names may contain ASCII letters, digits, underscores, and colons. 
     * It must match the regex [a-zA-Z_:][a-zA-Z0-9_:]*.
     * 
     * @param input
     * @return 
     */
    public static String fixMetricName(String input) {
        if (input == null || input.isEmpty()) {
            return "noName";
        }
        
        String result = input.trim();
        if (result.isEmpty()) {
            return "noName";
        }
        
        // replace known illegal characters 
        result = result.replaceAll("#", ":")
                       .replaceAll("\\.", "_");
       
        char first = result.charAt(0);
        if ( isAsciiLetter(first) || first == ':' || first == '_') {
            return result;
        }
        
        return "_" +result;
        
    }
    
    
    /**
     * Labels may contain ASCII letters, numbers, as well as underscores. 
     * They must match the regex [a-zA-Z_][a-zA-Z0-9_]*.
     * Label names beginning with __ (two "_") are reserved for internal use.
     * 
     * @param input
     * @return 
     */
    public static String fixLabelName(String input) {
        if (input == null || input.isEmpty()) {
            return "noName";
        }
        
        String result = input.trim();
        if (result.isEmpty()) {
            return "noName";
        }
        
        // replace known illegal characters 
        result = result.replaceAll("#", "")
                       .replaceAll("\\.", "_");
       
        char first = result.charAt(0);
        if ( isAsciiLetter(first)) {
            return result;
        } else if (first == '_' && result.length() > 1) {
            if (result.charAt(1) == '_') {
                // illegal __ found at the beginning of name
                return "f" +result;
            }
        }
        
        return "_" +result;
        
    }
    
    private static boolean isAsciiLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }
    
}
