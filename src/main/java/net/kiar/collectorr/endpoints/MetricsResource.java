package net.kiar.collectorr.endpoints;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import net.kiar.collectorr.metrics.PrometheusGauge;
import net.kiar.collectorr.repository.MetricsRepo;


@Path("/metrics")
public class MetricsResource {


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String deliver() {
        StringBuilder b = new StringBuilder();
        
        Map<String, List<PrometheusGauge>> sorted = MetricsRepo.INSTANCE.sortedData();
        
        for(Map.Entry<String, List<PrometheusGauge>> group : sorted.entrySet()) {
            List<PrometheusGauge> list = group.getValue();
            if (!list.isEmpty()) {
                b.append( list.get(0).toMetricString(true));
                for(int t = 1; t < list.size(); t++) {
                    b.append( list.get(t).toMetricString(false));
                }
                b.append("\n");
            }
        }
            
        return b.toString();
    }

}
