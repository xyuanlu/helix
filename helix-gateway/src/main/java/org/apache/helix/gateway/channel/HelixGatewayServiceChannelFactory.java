package org.apache.helix.gateway.channel;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.helix.gateway.api.service.HelixGatewayServiceChannel;
import org.apache.helix.gateway.channel.GatewayServiceChannelConfig;
import org.apache.helix.gateway.channel.HelixGatewayServiceGrpcService;
import org.apache.helix.gateway.service.GatewayServiceManager;


public class HelixGatewayServiceChannelFactory {

  public static HelixGatewayServiceChannel createServiceChannel(GatewayServiceChannelConfig config,
      GatewayServiceManager manager) {
    switch (config.getParticipantConnectionChannelType()) {
      case GRPC_SERVER:
        return new HelixGatewayServiceGrpcService(manager, config);
      case PULL_GRPC_CLIENT:
      case PULL_FILE_SHARE:
        throw new NotImplementedException("Not implemented yet");
      default:
        throw new IllegalArgumentException(
            "Unsupported processor type: " + config.getParticipantConnectionChannelType());
    }
  }
}
