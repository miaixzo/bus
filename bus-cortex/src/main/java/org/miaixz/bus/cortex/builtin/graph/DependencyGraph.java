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

import java.util.*;

/**
 * Directed dependency graph between services.
 * <p>
 * Edges are stored as {@code from -> upstream dependency}. Downstream impact analysis therefore walks the reverse
 * direction through {@link #getDownstream(String)}.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DependencyGraph {

    /**
     * Creates an empty dependency graph.
     */
    public DependencyGraph() {
    }

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

    /**
     * Returns a stable topological ordering when the graph is acyclic.
     *
     * @return topological order
     */
    public List<String> topologicalOrder() {
        Map<String, Integer> indegree = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> entry : edges.entrySet()) {
            indegree.putIfAbsent(entry.getKey(), 0);
            for (String dependency : entry.getValue()) {
                indegree.put(dependency, indegree.getOrDefault(dependency, 0) + 1);
            }
        }
        ArrayDeque<String> queue = new ArrayDeque<>();
        for (Map.Entry<String, Integer> entry : indegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }
        List<String> result = new ArrayList<>(indegree.size());
        while (!queue.isEmpty()) {
            String current = queue.removeFirst();
            result.add(current);
            for (String dependency : edges.getOrDefault(current, Collections.emptySet())) {
                int next = indegree.computeIfPresent(dependency, (key, value) -> value - 1);
                if (next == 0) {
                    queue.add(dependency);
                }
            }
        }
        return result;
    }

    /**
     * Returns whether the dependency graph contains a cycle.
     *
     * @return {@code true} when a cycle exists
     */
    public boolean hasCycle() {
        return !edges.isEmpty() && topologicalOrder().size() < nodeCount();
    }

    /**
     * Counts all unique nodes known to the graph, including dependency-only nodes.
     *
     * @return total number of distinct nodes
     */
    private int nodeCount() {
        Set<String> nodes = new HashSet<>(edges.keySet());
        for (Set<String> values : edges.values()) {
            nodes.addAll(values);
        }
        return nodes.size();
    }

}
