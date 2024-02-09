package net.kiar.collectorr.endpoints.config;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.kiar.collectorr.config.MappingsConfigLoader;
import net.kiar.collectorr.config.RuntimeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Path("/config/{configName}")
public class ConfigResource {
    private static final Logger log = LoggerFactory.getLogger(ConfigResource.class);

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String deliver(@PathParam("configName") String configName) {
        String s = MappingsConfigLoader.toYAML( RuntimeData.findConfig(configName));
        log.info("YAML is \n{}", s);
        return s;
    }

}
