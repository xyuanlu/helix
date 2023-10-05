package org.apache.helix.rest.common.dataprovider;

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
