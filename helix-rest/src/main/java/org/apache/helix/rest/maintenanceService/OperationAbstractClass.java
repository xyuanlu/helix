package org.apache.helix.rest.maintenanceService;

import java.util.Collection;
import java.util.Map;


public interface OperationAbstractClass {

  // add reference to cache in InstanceService here

  // operation check
  MaintenanceManagementInstanceInfo operationCheckForTakeSingleInstance(String instanceName,
       Map<String, String> operationConfig);

  MaintenanceManagementInstanceInfo operationCheckForFreeSingleInstance();

  Map<String, MaintenanceManagementInstanceInfo> operationCheckForTakeInstances(
      Collection<String> instances, Map<String, String> operationConfig);

  Map<String, MaintenanceManagementInstanceInfo> operationCheckForFreeInstances();

  // operation execute
  MaintenanceManagementInstanceInfo operationExecForTakeSingleInstance(String instanceName,
      Map<String, String> operationConfig);

  MaintenanceManagementInstanceInfo operationExecForFreeSingleInstance();

  Map<String, MaintenanceManagementInstanceInfo> operationExecForTakeInstances(
      Collection<String> instances, Map<String, String> operationConfig);

  Map<String, MaintenanceManagementInstanceInfo> operationExecForFreeInstances();
}
