/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.cortex.builtin.graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Directed dependency graph between services.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DependencyGraph {

    // from → set of direct dependencies (upstream)
    /**
     * Adjacency map from a service to the services it directly depends on.
     */
    private final Map<String, Set<String>> edges = new HashMap<>();

    /**
     * Records that {@code from} depends on {@code to}.
     *
     * @param from dependent service
     * @param to   dependency service
     */
    public void addEdge(String from, String to) {
        edges.computeIfAbsent(from, k -> new HashSet<>()).add(to);
    }

    /**
     * Returns the direct upstream dependencies of the given service.
     *
     * @param serviceId service identifier
     * @return set of upstream service IDs
     */
    public Set<String> getUpstream(String serviceId) {
        return Collections.unmodifiableSet(edges.getOrDefault(serviceId, Collections.emptySet()));
    }

    /**
     * Returns all services that directly depend on the given service.
     *
     * @param serviceId service identifier
     * @return set of downstream service IDs
     */
    public Set<String> getDownstream(String serviceId) {
        Set<String> result = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : edges.entrySet()) {
            if (entry.getValue().contains(serviceId)) {
                result.add(entry.getKey());
            }
        }
        return Collections.unmodifiableSet(result);
    }

}
