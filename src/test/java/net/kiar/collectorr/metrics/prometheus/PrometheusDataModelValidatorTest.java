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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

/**
 *
 * @author ranSprd
 */
public class PrometheusDataModelValidatorTest {
    
    @Test
    public void testixMetricName() {
        assertEquals("noName", PrometheusDataModelValidator.fixMetricName(null));
        assertEquals("noName", PrometheusDataModelValidator.fixMetricName(""));
        assertEquals("noName", PrometheusDataModelValidator.fixMetricName(" "));
        
        assertEquals("_0_name", PrometheusDataModelValidator.fixMetricName("0_name"));
        
        assertEquals("valid", PrometheusDataModelValidator.fixMetricName("valid"));
        assertEquals(":valid", PrometheusDataModelValidator.fixMetricName(":valid"));
        assertEquals("_valid", PrometheusDataModelValidator.fixMetricName("_valid"));
        assertEquals("__valid", PrometheusDataModelValidator.fixMetricName("__valid"));
        
    }
    
    @Test
    public void testixLabelName() {
        assertEquals("noName", PrometheusDataModelValidator.fixLabelName(null));
        assertEquals("noName", PrometheusDataModelValidator.fixLabelName(""));
        assertEquals("noName", PrometheusDataModelValidator.fixLabelName(" "));
        
        assertEquals("_0_name", PrometheusDataModelValidator.fixLabelName("0_name"));
        assertEquals("f__0_name", PrometheusDataModelValidator.fixLabelName("__0_name"));
        
        assertEquals("valid", PrometheusDataModelValidator.fixLabelName("valid"));
        
    }
    
    @Test
    public void testFixLabelValue() {
        assertNull(PrometheusDataModelValidator.fixLabelValue(null));
        assertEquals(" ", PrometheusDataModelValidator.fixLabelValue(" "));
        assertEquals("Couch", PrometheusDataModelValidator.fixLabelValue("\"Couch\""));
    }
    
}
