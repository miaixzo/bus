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
package org.miaixz.bus.cortex.setting.revision;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;

/**
 * Converts string-based {@code setting.revision} numbers at storage boundaries.
 *
 * @author Kimi Liu
 */
public class RevisionNumbers {

    /**
     * Keeps configuration revision-number handling on the static API.
     */
    public RevisionNumbers() {
        // No initialization required.
    }

    /**
     * Increments the parsed revision using signed-long arithmetic, starting at 1 for null or empty input.
     * This method does not detect overflow: Long.MAX_VALUE wraps to Long.MIN_VALUE.
     *
     * @param current signed-long revision text, or null or empty to start at 1
     * @return incremented revision in decimal form
     * @throws IllegalArgumentException if nonempty input, after trimming, is not a signed-long integer
     */
    public static String next(String current) {
        Long currentValue = toLongOrNull(current);
        return Long.toString(currentValue == null ? 1L : currentValue + 1L);
    }

    /**
     * Parses a signed-long revision after trimming. Only Java null or the empty string denotes absence;
     * whitespace-only text is invalid. Signed and negative values are accepted within the long range.
     *
     * @param revision revision text, possibly null or empty
     * @return parsed revision, or null only for null or empty input
     * @throws IllegalArgumentException if nonempty input, after trimming, is not a signed-long integer
     */
    public static Long toLongOrNull(String revision) {
        if (StringKit.isEmpty(revision)) {
            return null;
        }
        try {
            return Long.valueOf(revision.trim());
        } catch (NumberFormatException e) {
            Logger.warn(
                    false,
                    "Cortex",
                    e,
                    "Cortex operation failed: provider={}, recoverable={}, exception={}",
                    "RevisionNumbers",
                    false,
                    e.getClass().getSimpleName());
            throw new IllegalArgumentException("Setting item revision must be numeric: " + revision, e);
        }
    }

    /**
     * Converts one revision text to a required numeric value.
     * @param revision required revision text; surrounding whitespace is trimmed, but whitespace-only input is invalid
     * @return numeric revision
     * @throws IllegalArgumentException if the revision is absent or not a signed-long integer
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
     * @param revision revision text
     * @return parsed signed-long value, with null or empty input mapped to zero
     * @throws IllegalArgumentException if nonempty input, after trimming, is not a signed-long integer
     */
    public static long sortKey(String revision) {
        Long value = toLongOrNull(revision);
        return value == null ? 0L : value;
    }

}
