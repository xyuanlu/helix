package org.apache.helix.metaclient.impl.zk;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.helix.metaclient.api.ChildChangeListener;
import org.apache.helix.metaclient.api.DataChangeListener;
import org.apache.helix.metaclient.api.DirectChildChangeListener;
import org.apache.helix.metaclient.impl.zk.factory.ZkMetaClientConfig;
import org.apache.helix.zookeeper.datamodel.ZNRecord;
import org.apache.helix.zookeeper.datamodel.serializer.ZNRecordSerializer;
import org.testng.Assert;
import org.testng.annotations.Test;


public class TestMetaclientExample extends ZkMetaClientTestBase{

  protected static ZkMetaClient<ZNRecord> createZkMetaClientWithZNRecordSerilizer() {
    ZkMetaClientConfig config =
        new ZkMetaClientConfig.ZkMetaClientConfigBuilder().setConnectionAddress(ZK_ADDR)
            .setZkSerializer(new ZNRecordSerializer())
            .build();
    return new ZkMetaClient<>(config);
  }

  @Test
  public void testChildChangeListener() throws Exception {
    final String basePath = "/testChildChangeListener";
    final int count = 100;
    // MetaClient extends auto closable
    try (ZkMetaClient<ZNRecord> zkMetaClient = createZkMetaClientWithZNRecordSerilizer()) {
      // call connect() to connect to ZK
      zkMetaClient.connect();

      // this is for test only to count we dont miss event
      CountDownLatch countDownLatch = new CountDownLatch(count*2);

      // subscribe recursive child change on basePath
      ChildChangeListener listener = new ChildChangeListener() {

        @Override
        public void handleChildChange(String changedPath, ChangeType changeType) throws Exception {
          if (changeType == ChangeType.ENTRY_CREATED) {
            System.out.println("New ZNode created " + changedPath);
          }

          if (changeType == ChangeType.ENTRY_DELETED) {
            System.out.println("ZNode removed " + changedPath);
          }
          countDownLatch.countDown();

        }
      };
      zkMetaClient.create(basePath, new ZNRecord(basePath));
      Assert.assertTrue(
          zkMetaClient.subscribeChildChanges(basePath, listener, false)
      );

      // create sub ZNode and remove sub Znode under basePath
      for (int i=0; i<count; ++i) {
        String key = basePath+"/c1_" +i;
        // create ZnRecord and add value to the list
        List<String> llist = new LinkedList<String>();
        llist.add("a");
        llist.add("b");
        ZNRecord znode = new ZNRecord("/c1_"+i); // ZNRecord ID is the path
        znode.setListField("key_of_list", llist);

        // create ZNRecord and write value
        zkMetaClient.create( key, znode);

        // read data of the Znode
        System.out.println(zkMetaClient.get(key).getListFields().get("key_of_list"));
                          // getZnRecordObject //get all ListFields // get the list

        // remove that ZNode
        zkMetaClient.delete(key);

      }
      Assert.assertTrue(countDownLatch.await(50000000, TimeUnit.MILLISECONDS));

    }
  }

  /* sample output
    New ZNode created /testChildChangeListener/c1_96
    [a, b]
    New ZNode removed /testChildChangeListener/c1_96
    New ZNode created /testChildChangeListener/c1_97
    [a, b]
    New ZNode removed /testChildChangeListener/c1_97
    New ZNode created /testChildChangeListener/c1_98
    [a, b]
    New ZNode removed /testChildChangeListener/c1_98
    New ZNode created /testChildChangeListener/c1_99
    [a, b]
    New ZNode removed /testChildChangeListener/c1_99
   */

}
