package org.apache.helix.rest.server.service;

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

import org.apache.helix.rest.server.json.cluster.ClusterInfo;
import org.apache.helix.rest.server.json.cluster.ClusterTopology;


/**
 * A rest wrapper service that provides information about cluster
 * TODO add more business logic and simplify the workload on ClusterAccessor
 */
public interface ClusterService {
  /**
   * Get cluster topology
   * @param cluster
   * @return
   */
  ClusterTopology getClusterTopology(String cluster);

  /**
   * Get the topology of a virtual cluster. If useRealTopology is true, return the real topology
   * of the cluster. If useRealTopology is false, return the virtual topology of the cluster.
   * @param cluster the cluster name
   * @param useRealTopology whether to use the real topology or the virtual topology
   * @return the cluster topology
   */
  ClusterTopology getTopologyOfVirtualCluster(String cluster, boolean useRealTopology);

  /**
   * Get cluster basic information
   * @param clusterId
   * @return
   */
  ClusterInfo getClusterInfo(String clusterId);

  /**
   * Check if the cluster is topology aware
   * @param clusterId
   * @return
   */
  boolean isClusterTopologyAware(String clusterId);
}
