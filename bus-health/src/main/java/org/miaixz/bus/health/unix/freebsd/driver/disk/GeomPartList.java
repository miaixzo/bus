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
package org.miaixz.bus.health.unix.freebsd.driver.disk;

import java.util.*;
import java.util.function.ToIntFunction;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.HWPartition;

/**
 * Queries geom part list
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class GeomPartList {

    /**
     * The GEOM_PART_LIST constant.
     */
    private static final String GEOM_PART_LIST = "geom part list";

    /**
     * The STAT_FILESIZE constant.
     */
    private static final String STAT_FILESIZE = "stat -f %i /dev/";

    /**
     * Constructs a new GeomPartList instance.
     */
    public GeomPartList() {
        // No initialization required.
    }

    /**
     * Queries partition data using geom, mount, and stat commands
     *
     * @return A map with disk name as the key and a List of partitions as the value
     */
    public static Map<String, List<HWPartition>> queryPartitions() {
        return parsePartitions(
                Executor.runNative(GEOM_PART_LIST),
                Mount.queryPartitionToMountMap(),
                part -> Parsing.parseIntOrDefault(Executor.getFirstAnswer(STAT_FILESIZE + part), Normal._0));
    }

    /**
     * Parses {@code geom part list} output and groups every partition under its physical disk.
     *
     * @param geom        the command output
     * @param mountMap    the device-to-mount-point map
     * @param minorLookup the function that reads a partition's minor device number
     * @return the partitions grouped by physical disk name
     */
    static Map<String, List<HWPartition>> parsePartitions(
            List<String> geom,
            Map<String, String> mountMap,
            ToIntFunction<String> minorLookup) {
        Map<String, List<HWPartition>> byGeom = new HashMap<>();
        Map<String, String> geomOf = new HashMap<>();
        PartitionBuilder partition = null;
        String geomName = null;
        boolean inProviders = false;
        for (String line : geom) {
            line = line.trim();
            if (line.startsWith("Geom name:") || line.equals("Providers:") || line.equals("Consumers:")) {
                addPartition(byGeom, geomOf, geomName, partition, mountMap, minorLookup);
                partition = null;
                inProviders = line.equals("Providers:");
                if (line.startsWith("Geom name:")) {
                    geomName = line.substring(line.lastIndexOf(Symbol.C_SPACE) + Normal._1);
                }
            } else if (inProviders && geomName != null) {
                String[] split = line.split("\\s+");
                if (line.contains("Name:")) {
                    addPartition(byGeom, geomOf, geomName, partition, mountMap, minorLookup);
                    partition = new PartitionBuilder(split[split.length - Normal._1]);
                } else if (partition != null && split.length >= Normal._2) {
                    if (line.startsWith("Mediasize:")) {
                        partition.size = Parsing.parseLongOrDefault(split[Normal._1], 0L);
                    } else if (line.startsWith("rawuuid:")) {
                        partition.uuid = split[Normal._1];
                    } else if (line.startsWith("type:")) {
                        partition.type = split[Normal._1];
                    } else if (line.startsWith("label:") && !"(null)".equals(split[Normal._1])) {
                        partition.label = split[Normal._1];
                    }
                }
            }
        }
        addPartition(byGeom, geomOf, geomName, partition, mountMap, minorLookup);

        Map<String, List<HWPartition>> partitionMap = new HashMap<>();
        for (Map.Entry<String, List<HWPartition>> entry : byGeom.entrySet()) {
            partitionMap.computeIfAbsent(physicalDisk(entry.getKey(), geomOf), key -> new ArrayList<>())
                    .addAll(entry.getValue());
        }
        for (List<HWPartition> partitions : partitionMap.values()) {
            partitions.sort(Comparator.comparing(HWPartition::getName));
        }
        return partitionMap;
    }

    /**
     * Follows nested partition schemes up to their physical disk.
     *
     * @param geomName the starting geometry name
     * @param geomOf   the child-to-parent geometry map
     * @return the physical disk name
     */
    private static String physicalDisk(String geomName, Map<String, String> geomOf) {
        String disk = geomName;
        for (int depth = Normal._0; depth < Normal._8; depth++) {
            String parent = geomOf.get(disk);
            if (parent == null) {
                break;
            }
            disk = parent;
        }
        return disk;
    }

    /**
     * Adds a completed partition to its geometry.
     *
     * @param byGeom      the partitions grouped by geometry
     * @param geomOf      the child-to-parent geometry map
     * @param geomName    the current geometry name
     * @param partition   the partition under construction
     * @param mountMap    the device-to-mount-point map
     * @param minorLookup the minor device number lookup
     */
    private static void addPartition(
            Map<String, List<HWPartition>> byGeom,
            Map<String, String> geomOf,
            String geomName,
            PartitionBuilder partition,
            Map<String, String> mountMap,
            ToIntFunction<String> minorLookup) {
        if (geomName != null && partition != null) {
            geomOf.put(partition.name, geomName);
            byGeom.computeIfAbsent(geomName, key -> new ArrayList<>()).add(
                    new HWPartition(partition.name, partition.name, partition.type, partition.uuid, partition.label,
                            partition.size, Normal._0, minorLookup.applyAsInt(partition.name),
                            mountPoint(partition, mountMap)));
        }
    }

    /**
     * Resolves a partition mount point by its device name, GPT label, or GPT identifier.
     *
     * @param partition the partition details
     * @param mountMap  the device-to-mount-point map
     * @return the mount point, or an empty string
     */
    private static String mountPoint(PartitionBuilder partition, Map<String, String> mountMap) {
        String mountPoint = mountMap.get(partition.name);
        if (mountPoint == null && !partition.label.isEmpty()) {
            mountPoint = mountMap.get("gpt/" + partition.label);
        }
        if (mountPoint == null && !Normal.UNKNOWN.equals(partition.uuid)) {
            mountPoint = mountMap.get("gptid/" + partition.uuid);
        }
        return mountPoint == null ? Normal.EMPTY : mountPoint;
    }

    /**
     * Holds fields collected for one partition.
     *
     * @author Kimi Liu
     */
    private static final class PartitionBuilder {

        /** Partition name. */
        private final String name;

        /** Partition type. */
        private String type = Normal.UNKNOWN;

        /** Partition UUID. */
        private String uuid = Normal.UNKNOWN;

        /** Partition label. */
        private String label = Normal.EMPTY;

        /** Partition size in bytes. */
        private long size;

        /**
         * Creates a partition builder.
         *
         * @param name the partition name
         */
        private PartitionBuilder(String name) {
            this.name = name;
        }
    }

}
