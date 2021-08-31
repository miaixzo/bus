/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org OSHI and other contributors.                 *
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
package org.aoju.bus.health.unix.aix.hardware;

import com.sun.jna.Native;
import com.sun.jna.platform.unix.aix.Perfstat;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.tuple.Pair;
import org.aoju.bus.health.builtin.hardware.AbstractHWDiskStore;
import org.aoju.bus.health.builtin.hardware.HWDiskStore;
import org.aoju.bus.health.builtin.hardware.HWPartition;
import org.aoju.bus.health.unix.aix.drivers.Ls;
import org.aoju.bus.health.unix.aix.drivers.Lscfg;
import org.aoju.bus.health.unix.aix.drivers.Lspv;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * AIX hard disk implementation.
 *
 * @author Kimi Liu
 * @version 6.2.8
 * @since JDK 1.8+
 */
@ThreadSafe
public final class AixHWDiskStore extends AbstractHWDiskStore {

    private final Supplier<Perfstat.perfstat_disk_t[]> diskStats;

    private long reads = 0L;
    private long readBytes = 0L;
    private long writes = 0L;
    private long writeBytes = 0L;
    private long currentQueueLength = 0L;
    private long transferTime = 0L;
    private long timeStamp = 0L;
    private List<HWPartition> partitionList;

    private AixHWDiskStore(String name, String model, String serial, long size, Supplier<Perfstat.perfstat_disk_t[]> diskStats) {
        super(name, model, serial, size);
        this.diskStats = diskStats;
    }

    /**
     * Gets the disks on this machine
     *
     * @param diskStats Memoized supplier of disk statistics
     * @return a list of {@link HWDiskStore} objects representing the disks
     */
    public static List<HWDiskStore> getDisks(Supplier<Perfstat.perfstat_disk_t[]> diskStats) {
        Map<String, Pair<Integer, Integer>> majMinMap = Ls.queryDeviceMajorMinor();
        List<AixHWDiskStore> storeList = new ArrayList<>();
        for (Perfstat.perfstat_disk_t disk : diskStats.get()) {
            String storeName = Native.toString(disk.name);
            Pair<String, String> ms = Lscfg.queryModelSerial(storeName);
            String model = null == ms.getLeft() ? Native.toString(disk.description) : ms.getLeft();
            String serial = null == ms.getRight() ? Normal.UNKNOWN : ms.getRight();
            storeList.add(createStore(storeName, model, serial, disk.size << 20, diskStats, majMinMap));
        }
        return storeList.stream()
                .sorted(Comparator.comparingInt(
                        s -> s.getPartitions().isEmpty() ? Integer.MAX_VALUE : s.getPartitions().get(0).getMajor()))
                .collect(Collectors.toList());
    }

    private static AixHWDiskStore createStore(String diskName, String model, String serial, long size,
                                              Supplier<Perfstat.perfstat_disk_t[]> diskStats, Map<String, Pair<Integer, Integer>> majMinMap) {
        AixHWDiskStore store = new AixHWDiskStore(diskName, model.isEmpty() ? Normal.UNKNOWN : model, serial, size,
                diskStats);
        store.partitionList = Collections.unmodifiableList(Lspv.queryLogicalVolumes(diskName, majMinMap).stream()
                .sorted(Comparator.comparing(HWPartition::getMinor).thenComparing(HWPartition::getName))
                .collect(Collectors.toList()));
        store.updateAttributes();
        return store;
    }

    @Override
    public long getReads() {
        return reads;
    }

    @Override
    public long getReadBytes() {
        return readBytes;
    }

    @Override
    public long getWrites() {
        return writes;
    }

    @Override
    public long getWriteBytes() {
        return writeBytes;
    }

    @Override
    public long getCurrentQueueLength() {
        return currentQueueLength;
    }

    @Override
    public long getTransferTime() {
        return transferTime;
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public List<HWPartition> getPartitions() {
        return this.partitionList;
    }

    @Override
    public boolean updateAttributes() {
        for (Perfstat.perfstat_disk_t stat : diskStats.get()) {
            String name = Native.toString(stat.name);
            if (name.equals(this.getName())) {
                // we only have total transfers so estimate read/write ratio from blocks
                long blks = stat.rblks + stat.wblks;
                this.reads = stat.xfers;
                if (blks > 0L) {
                    this.writes = stat.xfers * stat.wblks / blks;
                    this.reads -= this.writes;
                }
                this.readBytes = stat.rblks * stat.bsize;
                this.writeBytes = stat.wblks * stat.bsize;
                this.currentQueueLength = stat.qdepth;
                this.transferTime = stat.time;
                return true;
            }
        }
        return false;
    }

}
