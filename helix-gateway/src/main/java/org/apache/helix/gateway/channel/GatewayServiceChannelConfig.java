package org.apache.helix.gateway.channel;

import static org.apache.helix.gateway.constant.GatewayServiceGrpcDefaultConfig.*;


public class GatewayServiceChannelConfig {
  public enum ChannelType {
    GRPC_SERVER, PULL_GRPC_CLIENT, PULL_FILE_SHARE
  }

  // service configs
  private ChannelType _participantConnectionChannelType;
  private ChannelType _shardStatenChannelType;

  // grpc server configs
  private int _grpcServerPort;
  private int _serverHeartBeatInterval;
  private int _maxAllowedClientHeartBeatInterval;
  private int _clientTimeout;
  private boolean _enableReflectionService;

  // pull mode config
  private int pullIntervalSec;
  // TODO: configs for pull mode grpc client

  // TODO: configs for pull mode with file

  // getters
  public ChannelType getParticipantConnectionChannelType() {
    return _participantConnectionChannelType;
  }

  public ChannelType getShardStatenChannelType() {
    return _shardStatenChannelType;
  }

  public int getGrpcServerPort() {
    return _grpcServerPort;
  }

  public int getServerHeartBeatInterval() {
    return _serverHeartBeatInterval;
  }

  public int getMaxAllowedClientHeartBeatInterval() {
    return _maxAllowedClientHeartBeatInterval;
  }

  public int getClientTimeout() {
    return _clientTimeout;
  }

  public boolean getEnableReflectionService() {
    return _enableReflectionService;
  }

  public int getPullIntervalSec() {
    return pullIntervalSec;
  }

  public GatewayServiceChannelConfig(int grpcServerPort, ChannelType participantConnectionChannelType,
      ChannelType shardStatenChannelType, int serverHeartBeatInterval, int maxAllowedClientHeartBeatInterval,
      int clientTimeout, boolean enableReflectionService, int pullIntervalSec) {
    _grpcServerPort = grpcServerPort;
    _participantConnectionChannelType = participantConnectionChannelType;
    _shardStatenChannelType = shardStatenChannelType;
    this._serverHeartBeatInterval = serverHeartBeatInterval;
    this._maxAllowedClientHeartBeatInterval = maxAllowedClientHeartBeatInterval;
    this._clientTimeout = clientTimeout;
    this._enableReflectionService = enableReflectionService;
    this.pullIntervalSec = pullIntervalSec;
  }

  public static class GatewayServiceProcessorConfigBuilder {

    // service configs
    private ChannelType _participantConnectionChannelType = ChannelType.GRPC_SERVER;
    private ChannelType _shardStatenChannelType = ChannelType.GRPC_SERVER;

    // grpc server configs
    private int _grpcServerPort;
    private int _serverHeartBeatInterval = DEFAULT_SERVER_HEARTBEAT_INTERVAL;
    private int _maxAllowedClientHeartBeatInterval = DEFAULT_AMX_ALLOWED_CLIENT_HEARTBEAT_INTERVAL;
    private int _clientTimeout = DEFAULT_CLIENT_TIMEOUT;
    private boolean _enableReflectionService = true;

    // pull mode config
    private int _pullIntervalSec = 60;
    // pull mode grpc client configs

    // pull mode file  configs

    public GatewayServiceProcessorConfigBuilder setParticipantConnectionChannelType(ChannelType channelType) {
      _participantConnectionChannelType = channelType;
      return this;
    }

    public GatewayServiceProcessorConfigBuilder setShardStateProcessorType(ChannelType channelType) {
      _shardStatenChannelType = channelType;
      return this;
    }

    public GatewayServiceProcessorConfigBuilder setGrpcServerPort(int grpcServerPort) {
      _grpcServerPort = grpcServerPort;
      return this;
    }

    public GatewayServiceProcessorConfigBuilder setServerHeartBeatInterval(int serverHeartBeatInterval) {
      _serverHeartBeatInterval = serverHeartBeatInterval;
      return this;
    }

    public GatewayServiceProcessorConfigBuilder setMaxAllowedClientHeartBeatInterval(
        int maxAllowedClientHeartBeatInterval) {
      _maxAllowedClientHeartBeatInterval = maxAllowedClientHeartBeatInterval;
      return this;
    }

    public GatewayServiceProcessorConfigBuilder setClientTimeout(int clientTimeout) {
      _clientTimeout = clientTimeout;
      return this;
    }

    public GatewayServiceProcessorConfigBuilder setEnableReflectionService(boolean enableReflectionService) {
      _enableReflectionService = enableReflectionService;
      return this;
    }

    public GatewayServiceProcessorConfigBuilder setPullIntervalSec(int pullIntervalSec) {
      _pullIntervalSec = pullIntervalSec;
      return this;
    }

    public void validate() {
      if ((_participantConnectionChannelType == ChannelType.PULL_GRPC_CLIENT
          && _shardStatenChannelType != ChannelType.PULL_GRPC_CLIENT) || (
          _participantConnectionChannelType != ChannelType.PULL_GRPC_CLIENT
              && _shardStatenChannelType == ChannelType.PULL_GRPC_CLIENT)) {
        throw new IllegalArgumentException(
            "Unsupported channel type config: ConnectionChannelType: " + _participantConnectionChannelType
                + " shardStatenChannelType: " + _shardStatenChannelType);
      }
    }

    public GatewayServiceChannelConfig build() {
      validate();
      return new GatewayServiceChannelConfig(_grpcServerPort, _participantConnectionChannelType,
          _shardStatenChannelType, _serverHeartBeatInterval, _maxAllowedClientHeartBeatInterval, _clientTimeout,
          _enableReflectionService, _pullIntervalSec);
    }
  }
}
