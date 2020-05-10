/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
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
 ********************************************************************************/
package org.aoju.bus.health.unix.solaris.hardware;

import com.sun.jna.platform.unix.solaris.LibKstat.Kstat;
import com.sun.jna.platform.unix.solaris.LibKstat.KstatIO;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.tuple.Quintet;
import org.aoju.bus.health.builtin.hardware.AbstractHWDiskStore;
import org.aoju.bus.health.builtin.hardware.HWDiskStore;
import org.aoju.bus.health.builtin.hardware.HWPartition;
import org.aoju.bus.health.unix.solaris.KstatCtl;
import org.aoju.bus.health.unix.solaris.KstatCtl.KstatChain;
import org.aoju.bus.health.unix.solaris.drivers.Iostat;
import org.aoju.bus.health.unix.solaris.drivers.Lshal;
import org.aoju.bus.health.unix.solaris.drivers.Prtvtoc;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Solaris hard disk implementation.
 *
 * @author Kimi Liu
 * @version 5.9.0
 * @since JDK 1.8+
 */
@ThreadSafe
public final class SolarisHWDiskStore extends AbstractHWDiskStore {

    private long reads = 0L;
    private long readBytes = 0L;
    private long writes = 0L;
    private long writeBytes = 0L;
    private long currentQueueLength = 0L;
    private long transferTime = 0L;
    private long timeStamp = 0L;
    private List<HWPartition> partitionList;

    private SolarisHWDiskStore(String name, String model, String serial, long size) {
        super(name, model, serial, size);
    }

    /**
     * Gets the disks on this machine
     *
     * @return an {@code UnmodifiableList} of {@link HWDiskStore} objects
     * representing the disks
     */
    public static List<HWDiskStore> getDisks() {
        // Create map to correlate disk name with block device mount point for
        // later use in partition info
        Map<String, String> deviceMap = Iostat.queryPartitionToMountMap();

        // Create map to correlate disk name with block device mount point for
        // later use in partition info. Run lshal, if available, to get block device
        // major (we'll use partition # for minor)
        Map<String, Integer> majorMap = Lshal.queryDiskToMajorMap();

        // Create map of model, vendor, product, serial, size
        // We'll use Model if available, otherwise Vendor+Product
        Map<String, Quintet<String, String, String, String, Long>> deviceStringMap = Iostat
                .queryDeviceStrings(deviceMap.keySet());

        List<SolarisHWDiskStore> storeList = new ArrayList<>();
        for (Entry<String, Quintet<String, String, String, String, Long>> entry : deviceStringMap.entrySet()) {
            String storeName = entry.getKey();
            Quintet<String, String, String, String, Long> val = entry.getValue();
            storeList.add(createStore(storeName, val.getA(), val.getB(), val.getC(), val.getD(), val.getE(),
                    deviceMap.getOrDefault(storeName, Normal.EMPTY), majorMap.getOrDefault(storeName, 0)));
        }

        return Collections.unmodifiableList(storeList);
    }

    private static SolarisHWDiskStore createStore(String diskName, String model, String vendor, String product,
                                                  String serial, long size, String mount, int major) {
        SolarisHWDiskStore store = new SolarisHWDiskStore(diskName,
                model.isEmpty() ? (vendor + Symbol.SPACE + product).trim() : model, serial, size);
        store.partitionList = Collections.unmodifiableList(Prtvtoc.queryPartitions(mount, major).stream()
                .sorted(Comparator.comparing(HWPartition::getName)).collect(Collectors.toList()));
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
        try (KstatChain kc = KstatCtl.openChain()) {
            Kstat ksp = kc.lookup(null, 0, getName());
            if (ksp != null && kc.read(ksp)) {
                KstatIO data = new KstatIO(ksp.ks_data);
                this.reads = data.reads;
                this.writes = data.writes;
                this.readBytes = data.nread;
                this.writeBytes = data.nwritten;
                this.currentQueueLength = (long) data.wcnt + data.rcnt;
                // rtime and snaptime are nanoseconds, convert to millis
                this.transferTime = data.rtime / 1_000_000L;
                this.timeStamp = ksp.ks_snaptime / 1_000_000L;
                return true;
            }
        }
        return false;
    }

}
