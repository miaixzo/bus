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
package org.miaixz.bus.metrics.bridge;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * Immutable operating-system uptime and optional process-state inventory.
 *
 * @param uptimeSeconds        system uptime in seconds
 * @param processCountsByState process counts keyed by normalized state
 * @author Kimi Liu
 */
public record GeneralSnapshot(long uptimeSeconds, Map<String, Long> processCountsByState) {

    /**
     * Process states exported by the host metrics convention.
     */
    private static final Set<String> STATES = Set.of("defunct", "running", "sleeping", "stopped", "other");

    /**
     * Validates and orders the snapshot.
     */
    public GeneralSnapshot {
        if (uptimeSeconds < 0) {
            throw new IllegalArgumentException("System uptime must not be negative");
        }
        Objects.requireNonNull(processCountsByState, "Process counts must not be null");
        Map<String, Long> ordered = new TreeMap<>();
        processCountsByState.forEach((state, count) -> {
            if (!STATES.contains(state)) {
                throw new IllegalArgumentException("Unsupported process state: " + state);
            }
            if (count == null || count < 0) {
                throw new IllegalArgumentException("Process count must not be negative");
            }
            ordered.put(state, count);
        });
        processCountsByState = Map.copyOf(ordered);
    }

}
