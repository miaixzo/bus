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
package org.miaixz.bus.health.unix.platform.aix.hardware;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.builtin.hardware.HWDiskStore;
import org.miaixz.bus.health.builtin.hardware.HWPartition;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHWDiskStore;
import org.miaixz.bus.health.unix.platform.aix.driver.Ls;
import org.miaixz.bus.health.unix.platform.aix.driver.Lscfg;
import org.miaixz.bus.health.unix.platform.aix.driver.Lspv;

import com.sun.jna.Native;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_disk_t;

/**
 * AIX hard disk implementation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class AixHWDiskStore extends AbstractHWDiskStore {

    /**
     * The diskStats value.
     */
    private final Supplier<perfstat_disk_t[]> diskStats;

    /**
     * The reads value.
     */
    private long reads = 0L;
    /**
     * The readBytes value.
     */
    private long readBytes = 0L;
    /**
     * The writes value.
     */
    private long writes = 0L;
    /**
     * The writeBytes value.
     */
    private long writeBytes = 0L;
    /**
     * The currentQueueLength value.
     */
    private long currentQueueLength = 0L;
    /**
     * The transferTime value.
     */
    private long transferTime = 0L;
    /**
     * The timeStamp value.
     */
    private long timeStamp = 0L;
    /**
     * The partitionList value.
     */
    private List<HWPartition> partitionList;

    /**
     * Creates a new AixHWDiskStore instance.
     *
     * @param name      the name
     * @param model     the model
     * @param serial    the serial
     * @param size      the size
     * @param diskStats the disk stats
     */
    private AixHWDiskStore(String name, String model, String serial, long size, Supplier<perfstat_disk_t[]> diskStats) {
        super(name, model, serial, size);
        this.diskStats = diskStats;
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
        return storeList.stream().sorted(
                Comparator.comparingInt(
                        s -> s.getPartitions().isEmpty() ? Integer.MAX_VALUE : s.getPartitions().get(0).getMajor()))
                .collect(Collectors.toList());
    }

    /**
     * Creates the store.
     *
     * @param diskName  the disk name
     * @param model     the model
     * @param serial    the serial
     * @param size      the size
     * @param diskStats the disk stats
     * @param majMinMap the maj min map
     * @return the create store result
     */
    private static AixHWDiskStore createStore(
            String diskName,
            String model,
            String serial,
            long size,
            Supplier<perfstat_disk_t[]> diskStats,
            Map<String, Pair<Integer, Integer>> majMinMap) {
        AixHWDiskStore store = new AixHWDiskStore(diskName, model.isEmpty() ? Normal.UNKNOWN : model, serial, size,
                diskStats);
        store.partitionList = Lspv.queryLogicalVolumes(diskName, majMinMap);
        store.updateAttributes();
        return store;
    }

    /**
     * Returns the reads.
     *
     * @return the get reads result
     */
    @Override
    public synchronized long getReads() {
        return reads;
    }

    /**
     * Returns the read bytes.
     *
     * @return the get read bytes result
     */
    @Override
    public synchronized long getReadBytes() {
        return readBytes;
    }

    /**
     * Returns the writes.
     *
     * @return the get writes result
     */
    @Override
    public synchronized long getWrites() {
        return writes;
    }

    /**
     * Returns the write bytes.
     *
     * @return the get write bytes result
     */
    @Override
    public synchronized long getWriteBytes() {
        return writeBytes;
    }

    /**
     * Returns the current queue length.
     *
     * @return the get current queue length result
     */
    @Override
    public synchronized long getCurrentQueueLength() {
        return currentQueueLength;
    }

    /**
     * Returns the transfer time.
     *
     * @return the get transfer time result
     */
    @Override
    public synchronized long getTransferTime() {
        return transferTime;
    }

    /**
     * Returns the time stamp.
     *
     * @return the get time stamp result
     */
    @Override
    public synchronized long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Returns the partitions.
     *
     * @return the get partitions result
     */
    @Override
    public List<HWPartition> getPartitions() {
        return this.partitionList;
    }

    /**
     * Updates the attributes.
     *
     * @return the update attributes result
     */
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

}
