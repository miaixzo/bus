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
import java.util.OptionalLong;

/**
 * Immutable cumulative snapshot for the current process.
 *
 * @param userTimeMillis      cumulative user CPU time
 * @param kernelTimeMillis    cumulative kernel CPU time
 * @param uptimeMillis        process uptime
 * @param residentBytes       resident memory size
 * @param virtualBytes        virtual memory size
 * @param readBytes           cumulative bytes read
 * @param writeBytes          cumulative bytes written
 * @param threadCount         live process threads
 * @param openFiles           open file descriptors when available
 * @param handles             open handles when available
 * @param minorFaults         minor page faults when available
 * @param majorFaults         major page faults when available
 * @param voluntarySwitches   voluntary context switches when available
 * @param involuntarySwitches involuntary context switches when available
 * @author Kimi Liu
 */
public record ProcessSnapshot(long userTimeMillis, long kernelTimeMillis, long uptimeMillis, long residentBytes,
        long virtualBytes, long readBytes, long writeBytes, long threadCount, OptionalLong openFiles,
        OptionalLong handles, OptionalLong minorFaults, OptionalLong majorFaults, OptionalLong voluntarySwitches,
        OptionalLong involuntarySwitches) {

    /**
     * Validates all required and optional cumulative values.
     */
    public ProcessSnapshot {
        long[] values = { userTimeMillis, kernelTimeMillis, uptimeMillis, residentBytes, virtualBytes, readBytes,
                writeBytes, threadCount };
        for (long value : values) {
            if (value < 0) {
                throw new IllegalArgumentException("Process values must not be negative");
            }
        }
        openFiles = checked(openFiles, "Open-file count");
        handles = checked(handles, "Handle count");
        minorFaults = checked(minorFaults, "Minor fault count");
        majorFaults = checked(majorFaults, "Major fault count");
        voluntarySwitches = checked(voluntarySwitches, "Voluntary context-switch count");
        involuntarySwitches = checked(involuntarySwitches, "Involuntary context-switch count");
    }

    /**
     * Validates one optional cumulative process value.
     *
     * @param value optional value
     * @param label diagnostic label
     * @return the validated value
     */
    private static OptionalLong checked(OptionalLong value, String label) {
        Objects.requireNonNull(value, label + " must not be null");
        if (value.isPresent() && value.getAsLong() < 0) {
            throw new IllegalArgumentException(label + " must not be negative");
        }
        return value;
    }

}
