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

/**
 * Immutable physical-memory snapshot.
 *
 * @param totalBytes total physical memory in bytes
 * @param usedBytes  used physical memory in bytes
 * @param freeBytes  available physical memory in bytes
 * @author Kimi Liu
 */
public record MemorySnapshot(long totalBytes, long usedBytes, long freeBytes) {

    /**
     * Validates the memory invariant.
     */
    public MemorySnapshot {
        if (totalBytes < 0 || usedBytes < 0 || freeBytes < 0 || saturatedAdd(usedBytes, freeBytes) != totalBytes) {
            throw new IllegalArgumentException("Memory values must be non-negative and used + free must equal total");
        }
    }

    /**
     * Adds two non-negative values without overflowing.
     *
     * @param left  left operand
     * @param right right operand
     * @return the sum, or {@link Long#MAX_VALUE} when it would overflow
     */
    private static long saturatedAdd(long left, long right) {
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }

}
