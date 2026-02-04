/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.builtin.hardware.common;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.miaixz.bus.health.builtin.hardware.LogicalVolumeGroup;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class AbstractLogicalVolumeGroup implements LogicalVolumeGroup {

    private final String name;
    private final Map<String, Set<String>> lvMap;
    private final Set<String> pvSet;

    /**
     * @param name  Name of the volume group
     * @param lvMap Logical volumes derived from this volume group and the physical volumes its mapped to.
     * @param pvSet Set of physical volumes this volume group consists of.
     */
    protected AbstractLogicalVolumeGroup(String name, Map<String, Set<String>> lvMap, Set<String> pvSet) {
        this.name = name;
        for (Entry<String, Set<String>> entry : lvMap.entrySet()) {
            lvMap.put(entry.getKey(), Collections.unmodifiableSet(entry.getValue()));
        }
        this.lvMap = Collections.unmodifiableMap(lvMap);
        this.pvSet = Collections.unmodifiableSet(pvSet);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, Set<String>> getLogicalVolumes() {
        return lvMap;
    }

    @Override
    public Set<String> getPhysicalVolumes() {
        return pvSet;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Logical Volume Group: ");
        sb.append(name).append("\n |-- PVs: ");
        sb.append(pvSet.toString());
        for (Entry<String, Set<String>> entry : lvMap.entrySet()) {
            sb.append("\n |-- LV: ").append(entry.getKey());
            Set<String> mappedPVs = entry.getValue();
            if (!mappedPVs.isEmpty()) {
                sb.append(" --> ").append(mappedPVs);
            }
        }
        return sb.toString();
    }

}
