package net.kiar.collectorr.endpoints.topics;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;
import net.kiar.collectorr.repository.MqttTopicStats;


@Path("/topics/unknown")
public class UnknownResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, MqttTopicStats.UnknownTopicStatistic> deliver() {
        return MqttTopicStats.getInstance().getUnknowTopicsStatistics();
    }

}
