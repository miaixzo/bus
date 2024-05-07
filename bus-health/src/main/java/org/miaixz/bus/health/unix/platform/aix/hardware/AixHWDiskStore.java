/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI Team and other contributors.          *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.health.unix.platform.aix.hardware;

import com.sun.jna.Native;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_disk_t;
import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.builtin.hardware.HWDiskStore;
import org.miaixz.bus.health.builtin.hardware.HWPartition;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHWDiskStore;
import org.miaixz.bus.health.unix.platform.aix.driver.Ls;
import org.miaixz.bus.health.unix.platform.aix.driver.Lscfg;
import org.miaixz.bus.health.unix.platform.aix.driver.Lspv;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * AIX hard disk implementation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class AixHWDiskStore extends AbstractHWDiskStore {

    private final Supplier<perfstat_disk_t[]> diskStats;

    private long reads = 0L;
    private long readBytes = 0L;
    private long writes = 0L;
    private long writeBytes = 0L;
    private long currentQueueLength = 0L;
    private long transferTime = 0L;
    private long timeStamp = 0L;
    private List<HWPartition> partitionList;

    private AixHWDiskStore(String name, String model, String serial, long size, Supplier<perfstat_disk_t[]> diskStats) {
        super(name, model, serial, size);
        this.diskStats = diskStats;
    }

    @Override
    public synchronized long getReads() {
        return reads;
    }

    @Override
    public synchronized long getReadBytes() {
        return readBytes;
    }

    @Override
    public synchronized long getWrites() {
        return writes;
    }

    @Override
    public synchronized long getWriteBytes() {
        return writeBytes;
    }

    @Override
    public synchronized long getCurrentQueueLength() {
        return currentQueueLength;
    }

    @Override
    public synchronized long getTransferTime() {
        return transferTime;
    }

    @Override
    public synchronized long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public List<HWPartition> getPartitions() {
        return this.partitionList;
    }

    @Override
    public synchronized boolean updateAttributes() {
        long now = System.currentTimeMillis();
        for (perfstat_disk_t stat : diskStats.get()) {
            String name = Native.toString(stat.name);
            if (name.equals(this.getName())) {
                // we only have total transfers so estimate read/write ratio from blocks
                long blks = stat.rblks + stat.wblks;
                if (blks == 0L) {
                    this.reads = stat.xfers;
                    this.writes = 0L;
                } else {
                    long approximateReads = Math.round(stat.xfers * stat.rblks / (double) blks);
                    long approximateWrites = stat.xfers - approximateReads;
                    // Enforce monotonic increase
                    if (approximateReads > this.reads) {
                        this.reads = approximateReads;
                    }
                    if (approximateWrites > this.writes) {
                        this.writes = approximateWrites;
                    }
                }
                this.readBytes = stat.rblks * stat.bsize;
                this.writeBytes = stat.wblks * stat.bsize;
                this.currentQueueLength = stat.qdepth;
                this.transferTime = stat.time;
                this.timeStamp = now;
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the disks on this machine
     *
     * @param diskStats Memoized supplier of disk statistics
     * @return a list of {@link HWDiskStore} objects representing the disks
     */
    public static List<HWDiskStore> getDisks(Supplier<perfstat_disk_t[]> diskStats) {
        Map<String, Pair<Integer, Integer>> majMinMap = Ls.queryDeviceMajorMinor();
        List<AixHWDiskStore> storeList = new ArrayList<>();
        for (perfstat_disk_t disk : diskStats.get()) {
            String storeName = Native.toString(disk.name);
            Pair<String, String> ms = Lscfg.queryModelSerial(storeName);
            String model = ms.getLeft() == null ? Native.toString(disk.description) : ms.getLeft();
            String serial = ms.getRight() == null ? Normal.UNKNOWN : ms.getRight();
            storeList.add(createStore(storeName, model, serial, disk.size << 20, diskStats, majMinMap));
        }
        return storeList.stream()
                .sorted(Comparator.comparingInt(
                        s -> s.getPartitions().isEmpty() ? Integer.MAX_VALUE : s.getPartitions().get(0).getMajor()))
                .collect(Collectors.toList());
    }

    private static AixHWDiskStore createStore(String diskName, String model, String serial, long size,
                                              Supplier<perfstat_disk_t[]> diskStats, Map<String, Pair<Integer, Integer>> majMinMap) {
        AixHWDiskStore store = new AixHWDiskStore(diskName, model.isEmpty() ? Normal.UNKNOWN : model, serial, size,
                diskStats);
        store.partitionList = Lspv.queryLogicalVolumes(diskName, majMinMap);
        store.updateAttributes();
        return store;
    }
}
