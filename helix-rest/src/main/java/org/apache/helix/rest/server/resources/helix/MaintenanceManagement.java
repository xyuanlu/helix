package org.apache.helix.rest.server.resources.helix;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.helix.manager.zk.ZKHelixDataAccessor;
import org.apache.helix.rest.common.HttpConstants;
import org.apache.helix.rest.server.service.InstanceService;
import org.apache.helix.rest.server.service.InstanceServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Path("/clusters/{clusterId}/MaintenanceManagement")
public class MaintenanceManagement extends AbstractHelixResource {
  private static Logger LOG = LoggerFactory.getLogger(
      org.apache.helix.rest.server.resources.helix.ResourceAssignmentOptimizerAccessor.class
          .getName());

  private static class InputJsonContent {
    @JsonProperty("OperationClassName")
    String operationClassName;
  }

  @Path("takeInstance")
  @ResponseMetered(name = HttpConstants.WRITE_REQUEST)
  @Timed(name = HttpConstants.WRITE_REQUEST)
  @POST
  public Response takeInstance(@PathParam("clusterId") String clusterId,String content) {
    try {
      InstanceService instanceService =
          new InstanceServiceImpl((ZKHelixDataAccessor) getDataAccssor(clusterId),
              getConfigAccessor(), false, false, getNamespace());

      String operationClassName = "org.apache.helix.rest.server.service.OperationImpl"; //readInput(content);
      return JSONRepresentation(instanceService.takeInstance(clusterId,
          "HMP-node_0-ltx1-app2319.stg.linkedin.com_6467",
          Collections.singletonList("HelixInstanceStoppableCheck"), new HashMap<>(),
          Collections.singletonList(operationClassName), new HashMap<>(), true));
    } catch (Exception e) {
      LOG.error("Failed to takeInstances:" + Arrays.toString(e.getStackTrace()));
      return badRequest("Failed to takeInstances: " + e);
    }
  }

  @ResponseMetered(name = HttpConstants.WRITE_REQUEST)
  @Timed(name = HttpConstants.WRITE_REQUEST)
  @POST
  @Path("freeInstances")
  public Response freeInstances(String content) {
    try {
      return JSONRepresentation("free");

    } catch (Exception e) {
      LOG.error("Failed to freeInstances:" + Arrays.toString(e.getStackTrace()));
      return badRequest("Failed to freeInstances: " + e);
    }
  }

}