package org.apache.helix.gateway.service;

import org.apache.helix.gateway.api.service.HelixGatewayServiceProcessor;
import org.apache.helix.gateway.server.grpcserver.HelixGatewayServiceGrpcService;


public class HelixGatewayServiceProcessorFactory {

  public static HelixGatewayServiceProcessor createProcessor(GatewayServiceProcessorConfig config,
      GatewayServiceManager manager) {
    switch (config.getParticipantConnectionProcessorType()) {
      case GRPC_SERVER:
        return new HelixGatewayServiceGrpcService(manager);
      case PULL_GRPC_CLIENT:
      case PULL_FILE_SHARE:
      default:
        throw new IllegalArgumentException(
            "Unsupported processor type: " + config.getParticipantConnectionProcessorType());
    }
  }
}
