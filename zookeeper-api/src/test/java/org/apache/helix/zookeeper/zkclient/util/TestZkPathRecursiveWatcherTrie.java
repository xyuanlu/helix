package org.apache.helix.zookeeper.zkclient.util;

import org.apache.helix.zookeeper.zkclient.RecursivePersistListener;
import org.apache.zookeeper.Watcher;
import org.testng.Assert;


public class TestZkPathRecursiveWatcherTrie {
  ZkPathRecursiveWatcherTrie _recursiveWatcherTrie = new ZkPathRecursiveWatcherTrie();

  /**
   * test case create a tire tree of following structure. '*' means how many listener is added
   * on that path.
   * [x] mean the listener is removed as step 'x' indicated in the test comment.
   *
   *                         "/"
   *                         a
   *                    /   |        \
   *                  b*    b2* [2]    b3
   *                 /                 \
   *                c                   c
   *          / /   |  \  \              \
   *        d* d1* d2* d3* d4*           d
   *                                      \
   *                                      e
   *                                      \
   *                                      f**   [1] [3]
   *
   */
  @org.testng.annotations.Test
  public void testAddRemoveWatcher() {
    System.out.println("");
    _recursiveWatcherTrie.addRecursiveListener("/a/b/c/d", new Test());
    _recursiveWatcherTrie.addRecursiveListener("/a/b/c/d1", new Test());
    _recursiveWatcherTrie.addRecursiveListener("/a/b/c/d2", new Test());
    _recursiveWatcherTrie.addRecursiveListener("/a/b/c/d3", new Test());
    _recursiveWatcherTrie.addRecursiveListener("/a/b/c/d4", new Test());

    Test listenerOnb = new Test();
    _recursiveWatcherTrie.addRecursiveListener("/a/b", listenerOnb);
    Test listenerOnb2 = new Test();
    _recursiveWatcherTrie.addRecursiveListener("/a/b2", listenerOnb2);
    Test listenerOnf_1 = new Test();
    _recursiveWatcherTrie.addRecursiveListener("/a/b3/c/d/e/f", listenerOnf_1);
    Test listenerOnf_2 = new Test();
    _recursiveWatcherTrie.addRecursiveListener("/a/b3/c/d/e/f", listenerOnf_2);

    // node f should have 2 listeners
    Assert.assertEquals(
        _recursiveWatcherTrie.getRootNode().getChild("a").getChild("b3").getChild("c").getChild("d")
            .getChild("e").getChild("f").getRecursiveListeners().size(), 2);

   /* _recursiveWatcherTrie.removeRecursiveListener("/a/b3/c/d/e/f", listenerOnf_1); // step [1]
    _recursiveWatcherTrie.removeRecursiveListener("/a/b2", listenerOnb2);          //  step[2]
    //b2 will be removed. node "a" should have 2 children, b and b3.
    Assert.assertEquals(_recursiveWatcherTrie.getRootNode().getChild("a").getChildren().size(), 2);
    Assert.assertTrue(_recursiveWatcherTrie.getRootNode().getChild("a").getChildren().contains("b3"));
    Assert.assertTrue(_recursiveWatcherTrie.getRootNode().getChild("a").getChildren().contains("b"));
    // path "/a/b3/c/d/e/f still exists with end node "f" has one listener
    Assert.assertEquals(
        _recursiveWatcherTrie.getRootNode().getChild("a").getChild("b3").getChild("c").getChild("d")
            .getChild("e").getChildren().size(), 1);
    Assert.assertEquals(
        _recursiveWatcherTrie.getRootNode().getChild("a").getChild("b3").getChild("c").getChild("d")
            .getChild("e").getChild("f").getRecursiveListeners().size(), 1);

    // removing all listeners of /a/b3/c/d/e/f.
    _recursiveWatcherTrie.removeRecursiveListener("/a/b3/c/d/e/f", listenerOnf_1); // test no op
    _recursiveWatcherTrie.removeRecursiveListener("/a/b3/c/d/e/f", listenerOnf_2);
    // b3 should be removed as well as all children nodes of b3
    Assert.assertEquals(_recursiveWatcherTrie.getRootNode().getChild("a").getChildren().size(), 1);
 */
  }

  class Test implements RecursivePersistListener {

    @Override
    public void handleZNodeChange(String dataPath, Watcher.Event.EventType eventType)
        throws Exception {

    }
  }
}
