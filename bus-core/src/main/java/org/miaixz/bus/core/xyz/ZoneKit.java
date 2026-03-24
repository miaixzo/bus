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
     * {@link ZoneId}转换为{@link TimeZone}，{@code null}则返回系统默认值
     *
     * @param zoneId {@link ZoneId}，{@code null}则返回系统默认值
     * @return {@link TimeZone}
     */
    public static TimeZone getTimeZone(final ZoneId zoneId) {
        if (null == zoneId) {
            return TimeZone.getDefault();
        }

        return TimeZone.getTimeZone(zoneId);
    }

    /**
     * 在{@link ZoneId#SHORT_IDS}中映射ID后，委托给{@link TimeZone#getTimeZone(String)}。
     * <p>
     * 在Java 25中，使用{@link ZoneId#SHORT_IDS}中的ID调用{@link TimeZone#getTimeZone(String)}会在{@link System#err}中写入如下形式的消息：
     * </p>
     *
     * <pre>
     * WARNING: Use of the three-letter time zone ID "the-short-id" is deprecated and it will be removed in a future release
     * </pre>
     * <p>
     * 您可以通过设置系统属性{@code "TimeZone.mapShortIDs=false"}来禁用从{@link ZoneId#SHORT_IDS}的映射。
     * </p>
     *
     * @param id 与{@link TimeZone#getTimeZone(String)}相同。
     * @return 与{@link TimeZone#getTimeZone(String)}相同。
     */
    public static TimeZone getTimeZone(final String id) {
        return TimeZone.getTimeZone(
                Keys.IS_AT_LEAST_JDK25 && Keys.getBoolean("TimeZone.mapShortIDs", true)
                        ? ZoneId.SHORT_IDS.getOrDefault(id, id)
                        : id);
    }

}
