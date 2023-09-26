package org.apache.helix.rest.common.dataprovider;

import org.apache.helix.HelixConstants;


public class RestCacheCallBackHandler {

  public class ChangeContext {

    // the Helix property that changed
    private HelixConstants.ChangeType _changeType;

  }

  public enum EventType {
    INIT,
    CALLBACK,
    FINALIZE
  }
}
