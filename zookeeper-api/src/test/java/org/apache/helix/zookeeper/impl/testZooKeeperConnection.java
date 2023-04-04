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
  final int count = 2000;
  final int[] get_count = {0};
  CountDownLatch countDownLatch = new CountDownLatch(count);

  @Test
  void testPersistWatcher() throws IOException, KeeperException, InterruptedException {
    Watcher watcher1 = new PersistWatcher();
    ZkClient zkClient =   new org.apache.helix.zookeeper.impl.client.ZkClient("localhost:2121");

    IZkConnection _zk = zkClient.getConnection();
    String path="/testPersistWatcher";
    _zk.delete(path);
    _zk.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    //_zk.getChildren("/root", true);
    _zk.addWatch(path, watcher1, AddWatchMode.PERSISTENT);

    for (int i=0; i<2000; ++i) {
      _zk.writeData(path, "datat".getBytes(), -1);
    }
    Assert.assertTrue(countDownLatch.await(50000, TimeUnit.MILLISECONDS));
  }

  class PersistWatcher implements Watcher {
    @Override
    public void process(WatchedEvent watchedEvent) {
      System.out.println("path: " + watchedEvent.getPath());
      System.out.println("type: " + watchedEvent.getType());
      get_count[0]++;
      System.out.println("[Xyy] data change event  " + watchedEvent.getPath() + " " + get_count[0]);
      countDownLatch.countDown();
    }
  }
}