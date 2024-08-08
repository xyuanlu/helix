package org.apache.helix.gateway.service;

import static org.apache.helix.gateway.constant.GatewayServiceGrpcDefaultConfig.*;


public class GatewayServiceProcessorConfig {
  public enum ProcessorType {
    GRPC_SERVER, PULL_GRPC_CLIENT, PULL_FILE_SHARE
  }

  // service configs
  private ProcessorType _participantConnectionProcessorType;
  private ProcessorType _shardStatenProcessorType;

  // grpc server configs
  private int _grpcServerPort;
  private int serverHeartBeatInterval;
  private int maxAllowedClientHeartBeatInterval;
  private int clientTimeout;
  private boolean enableReflectionService;

  // pull mode config
  private int pullIntervalSec;
  // pull mode grpc client configs

  // pull mode file  configs

  // getters
  public ProcessorType getParticipantConnectionProcessorType() {
    return _participantConnectionProcessorType;
  }

  public ProcessorType getShardStatenProcessorType() {
    return _shardStatenProcessorType;
  }

  public int getGrpcServerPort() {
    return _grpcServerPort;
  }

  public int getServerHeartBeatInterval() {
    return serverHeartBeatInterval;
  }

  public int getMaxAllowedClientHeartBeatInterval() {
    return maxAllowedClientHeartBeatInterval;
  }

  public int getClientTimeout() {
    return clientTimeout;
  }

  public boolean getEnableReflectionService() {
    return enableReflectionService;
  }

  public int getPullIntervalSec() {
    return pullIntervalSec;
  }

  public GatewayServiceProcessorConfig(int grpcServerPort, ProcessorType participantConnectionProcessorType,
      ProcessorType shardStatenProcessorType, int serverHeartBeatInterval, int maxAllowedClientHeartBeatInterval,
      int clientTimeout, boolean enableReflectionService, int pullIntervalSec) {
    _grpcServerPort = grpcServerPort;
    _participantConnectionProcessorType = participantConnectionProcessorType;
    _shardStatenProcessorType = shardStatenProcessorType;
    this.serverHeartBeatInterval = serverHeartBeatInterval;
    this.maxAllowedClientHeartBeatInterval = maxAllowedClientHeartBeatInterval;
    this.clientTimeout = clientTimeout;
    this.enableReflectionService = enableReflectionService;
    this.pullIntervalSec = pullIntervalSec;
  }

  public static class GatewayServiceProcessorConfigBuilder {

    // service configs
    private ProcessorType _participantConnectionProcessorType = ProcessorType.GRPC_SERVER;
    private ProcessorType _shardStatenProcessorType = ProcessorType.GRPC_SERVER;

    // grpc server configs
    private int _grpcServerPort;
    private int serverHeartBeatInterval = DEFAULT_SERVER_HEARTBEAT_INTERVAL;
    private int maxAllowedClientHeartBeatInterval = DEFAULT_AMX_ALLOWED_CLIENT_HEARTBEAT_INTERVAL;
    private int clientTimeout = DEFAULT_CLIENT_TIMEOUT;
    private boolean enableReflectionService = true;

    // pull mode config
    private int pullIntervalSec = 60;
    // pull mode grpc client configs

    // pull mode file  configs

    public GatewayServiceProcessorConfigBuilder setParticipantConnectionProcessorType(ProcessorType processorType) {
      _participantConnectionProcessorType = processorType;
      return this;
    }

    public GatewayServiceProcessorConfigBuilder setShardStateProcessorType(ProcessorType processorType) {
      _shardStatenProcessorType = processorType;
      return this;
    }

    public GatewayServiceProcessorConfigBuilder setGrpcServerPort(int grpcServerPort) {
      _grpcServerPort = grpcServerPort;
      return this;
    }

    public void validate() {

    }

    public GatewayServiceProcessorConfig build() {
      validate();
      return new GatewayServiceProcessorConfig(_grpcServerPort, _participantConnectionProcessorType,
          _shardStatenProcessorType, serverHeartBeatInterval, maxAllowedClientHeartBeatInterval, clientTimeout,
          enableReflectionService, pullIntervalSec);
    }
  }
}
