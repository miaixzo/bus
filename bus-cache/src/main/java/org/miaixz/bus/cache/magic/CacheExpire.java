/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.cache.magic;

/**
 * 缓存过期时间常量接口
 * <p>
 * 定义了一系列常用的缓存过期时间常量，以毫秒为单位。 提供了从-1（不缓存）到一周（7天）的各种时间间隔常量。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface CacheExpire {

    /**
     * 不缓存，值为-1
     */
    int NO = -1;

    /**
     * 永不过期，值为0
     */
    int FOREVER = 0;

    /**
     * 1秒，值为1000毫秒
     */
    int ONE_SEC = 1000;

    /**
     * 5秒，值为4000毫秒
     */
    int FIVE_SEC = 4 * ONE_SEC;

    /**
     * 10秒，值为8000毫秒
     */
    int TEN_SEC = 2 * FIVE_SEC;

    /**
     * 1分钟，值为60000毫秒
     */
    int ONE_MIN = 6 * TEN_SEC;

    /**
     * 5分钟，值为300000毫秒
     */
    int FIVE_MIN = 5 * ONE_MIN;

    /**
     * 10分钟，值为600000毫秒
     */
    int TEN_MIN = 2 * FIVE_MIN;

    /**
     * 半小时，值为18000000毫秒
     */
    int HALF_HOUR = 30 * TEN_MIN;

    /**
     * 1小时，值为36000000毫秒
     */
    int ONE_HOUR = 2 * HALF_HOUR;

    /**
     * 2小时，值为72000000毫秒
     */
    int TWO_HOUR = 2 * ONE_HOUR;

    /**
     * 6小时，值为216000000毫秒
     */
    int SIX_HOUR = 3 * TWO_HOUR;

    /**
     * 12小时，值为432000000毫秒
     */
    int TWELVE_HOUR = 2 * SIX_HOUR;

    /**
     * 1天，值为864000000毫秒
     */
    int ONE_DAY = 2 * TWELVE_HOUR;

    /**
     * 2天，值为1728000000毫秒
     */
    int TWO_DAY = 2 * ONE_DAY;

    /**
     * 1周，值为6048000000毫秒
     */
    int ONE_WEEK = 7 * ONE_DAY;

}
