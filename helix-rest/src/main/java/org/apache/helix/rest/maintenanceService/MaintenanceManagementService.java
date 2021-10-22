package org.apache.helix.rest.maintenanceService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import org.apache.helix.ConfigAccessor;
import org.apache.helix.HelixException;
import org.apache.helix.manager.zk.ZKHelixDataAccessor;
import org.apache.helix.model.CurrentState;
import org.apache.helix.model.ExternalView;
import org.apache.helix.model.InstanceConfig;
import org.apache.helix.model.LiveInstance;
import org.apache.helix.model.RESTConfig;
import org.apache.helix.rest.client.CustomRestClient;
import org.apache.helix.rest.client.CustomRestClientFactory;
import org.apache.helix.rest.common.HelixDataAccessorWrapper;
import org.apache.helix.rest.server.json.instance.InstanceInfo;
import org.apache.helix.rest.server.json.instance.StoppableCheck;
import org.apache.helix.rest.server.service.InstanceService;
import org.apache.helix.rest.server.service.InstanceServiceImpl;
import org.apache.helix.util.HelixUtil;
import org.apache.helix.util.InstanceValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MaintenanceManagementService {
  private static final Logger LOG = LoggerFactory.getLogger(InstanceServiceImpl.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final ExecutorService POOL = Executors.newCachedThreadPool();

  // Metric names for custom instance check
  private static final String CUSTOM_INSTANCE_CHECK_HTTP_REQUESTS_ERROR_TOTAL =
      MetricRegistry.name(InstanceService.class, "custom_instance_check_http_requests_error_total");
  private static final String CUSTOM_INSTANCE_CHECK_HTTP_REQUESTS_DURATION =
      MetricRegistry.name(InstanceService.class, "custom_instance_check_http_requests_duration");

  private final ConfigAccessor _configAccessor;
  private final CustomRestClient _customRestClient;
  private final String _namespace;
  private final boolean _skipZKRead;
  private final boolean _continueOnFailures;
  private final HelixDataAccessorWrapper _dataAccessor;


  public MaintenanceManagementService(ZKHelixDataAccessor dataAccessor, ConfigAccessor configAccessor,
      boolean skipZKRead, String namespace) {
    this(dataAccessor, configAccessor, CustomRestClientFactory.get(), skipZKRead, false, namespace);
  }

  public MaintenanceManagementService(ZKHelixDataAccessor dataAccessor, ConfigAccessor configAccessor,
      boolean skipZKRead, boolean continueOnFailures, String namespace) {
    this(dataAccessor, configAccessor, CustomRestClientFactory.get(), skipZKRead,
        continueOnFailures, namespace);
  }

  @VisibleForTesting
  MaintenanceManagementService(ZKHelixDataAccessor dataAccessor, ConfigAccessor configAccessor,
      CustomRestClient customRestClient, boolean skipZKRead, boolean continueOnFailures,
      String namespace) {
    _dataAccessor = new HelixDataAccessorWrapper(dataAccessor, customRestClient, namespace);
    _configAccessor = configAccessor;
    _customRestClient = customRestClient;
    _skipZKRead = skipZKRead;
    _continueOnFailures = continueOnFailures;
    _namespace = namespace;
  }

  public InstanceInfo getInstanceHealthInfo(String clusterId, String instanceName,
      List<HealthCheck> healthChecks) {
    InstanceInfo.Builder instanceInfoBuilder = new InstanceInfo.Builder(instanceName);

    InstanceConfig instanceConfig =
        _dataAccessor.getProperty(_dataAccessor.keyBuilder().instanceConfig(instanceName));
    LiveInstance liveInstance =
        _dataAccessor.getProperty(_dataAccessor.keyBuilder().liveInstance(instanceName));
    if (instanceConfig != null) {
      instanceInfoBuilder.instanceConfig(instanceConfig.getRecord());
    } else {
      LOG.warn("Missing instance config for {}", instanceName);
    }
    if (liveInstance != null) {
      instanceInfoBuilder.liveInstance(liveInstance.getRecord());
      String sessionId = liveInstance.getEphemeralOwner();

      List<String> resourceNames = _dataAccessor
          .getChildNames(_dataAccessor.keyBuilder().currentStates(instanceName, sessionId));
      instanceInfoBuilder.resources(resourceNames);
      List<String> partitions = new ArrayList<>();
      for (String resourceName : resourceNames) {
        CurrentState currentState = _dataAccessor.getProperty(
            _dataAccessor.keyBuilder().currentState(instanceName, sessionId, resourceName));
        if (currentState != null && currentState.getPartitionStateMap() != null) {
          partitions.addAll(currentState.getPartitionStateMap().keySet());
        } else {
          LOG.warn(
              "Current state is either null or partitionStateMap is missing. InstanceName: {}, SessionId: {}, ResourceName: {}",
              instanceName, sessionId, resourceName);
        }
      }
      instanceInfoBuilder.partitions(partitions);
    } else {
      LOG.warn("Missing live instance for {}", instanceName);
    }
    try {
      Map<String, Boolean> healthStatus =
          getInstanceHealthStatus(clusterId, instanceName, healthChecks);
      instanceInfoBuilder.healthStatus(healthStatus);
    } catch (HelixException ex) {
      LOG.error(
          "Exception while getting health status. Cluster: {}, Instance: {}, reporting health status as unHealth",
          clusterId, instanceName, ex);
      instanceInfoBuilder.healthStatus(false);
    }

    return instanceInfoBuilder.build();

  }


  public MaintenanceManagementInstanceInfo takeInstance(String clusterId, String instanceName,
      List<String> healthChecks, Map<String, String> healthCheckConfig, List<String> operations,
      Map<String, String> operationConfig, boolean performOperation) {

    // health check
    MaintenanceManagementInstanceInfo instanceInfo =
        batchInstanceHealthCheck(clusterId, ImmutableList.of(instanceName), healthChecks,
            healthCheckConfig).get(instanceName);
    if (!instanceInfo.isSuccessful()) {
      return instanceInfo;
    }

    // operation check and execute
    List<OperationAbstractClass> operationAbstractClassList = getAllOperationClasses(operations);
    // do all the checks
    for (OperationAbstractClass operationClass : operationAbstractClassList) {
      MaintenanceManagementInstanceInfo checkResult =
          operationClass.operationCheckForTakeSingleInstance(instanceName, operationConfig);
      if (!checkResult.isSuccessful()) {
        instanceInfo.mergeResult(checkResult);
      }
    }
    if (performOperation) {
      // do all operations
      for (OperationAbstractClass operationClass : operationAbstractClassList) {
        instanceInfo.mergeResult(
            operationClass.operationExecForTakeSingleInstance(instanceName, operationConfig));
        if (!instanceInfo.isSuccessful()) {
          break;
        }
      }
    }
    return instanceInfo;
  }

  public Map<String, MaintenanceManagementInstanceInfo> takeInstances(String clusterId,
      List<String> instances, List<String> healthChecks, Map<String, String> healthCheckConfig,
      List<String> operations, Map<String, String> operationConfig, boolean performOperation) {
    Set<String> instancesSet = new HashSet<>(instances);
    // health check
    Map<String, MaintenanceManagementInstanceInfo> instanceInfos =
        batchInstanceHealthCheck(clusterId, instances, healthChecks, healthCheckConfig);
    instanceInfos.forEach((key, value) -> {
      if (!value.isSuccessful()) {
        instancesSet.remove(key);
      }
    });

    // get all operation classes
    List<OperationAbstractClass> operationAbstractClassList = getAllOperationClasses(operations);

    // do all the checks
    for (OperationAbstractClass operationClass : operationAbstractClassList) {
      Map<String, MaintenanceManagementInstanceInfo> checkResults =
          operationClass.operationCheckForTakeInstances(instancesSet, operationConfig);
      checkResults.forEach((k, v) -> {
        if (!v.isSuccessful()) {
          instancesSet.remove(k);
          instanceInfos.get(k).mergeResult(v);
        }
      });
      if (instancesSet.isEmpty()) {
        return instanceInfos;
      }
    }
    if (performOperation) {
      // do all operations
      for (OperationAbstractClass operationClass : operationAbstractClassList) {
        Map<String, MaintenanceManagementInstanceInfo> checkResults =
            operationClass.operationExecForTakeInstances(instancesSet, operationConfig);
        checkResults.forEach((k, v) -> {
          if (!v.isSuccessful()) {
            instancesSet.remove(k);
          }
          instanceInfos.get(k).mergeResult(v);
        });
      }
    }
    return instanceInfos;
  }

  private List<OperationAbstractClass> getAllOperationClasses(List<String> operations) {
    List<OperationAbstractClass> operationAbstractClassList = new ArrayList<>();
    for (String operationClassName : operations) {
      try {
        OperationAbstractClass userOperation =
            (OperationAbstractClass) HelixUtil.loadClass(getClass(), operationClassName)
                .newInstance();
        operationAbstractClassList.add(userOperation);
      } catch (Exception e) {
        LOG.error("No operation class found:" + Arrays.toString(e.getStackTrace()));
      }
    }
    return operationAbstractClassList;
  }

  public StoppableCheck getInstanceStoppableCheck(String clusterId, String instanceName,
      String jsonContent) throws IOException {
    return batchGetInstancesStoppableChecks(clusterId, ImmutableList.of(instanceName), jsonContent)
        .get(instanceName);
  }

  public Map<String, StoppableCheck> batchGetInstancesStoppableChecks(String clusterId,
      List<String> instances, String jsonContent) throws IOException {
    System.out.println("batchGetInstancesStoppableChecks: jsonContent:  " + (jsonContent==null));
    // helix instance check
    Map<String, StoppableCheck> finalStoppableChecks = new HashMap<>();

    List<String> instancesForCustomInstanceLevelChecks =
        batchHelixInstanceStoppableCheck(clusterId, instances, finalStoppableChecks);
    // custom check, includes partition check.
    batchCustomInstanceStoppableCheck(clusterId, instancesForCustomInstanceLevelChecks,
        finalStoppableChecks, getCustomPayLoads(jsonContent));
    return finalStoppableChecks;


  }


  // finalStoppableChecks contains instances that does not pass this health check
  private List<String> batchHelixInstanceStoppableCheck(String clusterId,
      Collection<String> instances, Map<String, StoppableCheck> finalStoppableChecks) {
    Map<String, Future<StoppableCheck>> helixInstanceChecks = instances.stream().collect(Collectors
        .toMap(Function.identity(),
            instance -> POOL.submit(() -> performHelixOwnInstanceCheck(clusterId, instance))));
    return filterInstancesForNextCheck(helixInstanceChecks, finalStoppableChecks);
  }

  //
  private void batchCustomInstanceStoppableCheck(String clusterId, List<String> instances,
      Map<String, StoppableCheck> finalStoppableChecks, Map<String, String> customPayLoads) {
    if (instances.isEmpty() || customPayLoads == null) {
      // if all instances failed at previous checks then all checks are not required
      return;
    }
    RESTConfig restConfig = _configAccessor.getRESTConfig(clusterId);
    if (restConfig == null) {
      String errorMessage = String.format(
          "The cluster %s hasn't enabled client side health checks yet, "
              + "thus the stoppable check result is inaccurate", clusterId);
      LOG.error(errorMessage);
      return;
      // TODO: add exception back, this is only for POC
      // throw new HelixException(errorMessage);
    }
    Map<String, Future<StoppableCheck>> customInstanceLevelChecks = instances.stream().collect(
        Collectors.toMap(Function.identity(), instance -> POOL.submit(
            () -> performCustomInstanceCheck(clusterId, instance, restConfig.getBaseUrl(instance),
                customPayLoads))));
    List<String> instancesForCustomPartitionLevelChecks =
        filterInstancesForNextCheck(customInstanceLevelChecks, finalStoppableChecks);
    if (!instancesForCustomPartitionLevelChecks.isEmpty()) {
      Map<String, StoppableCheck> instancePartitionLevelChecks =
          performPartitionsCheck(instancesForCustomPartitionLevelChecks, restConfig,
              customPayLoads);
      for (Map.Entry<String, StoppableCheck> instancePartitionStoppableCheckEntry : instancePartitionLevelChecks
          .entrySet()) {
        String instance = instancePartitionStoppableCheckEntry.getKey();
        StoppableCheck stoppableCheck = instancePartitionStoppableCheckEntry.getValue();
        addStoppableCheck(finalStoppableChecks, instance, stoppableCheck);
      }
    }
  }

  // returned map is all instances with pass or fail
  private Map<String, MaintenanceManagementInstanceInfo> batchInstanceHealthCheck(String clusterId,
      List<String> instances, List<String> healthChecks, Map<String, String> healthCheckConfig) {
    List<String> instanceSetForNext = new ArrayList<>(instances);
    Map<String, MaintenanceManagementInstanceInfo> instanceInfos = new HashMap<>();
    Map<String, StoppableCheck> finalStoppableChecks = new HashMap<>();
    for (String healthCheck : healthChecks) {
      // should we add per zone filter?
      if (healthCheck.equals("HelixInstanceStoppableCheck")) {
        // this is helix own check
        List<String> instancesForCustomInstanceLevelChecks =
            batchHelixInstanceStoppableCheck(clusterId, instanceSetForNext, finalStoppableChecks);
        LOG.info("batchInstanceHealthCheck  instancesForCustomInstanceLevelChecks:"
            + instancesForCustomInstanceLevelChecks.toString());
        instanceSetForNext = instancesForCustomInstanceLevelChecks;
      } else if (healthCheck.equals("CostumeInstanceStoppableCheck")) {
        // custom check, includes custom Instance check and partition check.
        batchCustomInstanceStoppableCheck(clusterId, instanceSetForNext, finalStoppableChecks,
            healthCheckConfig);
      } else {
        throw new UnsupportedOperationException(healthCheck + " is not supported yet!");
      }
    }
    for (String instance : instances) {
      MaintenanceManagementInstanceInfo result;
      if (finalStoppableChecks.containsKey(instance) && !finalStoppableChecks.get(instance)
          .isStoppable()) {
        StoppableCheck instanceStoppableCheck = finalStoppableChecks.get(instance);
        result =
            new MaintenanceManagementInstanceInfo(MaintenanceManagementInstanceInfo.Status.FAILURE);
        result.addFailureMessage(instanceStoppableCheck.getFailedChecks());
        instanceSetForNext.remove(instance);
      } else {
        result =
            new MaintenanceManagementInstanceInfo(MaintenanceManagementInstanceInfo.Status.SUCCESS);
      }
      instanceInfos.put(instance, result);
    }
    return instanceInfos;
  }


  private void addStoppableCheck(Map<String, StoppableCheck> stoppableChecks, String instance,
      StoppableCheck stoppableCheck) {
    if (!stoppableChecks.containsKey(instance)) {
      stoppableChecks.put(instance, stoppableCheck);
    } else {
      // Merge two checks
      stoppableChecks.get(instance).add(stoppableCheck);
    }
  }

  private List<String> filterInstancesForNextCheck(
      Map<String, Future<StoppableCheck>> futureStoppableCheckByInstance,
      Map<String, StoppableCheck> finalStoppableCheckByInstance) {
    List<String> instancesForNextCheck = new ArrayList<>();
    for (Map.Entry<String, Future<StoppableCheck>> entry : futureStoppableCheckByInstance
        .entrySet()) {
      String instance = entry.getKey();
      LOG.info("filterInstancesForNextCheck. Instance: {}", instance);
      try {
        StoppableCheck stoppableCheck = entry.getValue().get();
        if (!stoppableCheck.isStoppable()) {
          // put the check result of the failed-to-stop instances
          addStoppableCheck(finalStoppableCheckByInstance, instance, stoppableCheck);
        }
        if (stoppableCheck.isStoppable() || _continueOnFailures){
          // instance passed this around of check or mandatory all checks
          // will be checked in the next round
          instancesForNextCheck.add(instance);
        }
      } catch (InterruptedException | ExecutionException e) {
        LOG.error("Failed to get StoppableChecks in parallel. Instance: {}", instance, e);
      }
    }

    return instancesForNextCheck;
  }

  private StoppableCheck performHelixOwnInstanceCheck(String clusterId, String instanceName) {
    LOG.info("Perform helix own custom health checks for {}/{}", clusterId, instanceName);
    Map<String, Boolean> helixStoppableCheck = getInstanceHealthStatus(clusterId, instanceName,
        HealthCheck.STOPPABLE_CHECK_LIST);

    return new StoppableCheck(helixStoppableCheck, StoppableCheck.Category.HELIX_OWN_CHECK);
  }

  private StoppableCheck performCustomInstanceCheck(String clusterId, String instanceName,
      String baseUrl, Map<String, String> customPayLoads) {
    LOG.info("Perform instance level client side health checks for {}/{}", clusterId, instanceName);
    MetricRegistry metrics = SharedMetricRegistries.getOrCreate(_namespace);

    // Total requests metric is included as an attribute(Count) in timers
    try (final Timer.Context timer = metrics.timer(CUSTOM_INSTANCE_CHECK_HTTP_REQUESTS_DURATION)
        .time()) {
      Map<String, Boolean> instanceStoppableCheck =
          _customRestClient.getInstanceStoppableCheck(baseUrl, customPayLoads);
      return new StoppableCheck(instanceStoppableCheck,
          StoppableCheck.Category.CUSTOM_INSTANCE_CHECK);
    } catch (IOException ex) {
      LOG.error("Custom client side instance level health check for {}/{} failed.", clusterId,
          instanceName, ex);
      metrics.counter(CUSTOM_INSTANCE_CHECK_HTTP_REQUESTS_ERROR_TOTAL).inc();
      return new StoppableCheck(false, Collections.singletonList(instanceName),
          StoppableCheck.Category.CUSTOM_INSTANCE_CHECK);
    }
  }

  private Map<String, StoppableCheck> performPartitionsCheck(List<String> instances,
      RESTConfig restConfig, Map<String, String> customPayLoads) {
    Map<String, Map<String, Boolean>> allPartitionsHealthOnLiveInstance =
        _dataAccessor.getAllPartitionsHealthOnLiveInstance(restConfig, customPayLoads, _skipZKRead);
    List<ExternalView> externalViews =
        _dataAccessor.getChildValues(_dataAccessor.keyBuilder().externalViews(), true);
    Map<String, StoppableCheck> instanceStoppableChecks = new HashMap<>();
    for (String instanceName : instances) {
      Map<String, List<String>> unHealthyPartitions = InstanceValidationUtil
          .perPartitionHealthCheck(externalViews, allPartitionsHealthOnLiveInstance, instanceName,
              _dataAccessor);

      List<String> unHealthyPartitionsList = new ArrayList<>();
      for (String partitionName : unHealthyPartitions.keySet()) {
        for (String reason : unHealthyPartitions.get(partitionName)) {
          unHealthyPartitionsList.add(reason.toUpperCase() + ":" + partitionName);
        }
      }
      StoppableCheck stoppableCheck = new StoppableCheck(unHealthyPartitionsList.isEmpty(),
          unHealthyPartitionsList, StoppableCheck.Category.CUSTOM_PARTITION_CHECK);
      instanceStoppableChecks.put(instanceName, stoppableCheck);
    }

    return instanceStoppableChecks;
  }

  private Map<String, String> getCustomPayLoads(String jsonContent) throws IOException {
    if (jsonContent == null) {
      return null;
    }
    Map<String, String> result = new HashMap<>();
    JsonNode jsonNode = OBJECT_MAPPER.readTree(jsonContent);
    // parsing the inputs as string key value pairs
    jsonNode.fields().forEachRemaining(kv -> result.put(kv.getKey(), kv.getValue().asText()));
    return result;
  }

  @VisibleForTesting
 protected Map<String, Boolean> getInstanceHealthStatus(String clusterId, String instanceName,
      List<HealthCheck> healthChecks) {
    Map<String, Boolean> healthStatus = new HashMap<>();
    for (HealthCheck healthCheck : healthChecks) {
      switch (healthCheck) {
        case INVALID_CONFIG:
          boolean validConfig;
          try {
            validConfig =
                InstanceValidationUtil.hasValidConfig(_dataAccessor, clusterId, instanceName);
          } catch (HelixException e) {
            validConfig = false;
            LOG.warn("Cluster {} instance {} doesn't have valid config: {}", clusterId, instanceName,
                e.getMessage());
          }

          // TODO: should add reason to request response
          healthStatus.put(HealthCheck.INVALID_CONFIG.name(), validConfig);
          if (!validConfig) {
            // No need to do remaining health checks.
            return healthStatus;
          }
          break;
        case INSTANCE_NOT_ENABLED:
          healthStatus.put(HealthCheck.INSTANCE_NOT_ENABLED.name(),
              InstanceValidationUtil.isEnabled(_dataAccessor, instanceName));
          break;
        case INSTANCE_NOT_ALIVE:
          healthStatus.put(HealthCheck.INSTANCE_NOT_ALIVE.name(),
              InstanceValidationUtil.isAlive(_dataAccessor, instanceName));
          break;
        case INSTANCE_NOT_STABLE:
          boolean isStable = InstanceValidationUtil.isInstanceStable(_dataAccessor, instanceName);
          healthStatus.put(HealthCheck.INSTANCE_NOT_STABLE.name(), isStable);
          break;
        case HAS_ERROR_PARTITION:
          healthStatus.put(HealthCheck.HAS_ERROR_PARTITION.name(),
              !InstanceValidationUtil.hasErrorPartitions(_dataAccessor, clusterId, instanceName));
          break;
        case HAS_DISABLED_PARTITION:
          healthStatus.put(HealthCheck.HAS_DISABLED_PARTITION.name(),
              !InstanceValidationUtil.hasDisabledPartitions(_dataAccessor, clusterId, instanceName));
          break;
        case EMPTY_RESOURCE_ASSIGNMENT:
          healthStatus.put(HealthCheck.EMPTY_RESOURCE_ASSIGNMENT.name(),
              InstanceValidationUtil.hasResourceAssigned(_dataAccessor, clusterId, instanceName));
          break;
        case MIN_ACTIVE_REPLICA_CHECK_FAILED:
          healthStatus.put(HealthCheck.MIN_ACTIVE_REPLICA_CHECK_FAILED.name(),
              InstanceValidationUtil.siblingNodesActiveReplicaCheck(_dataAccessor, instanceName));
          break;
        default:
          LOG.error("Unsupported health check: {}", healthCheck);
          break;
      }
    }

    return healthStatus;
  }
}
