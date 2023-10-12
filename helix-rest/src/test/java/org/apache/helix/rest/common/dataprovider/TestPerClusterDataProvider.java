package org.apache.helix.rest.common.dataprovider;

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


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.helix.HelixDataAccessor;
import org.apache.helix.manager.zk.ZKHelixDataAccessor;
import org.apache.helix.model.ClusterConfig;
import org.apache.helix.model.IdealState;
import org.apache.helix.model.InstanceConfig;
import org.apache.helix.model.ResourceConfig;
import org.apache.helix.rest.server.AbstractTestClass;
import org.apache.helix.rest.server.ServerContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class TestPerClusterDataProvider extends AbstractTestClass {

  @BeforeClass
  public void beforeClass() {
    for (String cluster : _clusters) {
      ClusterConfig clusterConfig = createClusterConfig(cluster);
      _configAccessor.setClusterConfig(cluster, clusterConfig);
    }
  }

  private ClusterConfig createClusterConfig(String cluster) {
    ClusterConfig clusterConfig = _configAccessor.getClusterConfig(cluster);

    clusterConfig.setPersistBestPossibleAssignment(true);
    clusterConfig.getRecord().setSimpleField("SimpleField1", "Value1");
    clusterConfig.getRecord().setSimpleField("SimpleField2", "Value2");

    clusterConfig.getRecord().setListField("ListField1", Arrays.asList("Value1", "Value2", "Value3"));
    clusterConfig.getRecord().setListField("ListField2", Arrays.asList("Value2", "Value1", "Value3"));

    clusterConfig.getRecord().setMapField("MapField1", new HashMap<String, String>() {
      {
        put("key1", "value1");
        put("key2", "value2");
      }
    });
    clusterConfig.getRecord().setMapField("MapField2", new HashMap<String, String>() {
      {
        put("key3", "value1");
        put("key4", "value2");
      }
    });

    return clusterConfig;
  }

  @Test
  public void testPerClusterDataProvider() {
    System.out.println("123");
    ServerContext serverContext = new ServerContext(ZK_ADDR);
    HelixRestDataProviderManager manager =
        new HelixRestDataProviderManager(serverContext.getRealmAwareZkClient(), serverContext.getHelixAdmin());
    RestServiceDataProvider restServiceDataProvider = manager.getRestServiceDataProvider();
    // compare data get using data accessor the same as the one get from restServiceDataProvider.
    for (String cluster : _clusters) {
      PerClusterDataProvider clusterDataProvider = restServiceDataProvider.getClusterData(cluster);
      ClusterConfig clusterConfig = _configAccessor.getClusterConfig(cluster);
      Assert.assertTrue(clusterConfig.getRecord().equals(clusterDataProvider.getClusterConfig().getRecord()));

      Map<String, InstanceConfig> instanceConfigMap = clusterDataProvider.getInstanceConfigMap();
      for (String instanceName : instanceConfigMap.keySet()) {
        Assert.assertTrue(instanceConfigMap.get(instanceName).getRecord()
            .equals(_configAccessor.getInstanceConfig(cluster, instanceName).getRecord()));
      }

      Map<String, ResourceConfig> resourceConfigMap = clusterDataProvider.getResourceConfigMap();
      for (String resourceName : resourceConfigMap.keySet()) {
        Assert.assertTrue(resourceConfigMap.get(resourceName).getRecord()
            .equals(_configAccessor.getResourceConfig(cluster, resourceName).getRecord()));
      }

      HelixDataAccessor helixDataAccessor = new ZKHelixDataAccessor(cluster, _baseAccessor);
      Map<String, IdealState> idealStateMap = clusterDataProvider.getIdealStates();
      for(String resourceName : idealStateMap.keySet()) {
        Assert.assertTrue(idealStateMap.get(resourceName).getRecord()
            .equals(helixDataAccessor.getProperty(helixDataAccessor.keyBuilder().idealStates(resourceName)).getRecord()));
      }

    }
  }
}
