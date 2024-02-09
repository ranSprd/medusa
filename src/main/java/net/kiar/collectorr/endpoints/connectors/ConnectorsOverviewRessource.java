package net.kiar.collectorr.endpoints.connectors;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import net.kiar.collectorr.connector.ConnectorFactory;
import net.kiar.collectorr.connector.ConnectorSummaryStatistics;


@Path("/connectors")
public class ConnectorsOverviewRessource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ConnectorSummaryStatistics> deliver() {
        return ConnectorFactory.data.getConnectorsOverview();
    }

}
