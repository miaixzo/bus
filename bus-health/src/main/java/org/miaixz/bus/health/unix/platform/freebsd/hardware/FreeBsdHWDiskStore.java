/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health.unix.platform.freebsd.hardware;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.HWDiskStore;
import org.miaixz.bus.health.builtin.hardware.HWPartition;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHWDiskStore;
import org.miaixz.bus.health.unix.platform.freebsd.BsdSysctlKit;
import org.miaixz.bus.health.unix.platform.freebsd.driver.disk.GeomDiskList;
import org.miaixz.bus.health.unix.platform.freebsd.driver.disk.GeomPartList;

import java.util.*;
import java.util.stream.Collectors;

/**
 * FreeBSD hard disk implementation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class FreeBsdHWDiskStore extends AbstractHWDiskStore {

    private long reads = 0L;
    private long readBytes = 0L;
    private long writes = 0L;
    private long writeBytes = 0L;
    private long currentQueueLength = 0L;
    private long transferTime = 0L;
    private long timeStamp = 0L;
    private List<HWPartition> partitionList;

    private FreeBsdHWDiskStore(String name, String model, String serial, long size) {
        super(name, model, serial, size);
    }

    /**
     * Gets the disks on this machine
     *
     * @return a list of {@link HWDiskStore} objects representing the disks
     */
    public static List<HWDiskStore> getDisks() {
        // Result
        List<HWDiskStore> diskList = new ArrayList<>();

        // Get map of disk names to partitions
        Map<String, List<HWPartition>> partitionMap = GeomPartList.queryPartitions();

        // Get map of disk names to disk info
        Map<String, Triplet<String, String, Long>> diskInfoMap = GeomDiskList.queryDisks();

        // Get list of disks from sysctl
        List<String> devices = Arrays
                .asList(Pattern.SPACES_PATTERN.split(BsdSysctlKit.sysctl("kern.disks", Normal.EMPTY)));

        // Run iostat -Ix to enumerate disks by name and get kb r/w
        List<String> iostat = Executor.runNative("iostat -Ix");
        long now = System.currentTimeMillis();
        for (String line : iostat) {
            String[] split = Pattern.SPACES_PATTERN.split(line);
            if (split.length > 6 && devices.contains(split[0])) {
                Triplet<String, String, Long> storeInfo = diskInfoMap.get(split[0]);
                FreeBsdHWDiskStore store = (storeInfo == null)
                        ? new FreeBsdHWDiskStore(split[0], Normal.UNKNOWN, Normal.UNKNOWN, 0L)
                        : new FreeBsdHWDiskStore(split[0], storeInfo.getLeft(), storeInfo.getMiddle(),
                                storeInfo.getRight());
                store.reads = (long) Parsing.parseDoubleOrDefault(split[1], 0d);
                store.writes = (long) Parsing.parseDoubleOrDefault(split[2], 0d);
                // In KB
                store.readBytes = (long) (Parsing.parseDoubleOrDefault(split[3], 0d) * 1024);
                store.writeBytes = (long) (Parsing.parseDoubleOrDefault(split[4], 0d) * 1024);
                // # transactions
                store.currentQueueLength = Parsing.parseLongOrDefault(split[5], 0L);
                // In seconds, multiply for ms
                store.transferTime = (long) (Parsing.parseDoubleOrDefault(split[6], 0d) * 1000);
                store.partitionList = Collections
                        .unmodifiableList(partitionMap.getOrDefault(split[0], Collections.emptyList()).stream()
                                .sorted(Comparator.comparing(HWPartition::getName)).collect(Collectors.toList()));
                store.timeStamp = now;
                diskList.add(store);
            }
        }
        return diskList;
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
        List<String> output = Executor.runNative("iostat -Ix " + getName());
        long now = System.currentTimeMillis();
        boolean diskFound = false;
        for (String line : output) {
            String[] split = Pattern.SPACES_PATTERN.split(line);
            if (split.length < 7 || !split[0].equals(getName())) {
                continue;
            }
            diskFound = true;
            this.reads = (long) Parsing.parseDoubleOrDefault(split[1], 0d);
            this.writes = (long) Parsing.parseDoubleOrDefault(split[2], 0d);
            // In KB
            this.readBytes = (long) (Parsing.parseDoubleOrDefault(split[3], 0d) * 1024);
            this.writeBytes = (long) (Parsing.parseDoubleOrDefault(split[4], 0d) * 1024);
            // # transactions
            this.currentQueueLength = Parsing.parseLongOrDefault(split[5], 0L);
            // In seconds, multiply for ms
            this.transferTime = (long) (Parsing.parseDoubleOrDefault(split[6], 0d) * 1000);
            this.timeStamp = now;
        }
        return diskFound;
    }

}
