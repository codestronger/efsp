package edu.suffolk.litlab.efspserver.services;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class RootService {

  private static final Logger log = LoggerFactory.getLogger(RootService.class);

  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAll() {
    EndpointReflection ef = new EndpointReflection("");
    var endPoints =
        ef.getClassPaths(
            List.of(
                AuthenticationService.class,
                JurisdictionSwitch.class,
                MessageSettingsService.class,
                ApiUserSettingsService.class));
    endPoints.put("getVersionInfo", ServiceHelpers.EXTERNAL_URL + "/about");
    log.info("Showing the root services");
    return Response.ok(endPoints).build();
  }

  @GET
  @Path("/about")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getVersionInfo() {
    final var properties = new Properties();
    try (var is = RootService.class.getResourceAsStream("/version.properties")) {
      properties.load(is);
      return Response.ok("{\"version\": \"" + properties.getProperty("version") + "\"}").build();
    } catch (IOException e) {
      return Response.status(500).entity("Could not load version info").build();
    }
  }
}
