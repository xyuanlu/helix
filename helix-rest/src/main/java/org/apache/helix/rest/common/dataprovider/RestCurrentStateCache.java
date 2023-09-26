package org.apache.helix.rest.common.dataprovider;

import java.util.concurrent.ConcurrentHashMap;
import org.apache.helix.HelixDataAccessor;
import org.apache.helix.model.CurrentState;


public class RestCurrentStateCache {

  private ConcurrentHashMap<String, ConcurrentHashMap<String, CurrentState>>  _objCache;

  public RestCurrentStateCache() {
    _objCache = new ConcurrentHashMap<>();
  }

  public void init(final HelixDataAccessor accessor) {
    //_objCache = new ConcurrentHashMap<>(accessor.getChildValuesMap(accessor.keyBuilder().currentStates(), true));
  }
}