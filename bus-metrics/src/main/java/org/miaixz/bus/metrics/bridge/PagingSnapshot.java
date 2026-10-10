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
 * Immutable paging-space usage and cumulative paging-operation snapshot.
 *
 * @param totalBytes total paging space in bytes
 * @param usedBytes  used paging space in bytes
 * @param freeBytes  free paging space in bytes
 * @param pageIns    cumulative page-in operations when available
 * @param pageOuts   cumulative page-out operations when available
 * @author Kimi Liu
 */
public record PagingSnapshot(long totalBytes, long usedBytes, long freeBytes, OptionalLong pageIns,
        OptionalLong pageOuts) {

    /**
     * Validates the paging invariant and optional values.
     */
    public PagingSnapshot {
        pageIns = checked(pageIns, "Page-in count");
        pageOuts = checked(pageOuts, "Page-out count");
        if (totalBytes < 0 || usedBytes < 0 || freeBytes < 0 || saturatedAdd(usedBytes, freeBytes) != totalBytes) {
            throw new IllegalArgumentException("Paging values must be non-negative and used + free must equal total");
        }
    }

    /**
     * Validates one optional cumulative paging value.
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
