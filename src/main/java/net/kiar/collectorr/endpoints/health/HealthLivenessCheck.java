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
package net.kiar.collectorr.endpoints.health;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import net.kiar.collectorr.connector.ConnectorFactory;
import net.kiar.collectorr.model.HealthState;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

/**
 *
 * @author ranSprd
 */
@Liveness
@ApplicationScoped
public class HealthLivenessCheck implements HealthCheck {
  
    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.builder();
        builder.name("Connectors health");
        
        List<HealthState> states = ConnectorFactory.data.overAllHealtStates();
        builder.status( states.stream().allMatch(state -> state.isHealthy()));
        states.forEach(state -> builder.withData(state.getName(), state.isHealthy()));
        
        return builder.build();
//                .
        
//        return HealthCheckResponse.up("Simple health check");
    }    
}
