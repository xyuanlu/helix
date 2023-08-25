package org.apache.helix.integration.rebalancer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.helix.ConfigAccessor;
import org.apache.helix.HelixDataAccessor;
import org.apache.helix.HelixRollbackException;
import org.apache.helix.NotificationContext;
import org.apache.helix.TestHelper;
import org.apache.helix.common.ZkTestBase;
import org.apache.helix.constants.InstanceConstants;
import org.apache.helix.controller.rebalancer.strategy.CrushEdRebalanceStrategy;
import org.apache.helix.controller.rebalancer.waged.AssignmentMetadataStore;
import org.apache.helix.integration.manager.ClusterControllerManager;
import org.apache.helix.integration.manager.MockParticipantManager;
import org.apache.helix.manager.zk.ZKHelixDataAccessor;
import org.apache.helix.manager.zk.ZkBucketDataAccessor;
import org.apache.helix.model.BuiltInStateModelDefinitions;
import org.apache.helix.model.ClusterConfig;
import org.apache.helix.model.ExternalView;
import org.apache.helix.model.Message;
import org.apache.helix.model.ResourceAssignment;
import org.apache.helix.participant.StateMachineEngine;
import org.apache.helix.participant.statemachine.StateModel;
import org.apache.helix.participant.statemachine.StateModelFactory;
import org.apache.helix.participant.statemachine.StateModelInfo;
import org.apache.helix.participant.statemachine.Transition;
import org.apache.helix.tools.ClusterVerifiers.StrictMatchExternalViewVerifier;
import org.apache.helix.tools.ClusterVerifiers.ZkHelixClusterVerifier;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class TestInstanceOfflineAndOnlineShortly extends ZkTestBase  {
  protected final int NUM_NODE = 6;
  protected static final int START_PORT = 12918;
  protected static final int PARTITIONS = 20;

  protected final String CLASS_NAME = getShortClassName();
  protected final String CLUSTER_NAME = CLUSTER_PREFIX + "_" + CLASS_NAME;
  private int REPLICA = 3;
  protected ClusterControllerManager _controller;
  List<MockParticipantManager> _participants = new ArrayList<>();
  List<String> _participantNames = new ArrayList<>();
  private Set<String> _allDBs = new HashSet<>();
  private ZkHelixClusterVerifier _clusterVerifier;
  private ConfigAccessor _configAccessor;
  private long _stateModelDelay = 30L;
  protected AssignmentMetadataStore _assignmentMetadataStore;
  HelixDataAccessor _dataAccessor;

  @BeforeClass
  public void beforeClass() throws Exception {
    System.out.println("START " + CLASS_NAME + " at " + new Date(System.currentTimeMillis()));

    _gSetupTool.addCluster(CLUSTER_NAME, true);

    for (int i = 0; i < NUM_NODE; i++) {
      String participantName = PARTICIPANT_PREFIX + "_" + (START_PORT + i);
      addParticipant(participantName);
    }

    // start controller
    String controllerName = CONTROLLER_PREFIX + "_0";
    _controller = new ClusterControllerManager(ZK_ADDR, CLUSTER_NAME, controllerName);
    _controller.syncStart();
    _clusterVerifier = new StrictMatchExternalViewVerifier.Builder(CLUSTER_NAME).setZkAddr(ZK_ADDR)
        .setDeactivatedNodeAwareness(true)
        .setResources(_allDBs)
        .setWaitTillVerify(TestHelper.DEFAULT_REBALANCE_PROCESSING_WAIT_TIME)
        .build();
    enablePersistBestPossibleAssignment(_gZkClient, CLUSTER_NAME, true);
    _configAccessor = new ConfigAccessor(_gZkClient);
    _dataAccessor = new ZKHelixDataAccessor(CLUSTER_NAME, _baseAccessor);

    ClusterConfig clusterConfig = _configAccessor.getClusterConfig(CLUSTER_NAME);
    clusterConfig.stateTransitionCancelEnabled(true);
    _configAccessor.setClusterConfig(CLUSTER_NAME, clusterConfig);

    setUpWagedBaseline();
  }

  @Test
  public void testInstanceOfflineThenOnline() throws Exception {
    // add a resource where downward state transition is slow
    createResourceWithDelayedRebalance(CLUSTER_NAME, "TEST_DB3_DELAYED_CRUSHED", "MasterSlave", PARTITIONS, REPLICA,
        REPLICA - 1, 0, CrushEdRebalanceStrategy.class.getName());
    _allDBs.add("TEST_DB3_DELAYED_CRUSHED");
    // add a resource where downward state transition is slow
    createResourceWithWagedRebalance(CLUSTER_NAME, "TEST_DB4_DELAYED_WAGED", "MasterSlave",
        PARTITIONS, REPLICA, REPLICA - 1);
    _allDBs.add("TEST_DB4_DELAYED_WAGED");
    // wait for assignment to finish
    Assert.assertTrue(_clusterVerifier.verifyByPolling());

    // set upward delay to a large number
    _stateModelDelay = -3000000L;
    addParticipant(PARTICIPANT_PREFIX + "_" + (START_PORT + NUM_NODE));

    System.out.println("added new instance : " + PARTICIPANT_PREFIX + "_" + (START_PORT + NUM_NODE));

    _participants.get(3).syncStop();
    Thread.sleep(5000);

    // retsart an old participant
    MockParticipantManager participant =
        new MockParticipantManager(ZK_ADDR, _participants.get(3).getClusterName(),
            _participants.get(3).getInstanceName());
    participant.syncStart();
    System.err.println("Restart " + _participants.get(3).getInstanceName());


    Thread.sleep(50000000000L);

  }


  private void addParticipant(String participantName) {
    _gSetupTool.addInstanceToCluster(CLUSTER_NAME, participantName);

    // start dummy participants
    MockParticipantManager participant = new MockParticipantManager(ZK_ADDR, CLUSTER_NAME, participantName);
    StateMachineEngine stateMachine = participant.getStateMachineEngine();
    // Using a delayed state model
    TestInstanceOfflineAndOnlineShortly.StDelayMSStateModelFactory
        delayFactory = new TestInstanceOfflineAndOnlineShortly.StDelayMSStateModelFactory();
    stateMachine.registerStateModelFactory("MasterSlave", delayFactory);

    participant.syncStart();
    _participants.add(participant);
    _participantNames.add(participantName);
  }

  private void setUpWagedBaseline() {
    _assignmentMetadataStore = new AssignmentMetadataStore(new ZkBucketDataAccessor(ZK_ADDR), CLUSTER_NAME) {
      public Map<String, ResourceAssignment> getBaseline() {
        // Ensure this metadata store always read from the ZK without using cache.
        super.reset();
        return super.getBaseline();
      }

      public synchronized Map<String, ResourceAssignment> getBestPossibleAssignment() {
        // Ensure this metadata store always read from the ZK without using cache.
        super.reset();
        return super.getBestPossibleAssignment();
      }
    };

    // Set test instance capacity and partition weights
    ClusterConfig clusterConfig = _dataAccessor.getProperty(_dataAccessor.keyBuilder().clusterConfig());
    String testCapacityKey = "TestCapacityKey";
    clusterConfig.setInstanceCapacityKeys(Collections.singletonList(testCapacityKey));
    clusterConfig.setDefaultInstanceCapacityMap(Collections.singletonMap(testCapacityKey, 100));
    clusterConfig.setDefaultPartitionWeightMap(Collections.singletonMap(testCapacityKey, 1));
    _dataAccessor.setProperty(_dataAccessor.keyBuilder().clusterConfig(), clusterConfig);
  }

  // A state transition model where either downward ST are slow (_stateModelDelay >0) or upward ST are slow (_stateModelDelay <0)
  public class StDelayMSStateModelFactory extends StateModelFactory<TestInstanceOfflineAndOnlineShortly.StDelayMSStateModel> {

    @Override
    public TestInstanceOfflineAndOnlineShortly.StDelayMSStateModel createNewStateModel(String resourceName, String partitionKey) {
      TestInstanceOfflineAndOnlineShortly.StDelayMSStateModel model = new TestInstanceOfflineAndOnlineShortly.StDelayMSStateModel();
      return model;
    }
  }

  @StateModelInfo(initialState = "OFFLINE", states = {"MASTER", "SLAVE", "ERROR"})
  public class StDelayMSStateModel extends StateModel {

    public StDelayMSStateModel() {
      _cancelled = false;
    }

    private void sleepWhileNotCanceled(long sleepTime) throws InterruptedException{
      while(sleepTime >0 && !isCancelled()) {
        Thread.sleep(5000);
        sleepTime = sleepTime - 5000;
        //System.out.println("sleep " + sleepTime);
      }
      if (isCancelled()) {
        _cancelled = false;
        throw new HelixRollbackException("EX");
      }
    }

    @Transition(to = "SLAVE", from = "OFFLINE")
    public void onBecomeSlaveFromOffline(Message message, NotificationContext context) throws InterruptedException {
      if (_stateModelDelay < 0) {
        sleepWhileNotCanceled(Math.abs(_stateModelDelay));
      }
      //System.out.println("OFFLINE to SLAVE");
    }

    @Transition(to = "MASTER", from = "SLAVE")
    public void onBecomeMasterFromSlave(Message message, NotificationContext context) throws InterruptedException {
      if (_stateModelDelay < 0) {
        sleepWhileNotCanceled(Math.abs(_stateModelDelay));
      }
    }

    @Transition(to = "SLAVE", from = "MASTER")
    public void onBecomeSlaveFromMaster(Message message, NotificationContext context) throws InterruptedException {
      if (_stateModelDelay > 0) {
        sleepWhileNotCanceled(_stateModelDelay);
      }
    }

    @Transition(to = "OFFLINE", from = "SLAVE")
    public void onBecomeOfflineFromSlave(Message message, NotificationContext context) throws InterruptedException {
      if (_stateModelDelay > 0) {
        sleepWhileNotCanceled(_stateModelDelay);
      }
    }

    @Transition(to = "DROPPED", from = "OFFLINE")
    public void onBecomeDroppedFromOffline(Message message, NotificationContext context) throws InterruptedException {
      if (_stateModelDelay > 0) {
        sleepWhileNotCanceled(_stateModelDelay);
      }
    }
  }
}
