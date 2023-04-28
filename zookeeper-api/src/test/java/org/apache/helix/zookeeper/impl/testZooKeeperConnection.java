package org.apache.helix.zookeeper.impl;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.helix.zookeeper.impl.ZkTestBase;
import org.apache.helix.zookeeper.zkclient.IZkConnection;
import org.apache.helix.zookeeper.zkclient.ZkClient;
import org.apache.helix.zookeeper.zkclient.ZkConnection;
import org.apache.zookeeper.AddWatchMode;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.testng.Assert;
import org.testng.annotations.Test;


public class testZooKeeperConnection extends ZkTestBase {
  final int count = 100;
  final int[] get_count = {0};
  CountDownLatch countDownLatch = new CountDownLatch(count);

/*
  @Test
  void testPersistWatcher() throws IOException, KeeperException, InterruptedException {
    Watcher watcher1 = new PersistWatcher();
    ZkClient zkClient =   new org.apache.helix.zookeeper.impl.client.ZkClient("localhost:2121");

    IZkConnection _zk = zkClient.getConnection();
    String path="/testPersistWatcher";
    //_zk.delete(path);
    _zk.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    //_zk.getChildren("/root", true);
    _zk.addWatch(path, watcher1, AddWatchMode.PERSISTENT);

    for (int i=0; i<200; ++i) {
      _zk.writeData(path, "datat".getBytes(), -1);
    }

    Assert.assertTrue(countDownLatch.await(50000, TimeUnit.MILLISECONDS));

    _zk.readData(path, null, true);
    _zk.getChildren(path, true);
    for (int i=0; i<200; ++i) {
      _zk.writeData(path, ("datat"+i).getBytes(), -1);
      _zk.create(path+"/" +i, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
      //_zk.create(path+ "/" + i, "datat".getBytes(), CreateMode.EPHEMERAL);
    }
    //_zk.create(path+"/1" , null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

    zkClient.close();

  }*/

  @Test //(dependsOnMethods="testPersistWatcher")
  void testPersistRecursiveWatcher() throws IOException, KeeperException, InterruptedException {
    Watcher watcher1 = new PersistWatcher();
    ZkClient zkClient =   new org.apache.helix.zookeeper.impl.client.ZkClient("localhost:2121");

    IZkConnection _zk = zkClient.getConnection();
    String path="/testPersistWatcher";
    //_zk.delete(path);
    _zk.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    _zk.create(path+ "/" + 0 , "datat".getBytes(), CreateMode.PERSISTENT);
    _zk.addWatch(path, watcher1, AddWatchMode.PERSISTENT_RECURSIVE);

    for (int i=0; i<100; ++i) {
      _zk.create(path+ "/0" + "/" + i, "datat".getBytes(), CreateMode.PERSISTENT);

    }
    Assert.assertTrue(countDownLatch.await(50000, TimeUnit.MILLISECONDS));
    _zk.addWatch(path, watcher1, AddWatchMode.PERSISTENT);
    for (int i=0; i<100; ++i) {
     // _zk.create(path+"/abc" + i , null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
      _zk.create(path+ "/abc" + i, "datat".getBytes(),  ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

    }
    _zk.getChildren(path, true);
    for (int i=0; i<100; ++i) {
      _zk.create(path+"/abcd" + i , "datat".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

    }
        zkClient.close();


  }


  class PersistWatcher implements Watcher {
    @Override
    public void process(WatchedEvent watchedEvent) {
      System.out.println("path: " + watchedEvent.getPath());
      System.out.println("type: " + watchedEvent.getType());
      get_count[0]++;
      System.out.println("[Xyy] change event  " + watchedEvent.getPath() + " " + get_count[0]);
      countDownLatch.countDown();
    }
  }
}