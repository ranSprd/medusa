package net.kiar.collectorr.endpoints.connectors.unprocessed;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.kiar.collectorr.connector.ConnectorFactory;
import net.kiar.collectorr.repository.MqttTopicStats;


@Path("/connectors/{connectorName}/unprocessed")
public class UnprocessedResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<UnprocessedStatisticsDTO> deliver(@PathParam("connectorName") String connectorName) {
        
        Map<String, MqttTopicStats.UnknownTopicStatistic> result = ConnectorFactory.data.getUnknownStatsForConnector(connectorName);
        
        if (result != null) {
            return result.entrySet().stream()
                    .map(entry -> new UnprocessedStatisticsDTO(entry.getKey(), entry.getValue()))
                    .sorted( (o1, o2) -> Integer.compare(o2.stats.getReceivedCount(), o1.stats.getReceivedCount())) // sort descending
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }
    
    
    public static class UnprocessedStatisticsDTO {
        private String id;
        private MqttTopicStats.UnknownTopicStatistic stats;

        public UnprocessedStatisticsDTO(String id, MqttTopicStats.UnknownTopicStatistic stats) {
            this.id = id;
            this.stats = stats;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public MqttTopicStats.UnknownTopicStatistic getStats() {
            return stats;
        }

        public void setStats(MqttTopicStats.UnknownTopicStatistic stats) {
            this.stats = stats;
        }
        
    }
    
    

}
