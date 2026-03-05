/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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
package org.miaixz.bus.health.linux.driver.proc;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.linux.ProcPath;

/**
 * Utility to read disk statistics from {@code /proc/diskstats}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class DiskStats {

    /**
     * Reads the statistics in {@code /proc/diskstats} and returns the results.
     *
     * @return A map with each disk's name as the key, and an EnumMap as the value, where the numeric values in
     *         {@link IoStat} are mapped to a {@link Long} value.
     */
    public static Map<String, Map<IoStat, Long>> getDiskStats() {
        Map<String, Map<IoStat, Long>> diskStatMap = new HashMap<>();
        IoStat[] enumArray = IoStat.class.getEnumConstants();
        List<String> diskStats = Builder.readFile(ProcPath.DISKSTATS);
        for (String stat : diskStats) {
            String[] split = Pattern.SPACES_PATTERN.split(stat.trim());
            Map<IoStat, Long> statMap = new EnumMap<>(IoStat.class);
            String name = null;
            for (int i = 0; i < enumArray.length && i < split.length; i++) {
                if (enumArray[i] == IoStat.NAME) {
                    name = split[i];
                } else {
                    statMap.put(enumArray[i], Parsing.parseLongOrDefault(split[i], 0L));
                }
            }
            if (name != null) {
                diskStatMap.put(name, statMap);
            }
        }
        return diskStatMap;
    }

    /**
     * Enum corresponding to the fields in the output of {@code /proc/diskstats}
     */
    public enum IoStat {
        /**
         * The device major number.
         */
        MAJOR,
        /**
         * The device minor number.
         */
        MINOR,
        /**
         * The device name.
         */
        NAME,
        /**
         * The total number of reads completed successfully.
         */
        READS,
        /**
         * Reads which are adjacent to each other merged for efficiency.
         */
        READS_MERGED,
        /**
         * The total number of sectors read successfully.
         */
        READS_SECTOR,
        /**
         * The total number of milliseconds spent by all reads.
         */
        READS_MS,
        /**
         * The total number of writes completed successfully.
         */
        WRITES,
        /**
         * Writes which are adjacent to each other merged for efficiency.
         */
        WRITES_MERGED,
        /**
         * The total number of sectors written successfully.
         */
        WRITES_SECTOR,
        /**
         * The total number of milliseconds spent by all writes.
         */
        WRITES_MS,
        /**
         * Incremented as requests are given to appropriate struct request_queue and decremented as they finish.
         */
        IO_QUEUE_LENGTH,
        /**
         * The total number of milliseconds spent doing I/Os.
         */
        IO_MS,
        /**
         * Incremented at each I/O start, I/O completion, I/O merge, or read of these stats by the number of I/Os in
         * progress {@link #IO_QUEUE_LENGTH} times the number of milliseconds spent doing I/O since the last update of
         * this field.
         */
        IO_MS_WEIGHTED,
        /**
         * The total number of discards completed successfully.
         */
        DISCARDS,
        /**
         * Discards which are adjacent to each other merged for efficiency.
         */
        DISCARDS_MERGED,
        /**
         * The total number of sectors discarded successfully.
         */
        DISCARDS_SECTOR,
        /**
         * The total number of milliseconds spent by all discards.
         */
        DISCARDS_MS,
        /**
         * The total number of flush requests completed successfully.
         */
        FLUSHES,
        /**
         * The total number of milliseconds spent by all flush requests.
         */
        FLUSHES_MS
    }

}
