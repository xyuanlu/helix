package org.apache.helix.rest.maintenanceService;

import java.util.ArrayList;
import java.util.List;


public class MaintenanceManagementInstanceInfo {

  public enum Status {
    SUCCESS,
    FAILURE
  }
  private List<Object> operationResult;
  private Status status;
  private List<String> messages;

  public MaintenanceManagementInstanceInfo(Status status) {
    this.status = status;
    this.operationResult = new ArrayList<>();
    this.messages = new ArrayList<>();
  }

  public MaintenanceManagementInstanceInfo(Status status, List<Object> newoperationResult) {
    this.status = status;
    this.operationResult = new ArrayList<>(newoperationResult);
    this.messages = new ArrayList<>();
  }

  public List<String> getMessages(){
    return messages;
  }

  public List<Object> getOperationResult(){
    return operationResult;
  }

  public void addFailureMessage(List<String> msg) {
    messages.addAll(msg);
  }

  public boolean isSuccessful() {return status.equals(Status.SUCCESS);}

  public void mergeResult(MaintenanceManagementInstanceInfo info) {
    operationResult.addAll(info.getOperationResult());
    messages.addAll(info.getMessages());
    status =  info.isSuccessful() && isSuccessful() ? Status.SUCCESS : Status.FAILURE;
  }

}
