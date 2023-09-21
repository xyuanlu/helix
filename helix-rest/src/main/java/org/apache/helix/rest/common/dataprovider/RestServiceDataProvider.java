package org.apache.helix.rest.common.dataprovider;

import java.util.HashMap;
import java.util.Map;


public class RestServiceDataProvider {
  protected Map<String, PerClusterDataProvider> _clusterDataProviders ;


  public RestServiceDataProvider() {
    _clusterDataProviders = new HashMap<>();
  }

  public ClusterStatusSnapshot getClusterStatusSnapshot(String clusterName) {
    return _clusterDataProviders.get(clusterName).getClusterStatusSnapshot();
  }

}
