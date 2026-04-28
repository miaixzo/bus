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
package org.miaixz.bus.health.unix.platform.solaris.hardware;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
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

import com.sun.jna.platform.unix.solaris.LibKstat.Kstat;
import com.sun.jna.platform.unix.solaris.LibKstat.KstatIO;

/**
 * Solaris hard disk implementation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class SolarisHWDiskStore extends AbstractHWDiskStore {

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
     * Creates a new SolarisHWDiskStore instance.
     *
     * @param name   the name
     * @param model  the model
     * @param serial the serial
     * @param size   the size
     */
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
        Map<String, Tuple> deviceStringMap = Iostat.queryDeviceStrings(deviceMap.keySet());

        List<HWDiskStore> storeList = new ArrayList<>();
        for (Entry<String, Tuple> entry : deviceStringMap.entrySet()) {
            String storeName = entry.getKey();
            Tuple val = entry.getValue();
            storeList.add(
                    createStore(
                            storeName,
                            val.get(0),
                            val.get(1),
                            val.get(2),
                            val.get(3),
                            val.get(4),
                            deviceMap.getOrDefault(storeName, Normal.EMPTY),
                            majorMap.getOrDefault(storeName, 0)));
        }

        return storeList;
    }

    /**
     * Creates the store.
     *
     * @param diskName the disk name
     * @param model    the model
     * @param vendor   the vendor
     * @param product  the product
     * @param serial   the serial
     * @param size     the size
     * @param mount    the mount
     * @param major    the major
     * @return the create store result
     */
    private static SolarisHWDiskStore createStore(
            String diskName,
            String model,
            String vendor,
            String product,
            String serial,
            long size,
            String mount,
            int major) {
        SolarisHWDiskStore store = new SolarisHWDiskStore(diskName,
                model.isEmpty() ? (vendor + Symbol.SPACE + product).trim() : model, serial, size);
        store.partitionList = Collections.unmodifiableList(
                Prtvtoc.queryPartitions(mount, major).stream().sorted(Comparator.comparing(HWPartition::getName))
                        .collect(Collectors.toList()));
        store.updateAttributes();
        return store;
    }

    /**
     * Returns the reads.
     *
     * @return the get reads result
     */
    @Override
    public long getReads() {
        return reads;
    }

    /**
     * Returns the read bytes.
     *
     * @return the get read bytes result
     */
    @Override
    public long getReadBytes() {
        return readBytes;
    }

    /**
     * Returns the writes.
     *
     * @return the get writes result
     */
    @Override
    public long getWrites() {
        return writes;
    }

    /**
     * Returns the write bytes.
     *
     * @return the get write bytes result
     */
    @Override
    public long getWriteBytes() {
        return writeBytes;
    }

    /**
     * Returns the current queue length.
     *
     * @return the get current queue length result
     */
    @Override
    public long getCurrentQueueLength() {
        return currentQueueLength;
    }

    /**
     * Returns the transfer time.
     *
     * @return the get transfer time result
     */
    @Override
    public long getTransferTime() {
        return transferTime;
    }

    /**
     * Returns the time stamp.
     *
     * @return the get time stamp result
     */
    @Override
    public long getTimeStamp() {
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

    /**
     * Updates the attributes2.
     *
     * @return the update attributes2 result
     */
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
        Object[] results = KstatKit.queryKstat2(
                "kstat:/disk/" + alpha + "/" + getName() + "/0",
                "reads",
                "writes",
                "nread",
                "nwritten",
                "wcnt",
                "rcnt",
                "rtime",
                "snaptime");
        // If failure try io notation
        if (results[results.length - 1] == null) {
            results = KstatKit.queryKstat2(
                    "kstat:/disk/" + alpha + "/" + numeric + "/io",
                    "reads",
                    "writes",
                    "nread",
                    "nwritten",
                    "wcnt",
                    "rcnt",
                    "rtime",
                    "snaptime");
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
