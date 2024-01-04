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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.kiar.collectorr.payloads.DataProvider;

/**
 * This string can contain placeholders in form of {name}.
 * 
 * @author ranSprd
 */
public class PlaceholderString {
    
    
    private List<PlaceholderData> parts = new ArrayList<>();
    
    public PlaceholderString(String input) {
        
        if (input == null) {
            return;
        }
        
        boolean inPlaceholder = false;
        StringBuilder placeholderName = null;
        StringBuilder prefix = new StringBuilder();
        for(char c : input.toCharArray()) {
            if (inPlaceholder) {
                if (c == '}') {
                    // placeholder complete
                    parts.add( new PlaceholderData(prefix.toString(), placeholderName.toString()));
                    prefix = new StringBuilder();
                    inPlaceholder = false;
                } else {
                    placeholderName.append(c);
                }
            } else if (c == '{') {
                inPlaceholder = true;
                placeholderName = new StringBuilder();
            } else {
                prefix.append(c);
            }
        }
        if (!prefix.isEmpty()) {
            parts.add( new PlaceholderData(prefix.toString(), null));
        }
    }
    
    public List<String> getPlaceholderNames() {
        return parts.stream()
                .filter(ph -> ph.hasPlaceholderName() )
                .map(ph -> ph.placeholderName)
                .collect(Collectors.toList());
    }
    
    
    public String getProcessed() {
        // another alternative is to use another DataProvider which delivers ""
        return getProcessed(null);
    }
    
    public String getProcessed(DataProvider dataProvider) {
        StringBuilder result = new StringBuilder();
        for(PlaceholderData dummy : parts) {
            if (dummy.prefix != null) {
                result.append(dummy.prefix);
            }
            if (dummy.hasPlaceholderName() && dataProvider != null) {
                result.append( dataProvider.resolve(dummy.placeholderName, ""));
            }
        }
        return result.toString();
        
    }
    
    private static class PlaceholderData {
        private String prefix;
        private String placeholderName;

        public PlaceholderData(String prefix, String placeholderName) {
            this.prefix = prefix;
            this.placeholderName = placeholderName;
        }

        public boolean hasPlaceholderName() {
            return placeholderName != null && !placeholderName.isBlank();
        }
        
    }
    
}
