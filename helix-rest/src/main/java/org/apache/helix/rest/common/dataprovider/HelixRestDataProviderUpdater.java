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

import java.util.List;
import org.apache.helix.HelixDataAccessor;
import org.apache.helix.NotificationContext;
import org.apache.helix.api.listeners.ClusterConfigChangeListener;
import org.apache.helix.api.listeners.ControllerChangeListener;
import org.apache.helix.api.listeners.CurrentStateChangeListener;
import org.apache.helix.api.listeners.CustomizedStateChangeListener;
import org.apache.helix.api.listeners.CustomizedStateConfigChangeListener;
import org.apache.helix.api.listeners.CustomizedStateRootChangeListener;
import org.apache.helix.api.listeners.IdealStateChangeListener;
import org.apache.helix.api.listeners.InstanceConfigChangeListener;
import org.apache.helix.api.listeners.LiveInstanceChangeListener;
import org.apache.helix.api.listeners.MessageListener;
import org.apache.helix.api.listeners.ResourceConfigChangeListener;
import org.apache.helix.api.listeners.TaskCurrentStateChangeListener;
import org.apache.helix.model.ClusterConfig;
import org.apache.helix.model.CurrentState;
import org.apache.helix.model.CustomizedState;
import org.apache.helix.model.CustomizedStateConfig;
import org.apache.helix.model.IdealState;
import org.apache.helix.model.InstanceConfig;
import org.apache.helix.model.LiveInstance;
import org.apache.helix.model.Message;
import org.apache.helix.model.ResourceConfig;


// actually handels event change and update PerCLusterDataProvider
public class HelixRestDataProviderUpdater implements IdealStateChangeListener, LiveInstanceChangeListener,
                                                     MessageListener, CurrentStateChangeListener,
                                                     TaskCurrentStateChangeListener, CustomizedStateRootChangeListener,
                                                     CustomizedStateChangeListener, CustomizedStateConfigChangeListener,
                                                     ControllerChangeListener, InstanceConfigChangeListener,
                                                     ResourceConfigChangeListener, ClusterConfigChangeListener {


  private HelixDataAccessor _accessor;
  private PerClusterDataProvider _dataProvider;

 public HelixRestDataProviderUpdater(HelixDataAccessor accessor, PerClusterDataProvider dataProvider ){
   _accessor = accessor;
   _dataProvider = dataProvider;

 }
  @Override
  public void onStateChange(String instanceName, List<CurrentState> statesInfo, NotificationContext changeContext) {

  }

  @Override
  public void onClusterConfigChange(ClusterConfig clusterConfig, NotificationContext context) {

  }

  @Override
  public void onControllerChange(NotificationContext changeContext) {

  }

  @Override
  public void onCustomizedStateChange(String instanceName, List<CustomizedState> customizedStatesInfo,
      NotificationContext changeContext) {

  }

  @Override
  public void onCustomizedStateConfigChange(CustomizedStateConfig customizedStateConfig, NotificationContext context) {

  }

  @Override
  public void onCustomizedStateRootChange(String instanceName, List<String> customizedStateTypes,
      NotificationContext changeContext) {

  }

  @Override
  public void onInstanceConfigChange(List<InstanceConfig> instanceConfigs, NotificationContext context) {

  }

  @Override
  public void onLiveInstanceChange(List<LiveInstance> liveInstances, NotificationContext changeContext) {

  }

  @Override
  public void onMessage(String instanceName, List<Message> messages, NotificationContext changeContext) {

  }

  @Override
  public void onResourceConfigChange(List<ResourceConfig> resourceConfigs, NotificationContext context) {

  }

  @Override
  public void onTaskCurrentStateChange(String instanceName, List<CurrentState> statesInfo,
      NotificationContext changeContext) {

  }

  @Override
  public void onIdealStateChange(List<IdealState> idealState, NotificationContext changeContext)
      throws InterruptedException {

  }
}
