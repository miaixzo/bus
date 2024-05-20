/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.core.io.unit;

import org.miaixz.bus.core.toolkit.StringKit;

/**
 * 数据单位封装
 * 此类来自于：Spring-framework
 *
 * <pre>
 *     BYTES      1B      2^0     1
 *     KILOBYTES  1KB     2^10    1,024
 *     MEGABYTES  1MB     2^20    1,048,576
 *     GIGABYTES  1GB     2^30    1,073,741,824
 *     TERABYTES  1TB     2^40    1,099,511,627,776
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum DataUnit {

    /**
     * Bytes, 后缀表示为： {@code B}.
     */
    BYTES("B", DataSize.ofBytes(1)),

    /**
     * Kilobytes, 后缀表示为： {@code KB}.
     */
    KILOBYTES("KB", DataSize.ofKilobytes(1)),

    /**
     * Megabytes, 后缀表示为： {@code MB}.
     */
    MEGABYTES("MB", DataSize.ofMegabytes(1)),

    /**
     * Gigabytes, 后缀表示为： {@code GB}.
     */
    GIGABYTES("GB", DataSize.ofGigabytes(1)),

    /**
     * Terabytes, 后缀表示为： {@code TB}.
     */
    TERABYTES("TB", DataSize.ofTerabytes(1));

    private final String suffix;

    private final DataSize size;


    DataUnit(final String suffix, final DataSize size) {
        this.suffix = suffix;
        this.size = size;
    }

    /**
     * 通过后缀返回对应的 DataUnit
     *
     * @param suffix 单位后缀
     * @return 匹配到的{@link DataUnit}
     * @throws IllegalArgumentException 后缀无法识别报错
     */
    public static DataUnit fromSuffix(final String suffix) {
        for (final DataUnit candidate : values()) {
            // 支持类似于 3MB，3M，3m等写法
            if (StringKit.startWithIgnoreCase(candidate.suffix, suffix)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("Unknown data unit suffix '" + suffix + "'");
    }

    DataSize size() {
        return this.size;
    }

}
