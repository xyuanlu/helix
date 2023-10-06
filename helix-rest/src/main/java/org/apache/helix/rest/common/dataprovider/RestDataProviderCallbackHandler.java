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

// call back handler for cached data change. One handler per cluster
// update the cache when data changes

import org.apache.helix.NotificationContext;
import org.apache.helix.manager.zk.ZKHelixDataAccessor;
import org.apache.helix.manager.zk.ZkBaseDataAccessor;
import org.apache.helix.zookeeper.api.client.RealmAwareZkClient;
import org.apache.helix.zookeeper.datamodel.ZNRecord;
import org.apache.helix.zookeeper.zkclient.RecursivePersistListener;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.helix.HelixConstants.ChangeType.*;


public class RestDataProviderCallbackHandler implements RecursivePersistListener {
  private static Logger logger = LoggerFactory.getLogger(RestDataProviderCallbackHandler.class);

  HelixRestDataProviderUpdater _updater;
  PerClusterDataProvider _dataProvider;
  private final RealmAwareZkClient _zkClient;
  private final String _clusterName;

  public RestDataProviderCallbackHandler(RealmAwareZkClient zkClient, String clusterName,
      PerClusterDataProvider dataProvider) {
    _zkClient = zkClient;
    _clusterName = clusterName;
    _dataProvider = dataProvider;
    _updater = new HelixRestDataProviderUpdater(
        new ZKHelixDataAccessor(_clusterName, new ZkBaseDataAccessor<ZNRecord>(_zkClient)), _dataProvider);
    init();
  }

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

  private void subscribeClusterDataChange() {
    // call zkclient subscribe for change
  }

  private void init() {
    subscribeClusterDataChange();

  }
}
