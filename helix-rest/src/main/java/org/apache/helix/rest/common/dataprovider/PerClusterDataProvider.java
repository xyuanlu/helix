package org.apache.helix.rest.common.dataprovider;

public class PerClusterDataProvider {

  private final String _clusterName;

  public PerClusterDataProvider(String clusterName) {
    _clusterName = clusterName;
  }

  public ClusterStatusSnapshot getClusterStatusSnapshot() {
    return new ClusterStatusSnapshot(_clusterName);
  }
}
