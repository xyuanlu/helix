package org.apache.helix.gateway.util;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.util.concurrent.TimeUnit;
import org.apache.helix.gateway.service.GatewayServiceManager;


/**
 * Factory class to create GatewayServiceManager
 */
public class GatewayGrpcServiceInitHelper {
  public static final int DEFAULT_SERVER_HEARTBEAT_INTERVAL = 60;
  public static final int DEFAULT_AMX_ALLOWED_CLIENT_HEARTBEAT_INTERVAL = 60;
  public static final int DEFAULT_CLIENT_TIMEOUT = 5*60;

  public static Server createGatewayServer(int port) {
    GatewayServiceManager manager = new GatewayServiceManager();

    return new GatewayGrpcServerBuilder()
        .setPort(port)
        .setServerHeartBeatInterval(DEFAULT_SERVER_HEARTBEAT_INTERVAL)
        .setMaxAllowedClientHeartBeatInterval(DEFAULT_AMX_ALLOWED_CLIENT_HEARTBEAT_INTERVAL)
        .setClientTimeout(DEFAULT_CLIENT_TIMEOUT)
        .setGrpcService(manager.getGrpcService())
        .build();
  }

  public static class GatewayGrpcServerBuilder {
    private int port;
    private int serverHeartBeatInterval;
    private int maxAllowedClientHeartBeatInterval;
    private int clientTimeout;
    private BindableService service;

    public GatewayGrpcServerBuilder setPort(int port){
      this.port = port;
      return this;
    }

    public GatewayGrpcServerBuilder setServerHeartBeatInterval(int serverHeartBeatInterval){
      this.serverHeartBeatInterval = serverHeartBeatInterval;
      return this;
    }

    public GatewayGrpcServerBuilder setMaxAllowedClientHeartBeatInterval(int maxAllowedClientHeartBeatInterval){
      this.maxAllowedClientHeartBeatInterval = maxAllowedClientHeartBeatInterval;
      return this;
    }

    public GatewayGrpcServerBuilder setClientTimeout(int clientTimeout){
      this.clientTimeout = clientTimeout;
      return this;
    }

    public GatewayGrpcServerBuilder setGrpcService(BindableService service){
      this.service = service;
      return this;
    }

    public Server build() {
      validate();

      return ServerBuilder.forPort(port)
          .addService(service)
          .keepAliveTime(serverHeartBeatInterval, TimeUnit.SECONDS)  // HeartBeat time
          .keepAliveTimeout(clientTimeout, TimeUnit.SECONDS)  // KeepAlive timeout
          .permitKeepAliveTime(maxAllowedClientHeartBeatInterval, TimeUnit.SECONDS)  // Permit min HeartBeat time
          .permitKeepAliveWithoutCalls(true)  // Allow KeepAlive forever without active RPCs
          .build();
    }

    private void validate() {
      if (port == 0|| service == null) {
        throw new IllegalArgumentException("Port and service must be set");
      }
      if (clientTimeout < maxAllowedClientHeartBeatInterval) {
        throw new IllegalArgumentException("Client timeout is less than max allowed client heartbeat interval");
      }
    }
  }
}































