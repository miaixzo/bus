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

import java.util.Objects;

/**
 * Platform-declared availability of optional host metric fields.
 *
 * @param pagingFaults        supported paging-fault shape
 * @param contextSwitchTypes  whether voluntary and involuntary counts are distinct
 * @param unixFileDescriptors whether Unix file-descriptor counts are available
 * @param windowsHandles      whether Windows handle counts are available
 * @param networkConnections  whether connection inventory is available
 * @param container           whether container inventory is available
 * @author Kimi Liu
 */
public record HostMetricCapabilities(PagingFaultShape pagingFaults, boolean contextSwitchTypes,
        boolean unixFileDescriptors, boolean windowsHandles, boolean networkConnections, boolean container) {

    /**
     * Validates the capability set.
     */
    public HostMetricCapabilities {
        pagingFaults = Objects.requireNonNull(pagingFaults, "Paging fault shape must not be null");
    }

    /**
     * Shape of the paging-fault values exposed by an operating system.
     */
    public enum PagingFaultShape {
        /**
         * Major and minor faults are independently available.
         */
        SPLIT,
        /**
         * Only an unclassified total is available.
         */
        TOTAL,
        /**
         * Paging-fault values are unavailable.
         */
        NONE
    }

}
