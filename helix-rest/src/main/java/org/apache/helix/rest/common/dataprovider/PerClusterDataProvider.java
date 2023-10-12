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

import java.util.Map;
import org.apache.helix.BaseDataAccessor;
import org.apache.helix.HelixDataAccessor;
import org.apache.helix.PropertyKey;
import org.apache.helix.common.caches.PropertyCache;
import org.apache.helix.manager.zk.ZKHelixDataAccessor;
import org.apache.helix.model.ClusterConfig;
import org.apache.helix.model.ClusterConstraints;
import org.apache.helix.model.CurrentState;
import org.apache.helix.model.ExternalView;
import org.apache.helix.model.IdealState;
import org.apache.helix.model.InstanceConfig;
import org.apache.helix.model.LiveInstance;
import org.apache.helix.model.ResourceConfig;
import org.apache.helix.model.StateModelDefinition;
import org.apache.helix.zookeeper.api.client.RealmAwareZkClient;


/**
 * Dara cache for each Helix cluster. Configs, ideal stats and current states are read from ZK and updated
 * using event changes. External view are consolidated using current state.
 */
public class PerClusterDataProvider {

  private HelixDataAccessor _accessor;

  private RealmAwareZkClient _zkclient;

  private final String _clusterName;

  // Simple caches
  private final RestPropertyCache<InstanceConfig> _instanceConfigCache;
  private final RestPropertyCache<ClusterConfig> _clusterConfigCache;
  private final RestPropertyCache<ResourceConfig> _resourceConfigCache;
  private final RestPropertyCache<LiveInstance> _liveInstanceCache;
  private final RestPropertyCache<IdealState> _idealStateCache;
  private final RestPropertyCache<StateModelDefinition> _stateModelDefinitionCache;

  // special caches
  private final RestCurrentStateCache _currentStateCache;

  // TODO: add external view caches

  public PerClusterDataProvider(String clusterName, RealmAwareZkClient zkClient, BaseDataAccessor baseDataAccessor) {
    _clusterName =  clusterName;
    _accessor = new ZKHelixDataAccessor(clusterName, baseDataAccessor);

    _zkclient = zkClient;
    _clusterConfigCache = new RestPropertyCache<>("clusterConfig", new RestPropertyCache.PropertyCacheKeyFuncs<ClusterConfig>() {
      @Override
      public PropertyKey getRootKey(HelixDataAccessor accessor) {
        return accessor.keyBuilder().clusterConfigs();
      }

      @Override
      public PropertyKey getObjPropertyKey(HelixDataAccessor accessor, String objName) {
        return accessor.keyBuilder().clusterConfig();
      }

      @Override
      public String getObjName(ClusterConfig obj) {
        return obj.getClusterName();
      }
    });
    _instanceConfigCache = new RestPropertyCache<>("instanceConfig", new RestPropertyCache.PropertyCacheKeyFuncs<InstanceConfig>() {
      @Override
      public PropertyKey getRootKey(HelixDataAccessor accessor) {
        return accessor.keyBuilder().instanceConfigs();
      }

      @Override
      public PropertyKey getObjPropertyKey(HelixDataAccessor accessor, String objName) {
        return accessor.keyBuilder().instanceConfig(objName);
      }

      @Override
      public String getObjName(InstanceConfig obj) {
        return obj.getInstanceName();
      }
    });
    _resourceConfigCache = new RestPropertyCache<>("resourceConfig", new RestPropertyCache.PropertyCacheKeyFuncs<ResourceConfig>() {
      @Override
      public PropertyKey getRootKey(HelixDataAccessor accessor) {
        return accessor.keyBuilder().resourceConfigs();
      }

      @Override
      public PropertyKey getObjPropertyKey(HelixDataAccessor accessor, String objName) {
        return accessor.keyBuilder().resourceConfig(objName);
      }

      @Override
      public String getObjName(ResourceConfig obj) {
        return obj.getResourceName();
      }
    });
    _liveInstanceCache = new RestPropertyCache<>("liveInstance", new RestPropertyCache.PropertyCacheKeyFuncs<LiveInstance>() {
      @Override
      public PropertyKey getRootKey(HelixDataAccessor accessor) {
        return accessor.keyBuilder().liveInstances();
      }

      @Override
      public PropertyKey getObjPropertyKey(HelixDataAccessor accessor, String objName) {
        return accessor.keyBuilder().liveInstance(objName);
      }

      @Override
      public String getObjName(LiveInstance obj) {
        return obj.getInstanceName();
      }
    });
    _idealStateCache = new RestPropertyCache<>("idealState", new RestPropertyCache.PropertyCacheKeyFuncs<IdealState>() {
      @Override
      public PropertyKey getRootKey(HelixDataAccessor accessor) {
        return accessor.keyBuilder().idealStates();
      }

      @Override
      public PropertyKey getObjPropertyKey(HelixDataAccessor accessor, String objName) {
        return accessor.keyBuilder().idealStates(objName);
      }

      @Override
      public String getObjName(IdealState obj) {
        return obj.getResourceName();
      }
    });
    _stateModelDefinitionCache = new RestPropertyCache<>("stateModelDefinition", new RestPropertyCache.PropertyCacheKeyFuncs<StateModelDefinition>() {
      @Override
      public PropertyKey getRootKey(HelixDataAccessor accessor) {
        return accessor.keyBuilder().stateModelDefs();
      }

      @Override
      public PropertyKey getObjPropertyKey(HelixDataAccessor accessor, String objName) {
        return accessor.keyBuilder().stateModelDef(objName);
      }

      @Override
      public String getObjName(StateModelDefinition obj) {
        return obj.getId();
      }
    });

    // TODO: add current state cache
    _currentStateCache = new RestCurrentStateCache();
  }

  public ClusterConfig getClusterConfig() {
    return _clusterConfigCache.getPropertyMap().get(_clusterName);
  }

  public String getClusterName() {
    return _clusterName;
  }

  public Map<String, IdealState> getIdealStates() {
    return _idealStateCache.getPropertyMap();
  }

  public IdealState getIdealState(String resourceName) {
    return _idealStateCache.getPropertyMap().get(resourceName);
  }

  public Map<String, LiveInstance> getLiveInstances() {
    return _liveInstanceCache.getPropertyMap();
  }

  public Map<String, CurrentState> getCurrentState(String instanceName, String clientSessionId) {
    return null;
  }

  public Map<String, InstanceConfig> getInstanceConfigMap() {
    return _instanceConfigCache.getPropertyMap();
  }

  public Map<String, ResourceConfig> getResourceConfigMap() {
    return _resourceConfigCache.getPropertyMap();
  }
  public StateModelDefinition getStateModelDef(String stateModelDefRef) {
    return _stateModelDefinitionCache.getPropertyMap().get(stateModelDefRef);
  }

  // TODO: consolidate EV from CSs
  public Map<String, ExternalView> consolidateExternalViews() {
    return null;
  }

  // Used for dummy cache. Remove later
  public void initCache(final HelixDataAccessor accessor) {
    _clusterConfigCache.init(accessor);
    _instanceConfigCache.init(accessor);
    _resourceConfigCache.init(accessor);
    _liveInstanceCache.init(accessor);
    _idealStateCache.init(accessor);
    _stateModelDefinitionCache.init(accessor);
    _currentStateCache.init(accessor);
  }

  public void initCache() {
    _clusterConfigCache.init(_accessor);
    _instanceConfigCache.init(_accessor);
    _resourceConfigCache.init(_accessor);
    _liveInstanceCache.init(_accessor);
    _idealStateCache.init(_accessor);
    _stateModelDefinitionCache.init(_accessor);
    _currentStateCache.init(_accessor);
  }
}
