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
package org.miaixz.bus.health.unix.platform.solaris.hardware;

import com.sun.jna.platform.unix.solaris.LibKstat.Kstat;
import com.sun.jna.platform.unix.solaris.LibKstat.KstatIO;
import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.health.builtin.hardware.HWDiskStore;
import org.miaixz.bus.health.builtin.hardware.HWPartition;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHWDiskStore;
import org.miaixz.bus.health.unix.platform.solaris.KstatKit;
import org.miaixz.bus.health.unix.platform.solaris.KstatKit.KstatChain;
import org.miaixz.bus.health.unix.platform.solaris.driver.disk.Iostat;
import org.miaixz.bus.health.unix.platform.solaris.driver.disk.Lshal;
import org.miaixz.bus.health.unix.platform.solaris.driver.disk.Prtvtoc;
import org.miaixz.bus.health.unix.platform.solaris.software.SolarisOperatingSystem;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Solaris hard disk implementation.
 *
 * @author Kimi Liu
 * @since Java 17+
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
     * @return a list of {@link HWDiskStore} objects representing the disks
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
        Map<String, Tuple> deviceStringMap = Iostat
                .queryDeviceStrings(deviceMap.keySet());

        List<HWDiskStore> storeList = new ArrayList<>();
        for (Entry<String, Tuple> entry : deviceStringMap.entrySet()) {
            String storeName = entry.getKey();
            Tuple val = entry.getValue();
            storeList.add(createStore(storeName, val.get(0), val.get(1), val.get(2), val.get(3), val.get(4),
                    deviceMap.getOrDefault(storeName, Normal.EMPTY), majorMap.getOrDefault(storeName, 0)));
        }

        return storeList;
    }

    private static SolarisHWDiskStore createStore(String diskName, String model, String vendor, String product,
                                                  String serial, long size, String mount, int major) {
        SolarisHWDiskStore store = new SolarisHWDiskStore(diskName,
                model.isEmpty() ? (vendor + " " + product).trim() : model, serial, size);
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
        this.timeStamp = System.currentTimeMillis();
        if (SolarisOperatingSystem.HAS_KSTAT2) {
            // Use Kstat2 implementation
            return updateAttributes2();
        }
        try (KstatChain kc = KstatKit.openChain()) {
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

    private boolean updateAttributes2() {
        String fullName = getName();
        String alpha = fullName;
        String numeric = Normal.EMPTY;
        for (int c = 0; c < fullName.length(); c++) {
            if (fullName.charAt(c) >= '0' && fullName.charAt(c) <= '9') {
                alpha = fullName.substring(0, c);
                numeric = fullName.substring(c);
                break;
            }
        }
        // Try device style notation
        Object[] results = KstatKit.queryKstat2("kstat:/disk/" + alpha + "/" + getName() + "/0", "reads", "writes",
                "nread", "nwritten", "wcnt", "rcnt", "rtime", "snaptime");
        // If failure try io notation
        if (results[results.length - 1] == null) {
            results = KstatKit.queryKstat2("kstat:/disk/" + alpha + "/" + numeric + "/io", "reads", "writes", "nread",
                    "nwritten", "wcnt", "rcnt", "rtime", "snaptime");
        }
        if (results[results.length - 1] == null) {
            return false;
        }
        this.reads = results[0] == null ? 0L : (long) results[0];
        this.writes = results[1] == null ? 0L : (long) results[1];
        this.readBytes = results[2] == null ? 0L : (long) results[2];
        this.writeBytes = results[3] == null ? 0L : (long) results[3];
        this.currentQueueLength = results[4] == null ? 0L : (long) results[4];
        this.currentQueueLength += results[5] == null ? 0L : (long) results[5];
        // rtime and snaptime are nanoseconds, convert to millis
        this.transferTime = results[6] == null ? 0L : (long) results[6] / 1_000_000L;
        this.timeStamp = (long) results[7] / 1_000_000L;
        return true;
    }

}
