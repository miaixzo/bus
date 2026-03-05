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
package org.miaixz.bus.health.builtin.software;

import java.util.List;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;

/**
 * The File System is a logical arrangement, usually in a hierarchial tree, where files are placed for storage and
 * retrieval. It may consist of one or more file stores.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public interface FileSystem {

    /**
     * Get file stores on this machine Instantiates a list of {@link OSFileStore} objects, representing a storage pool,
     * device, partition, volume, concrete file system or other implementation specific means of file storage.
     *
     * @return A list of {@link OSFileStore} objects or an empty array if none are present.
     */
    List<OSFileStore> getFileStores();

    /**
     * Get file stores on this machine Instantiates a list of {@link OSFileStore} objects, representing a storage pool,
     * device, partition, volume, concrete file system or other implementation specific means of file storage.
     *
     * @param localOnly If true, filters the list to only local file stores. On Windows, also excluded CD-ROM drives.
     * @return A list of {@link OSFileStore} objects or an empty array if none are present.
     */
    List<OSFileStore> getFileStores(boolean localOnly);

    /**
     * The current number of open file descriptors. A file descriptor is an abstract handle used to access I/O resources
     * such as files and network connections. On UNIX-based systems there is a system-wide limit on the number of open
     * file descriptors. On Windows systems, this method returns the total number of handles held by Processes. While
     * Windows handles are conceptually similar to file descriptors, they may also refer to a number of non-I/O related
     * objects.
     *
     * @return The number of open file descriptors if available, 0 otherwise.
     */
    long getOpenFileDescriptors();

    /**
     * The maximum number of open file descriptors. A file descriptor is an abstract handle used to access I/O resources
     * such as files and network connections. On UNIX-based systems there is a system-wide limit on the number of open
     * file descriptors. On Windows systems, this method returns the theoretical max number of handles (2^24-2^15 on
     * 32-bit, 2^24-2^16 on 64-bit). There may be a lower per-process limit. While Windows handles are conceptually
     * similar to file descriptors, they may also refer to a number of non-I/O related objects.
     *
     * @return The maximum number of file descriptors if available, 0 otherwise.
     */
    long getMaxFileDescriptors();

    /**
     * The maximum number of open file descriptors per process. This returns the upper limit which applies to each
     * process. The actual limit of a process may be lower if configured.
     *
     * @return The maximum number of file descriptors of each process if available, 0 otherwise.
     */
    long getMaxFileDescriptorsPerProcess();

}
