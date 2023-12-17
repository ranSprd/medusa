package net.kiar.collectorr.endpoints.connectors.unknown;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;
import net.kiar.collectorr.connector.ConnectorFactory;
import net.kiar.collectorr.repository.MqttTopicStats;


@Path("/connectors/{connectorName}/unknown")
public class UnknownResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, MqttTopicStats.UnknownTopicStatistic> deliver(@PathParam("connectorName") String connectorName) {
        return ConnectorFactory.data.getUnknownStatsForConnector(connectorName);
    }
    
    

}
