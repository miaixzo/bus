/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.io.unit;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 数据大小，可以将类似于'12MB'表示转换为bytes长度的数字 此类来自于：Spring-framework
 *
 * <pre>
 *     byte        1B     1
 *     kilobyte    1KB    1,024
 *     megabyte    1MB    1,048,576
 *     gigabyte    1GB    1,073,741,824
 *     terabyte    1TB    1,099,511,627,776
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class DataSize implements Comparable<DataSize> {

    /**
     * The pattern for parsing.
     */
    private static final Pattern PATTERN = Pattern.compile("^([+-]?\\d+(\\.\\d+)?)([a-zA-Z]{0,2})$");

    /**
     * Bytes per Kilobyte(KB).
     */
    private static final long BYTES_PER_KB = 1024;

    /**
     * Bytes per Megabyte(MB).
     */
    private static final long BYTES_PER_MB = BYTES_PER_KB * 1024;

    /**
     * Bytes per Gigabyte(GB).
     */
    private static final long BYTES_PER_GB = BYTES_PER_MB * 1024;

    /**
     * Bytes per Terabyte(TB).
     */
    private static final long BYTES_PER_TB = BYTES_PER_GB * 1024;

    /**
     * bytes长度
     */
    private final long bytes;

    /**
     * 构造
     *
     * @param bytes 长度
     */
    private DataSize(final long bytes) {
        this.bytes = bytes;
    }

    /**
     * 获得对应bytes的DataSize
     *
     * @param bytes bytes大小，可正可负
     * @return this
     */
    public static DataSize ofBytes(final long bytes) {
        return new DataSize(bytes);
    }

    /**
     * 获得对应kilobytes的DataSize
     *
     * @param kilobytes kilobytes大小，可正可负
     * @return a DataSize
     */
    public static DataSize ofKilobytes(final long kilobytes) {
        return new DataSize(Math.multiplyExact(kilobytes, BYTES_PER_KB));
    }

    /**
     * 获得对应megabytes的DataSize
     *
     * @param megabytes megabytes大小，可正可负
     * @return a DataSize
     */
    public static DataSize ofMegabytes(final long megabytes) {
        return new DataSize(Math.multiplyExact(megabytes, BYTES_PER_MB));
    }

    /**
     * 获得对应gigabytes的DataSize
     *
     * @param gigabytes gigabytes大小，可正可负
     * @return a DataSize
     */
    public static DataSize ofGigabytes(final long gigabytes) {
        return new DataSize(Math.multiplyExact(gigabytes, BYTES_PER_GB));
    }

    /**
     * 获得对应terabytes的DataSize
     *
     * @param terabytes terabytes大小，可正可负
     * @return a DataSize
     */
    public static DataSize ofTerabytes(final long terabytes) {
        return new DataSize(Math.multiplyExact(terabytes, BYTES_PER_TB));
    }

    /**
     * 获得指定{@link DataUnit}对应的DataSize
     *
     * @param amount 大小
     * @param unit   数据大小单位，null表示默认的BYTES
     * @return DataSize
     */
    public static DataSize of(final long amount, DataUnit unit) {
        if (null == unit) {
            unit = DataUnit.BYTES;
        }
        return new DataSize(Math.multiplyExact(amount, unit.size().toBytes()));
    }

    /**
     * 获得指定{@link DataUnit}对应的DataSize
     *
     * @param amount 大小
     * @param unit   数据大小单位，null表示默认的BYTES
     * @return DataSize
     */
    public static DataSize of(final BigDecimal amount, DataUnit unit) {
        if (null == unit) {
            unit = DataUnit.BYTES;
        }
        return new DataSize(amount.multiply(new BigDecimal(unit.size().toBytes())).longValue());
    }

    /**
     * 获取指定数据大小文本对应的DataSize对象，如果无单位指定，默认获取{@link DataUnit#BYTES} 例如：
     * 
     * <pre>
     * "12KB" -- parses as "12 kilobytes"
     * "5MB"  -- parses as "5 megabytes"
     * "20"   -- parses as "20 bytes"
     * </pre>
     *
     * @param text the text to parse
     * @return the parsed DataSize
     * @see #parse(CharSequence, DataUnit)
     */
    public static long parse(final String text) {
        return parse(text, null).toBytes();
    }

    /**
     * 获取指定数据大小文本对应的DataSize对象，如果无单位指定，默认获取{@link DataUnit#BYTES} 例如：
     * 
     * <pre>
     * "12KB" -- parses as "12 kilobytes"
     * "5MB"  -- parses as "5 megabytes"
     * "20"   -- parses as "20 bytes"
     * </pre>
     *
     * @param text the text to parse
     * @return the parsed DataSize
     * @see #parse(CharSequence, DataUnit)
     */
    public static DataSize parse(final CharSequence text) {
        return parse(text, null);
    }

    /**
     * Obtain a DataSize from a text string such as {@code 12MB} using the specified default {@link DataUnit} if no unit
     * is specified. The string starts with a number followed optionally by a unit matching one of the supported
     * {@linkplain DataUnit suffixes}. Examples:
     * 
     * <pre>
     * "12KB" -- parses as "12 kilobytes"
     * "5MB"  -- parses as "5 megabytes"
     * "20"   -- parses as "20 kilobytes" (where the {@code
     * defaultUnit
     * } is {@link DataUnit#KILOBYTES})
     * </pre>
     *
     * @param text        the text to parse
     * @param defaultUnit 默认的数据单位
     * @return the parsed DataSize
     */
    public static DataSize parse(final CharSequence text, final DataUnit defaultUnit) {
        Assert.notNull(text, "Text must not be null");
        try {
            final Matcher matcher = PATTERN.matcher(StringKit.cleanBlank(text));
            Assert.state(matcher.matches(), "Does not match data size pattern");

            final DataUnit unit = determineDataUnit(matcher.group(3), defaultUnit);
            return DataSize.of(new BigDecimal(matcher.group(1)), unit);
        } catch (final Exception ex) {
            throw new IllegalArgumentException("'" + text + "' is not a valid data size", ex);
        }
    }

    /**
     * 决定数据单位，后缀不识别时使用默认单位
     *
     * @param suffix      后缀
     * @param defaultUnit 默认单位
     * @return {@link DataUnit}
     */
    private static DataUnit determineDataUnit(final String suffix, final DataUnit defaultUnit) {
        final DataUnit defaultUnitToUse = (defaultUnit != null ? defaultUnit : DataUnit.BYTES);
        return (StringKit.isNotEmpty(suffix) ? DataUnit.fromSuffix(suffix) : defaultUnitToUse);
    }

    /**
     * 可读的文件大小 参考 <a href=
     * "http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc">http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc</a>
     *
     * @param size Long类型大小
     * @return 大小
     */
    public static String format(final long size) {
        if (size <= 0) {
            return "0";
        }
        final int digitGroups = Math.min(Normal.CAPACITY_NAMES.length - 1, (int) (Math.log10(size) / Math.log10(1024)));
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + Symbol.SPACE
                + Normal.CAPACITY_NAMES[digitGroups];
    }

    /**
     * 是否为负数，不包括0
     *
     * @return 负数返回true，否则false
     */
    public boolean isNegative() {
        return this.bytes < 0;
    }

    /**
     * 返回bytes大小
     *
     * @return bytes大小
     */
    public long toBytes() {
        return this.bytes;
    }

    /**
     * 返回KB大小
     *
     * @return KB大小
     */
    public long toKilobytes() {
        return this.bytes / BYTES_PER_KB;
    }

    /**
     * 返回MB大小
     *
     * @return MB大小
     */
    public long toMegabytes() {
        return this.bytes / BYTES_PER_MB;
    }

    /**
     * 返回GB大小
     *
     * @return GB大小
     */
    public long toGigabytes() {
        return this.bytes / BYTES_PER_GB;
    }

    /**
     * 返回TB大小
     *
     * @return TB大小
     */
    public long toTerabytes() {
        return this.bytes / BYTES_PER_TB;
    }

    @Override
    public int compareTo(final DataSize other) {
        return Long.compare(this.bytes, other.bytes);
    }

    @Override
    public String toString() {
        return String.format("%dB", this.bytes);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final DataSize otherSize = (DataSize) other;
        return (this.bytes == otherSize.bytes);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.bytes);
    }

}
