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
package org.miaixz.bus.health.builtin.hardware;

import java.util.List;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;

/**
 * The GlobalMemory class tracks information about the use of a computer's physical memory (RAM) as well as any
 * available virtual memory.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public interface GlobalMemory {

    /**
     * The amount of actual physical memory, in bytes.
     *
     * @return Total number of bytes.
     */
    long getTotal();

    /**
     * The amount of physical memory currently available, in bytes.
     *
     * @return Available number of bytes.
     */
    long getAvailable();

    /**
     * The number of bytes in a memory page
     *
     * @return Page size in bytes.
     */
    long getPageSize();

    /**
     * Virtual memory, such as a swap file.
     *
     * @return A VirtualMemory object.
     */
    VirtualMemory getVirtualMemory();

    /**
     * Physical memory, such as banks of memory.
     * <p>
     * On Linux, requires elevated permissions. On FreeBSD and Solaris, requires installation of dmidecode.
     *
     * @return A list of PhysicalMemory objects.
     */
    List<PhysicalMemory> getPhysicalMemory();

}
