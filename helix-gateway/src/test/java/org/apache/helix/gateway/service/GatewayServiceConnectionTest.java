package org.apache.helix.gateway.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.helix.gateway.base.HelixGatewayTestBase;
import org.apache.helix.gateway.util.GatewayGrpcServiceInitHelper;
import org.testng.annotations.Test;
import proto.org.apache.helix.gateway.HelixGatewayServiceGrpc;
import proto.org.apache.helix.gateway.HelixGatewayServiceOuterClass;


public class GatewayServiceConnectionTest extends HelixGatewayTestBase {

  @Test
  public void TestLivenessDetection() throws IOException, InterruptedException {
    // start the gateway service
    Server server = GatewayGrpcServiceInitHelper.createGatewayServer(50051);
    server.start();

    // start the client
    HelixGatewayClient client = new HelixGatewayClient("localhost", 50051);
    client.connect();

    Thread.sleep(1000);  // Wait for the client to send the initial message
    client.shutdown();

  }

  public class HelixGatewayClient {

    private final ManagedChannel channel;
    private final HelixGatewayServiceGrpc.HelixGatewayServiceStub asyncStub;

    public HelixGatewayClient(String host, int port) {
      this.channel = ManagedChannelBuilder.forAddress(host, port)
          .usePlaintext()
          .keepAliveTime(30, TimeUnit.SECONDS)  // KeepAlive time
          .keepAliveTimeout(3, TimeUnit.MINUTES)  // KeepAlive timeout
          .keepAliveWithoutCalls(true)  // Allow KeepAlive without active RPCs
          .build();
      this.asyncStub = HelixGatewayServiceGrpc.newStub(channel);
    }

    public void connect() {
      StreamObserver<HelixGatewayServiceOuterClass.ShardStateMessage> requestObserver =
          asyncStub.report(new StreamObserver<HelixGatewayServiceOuterClass.TransitionMessage>() {
            @Override
            public void onNext(HelixGatewayServiceOuterClass.TransitionMessage value) {
              // Handle response from server
            }

            @Override
            public void onError(Throwable t) {
              // Handle error
            }

            @Override
            public void onCompleted() {
              // Handle stream completion
            }
          });

      // Send initial ShardStateMessage
      HelixGatewayServiceOuterClass.ShardStateMessage initialMessage =
          HelixGatewayServiceOuterClass.ShardStateMessage.newBuilder()
              .setShardState(HelixGatewayServiceOuterClass.ShardState.newBuilder()
                  .setInstanceName("instance1")
                  .setClusterName("TEST_CLUSTER")
                  .build())
              .build();
      requestObserver.onNext(initialMessage);

      // Add more logic to send additional messages if needed
    }

    public void shutdown() throws InterruptedException {
      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }

  class MockGatewayServiceManager extends GatewayServiceManager {
    @Override

  }
}
