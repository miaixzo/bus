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
package org.miaixz.bus.health.unix.platform.openbsd.hardware;

import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.HWDiskStore;
import org.miaixz.bus.health.builtin.hardware.HWPartition;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHWDiskStore;
import org.miaixz.bus.health.unix.platform.openbsd.OpenBsdSysctlKit;
import org.miaixz.bus.health.unix.platform.openbsd.driver.disk.Disklabel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;

/**
 * OpenBSD hard disk implementation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class OpenBsdHWDiskStore extends AbstractHWDiskStore {

    private final Supplier<List<String>> iostat = Memoizer.memoize(OpenBsdHWDiskStore::querySystatIostat, Memoizer.defaultExpiration());
    private final long currentQueueLength = 0L;
    private long reads = 0L;
    private long readBytes = 0L;
    private long writes = 0L;
    private long writeBytes = 0L;
    private long transferTime = 0L;
    private long timeStamp = 0L;
    private List<HWPartition> partitionList;

    private OpenBsdHWDiskStore(String name, String model, String serial, long size) {
        super(name, model, serial, size);
    }

    /**
     * Gets the disks on this machine.
     *
     * @return a list of {@link HWDiskStore} objects representing the disks
     */
    public static List<HWDiskStore> getDisks() {
        List<HWDiskStore> diskList = new ArrayList<>();
        List<String> dmesg = null; // Lazily fetch in loop if needed

        // Get list of disks from sysctl
        // hw.disknames=sd0:2cf69345d371cd82,cd0:,sd1:
        String[] devices = OpenBsdSysctlKit.sysctl("hw.disknames", Normal.EMPTY).split(Symbol.COMMA);
        OpenBsdHWDiskStore store;
        String diskName;
        for (String device : devices) {
            diskName = device.split(Symbol.COLON)[0];
            // get partitions using disklabel command (requires root)
            Tuple diskdata = Disklabel.getDiskParams(diskName);
            String model = diskdata.get(0);
            long size = diskdata.get(2);
            if (size <= 1) {
                if (dmesg == null) {
                    dmesg = Executor.runNative("dmesg");
                }
                java.util.regex.Pattern diskAt = java.util.regex.Pattern.compile(diskName + " at .*<(.+)>.*");
                java.util.regex.Pattern diskMB = java.util.regex.Pattern
                        .compile(diskName + ":.* (\\d+)MB, (?:(\\d+) bytes\\/sector, )?(?:(\\d+) sectors).*");
                for (String line : dmesg) {
                    Matcher m = diskAt.matcher(line);
                    if (m.matches()) {
                        model = m.group(1);
                    }
                    m = diskMB.matcher(line);
                    if (m.matches()) {
                        // Group 3 is sectors
                        long sectors = Parsing.parseLongOrDefault(m.group(3), 0L);
                        // Group 2 is optional capture of bytes per sector
                        long bytesPerSector = Parsing.parseLongOrDefault(m.group(2), 0L);
                        if (bytesPerSector == 0 && sectors > 0) {
                            // if we don't have bytes per sector guess at it based on total size and number
                            // of sectors
                            // Group 1 is size in MB, which may round
                            size = Parsing.parseLongOrDefault(m.group(1), 0L) << 20;
                            // Estimate bytes per sector. Should be "near" a power of 2
                            bytesPerSector = size / sectors;
                            // Multiply by 1.5 and round down to nearest power of 2:
                            bytesPerSector = Long.highestOneBit(bytesPerSector + bytesPerSector >> 1);
                        }
                        size = bytesPerSector * sectors;
                        break;
                    }
                }
            }
            store = new OpenBsdHWDiskStore(diskName, model, diskdata.get(1), size);
            store.partitionList = diskdata.get(3);
            store.updateAttributes();

            diskList.add(store);
        }
        return diskList;
    }

    private static List<String> querySystatIostat() {
        return Executor.runNative("systat -ab iostat");
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
        /*-
        └─ $ ▶ systat -b iostat
                0 users Load 2.04 4.02 3.96                          thinkpad.local 00:14:35
                DEVICE          READ    WRITE     RTPS    WTPS     SEC            STATS
                sd0           49937M   25774M  1326555 1695370   945.9
                cd0                0        0        0       0     0.0
                sd1          1573888      204       29       0     0.1
                Totals        49939M   25774M  1326585 1695371   946.0
                                                                               126568 total pages
                                                                               126568 dma pages
                                                                                  100 dirty pages
                                                                                   14 delwri bufs
                                                                                    0 busymap bufs
                                                                                 6553 avail kvaslots
                                                                                 6553 kvaslots
                                                                                    0 pending writes
                                                                                   12 pending reads
                                                                                    0 cache hits
                                                                                    0 high flips
                                                                                    0 high flops
                                                                                    0 dma flips
        */
        long now = System.currentTimeMillis();
        boolean diskFound = false;
        for (String line : iostat.get()) {
            String[] split = Pattern.SPACES_PATTERN.split(line);
            if (split.length < 7 && split[0].equals(getName())) {
                diskFound = true;
                this.readBytes = Parsing.parseMultipliedToLongs(split[1]);
                this.writeBytes = Parsing.parseMultipliedToLongs(split[2]);
                this.reads = (long) Parsing.parseDoubleOrDefault(split[3], 0d);
                this.writes = (long) Parsing.parseDoubleOrDefault(split[4], 0d);
                // In seconds, multiply for ms
                this.transferTime = (long) (Parsing.parseDoubleOrDefault(split[5], 0d) * 1000);
                this.timeStamp = now;
            }
        }
        return diskFound;
    }
}
