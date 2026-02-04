/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.xyz;

import java.time.ZoneId;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for {@link ZoneId} and {@link TimeZone}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZoneKit {

    /**
     * The {@link TimeZone} for UTC.
     */
    public static final TimeZone ZONE_UTC = TimeZone.getTimeZone("UTC");
    /**
     * The {@link ZoneId} for UTC.
     */
    public static final ZoneId ZONE_ID_UTC = ZONE_UTC.toZoneId();

    /**
     * Converts a {@link ZoneId} to a {@link TimeZone}.
     *
     * @param zoneId The `ZoneId`. If `null`, returns the system default `TimeZone`.
     * @return The corresponding `TimeZone`.
     */
    public static TimeZone toTimeZone(final ZoneId zoneId) {
        if (null == zoneId) {
            return TimeZone.getDefault();
        }
        return TimeZone.getTimeZone(zoneId);
    }

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

}
