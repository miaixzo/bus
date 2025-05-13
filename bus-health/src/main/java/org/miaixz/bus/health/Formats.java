/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health;

import java.math.BigInteger;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;

/**
 * 用于格式化单位或在数字类型之间转换的工具类。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Formats {

    /**
     * 将字节格式化为使用 IEC 标准的四舍五入字符串表示（匹配 Mac/Linux）。对于硬盘容量，使用 {@link #formatBytesDecimal(long)}。对于 Windows 的 KB、MB 和 GB
     * 显示，使用 JEDEC 单位时， 编辑返回的字符串以移除 'i' 以显示（不正确的）JEDEC 单位。
     *
     * @param bytes 字节数
     * @return 字节大小的四舍五入字符串表示
     */
    public static String formatBytes(long bytes) {
        if (bytes == 1L) { // 单个字节
            return String.format(Locale.ROOT, "%d byte", bytes);
        } else if (bytes < Normal.KIBI) { // 小于 1024 字节
            return String.format(Locale.ROOT, "%d bytes", bytes);
        } else if (bytes < Normal.MEBI) { // KiB
            return formatUnits(bytes, Normal.KIBI, "KiB");
        } else if (bytes < Normal.GIBI) { // MiB
            return formatUnits(bytes, Normal.MEBI, "MiB");
        } else if (bytes < Normal.TEBI) { // GiB
            return formatUnits(bytes, Normal.GIBI, "GiB");
        } else if (bytes < Normal.PEBI) { // TiB
            return formatUnits(bytes, Normal.TEBI, "TiB");
        } else if (bytes < Normal.EXBI) { // PiB
            return formatUnits(bytes, Normal.PEBI, "PiB");
        } else { // EiB
            return formatUnits(bytes, Normal.EXBI, "EiB");
        }
    }

    /**
     * 根据前缀将单位格式化为精确整数或小数，附加适当的单位。
     *
     * @param value  要格式化的值
     * @param prefix 单位乘数的除数
     * @param unit   表示单位的字符串
     * @return 格式化后的字符串值
     */
    private static String formatUnits(long value, long prefix, String unit) {
        if (value % prefix == 0) {
            return String.format(Locale.ROOT, "%d %s", value / prefix, unit);
        }
        return String.format(Locale.ROOT, "%.1f %s", (double) value / prefix, unit);
    }

    /**
     * 将字节格式化为使用十进制 SI 单位的四舍五入字符串表示。硬盘制造商用于容量表示。 大多数其他存储应使用 {@link #formatBytes(long)}。
     *
     * @param bytes 字节数
     * @return 字节大小的四舍五入字符串表示
     */
    public static String formatBytesDecimal(long bytes) {
        if (bytes == 1L) { // 单个字节
            return String.format(Locale.ROOT, "%d byte", bytes);
        } else if (bytes < Normal.KILO) { // 小于 1000 字节
            return String.format(Locale.ROOT, "%d bytes", bytes);
        } else {
            return formatValue(bytes, "B");
        }
    }

    /**
     * 将赫兹格式化为四舍五入字符串表示。
     *
     * @param hertz 赫兹值
     * @return 赫兹大小的四舍五入字符串表示
     */
    public static String formatHertz(long hertz) {
        return formatValue(hertz, "Hz");
    }

    /**
     * 将任意单位格式化为四舍五入字符串表示。
     *
     * @param value 要格式化的值
     * @param unit  要附加公制前缀的单位
     * @return 带有公制前缀的四舍五入字符串表示
     */
    public static String formatValue(long value, String unit) {
        if (value < Normal.KILO) {
            return String.format(Locale.ROOT, "%d %s", value, unit).trim();
        } else if (value < Normal.MEGA) { // K
            return formatUnits(value, Normal.KILO, "K" + unit);
        } else if (value < Normal.GIGA) { // M
            return formatUnits(value, Normal.MEGA, "M" + unit);
        } else if (value < Normal.TERA) { // G
            return formatUnits(value, Normal.GIGA, "G" + unit);
        } else if (value < Normal.PETA) { // T
            return formatUnits(value, Normal.TERA, "T" + unit);
        } else if (value < Normal.EXA) { // P
            return formatUnits(value, Normal.PETA, "P" + unit);
        } else { // E
            return formatUnits(value, Normal.EXA, "E" + unit);
        }
    }

    /**
     * 将以秒为单位经过的时间格式化为天、小时:分钟:秒。
     *
     * @param secs 经过的秒数
     * @return 经过时间的字符串表示
     */
    public static String formatElapsedSecs(long secs) {
        long eTime = secs;
        final long days = TimeUnit.SECONDS.toDays(eTime);
        eTime -= TimeUnit.DAYS.toSeconds(days);
        final long hr = TimeUnit.SECONDS.toHours(eTime);
        eTime -= TimeUnit.HOURS.toSeconds(hr);
        final long min = TimeUnit.SECONDS.toMinutes(eTime);
        eTime -= TimeUnit.MINUTES.toSeconds(min);
        final long sec = eTime;
        return String.format(Locale.ROOT, "%d days, %02d:%02d:%02d", days, hr, min, sec);
    }

    /**
     * 将无符号整数转换为有符号长整数。
     *
     * @param x 表示无符号整数的有符号整数
     * @return x 的无符号长整数值
     */
    public static long getUnsignedInt(int x) {
        return x & 0x0000_0000_ffff_ffffL;
    }

    /**
     * 将 32 位值表示为无符号整数。
     * <p>
     * 这是 Java 8 的 Integer.toUnsignedString 的 Java 7 实现。
     *
     * @param i 32 位值
     * @return 无符号整数的字符串表示
     */
    public static String toUnsignedString(int i) {
        if (i >= 0) {
            return Integer.toString(i);
        }
        return Long.toString(getUnsignedInt(i));
    }

    /**
     * 将 64 位值表示为无符号长整数。
     * <p>
     * 这是 Java 8 的 Long.toUnsignedString 的 Java 7 实现。
     *
     * @param l 64 位值
     * @return 无符号长整数的字符串表示
     */
    public static String toUnsignedString(long l) {
        if (l >= 0) {
            return Long.toString(l);
        }
        return BigInteger.valueOf(l).add(Normal.TWOS_COMPLEMENT_REF).toString();
    }

    /**
     * 将整数错误代码转换为其十六进制表示。
     *
     * @param errorCode 错误代码
     * @return 表示错误代码的字符串，格式为 0x....
     */
    public static String formatError(int errorCode) {
        return String.format(Locale.ROOT, Normal.HEX_ERROR, errorCode);
    }

    /**
     * 将浮点数四舍五入到最接近的整数。
     *
     * @param x 浮点数
     * @return 四舍五入后的整数
     */
    public static int roundToInt(double x) {
        return (int) Math.round(x);
    }

}