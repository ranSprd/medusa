package net.kiar.collectorr.endpoints;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.kiar.collectorr.metrics.PrometheusGauge;
import net.kiar.collectorr.repository.MetricsRepo;


@Path("/metrics")
public class MetricsResource {


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String deliver() {
        StringBuilder b = new StringBuilder();
        for(PrometheusGauge entry : MetricsRepo.INSTANCE.data()) {
            b.append(entry.toMetricString());
        }
        return b.toString();
    }

}
