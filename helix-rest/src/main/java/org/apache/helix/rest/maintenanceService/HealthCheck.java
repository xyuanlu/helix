package org.apache.helix.rest.maintenanceService;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;


public enum HealthCheck {
    /**
     * Check if instance is alive
     */
    INSTANCE_NOT_ALIVE,
    /**
     * Check if instance is enabled both in instance config and cluster config
     */
    INSTANCE_NOT_ENABLED,
    /**
     * Check if instance is stable
     * Stable means all the ideal state mapping matches external view (view of current state).
     */
    INSTANCE_NOT_STABLE,
    /**
     * Check if instance has 0 resource assigned
     */
    EMPTY_RESOURCE_ASSIGNMENT,
    /**
     * Check if instance has disabled partitions
     */
    HAS_DISABLED_PARTITION,
    /**
     * Check if instance has valid configuration (pre-requisite for all checks)
     */
    INVALID_CONFIG,
    /**
     * Check if instance has error partitions
     */
    HAS_ERROR_PARTITION,
    /**
     * Check if all resources hosted on the instance can still meet the min active replica
     * constraint if this instance is shutdown
     */
    MIN_ACTIVE_REPLICA_CHECK_FAILED;

    /**
     * Pre-defined list of checks to test if an instance can be stopped at runtime
     */
    public static List<HealthCheck> STOPPABLE_CHECK_LIST = Arrays.asList(HealthCheck.values());
    /**
     * Pre-defined list of checks to test if an instance is in healthy running state
     */
    public static List<HealthCheck> STARTED_AND_HEALTH_CHECK_LIST = ImmutableList
        .of(INVALID_CONFIG, INSTANCE_NOT_ALIVE, INSTANCE_NOT_ENABLED, INSTANCE_NOT_STABLE,
            EMPTY_RESOURCE_ASSIGNMENT);
  }

