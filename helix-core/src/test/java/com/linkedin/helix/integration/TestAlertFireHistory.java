package com.linkedin.helix.integration;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.linkedin.helix.HelixManager;
import com.linkedin.helix.PropertyType;
import com.linkedin.helix.TestHelper;
import com.linkedin.helix.TestHelper.StartCMResult;
import com.linkedin.helix.ZNRecord;
import com.linkedin.helix.controller.HelixControllerMain;
import com.linkedin.helix.healthcheck.HealthStatsAggregationTask;
import com.linkedin.helix.manager.zk.ZNRecordSerializer;
import com.linkedin.helix.manager.zk.ZkClient;
import com.linkedin.helix.tools.ClusterSetup;
import com.linkedin.helix.tools.ClusterStateVerifier;

/**
 *
 * setup a storage cluster and start a zk-based cluster controller in stand-alone mode
 * start 5 dummy participants verify the current states at end
 */

public class TestAlertFireHistory extends ZkStandAloneCMTestBase
{
  String _statName = "TestStat@DB=db1";
  String _stat = "TestStat";
  String metricName1 = "TestMetric1";
  String metricName2 = "TestMetric2";
  
  String _alertStr1 = "EXP(decay(1.0)(localhost_*.TestStat@DB=db1.TestMetric1))CMP(GREATER)CON(20)";
  String _alertStr2 = "EXP(decay(1.0)(localhost_*.TestStat@DB=db1.TestMetric2))CMP(GREATER)CON(100)";
  
  void setHealthData(int[] val1, int[] val2)
  { 
    for (int i = 0; i < NODE_NR; i++)
    {
      String instanceName = PARTICIPANT_PREFIX + "_" + (START_PORT + i);
      HelixManager manager = _startCMResultMap.get(instanceName)._manager;
      ZNRecord record = new ZNRecord(_stat);
      Map<String, String> valMap = new HashMap<String, String>();
      valMap.put(metricName1, val1[i] + "");
      valMap.put(metricName2, val2[i] + "");
      record.setSimpleField("TimeStamp", new Date().getTime() + "");
      record.setMapField(_statName, valMap);
      manager.getDataAccessor()
        .setProperty(PropertyType.HEALTHREPORT, record, manager.getInstanceName(), record.getId());
    }
  }
  
  @Test
  public void TestAlertHistory() throws InterruptedException
  {
    _setupTool.getClusterManagementTool().addAlert(CLUSTER_NAME, _alertStr1);
    _setupTool.getClusterManagementTool().addAlert(CLUSTER_NAME, _alertStr2);
    
    int[] metrics1 = {10, 15, 22, 24, 16};
    int[] metrics2 = {22, 115, 22, 141,16};
    setHealthData(metrics1, metrics2);
    
    String controllerName = CONTROLLER_PREFIX + "_0";
    HelixManager manager = _startCMResultMap.get(controllerName)._manager;
    HealthStatsAggregationTask task = new HealthStatsAggregationTask(_startCMResultMap.get(controllerName)._manager);
    task.run();
    Thread.sleep(100);
    
    ZNRecord history = manager.getDataAccessor().getProperty(PropertyType.ALERT_HISTORY);
    // 
    Assert.assertEquals(history.getMapFields().size(), 1);
    TreeMap<String, Map<String, String>> recordMap = new TreeMap<String, Map<String, String>>();
    recordMap.putAll( history.getMapFields());
    Map<String, String> lastRecord = recordMap.firstEntry().getValue();
    Assert.assertTrue(lastRecord.size() == 4);
    Assert.assertTrue(lastRecord.get("(localhost_12920.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("ON"));
    Assert.assertTrue(lastRecord.get("(localhost_12919.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("ON"));
    Assert.assertTrue(lastRecord.get("(localhost_12921.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("ON"));
    Assert.assertTrue(lastRecord.get("(localhost_12921.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("ON"));
    
    setHealthData(metrics1, metrics2);
    task.run();
    Thread.sleep(100);
    history = manager.getDataAccessor().getProperty(PropertyType.ALERT_HISTORY);
    // no change
    Assert.assertEquals(history.getMapFields().size(), 1);
    recordMap = new TreeMap<String, Map<String, String>>();
    recordMap.putAll( history.getMapFields());
    lastRecord = recordMap.firstEntry().getValue();
    Assert.assertTrue(lastRecord.size() == 4);
    Assert.assertTrue(lastRecord.get("(localhost_12920.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("ON"));
    Assert.assertTrue(lastRecord.get("(localhost_12919.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("ON"));
    Assert.assertTrue(lastRecord.get("(localhost_12921.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("ON"));
    Assert.assertTrue(lastRecord.get("(localhost_12921.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("ON"));
    
    int [] metrics3 = {21, 44, 22, 14, 16};
    int [] metrics4 = {122, 115, 222, 41,16};
    setHealthData(metrics3, metrics4);
    task.run();
    Thread.sleep(100);
    history = manager.getDataAccessor().getProperty(PropertyType.ALERT_HISTORY);
    // new delta should be recorded
    Assert.assertEquals(history.getMapFields().size(), 2);
    recordMap = new TreeMap<String, Map<String, String>>();
    recordMap.putAll( history.getMapFields());
    lastRecord = recordMap.lastEntry().getValue();
    Assert.assertEquals(lastRecord.size(), 6);
    Assert.assertTrue(lastRecord.get("(localhost_12918.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("ON"));
    Assert.assertTrue(lastRecord.get("(localhost_12921.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("OFF"));
    Assert.assertTrue(lastRecord.get("(localhost_12920.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("ON"));
    Assert.assertTrue(lastRecord.get("(localhost_12921.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("OFF"));
    Assert.assertTrue(lastRecord.get("(localhost_12918.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("ON"));
    Assert.assertTrue(lastRecord.get("(localhost_12919.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("ON"));
    
    int [] metrics5 = {0, 0, 0, 0, 0};
    int [] metrics6 = {0, 0, 0, 0,0};
    setHealthData(metrics5, metrics6);
    task.run();
    Thread.sleep(100);
    history = manager.getDataAccessor().getProperty(PropertyType.ALERT_HISTORY);
    // reset everything
    Assert.assertEquals(history.getMapFields().size(), 3);
    recordMap = new TreeMap<String, Map<String, String>>();
    recordMap.putAll( history.getMapFields());
    lastRecord = recordMap.lastEntry().getValue();
    Assert.assertTrue(lastRecord.size() == 6);
    Assert.assertTrue(lastRecord.get("(localhost_12918.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("OFF"));
    Assert.assertTrue(lastRecord.get("(localhost_12920.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("OFF"));
    Assert.assertTrue(lastRecord.get("(localhost_12920.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("OFF"));
    Assert.assertTrue(lastRecord.get("(localhost_12919.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("OFF"));
    Assert.assertTrue(lastRecord.get("(localhost_12918.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("OFF"));
    Assert.assertTrue(lastRecord.get("(localhost_12919.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("OFF"));
    
    // Size of the history should be 30
    for(int i = 0;i < 27; i++)
    {
      int x = i % 2;
      int y = (i+1) % 2;
      int[] metricsx = {19 + 3*x, 19 + 3*y, 19 + 4*x, 18+4*y, 17+5*y};
      int[] metricsy = {99 + 3*x, 99 + 3*y, 98 + 4*x, 98+4*y, 97+5*y};
      
      setHealthData(metricsx, metricsy);
      task.run();
      Thread.sleep(100);
      history = manager.getDataAccessor().getProperty(PropertyType.ALERT_HISTORY);
      
      Assert.assertEquals(history.getMapFields().size(), 3 + i + 1);
      recordMap = new TreeMap<String, Map<String, String>>();
      recordMap.putAll( history.getMapFields());
      lastRecord = recordMap.lastEntry().getValue();
      if(i == 0)
      {
        Assert.assertTrue(lastRecord.size() == 6);
        Assert.assertTrue(lastRecord.get("(localhost_12922.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("ON"));
        Assert.assertTrue(lastRecord.get("(localhost_12922.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("ON"));
        Assert.assertTrue(lastRecord.get("(localhost_12919.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("ON"));
        Assert.assertTrue(lastRecord.get("(localhost_12921.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("ON"));
        Assert.assertTrue(lastRecord.get("(localhost_12921.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("ON"));
        Assert.assertTrue(lastRecord.get("(localhost_12919.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("ON"));
      }
      else
      {
        Assert.assertTrue(lastRecord.size() == 10);
        if(x == 0)
        {
          Assert.assertTrue(lastRecord.get("(localhost_12922.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("ON"));
          Assert.assertTrue(lastRecord.get("(localhost_12922.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("ON"));
          Assert.assertTrue(lastRecord.get("(localhost_12920.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("OFF"));
          Assert.assertTrue(lastRecord.get("(localhost_12918.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("OFF"));
          Assert.assertTrue(lastRecord.get("(localhost_12919.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("ON"));
          Assert.assertTrue(lastRecord.get("(localhost_12921.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("ON"));
          Assert.assertTrue(lastRecord.get("(localhost_12920.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("OFF"));
          Assert.assertTrue(lastRecord.get("(localhost_12921.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("ON"));
          Assert.assertTrue(lastRecord.get("(localhost_12918.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("OFF"));
          Assert.assertTrue(lastRecord.get("(localhost_12919.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("ON"));
        }
        else
        {
          Assert.assertTrue(lastRecord.get("(localhost_12922.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("OFF"));
          Assert.assertTrue(lastRecord.get("(localhost_12922.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("OFF"));
          Assert.assertTrue(lastRecord.get("(localhost_12920.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("ON"));
          Assert.assertTrue(lastRecord.get("(localhost_12918.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("ON"));
          Assert.assertTrue(lastRecord.get("(localhost_12919.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("OFF"));
          Assert.assertTrue(lastRecord.get("(localhost_12921.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("OFF"));
          Assert.assertTrue(lastRecord.get("(localhost_12920.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("ON"));
          Assert.assertTrue(lastRecord.get("(localhost_12921.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("OFF"));
          Assert.assertTrue(lastRecord.get("(localhost_12918.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("ON"));
          Assert.assertTrue(lastRecord.get("(localhost_12919.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("OFF"));
        }
      }
    }
    // size limit is 30
    for(int i = 0;i < 10; i++)
    {
      int x = i % 2;
      int y = (i+1) % 2;
      int[] metricsx = {19 + 3*x, 19 + 3*y, 19 + 4*x, 18+4*y, 17+5*y};
      int[] metricsy = {99 + 3*x, 99 + 3*y, 98 + 4*x, 98+4*y, 97+5*y};
      
      setHealthData(metricsx, metricsy);
      task.run();
      Thread.sleep(100);
      history = manager.getDataAccessor().getProperty(PropertyType.ALERT_HISTORY);
      
      Assert.assertEquals(history.getMapFields().size(), 30);
      recordMap = new TreeMap<String, Map<String, String>>();
      recordMap.putAll( history.getMapFields());
      lastRecord = recordMap.lastEntry().getValue();
      
      Assert.assertEquals(lastRecord.size() , 10);
      if(x == 0)
      {
        Assert.assertTrue(lastRecord.get("(localhost_12922.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("ON"));
        Assert.assertTrue(lastRecord.get("(localhost_12922.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("ON"));
        Assert.assertTrue(lastRecord.get("(localhost_12920.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("OFF"));
        Assert.assertTrue(lastRecord.get("(localhost_12918.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("OFF"));
        Assert.assertTrue(lastRecord.get("(localhost_12919.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("ON"));
        Assert.assertTrue(lastRecord.get("(localhost_12921.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("ON"));
        Assert.assertTrue(lastRecord.get("(localhost_12920.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("OFF"));
        Assert.assertTrue(lastRecord.get("(localhost_12921.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("ON"));
        Assert.assertTrue(lastRecord.get("(localhost_12918.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("OFF"));
        Assert.assertTrue(lastRecord.get("(localhost_12919.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("ON"));
      }
      else
      {
        Assert.assertTrue(lastRecord.get("(localhost_12922.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("OFF"));
        Assert.assertTrue(lastRecord.get("(localhost_12922.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("OFF"));
        Assert.assertTrue(lastRecord.get("(localhost_12920.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("ON"));
        Assert.assertTrue(lastRecord.get("(localhost_12918.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("ON"));
        Assert.assertTrue(lastRecord.get("(localhost_12919.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("OFF"));
        Assert.assertTrue(lastRecord.get("(localhost_12921.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("OFF"));
        Assert.assertTrue(lastRecord.get("(localhost_12920.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("ON"));
        Assert.assertTrue(lastRecord.get("(localhost_12921.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("OFF"));
        Assert.assertTrue(lastRecord.get("(localhost_12918.TestStat@DB#db1.TestMetric2)GREATER(100)").equals("ON"));
        Assert.assertTrue(lastRecord.get("(localhost_12919.TestStat@DB#db1.TestMetric1)GREATER(20)").equals("OFF"));
      }
    }
    
  }

}