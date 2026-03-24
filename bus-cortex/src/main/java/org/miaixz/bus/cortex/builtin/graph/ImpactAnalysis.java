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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * BFS-based downstream impact analysis over a dependency graph.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ImpactAnalysis {

    /**
     * Dependency graph traversed to discover downstream impact.
     */
    private final DependencyGraph graph;

    /**
     * Creates an ImpactAnalysis over the given dependency graph.
     *
     * @param graph dependency graph to traverse
     */
    public ImpactAnalysis(DependencyGraph graph) {
        this.graph = graph;
    }

    /**
     * Finds all services transitively impacted when the given service changes.
     *
     * @param serviceId service that changed
     * @return list of impacted service IDs (BFS order, excluding the source)
     */
    public List<String> findImpacted(String serviceId) {
        List<String> impacted = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new ArrayDeque<>();
        queue.add(serviceId);
        visited.add(serviceId);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            for (String downstream : graph.getDownstream(current)) {
                if (visited.add(downstream)) {
                    impacted.add(downstream);
                    queue.add(downstream);
                }
            }
        }
        return impacted;
    }

}
