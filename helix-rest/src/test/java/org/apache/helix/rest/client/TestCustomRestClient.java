package org.apache.helix.rest.client;

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

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestCustomRestClient {
  private static final String HTTP_LOCALHOST = "http://localhost:1000";
  @Mock
  HttpClient _httpClient;

  @BeforeMethod
  public void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testGetInstanceStoppableCheck() throws IOException {
    MockCustomRestClient customRestClient = new MockCustomRestClient(_httpClient);
    String jsonResponse = "{\n" + "   \"check1\": \"false\",\n" + "   \"check2\": \"true\"\n" + "}";

    HttpResponse httpResponse = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);

    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    customRestClient.setJsonResponse(jsonResponse);
    when(_httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);

    Map<String, Boolean> healthCheck =
        customRestClient.getInstanceStoppableCheck(HTTP_LOCALHOST, Collections.emptyMap());
    Assert.assertFalse(healthCheck.get("check1"));
    Assert.assertTrue(healthCheck.get("check2"));
  }

  @Test(expectedExceptions = ClientProtocolException.class)
  public void testGetInstanceStoppableCheck_when_url_404() throws IOException {
    MockCustomRestClient customRestClient = new MockCustomRestClient(_httpClient);
    HttpResponse httpResponse = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);

    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    when(_httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);

    customRestClient.getInstanceStoppableCheck(HTTP_LOCALHOST, Collections.emptyMap());
  }

  @Test(expectedExceptions = IOException.class)
  public void testGetInstanceStoppableCheck_when_response_empty() throws IOException {
    MockCustomRestClient customRestClient = new MockCustomRestClient(_httpClient);
    HttpResponse httpResponse = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);

    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    when(_httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
    customRestClient.setJsonResponse("");

    customRestClient.getInstanceStoppableCheck(HTTP_LOCALHOST, Collections.emptyMap());
  }

  @Test
  public void testGetPartitionStoppableCheck() throws IOException {
    MockCustomRestClient customRestClient = new MockCustomRestClient(_httpClient);
    String jsonResponse = "\n" + "{\n" + "   \"db1\": {\n" + "      \"IS_HEALTHY\": \"false\"\n"
        + "   },\n" + "   \"db0\": {\n" + "      \"IS_HEALTHY\": \"true\"\n" + "   }\n" + "}";

    HttpResponse httpResponse = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);

    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    customRestClient.setJsonResponse(jsonResponse);
    when(_httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);

    Map<String, Boolean> partitionHealth = customRestClient.getPartitionStoppableCheck(HTTP_LOCALHOST,
        ImmutableList.of("db0", "db1"), Collections.emptyMap());

    Assert.assertTrue(partitionHealth.get("db0"));
    Assert.assertFalse(partitionHealth.get("db1"));
  }

  @Test(expectedExceptions = ClientProtocolException.class)
  public void testGetPartitionStoppableCheck_when_url_404() throws IOException {
    MockCustomRestClient customRestClient = new MockCustomRestClient(_httpClient);

    HttpResponse httpResponse = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);

    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    when(_httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);

    customRestClient.getPartitionStoppableCheck(HTTP_LOCALHOST,
        ImmutableList.of("db0", "db1"), Collections.emptyMap());
  }

  @Test(expectedExceptions = IOException.class)
  public void testGetPartitionStoppableCheck_when_response_empty() throws IOException {
    MockCustomRestClient customRestClient = new MockCustomRestClient(_httpClient);
    HttpResponse httpResponse = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);

    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    when(_httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
    customRestClient.setJsonResponse("");

    customRestClient.getPartitionStoppableCheck(HTTP_LOCALHOST,
        ImmutableList.of("db0", "db1"), Collections.emptyMap());
  }

  @Test (description = "Validate if the post request has the correct format")
  public void testPostRequestFormat() throws IOException {
    // a popular echo server that echos all the inputs
    // TODO: add a mock rest server
    final String echoServer = "http://httpbin.org/post";
    CustomRestClientImpl customRestClient = new CustomRestClientImpl(HttpClients.createDefault());
    HttpResponse response = customRestClient.post(echoServer, Collections.emptyMap());
    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
      JsonNode json = customRestClient.getJsonObject(response);

      Assert.assertEquals(json.get("headers").get("Accept").asText(), "application/json");
      Assert.assertEquals(json.get("data").asText(), "{}");
    }
  }

  @Test (description = "Validate if the post request has memory leak or no")
  public void testMultiplePost() throws IOException {
    // a popular echo server that echos all the inputs
    final String echoServer = "https://httpbin.org/redirect-to?url=http://httpbin.org/post";
    HttpClientBuilder httpClientBuilder = HttpClients.custom()
        .evictExpiredConnections()
        .setMaxConnPerRoute(1)
        .evictIdleConnections(Duration.ofSeconds(30).toMillis(), TimeUnit.MILLISECONDS);
    HttpClient httpClient = httpClientBuilder.build();
    CustomRestClientImpl customRestClient = new CustomRestClientImpl(httpClient);
    HttpResponse response;

    for (int i = 0; i < 5; i++) {
      response = customRestClient.post(echoServer, Collections.emptyMap());
      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        JsonNode json = customRestClient.getJsonObject(response);

        Assert.assertEquals(json.get("headers").get("Accept").asText(), "application/json");
        Assert.assertEquals(json.get("data").asText(), "{}");
      }
    }
  }

  @Test
  public void testGetPartitionStoppableCheckWhenTimeout() throws IOException {
    MockCustomRestClient customRestClient = new MockCustomRestClient(_httpClient);

    HttpResponse httpResponse = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);

    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    when(_httpClient.execute(any(HttpPost.class)))
        .thenThrow(new ConnectTimeoutException("Timeout Exception Happened!"));

    boolean timeoutExceptionHappened = false;
    try {
      customRestClient.getPartitionStoppableCheck(HTTP_LOCALHOST, ImmutableList.of("db0", "db1"),
          Collections.emptyMap());
    } catch (ConnectTimeoutException e) {
      timeoutExceptionHappened = true;
    }
    Assert.assertTrue(timeoutExceptionHappened);
  }

  private class MockCustomRestClient extends CustomRestClientImpl {
    private String _jsonResponse = "";

    MockCustomRestClient(HttpClient mockHttpClient) {
      super(mockHttpClient);
    }

    void setJsonResponse(String response) {
      _jsonResponse = response;
    }

    @Override
    protected JsonNode getJsonObject(HttpResponse httpResponse) throws IOException {
      return new ObjectMapper().readTree(_jsonResponse);
    }
  }

  @Test
  public void testGetAggregatedStoppableCheck() throws IOException {
    MockCustomRestClient customRestClient = new MockCustomRestClient(_httpClient);
    String jsonResponse = "{\n \"stoppableInstances\" : [\"n1\", \"n2\", \"n3\"],\n \"nonStoppableInstancesWithReasons\": "
        + "{\n    \"n4\": \"ERROR_PARTITION STILL_BOOTSTRAPPING\",\n    \"n10\": \"NOT_READY\"\n  }\n}";
    String clusterId = "cluster1";
    String[] instances = {"n1", "n2", "n3", "n4", "n10"};
    String[] healthyInstances = {"n1", "n2", "n3"};
    String[] nonStoppableInstances = {"n4", "n10"};

    HttpResponse httpResponse = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);

    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    customRestClient.setJsonResponse(jsonResponse);
    when(_httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);

    Map<String, List<String>> clusterHealth = customRestClient.getAggregatedStoppableCheck(HTTP_LOCALHOST,
        ImmutableList.of("n1", "n2", "n3", "n4", "n10"),
        ImmutableSet.of("n7", "n8"), clusterId, Collections.emptyMap());

    Assert.assertTrue(Arrays.stream(instances).allMatch(clusterHealth::containsKey));
    Assert.assertTrue(Arrays.stream(healthyInstances).allMatch(instance -> clusterHealth.get(instance).isEmpty()));
    Assert.assertTrue(Arrays.stream(nonStoppableInstances).noneMatch(instance -> clusterHealth.get(instance).isEmpty()));
  }

  @Test(description = "Test if the aggregated stoppable check request has the correct format when there"
      + "are duplicate instances in the instances list and the toBeStoppedInstances list.")
  public void testAggregatedCheckRemoveDuplicateInstances()
      throws IOException {
    String clusterId = "cluster1";

    MockCustomRestClient customRestClient = new MockCustomRestClient(_httpClient);
    HttpResponse httpResponse = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);

    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    when(_httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);

    customRestClient.getAggregatedStoppableCheck(HTTP_LOCALHOST,
        ImmutableList.of("n1", "n2"),
        ImmutableSet.of("n1"), clusterId, Collections.emptyMap());

    // Make sure that the duplicate instances are removed from the toBeStoppedInstances list
    ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    verify(_httpClient).execute(argThat(x -> {
      String request = null;
      try {
        request = EntityUtils.toString(((HttpPost) x).getEntity());
        JsonNode node = OBJECT_MAPPER.readTree(request);
        String instancesInRequest = node.get("instances").toString();
        Assert.assertEquals(instancesInRequest, "[\"n1\",\"n2\"]");
        Assert.assertNull(node.get("to_be_stopped_instances"));
        return true;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }));
  }
}
