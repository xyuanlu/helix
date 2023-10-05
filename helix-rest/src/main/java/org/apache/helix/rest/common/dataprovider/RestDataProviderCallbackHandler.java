package org.apache.helix.rest.common.dataprovider;


// call back handler for cached data change. One handler per cluster
// update the cache when data changes

import org.apache.helix.NotificationContext;
import org.apache.helix.manager.zk.CallbackHandler;
import org.apache.helix.zookeeper.zkclient.RecursivePersistListener;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.helix.HelixConstants.ChangeType.*;


public class RestDataProviderCallbackHandler implements RecursivePersistListener {
  private static Logger logger = LoggerFactory.getLogger(RestDataProviderCallbackHandler.class);

  HelixRestDataProviderUpdater _updater;
  @Override
  public void handleZNodeChange(String dataPath, Watcher.Event.EventType eventType) throws Exception {
    // consolidate change context and call enqueue event
  }

  public void enqueueTask(NotificationContext changeContext) throws Exception {
      // enqueue task
    // queue should be a dedup queue

  }
  public void invoke(NotificationContext changeContext) throws Exception {
    if (changeContext.getChangeType() == IDEAL_STATE) {
      // call on ideal state change
      logger.info("IDEAL_STATE change");

    } else if (changeContext.getChangeType() == INSTANCE_CONFIG) {
      logger.info("INSTANCE_CONFIG change");
    }

  }
}
