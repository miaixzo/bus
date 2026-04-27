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
package org.miaixz.bus.cortex.setting.item;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * Utility methods for converting string-based {@code setting.item.revision} numbers at storage boundaries.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ItemRevisionNumbers {

    /**
     * Prevents utility class instantiation.
     */
    private ItemRevisionNumbers() {

    }

    /**
     * Returns the next monotonic revision text.
     *
     * @param current current revision text
     * @return next revision text
     */
    public static String next(String current) {
        Long currentValue = toLongOrNull(current);
        return Long.toString(currentValue == null ? 1L : currentValue + 1L);
    }

    /**
     * Converts one revision text to a nullable numeric value.
     *
     * @param revision revision text
     * @return numeric revision, or {@code null} when absent
     */
    public static Long toLongOrNull(String revision) {
        if (StringKit.isEmpty(revision)) {
            return null;
        }
        try {
            return Long.valueOf(revision.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Setting item revision must be numeric: " + revision, e);
        }
    }

    /**
     * Converts one revision text to a required numeric value.
     *
     * @param revision revision text
     * @return numeric revision
     */
    public static long toLong(String revision) {
        Long value = toLongOrNull(revision);
        if (value == null) {
            throw new IllegalArgumentException("Setting item revision is required");
        }
        return value;
    }

    /**
     * Converts one numeric revision to revision text.
     *
     * @param revision numeric revision
     * @return revision text, or {@code null} when absent
     */
    public static String toString(Long revision) {
        return revision == null ? null : Long.toString(revision);
    }

    /**
     * Converts one revision text to a sortable numeric key.
     *
     * @param revision revision text
     * @return sortable numeric key
     */
    public static long sortKey(String revision) {
        Long value = toLongOrNull(revision);
        return value == null ? 0L : value;
    }

}
