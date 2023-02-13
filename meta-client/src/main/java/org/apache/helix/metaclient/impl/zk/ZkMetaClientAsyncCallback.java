package org.apache.helix.metaclient.impl.zk;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.helix.metaclient.api.AsyncCallback;
import org.apache.helix.metaclient.api.MetaClientInterface;
import org.apache.helix.metaclient.impl.zk.util.ZkMetaClientUtil;
import org.apache.helix.zookeeper.zkclient.callback.ZkAsyncCallbacks;


/**
 * Wrapper class for metaclient.api.AsyncCallback.
 * This wrapper class extends zk callback class. It has an object of user defined
 * metaclient.api.AsyncCallback.
 * Each callback will do default retry defined in ZkAsyncCallbacks. (defined in ZkAsyncCallbacks)
 *
 * ZkClient execute async callbacks at zkClient main thead, retry is handles in a separate retry
 * thread. In our first version of implementation, we will keep similar behavior and have
 * callbacks executed in ZkClient event thread, and reuse zkclient retry logic.
 */
public class ZkMetaClientAsyncCallback extends ZkAsyncCallbacks {

  public static class ZkMetaClientCreateCallbackHandler extends CreateCallbackHandler {
    AsyncCallback.VoidCallback _userCallback;

    public ZkMetaClientCreateCallbackHandler(AsyncCallback.VoidCallback cb) {
      _userCallback = cb;
    }

    @Override
    public void handle() {
      // TODO: the processResult is changed in another PR. Will remove the `null` param when
      // dependent PR is merged
      _userCallback.processResult(getRc(), getPath(), null);
    }
  }

  public static class ZkMetaClientDeleteCallbackHandler extends DeleteCallbackHandler {
    AsyncCallback.VoidCallback _userCallback;

    public ZkMetaClientDeleteCallbackHandler(AsyncCallback.VoidCallback cb) {
      _userCallback = cb;
    }

    @Override
    public void handle() {
      _userCallback.processResult(getRc(), getPath(), null);
    }
  }

  public static class ZkMetaClientSetCallbackHandler extends SetDataCallbackHandler {
    AsyncCallback.StatCallback _userCallback;

    public ZkMetaClientSetCallbackHandler(AsyncCallback.StatCallback cb) {
      _userCallback = cb;
    }

    @Override
    public void handle() {
      _userCallback.processResult(getRc(), getPath(), null, getStat() == null ? null
          : new MetaClientInterface.Stat(
              ZkMetaClientUtil.convertZkEntryModeToMetaClientEntryMode(getStat().getEphemeralOwner()),
              getStat().getVersion()));
    }
  }

  public static class ZkMetaClientExistCallbackHandler extends ExistsCallbackHandler {
    AsyncCallback.StatCallback _userCallback;

    public ZkMetaClientExistCallbackHandler(AsyncCallback.StatCallback cb) {
      _userCallback = cb;
    }

    @Override
    public void handle() {
      _userCallback.processResult(getRc(), getPath(), null, getStat() == null ? null
          : new MetaClientInterface.Stat(
              ZkMetaClientUtil.convertZkEntryModeToMetaClientEntryMode(getStat().getEphemeralOwner()),
              getStat().getVersion()));
    }
  }

  public static class ZkMetaClientGetCallbackHandler extends GetDataCallbackHandler {
    AsyncCallback.DataCallback _userCallback;

    public ZkMetaClientGetCallbackHandler(AsyncCallback.DataCallback cb) {
      _userCallback = cb;
    }

    @Override
    public void handle() {
      _userCallback.processResult(getRc(), getPath(), null, getData(),
          getStat() == null ? null : new MetaClientInterface.Stat(
              ZkMetaClientUtil.convertZkEntryModeToMetaClientEntryMode(getStat().getEphemeralOwner()),
              getStat().getVersion()));
    }
  }
}