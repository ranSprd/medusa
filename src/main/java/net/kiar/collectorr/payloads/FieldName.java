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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @todo this class contains a lot of string operations, this could be optimized
 * 
 * @author ranSprd
 */
public class FieldName {
    
    private boolean unique = true;
    private boolean arrayItem = false;
    
    private final String fullName;
    private String name;
    
    private String prefix = "";

    public FieldName(String rawName) {
        this.fullName = rawName == null? "" : rawName;
        this.name = extractName( this.fullName);
        if (this.fullName.contains("*")) {
            unique = false;
            
            int index = this.fullName.lastIndexOf(".*.");
            if (index > -1) {
                prefix = this.fullName.substring(0, index+3);
            }
        } else if (this.fullName.contains(".#")) {
            unique = true;
            Pattern iP = Pattern.compile("\\.#[0-9]*\\.");
            Matcher matcher = iP.matcher(this.fullName);

            if (matcher.find()) {
                arrayItem = true;
                int index = matcher.end(0);
                if (index > -1) {
                    prefix = this.fullName.substring(0, index);
                }
            }
        } else if (this.fullName.contains(".")) {
            int index = this.fullName.lastIndexOf(".");
            if (index > -1) {
                prefix = this.fullName.substring(0, index+1);
            }
        }
    }
    
    private static String extractName(String input) {
        int index = input.lastIndexOf('.')+1;
        if (index > 0 && index < input.length()) {
            return input.substring(index);
        }
        return input;
    }

    /**
     * fieldname can match several fields (e.g. arrays)
     * @return 
     */
    public boolean isUnique() {
        return unique;
    }

    public boolean isArrayItem() {
        return arrayItem;
    }
    
    public boolean hasPrefix() {
        return prefix != null && !prefix.isBlank();
    }
    
    public boolean isSamePrefix(FieldName other) {
        String str = prefix.replaceAll("\\.\\*\\.", "\\\\.#[0-9]*\\\\.");
        Pattern searchPattern = Pattern.compile(str);
        
        return searchPattern.matcher( other.prefix).matches();
    }
    
    /** name with prefix, if available */
    public String getFullName() {
        return fullName;
    }
    
    /** prefix part of the full name */
    public String getPrefix() {
        return prefix;
    }
    
    /** part of the name without prefix */
    public String getName(){
        return name;
    }
    
    
    
    
}
