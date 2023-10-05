package org.apache.helix.rest.common.dataprovider;

// init, register listener and manager callback handler for different clusters
// manage the providers lifecycle

import org.apache.helix.HelixAdmin;
import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.apache.helix.manager.zk.ZKHelixDataAccessor;
import org.apache.helix.zookeeper.api.client.RealmAwareZkClient;


public class HelixRestDataProviderManager {

  protected RealmAwareZkClient _zkclient;

  private HelixAdmin _helixAdmin;


  private RestServiceDataProvider _restServiceDataProvider;

  public HelixRestDataProviderManager(RealmAwareZkClient zkclient, HelixAdmin helixAdmin) {
    _zkclient = zkclient;
    _helixAdmin = helixAdmin;
    _restServiceDataProvider = new RestServiceDataProvider();
  }



  public RestServiceDataProvider getRestServiceDataProvider() {
    return _restServiceDataProvider;
  }
}
