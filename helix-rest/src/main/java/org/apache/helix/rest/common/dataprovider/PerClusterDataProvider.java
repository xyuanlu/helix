package org.apache.helix.rest.common.dataprovider;

import java.util.Map;
import org.apache.helix.HelixDataAccessor;
import org.apache.helix.PropertyKey;
import org.apache.helix.common.caches.PropertyCache;
import org.apache.helix.model.ClusterConfig;
import org.apache.helix.model.ClusterConstraints;
import org.apache.helix.model.CurrentState;
import org.apache.helix.model.ExternalView;
import org.apache.helix.model.IdealState;
import org.apache.helix.model.InstanceConfig;
import org.apache.helix.model.LiveInstance;
import org.apache.helix.model.ResourceConfig;
import org.apache.helix.model.StateModelDefinition;


// TODO: add init to this and property caches
// Add key function and root key


public class PerClusterDataProvider {

  private final String _clusterName;

  private final RestPropertyCache<InstanceConfig> _instanceConfigCache;
  private final RestPropertyCache<ClusterConfig> _clusterConfigCache;
  private final RestPropertyCache<ResourceConfig> _resourceConfigCache;
  private final RestPropertyCache<LiveInstance> _liveInstanceCache;
  private final RestPropertyCache<IdealState> _idealStateCache;
  private final RestPropertyCache<StateModelDefinition> _stateModelDefinitionCache;

  // special cache
  private final RestCurrentStateCache _currentStateCache;

  public PerClusterDataProvider(String clusterName) {
    _clusterName = clusterName;
    _clusterConfigCache = new RestPropertyCache<>("clusterConfig", new RestPropertyCache.PropertyCacheKeyFuncs<ClusterConfig>() {
      @Override
      public PropertyKey getRootKey(HelixDataAccessor accessor) {
        return accessor.keyBuilder().clusterConfig();
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

  public ClusterStatusSnapshot getClusterStatusSnapshot() {
    return new ClusterStatusSnapshot(_clusterName);
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

  public StateModelDefinition getStateModelDef(String stateModelDefRef) {
    return _stateModelDefinitionCache.getPropertyMap().get(stateModelDefRef);
  }

  // TODO: consolidate EV from CSs
  public Map<String, ExternalView> consolidateExternalViews() {
    return null;
  }
}
