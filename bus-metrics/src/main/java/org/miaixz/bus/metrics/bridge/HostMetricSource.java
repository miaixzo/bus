/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
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
package org.miaixz.bus.metrics.bridge;

import java.util.List;
import java.util.Optional;

/**
 * Bus-owned, dependency-neutral source of immutable host metric snapshots.
 *
 * @author Kimi Liu
 */
public interface HostMetricSource {

    /**
     * Returns the capabilities declared by the host metric source.
     *
     * @return platform-declared capabilities
     */
    HostMetricCapabilities capabilities();

    /**
     * Returns the current general operating-system snapshot.
     *
     * @return general operating-system snapshot
     */
    GeneralSnapshot general();

    /**
     * Returns the current CPU snapshot.
     *
     * @return CPU snapshot
     */
    CpuSnapshot cpu();

    /**
     * Returns the current memory snapshot.
     *
     * @return memory snapshot
     */
    MemorySnapshot memory();

    /**
     * Returns the current paging snapshot.
     *
     * @return paging snapshot
     */
    PagingSnapshot paging();

    /**
     * Returns current disk snapshots.
     *
     * @return immutable disk snapshots
     */
    List<DiskSnapshot> disks();

    /**
     * Returns current filesystem snapshots.
     *
     * @return immutable filesystem snapshots
     */
    List<FileSystemSnapshot> fileSystems();

    /**
     * Returns the current network snapshot.
     *
     * @return network snapshot
     */
    NetworkSnapshot network();

    /**
     * Returns the current process snapshot when the process still exists.
     *
     * @return current-process snapshot when the process still exists
     */
    Optional<ProcessSnapshot> currentProcess();

    /**
     * Returns container data when the host runs in a supported container.
     *
     * @return container snapshot when running in a supported container
     */
    Optional<ContainerSnapshot> container();

}
