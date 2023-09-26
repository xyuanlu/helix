package org.apache.helix.rest.common.dataprovider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.helix.HelixDataAccessor;
import org.apache.helix.HelixProperty;
import org.apache.helix.PropertyKey;
import org.apache.helix.common.caches.PropertyCache;


public class RestPropertyCache<T extends HelixProperty> {

  private ConcurrentHashMap<String, T> _objCache;
  private final String _propertyDescription;

  private final RestPropertyCache.PropertyCacheKeyFuncs<T> _keyFuncs;

  public interface PropertyCacheKeyFuncs<O extends HelixProperty> {
    /**
     * Get PropertyKey for the root of this type of object, used for LIST all objects
     * @return property key to object root
     */
    PropertyKey getRootKey(HelixDataAccessor accessor);

    /**
     * Get PropertyKey for a single object of this type, used for GET single instance of the type
     * @param objName object name
     * @return property key to the object instance
     */
    PropertyKey getObjPropertyKey(HelixDataAccessor accessor, String objName);

    /**
     * Get the string to identify the object when we actually use them. It's not necessarily the
     * "id" field of HelixProperty, but could have more semantic meanings of that object type
     * @param obj object instance
     * @return object identifier
     */
    String getObjName(O obj);
  }

  public RestPropertyCache(String propertyDescription, RestPropertyCache.PropertyCacheKeyFuncs<T> keyFuncs) {
    _keyFuncs = keyFuncs;
    _propertyDescription = propertyDescription;
  }

  public void init(final HelixDataAccessor accessor) {
    _objCache = new ConcurrentHashMap<>(accessor.getChildValuesMap(_keyFuncs.getRootKey(accessor), true));
  }

  public Map<String, T> getPropertyMap() {
    return Collections.unmodifiableMap(_objCache);
  }

}
