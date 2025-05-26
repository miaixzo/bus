/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.unix.platform.aix.driver;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.HWPartition;

/**
 * Utility to query lspv
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Lspv {

    /**
     * The lspv command incurs a lot of disk reads. Since partitions shouldn't change during operation, cache the result
     * here.
     */
    private static final Map<String, List<HWPartition>> PARTITION_CACHE = new ConcurrentHashMap<>();

    private Lspv() {
    }

    /**
     * Query {@code lspv} to get partition info, or return a cached value.
     *
     * @param device    The disk to get the volumes from.
     * @param majMinMap A map of device name to a pair with major and minor numbers.
     * @return A list of logical volumes (partitions) on this device.
     */
    public static List<HWPartition> queryLogicalVolumes(String device, Map<String, Pair<Integer, Integer>> majMinMap) {
        return PARTITION_CACHE.computeIfAbsent(device,
                d -> Collections.unmodifiableList(computeLogicalVolumes(d, majMinMap).stream()
                        .sorted(Comparator.comparing(HWPartition::getMinor).thenComparing(HWPartition::getName))
                        .collect(Collectors.toList())));
    }

    private static List<HWPartition> computeLogicalVolumes(String device,
            Map<String, Pair<Integer, Integer>> majMinMap) {
        List<HWPartition> partitions = new ArrayList<>();
        /*-
         $ lspv -L hdisk0
        PHYSICAL VOLUME:    hdisk0                   VOLUME GROUP:     rootvg
        PV IDENTIFIER:      000acfde95524f85 VG IDENTIFIER     000acfde00004c000000000395525276
        PV STATE:           active
        STALE PARTITIONS:   0                        ALLOCATABLE:      yes
        PP SIZE:            128 megabyte(s)          LOGICAL VOLUMES:  12
        TOTAL PPs:          271 (34688 megabytes)    VG DESCRIPTORS:   2
        FREE PPs:           227 (29056 megabytes)    HOT SPARE:        no
        USED PPs:           44 (5632 megabytes)      MAX REQUEST:      256 kilobytes
        FREE DISTRIBUTION:  54..46..19..54..54
        USED DISTRIBUTION:  01..08..35..00..00
         */
        String stateMarker = "PV STATE:";
        String sizeMarker = "PP SIZE:";
        long ppSize = 0L; // All physical partitions are the same size
        for (String s : Executor.runNative("lspv -L " + device)) {
            if (s.startsWith(stateMarker)) {
                if (!s.contains("active")) {
                    return partitions;
                }
            } else if (s.contains(sizeMarker)) {
                ppSize = Parsing.getFirstIntValue(s);
            }
        }
        if (ppSize == 0L) {
            return partitions;
        }
        // Convert to megabytes
        ppSize <<= 20;
        /*-
         $ lspv -p hdisk0
        hdisk0:
        PP RANGE  STATE   REGION        LV NAME             TYPE       MOUNT POINT
        1-1     used    outer edge    hd5                 boot       N/A
        2-55    free    outer edge
        56-59    used    outer middle  hd6                 paging     N/A
        60-61    used    outer middle  livedump            jfs2       /var/adm/ras/livedump
        62-62    used    outer middle  loglv01             jfslog     N/A
        63-63    used    outer middle  lv01                jfs        N/A
        64-109   free    outer middle
        110-110   used    center        hd8                 jfs2log    N/A
        111-112   used    center        hd4                 jfs2       /
        113-128   used    center        hd2                 jfs2       /usr
        129-131   used    center        hd9var              jfs2       /var
        132-132   used    center        hd3                 jfs2       /tmp
        133-133   used    center        hd9var              jfs2       /var
        134-136   used    center        hd10opt             jfs2       /opt
        137-137   used    center        hd11admin           jfs2       /admin
        138-140   used    center        hd2                 jfs2       /usr
        141-141   used    center        hd3                 jfs2       /tmp
        142-142   used    center        hd4                 jfs2       /
        143-143   used    center        hd9var              jfs2       /var
        144-144   used    center        hd2                 jfs2       /usr
        145-163   free    center
        164-217   free    inner middle
        218-271   free    inner edge
         */
        Map<String, String> mountMap = new HashMap<>();
        Map<String, String> typeMap = new HashMap<>();
        Map<String, Integer> ppMap = new HashMap<>();
        for (String s : Executor.runNative("lspv -p " + device)) {
            String[] split = Pattern.SPACES_PATTERN.split(s.trim());
            if (split.length >= 6 && "used".equals(split[1])) {
                // Region may have two words, so count from end
                String name = split[split.length - 3];
                mountMap.put(name, split[split.length - 1]);
                typeMap.put(name, split[split.length - 2]);
                int ppCount = 1 + Parsing.getNthIntValue(split[0], 2) - Parsing.getNthIntValue(split[0], 1);
                ppMap.put(name, ppCount + ppMap.getOrDefault(name, 0));
            }
        }
        for (Entry<String, String> entry : mountMap.entrySet()) {
            String mount = "N/A".equals(entry.getValue()) ? Normal.EMPTY : entry.getValue();
            // All maps should have same keys
            String name = entry.getKey();
            String type = typeMap.get(name);
            long size = ppSize * ppMap.get(name);
            Pair<Integer, Integer> majMin = majMinMap.get(name);
            int major = majMin == null ? Parsing.getFirstIntValue(name) : majMin.getLeft();
            int minor = majMin == null ? Parsing.getFirstIntValue(name) : majMin.getRight();
            partitions.add(new HWPartition(name, name, type, Normal.EMPTY, size, major, minor, mount));
        }
        return partitions;
    }

}
