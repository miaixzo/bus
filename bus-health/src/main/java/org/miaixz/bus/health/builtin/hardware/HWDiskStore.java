/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.builtin.hardware;

import java.util.List;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;

/**
 * A storage mechanism where data are recorded by various electronic, magnetic, optical, or mechanical changes to a
 * surface layer of one or more rotating disks or or flash storage such as a removable or solid state drive. In
 * constrast to a File System, defining the way an Operating system uses the storage, the Disk Store represents the
 * hardware which a FileSystem uses for its File Stores. Thread safe for the designed use of retrieving the most recent
 * data. Users should be aware that the {@link #updateAttributes()} method may update attributes, including the time
 * stamp, and should externally synchronize such usage to ensure consistent calculations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public interface HWDiskStore {

    /**
     * The disk name
     *
     * @return the name
     */
    String getName();

    /**
     * The disk model
     *
     * @return the model
     */
    String getModel();

    /**
     * The disk serial number, if available.
     *
     * @return the serial number
     */
    String getSerial();

    /**
     * The size of the disk
     *
     * @return the disk size, in bytes
     */
    long getSize();

    /**
     * The number of reads from the disk
     *
     * @return the reads
     */
    long getReads();

    /**
     * The number of bytes read from the disk
     *
     * @return the bytes read
     */
    long getReadBytes();

    /**
     * The number of writes to the disk
     *
     * @return the writes
     */
    long getWrites();

    /**
     * The number of bytes written to the disk
     *
     * @return the bytes written
     */
    long getWriteBytes();

    /**
     * The length of the disk queue (#I/O's in progress). Includes I/O requests that have been issued to the device
     * driver but have not yet completed. Not supported on macOS.
     *
     * @return the current disk queue length
     */
    long getCurrentQueueLength();

    /**
     * The time spent reading or writing, in milliseconds.
     *
     * @return the transfer time
     */
    long getTransferTime();

    /**
     * The partitions on this disk.
     *
     * @return an {@code UnmodifiableList} of the partitions on this drive.
     */
    List<HWPartition> getPartitions();

    /**
     * The time this disk's statistics were updated.
     *
     * @return the timeStamp, in milliseconds since the epoch.
     */
    long getTimeStamp();

    /**
     * Make a best effort to update all the statistics about the drive without needing to recreate the drive list. This
     * method provides for more frequent periodic updates of individual drive statistics but may be less efficient to
     * use if updating all drives. It will not detect if a removable drive has been removed and replaced by a different
     * drive in between method calls.
     *
     * @return True if the update was (probably) successful, false if the disk was not found
     */
    boolean updateAttributes();

}
