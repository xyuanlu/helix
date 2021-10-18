package org.apache.helix.rest.server.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class OperationImpl implements OperationAbstractClass {


  // checks
  @Override
  public MaintenanceManagementInstanceInfo operationCheckForTakeSingleInstance(String instanceName,
      Map<String, String> operationConfig) {
    return new MaintenanceManagementInstanceInfo(MaintenanceManagementInstanceInfo.Status.SUCCESS,
        Arrays.asList("operationCheckForTakeSingleInstance"));
  }

  @Override
  public MaintenanceManagementInstanceInfo operationCheckForFreeSingleInstance() {
    return new MaintenanceManagementInstanceInfo(MaintenanceManagementInstanceInfo.Status.SUCCESS,
        Arrays.asList("operationCheckForFreeSingleInstance"));
  }

  @Override
  public Map<String, MaintenanceManagementInstanceInfo> operationCheckForTakeInstances(
      Collection<String> instances, Map<String, String> operationConfig) {
    Map<String, MaintenanceManagementInstanceInfo> result = new HashMap<>();
    result.put("test", new MaintenanceManagementInstanceInfo(MaintenanceManagementInstanceInfo.Status.SUCCESS,
        Collections.singletonList("operationCheckForFreeSingleInstance")));
    return result;
  }

  @Override
  public Map<String, MaintenanceManagementInstanceInfo> operationCheckForFreeInstances() {
    Map<String, MaintenanceManagementInstanceInfo> result = new HashMap<>();
    result.put("test", new MaintenanceManagementInstanceInfo(MaintenanceManagementInstanceInfo.Status.SUCCESS,
        Collections.singletonList("operationCheckForFreeInstances")));
    return result;
  }

  // executes
  @Override
  public MaintenanceManagementInstanceInfo operationExecForTakeSingleInstance(String instanceName,
      Map<String, String> operationConfig) {
    return new MaintenanceManagementInstanceInfo(MaintenanceManagementInstanceInfo.Status.SUCCESS,
        Arrays.asList("operationExecForTakeSingleInstance"));
  }

  @Override
  public MaintenanceManagementInstanceInfo operationExecForFreeSingleInstance() {
    return new MaintenanceManagementInstanceInfo(MaintenanceManagementInstanceInfo.Status.SUCCESS,
        Arrays.asList("operationExecForFreeSingleInstance"));
  }

  @Override
  public Map<String, MaintenanceManagementInstanceInfo> operationExecForTakeInstances(
      Collection<String> instances, Map<String, String> operationConfig) {
    Map<String, MaintenanceManagementInstanceInfo> result = new HashMap<>();
    result.put("test", new MaintenanceManagementInstanceInfo(MaintenanceManagementInstanceInfo.Status.SUCCESS,
        Collections.singletonList("operationExecForTakeInstances")));
    return result;
  }

  @Override
  public Map<String, MaintenanceManagementInstanceInfo> operationExecForFreeInstances() {
    Map<String, MaintenanceManagementInstanceInfo> result = new HashMap<>();
    result.put("test", new MaintenanceManagementInstanceInfo(MaintenanceManagementInstanceInfo.Status.SUCCESS,
        Collections.singletonList("operationExecForFreeInstances")));
    return result;
  }
}
