/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
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
