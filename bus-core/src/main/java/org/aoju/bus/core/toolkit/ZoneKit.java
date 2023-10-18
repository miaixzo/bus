/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.core.toolkit;

import java.time.ZoneId;
import java.util.TimeZone;

/**
 * {@link ZoneId}和{@link TimeZone}相关封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZoneKit {

    /**
     * {@link ZoneId}转换为{@link TimeZone}，{@code null}则返回系统默认值
     *
     * @param zoneId {@link ZoneId}，{@code null}则返回系统默认值
     * @return {@link TimeZone}
     */
    public static TimeZone toTimeZone(ZoneId zoneId) {
        if (null == zoneId) {
            return TimeZone.getDefault();
        }

        return TimeZone.getTimeZone(zoneId);
    }

    /**
     * {@link TimeZone}转换为{@link ZoneId}，{@code null}则返回系统默认值
     *
     * @param timeZone {@link TimeZone}，{@code null}则返回系统默认值
     * @return {@link ZoneId}
     */
    public static ZoneId toZoneId(TimeZone timeZone) {
        if (null == timeZone) {
            return ZoneId.systemDefault();
        }

        return timeZone.toZoneId();
    }

}
