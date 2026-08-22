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
package org.miaixz.bus.health.mac.hardware;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.mac.driver.CpuFrequencyResidency;

/**
 * One interval of Apple Silicon CPU performance-state residency.
 *
 * @author Kimi Liu
 */
@Immutable
public class CpuResidencySample {

    /**
     * The per-core state residency map.
     */
    private final Map<String, Map<String, Long>> coreStates;

    /**
     * The per-complex state residency map.
     */
    private final Map<String, Map<String, Long>> complexStates;

    /**
     * Creates a new sample.
     *
     * @param coreStates    the ticks each core spent in each state
     * @param complexStates the ticks each CPU complex spent in each state
     */
    public CpuResidencySample(Map<String, Map<String, Long>> coreStates, Map<String, Map<String, Long>> complexStates) {
        this.coreStates = copy(coreStates);
        this.complexStates = copy(complexStates);
    }

    /**
     * Copies a nested map into insertion-ordered, unmodifiable maps.
     *
     * @param states the source states
     * @return the copied states
     */
    private static Map<String, Map<String, Long>> copy(Map<String, Map<String, Long>> states) {
        Map<String, Map<String, Long>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Long>> channel : states.entrySet()) {
            copy.put(channel.getKey(), Collections.unmodifiableMap(new LinkedHashMap<>(channel.getValue())));
        }
        return Collections.unmodifiableMap(copy);
    }

    /**
     * Gets the residency of individual cores.
     *
     * @return the residency keyed by core channel name
     */
    public Map<String, Map<String, Long>> getCoreStates() {
        return coreStates;
    }

    /**
     * Gets the residency of CPU complexes.
     *
     * @return the residency keyed by complex channel name
     * @see CpuFrequencyResidency#realizedComplexStates(Map)
     */
    public Map<String, Map<String, Long>> getComplexStates() {
        return complexStates;
    }

    /**
     * Returns a string representation of this sample.
     *
     * @return a string representation of this sample
     */
    @Override
    public String toString() {
        return "CpuResidencySample{cores=" + coreStates.keySet() + ", complexes=" + complexStates.keySet() + '}';
    }

}
