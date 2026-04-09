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
package org.miaixz.bus.core.xyz;

import java.time.ZoneId;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.lang.Keys;

/**
 * Utility class for {@link ZoneId} and {@link TimeZone}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ZoneKit {

    /**
     * Converts a {@link TimeZone} to a {@link ZoneId}.
     *
     * @param timeZone The `TimeZone`. If `null`, returns the system default `ZoneId`.
     * @return The corresponding `ZoneId`.
     */
    public static ZoneId toZoneId(final TimeZone timeZone) {
        if (null == timeZone) {
            return ZoneId.systemDefault();
        }
        return timeZone.toZoneId();
    }

    /**
     * Gets an available `TimeZone` ID for a given raw offset.
     *
     * @param rawOffset The offset from UTC.
     * @param timeUnit  The unit of the offset.
     * @return The `TimeZone` ID, or `null` if not found.
     */
    public static String getAvailableID(final int rawOffset, final TimeUnit timeUnit) {
        final String[] availableIDs = TimeZone
                .getAvailableIDs((int) ObjectKit.defaultIfNull(timeUnit, TimeUnit.MILLISECONDS).toMillis(rawOffset));
        return ArrayKit.isEmpty(availableIDs) ? null : availableIDs[0];
    }

    /**
     * Gets an available `TimeZone` for a given raw offset.
     *
     * @param rawOffset The offset from UTC.
     * @param timeUnit  The unit of the offset.
     * @return The `TimeZone`, or `null` if not found.
     */
    public static TimeZone getTimeZoneByOffset(final int rawOffset, final TimeUnit timeUnit) {
        final String id = getAvailableID(rawOffset, timeUnit);
        return null == id ? null : TimeZone.getTimeZone(id);
    }

    /**
     * Converts a {@link ZoneId} to a {@link TimeZone}; returns the system default when {@code zoneId} is {@code null}.
     *
     * @param zoneId the {@link ZoneId}; when {@code null}, the system default time zone is returned
     * @return {@link TimeZone}
     */
    public static TimeZone getTimeZone(final ZoneId zoneId) {
        if (null == zoneId) {
            return TimeZone.getDefault();
        }

        return TimeZone.getTimeZone(zoneId);
    }

    /**
     * Resolves the identifier through {@link ZoneId#SHORT_IDS} first, then delegates to
     * {@link TimeZone#getTimeZone(String)}.
     * <p>
     * In Java 25, calling {@link TimeZone#getTimeZone(String)} with an identifier from {@link ZoneId#SHORT_IDS} may
     * write a message similar to the following to {@link System#err}:
     * </p>
     *
     * <pre>
     * WARNING: Use of the three-letter time zone ID "the-short-id" is deprecated and it will be removed in a future release
     * </pre>
     * <p>
     * You can disable the mapping from {@link ZoneId#SHORT_IDS} by setting the system property
     * {@code "TimeZone.mapShortIDs=false"}.
     * </p>
     *
     * @param id same as {@link TimeZone#getTimeZone(String)}
     * @return same as {@link TimeZone#getTimeZone(String)}
     */
    public static TimeZone getTimeZone(final String id) {
        return TimeZone.getTimeZone(
                Keys.IS_AT_LEAST_JDK25 && Keys.getBoolean("TimeZone.mapShortIDs", true)
                        ? ZoneId.SHORT_IDS.getOrDefault(id, id)
                        : id);
    }

}
