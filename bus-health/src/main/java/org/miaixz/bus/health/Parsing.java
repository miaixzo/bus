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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Regex;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.logger.Logger;

/**
 * 字符串解析支持
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Parsing {

    /**
     * 默认日志消息模板，用于记录解析失败的情况
     */
    private static final String DEFAULT_LOG_MSG = "{} didn't parse. Returning default. {}";

    /**
     * 用于匹配赫兹值的正则表达式，例如 "2.00MHz"
     */
    private static final java.util.regex.Pattern HERTZ_PATTERN = java.util.regex.Pattern
            .compile("(\\d+(.\\d+)?) ?([kKMGT]?Hz).*");

    /**
     * 用于匹配字节值的正则表达式，例如 "4096 MB"
     */
    private static final java.util.regex.Pattern BYTES_PATTERN = java.util.regex.Pattern
            .compile("(\\d+) ?([kKMGT]?B?).*");

    /**
     * 用于匹配带单位的数字的正则表达式，例如 "53G"
     */
    private static final java.util.regex.Pattern UNITS_PATTERN = java.util.regex.Pattern
            .compile("(\\d+(.\\d+)?)[\\s]?([kKMGT])?");

    /**
     * 用于匹配时间格式 [dd-[hh:[mm:[ss[.sss]]]]] 的正则表达式
     */
    private static final java.util.regex.Pattern DHMS = java.util.regex.Pattern
            .compile("(?:(\\d+)-)?(?:(\\d+):)??(?:(\\d+):)?(\\d+)(?:\\.(\\d+))?");

    /**
     * 用于匹配 UUID 格式的正则表达式
     */
    private static final java.util.regex.Pattern UUID_PATTERN = java.util.regex.Pattern
            .compile(".*([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}).*");

    /**
     * 用于匹配 Windows 设备 ID 的供应商 ID、产品 ID 和序列号的正则表达式
     */
    private static final java.util.regex.Pattern VENDOR_PRODUCT_ID_SERIAL = java.util.regex.Pattern
            .compile(".*(?:VID|VEN)_(\\p{XDigit}{4})&(?:PID|DEV)_(\\p{XDigit}{4})(.*)\\\\(.*)");

    /**
     * 用于匹配 Linux lspci 机器可读格式的正则表达式
     */
    private static final java.util.regex.Pattern LSPCI_MACHINE_READABLE = java.util.regex.Pattern
            .compile("(.+)\\s\\[(.*?)\\]");

    /**
     * 用于匹配 Linux lspci 内存大小的正则表达式
     */
    private static final java.util.regex.Pattern LSPCI_MEMORY_SIZE = java.util.regex.Pattern
            .compile(".+\\s\\[size=(\\d+)([kKMGT])\\]");

    /**
     * PDH 时间戳与 1601 年开始的本地时间之间的差值（毫秒）
     */
    private static final long EPOCH_DIFF = 11_644_473_600_000L;

    /**
     * 当前时区的偏移量（毫秒）
     *
     */
    private static final int TZ_OFFSET = TimeZone.getDefault().getOffset(System.currentTimeMillis());

    /**
     * 十进制幂表，用于快速计算 10 的幂
     */
    private static final long[] POWERS_OF_TEN = { 1L, 10L, 100L, 1_000L, 10_000L, 100_000L, 1_000_000L, 10_000_000L,
            100_000_000L, 1_000_000_000L, 10_000_000_000L, 100_000_000_000L, 1_000_000_000_000L, 10_000_000_000_000L,
            100_000_000_000_000L, 1_000_000_000_000_000L, 10_000_000_000_000_000L, 100_000_000_000_000_000L,
            1_000_000_000_000_000_000L };

    /**
     * WMI 返回的 DateTime 格式化器
     */
    private static final DateTimeFormatter CIM_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SSSSSSZZZZZ",
            Locale.US);

    /**
     * 从字符串解析速度，例如 "2.00 MT/s" 解析为 2000000L。
     *
     * @param speed 传输速度。
     * @return {@link java.lang.Long} MT/s 值。如果无法解析，委托给 {@link #parseHertz(String)}。
     */
    public static long parseSpeed(String speed) {
        if (speed.contains("T/s")) {
            return parseHertz(speed.replace("T/s", "Hz"));
        }
        return parseHertz(speed);
    }

    /**
     * 从字符串解析赫兹值，例如 "2.00MHz" 解析为 2000000L。
     *
     * @param hertz 赫兹大小。
     * @return {@link java.lang.Long} 赫兹值，如果无法解析返回 -1。
     */
    public static long parseHertz(String hertz) {
        Matcher matcher = HERTZ_PATTERN.matcher(hertz.trim());
        if (matcher.find()) {
            // 正则表达式强制 #(.#) 格式，无需检查 NumberFormatException
            Map<String, Long> map = new HashMap<>() {
                {
                    put("Hz", 1L);
                    put("kHz", 1_000L);
                    put("MHz", 1_000_000L);
                    put("GHz", 1_000_000_000L);
                    put("THz", 1_000_000_000_000L);
                    put("PHz", 1_000_000_000_000_000L);
                }
            };
            double value = Double.valueOf(matcher.group(1)) * map.getOrDefault(matcher.group(3), -1L);
            if (value >= 0d) {
                return (long) value;
            }
        }
        return -1L;
    }

    /**
     * 解析以空格分隔的字符串的最后一个元素为整数值。
     *
     * @param s 要解析的字符串
     * @param i 如果无法解析，返回的默认整数
     * @return 解析的值或给定的默认值
     */
    public static int parseLastInt(String s, int i) {
        try {
            String ls = parseLastString(s);
            if (ls.toLowerCase(Locale.ROOT).startsWith("0x")) {
                return Integer.decode(ls);
            } else {
                return Integer.parseInt(ls);
            }
        } catch (NumberFormatException e) {
            Logger.trace(DEFAULT_LOG_MSG, s, e);
            return i;
        }
    }

    /**
     * 解析以空格分隔的字符串的最后一个元素为长整数值。
     *
     * @param s  要解析的字符串
     * @param li 如果无法解析，返回的默认长整数
     * @return 解析的值或给定的默认值
     */
    public static long parseLastLong(String s, long li) {
        try {
            String ls = parseLastString(s);
            if (ls.toLowerCase(Locale.ROOT).startsWith("0x")) {
                return Long.decode(ls);
            } else {
                return Long.parseLong(ls);
            }
        } catch (NumberFormatException e) {
            Logger.trace(DEFAULT_LOG_MSG, s, e);
            return li;
        }
    }

    /**
     * 解析以空格分隔的字符串的最后一个元素为双精度浮点数。
     *
     * @param s 要解析的字符串
     * @param d 如果无法解析，返回的默认双精度浮点数
     * @return 解析的值或给定的默认值
     */
    public static double parseLastDouble(String s, double d) {
        try {
            return Double.parseDouble(parseLastString(s));
        } catch (NumberFormatException e) {
            Logger.trace(DEFAULT_LOG_MSG, s, e);
            return d;
        }
    }

    /**
     * 解析以空格分隔的字符串的最后一个元素为字符串。
     *
     * @param s 要解析的字符串
     * @return 以空格分隔的最后一个元素
     */
    public static String parseLastString(String s) {
        String[] ss = Pattern.SPACES_PATTERN.split(s);
        // 保证至少有一个元素
        return ss[ss.length - 1];
    }

    /**
     * 将人类可读的 ASCII 字符串解析为字节数组，截断或用零填充（如果需要）以使数组具有指定长度。
     *
     * @param text   要解析的字符串
     * @param length 返回数组的长度
     * @return 指定长度的字节数组，包含前 length 个字符转换为字节。如果长度超过提供的字符串长度，将用零填充。
     */
    public static byte[] asciiStringToByteArray(String text, int length) {
        return Arrays.copyOf(text.getBytes(Charset.US_ASCII), length);
    }

    /**
     * 使用大端字节序将长整数值转换为字节数组，截断或用零填充（如果需要）以使数组具有指定长度。
     *
     * @param value     要转换的值
     * @param valueSize 表示值的字节数
     * @param length    返回的字节数
     * @return 指定长度的字节数组，表示长整数的前 valueSize 字节
     */
    public static byte[] longToByteArray(long value, int valueSize, int length) {
        long val = value;
        // 将长整数转换为 8 字节大端表示
        byte[] b = new byte[8];
        for (int i = 7; i >= 0 && val != 0L; i--) {
            b[i] = (byte) val;
            val >>>= 8;
        }
        // 复制最右边的 valueSize 字节
        // 例如，对于整数，我们需要最右边的 4 个字节
        return Arrays.copyOfRange(b, 8 - valueSize, 8 + length - valueSize);
    }

    /**
     * 将字符串转换为整数表示。
     *
     * @param text 人类可读的 ASCII 字符串
     * @param size 要转换为长整数的字符数。不得超过 8。
     * @return 表示字符串的整数，每个字符视为一个字节
     */
    public static long strToLong(String text, int size) {
        return byteArrayToLong(text.getBytes(Charset.US_ASCII), size);
    }

    /**
     * 将字节数组转换为其（长整数）表示，假设大端字节序。
     *
     * @param bytes 要转换的字节数组，不得小于要转换的大小
     * @param size  要转换为长整数的字节数。不得超过 8。
     * @return 表示字节数组的长整数
     */
    public static long byteArrayToLong(byte[] bytes, int size) {
        return byteArrayToLong(bytes, size, true);
    }

    /**
     * 将字节数组转换为其（长整数）表示，使用指定的字节序。
     *
     * @param bytes     要转换的字节数组，不得小于要转换的大小
     * @param size      要转换为长整数的字节数。不得超过 8。
     * @param bigEndian 如果为 true，使用大端字节序；如果为 false，使用小端字节序
     * @return 表示字节数组的长整数
     */
    public static long byteArrayToLong(byte[] bytes, int size, boolean bigEndian) {
        if (size > 8) {
            throw new IllegalArgumentException("不能转换超过 8 个字节。");
        }
        if (size > bytes.length) {
            throw new IllegalArgumentException("大小不能超过数组长度。");
        }
        long total = 0L;
        for (int i = 0; i < size; i++) {
            if (bigEndian) {
                total = total << 8 | bytes[i] & 0xff;
            } else {
                total = total << 8 | bytes[size - i - 1] & 0xff;
            }
        }
        return total;
    }

    /**
     * 将字节数组转换为其浮点表示。
     *
     * @param bytes  要转换的字节数组，不得小于要转换的大小
     * @param size   要转换为浮点数的字节数。不得超过 8。
     * @param fpBits 表示小数部分的位数
     * @return 浮点数，表示字节数组的整数部分，移位 fpBits 位，剩余位用作小数
     */
    public static float byteArrayToFloat(byte[] bytes, int size, int fpBits) {
        return byteArrayToLong(bytes, size) / (float) (1 << fpBits);
    }

    /**
     * 将无符号整数转换为长整数值。假设指定整数值的所有位都是数据位，包括 Java 通常视为符号位的最有效位。 仅当确定整数值表示无符号整数时使用此方法，例如当整数由 JNA 库在保存无符号整数的结构中返回时。
     *
     * @param unsignedValue 要转换的无符号整数值
     * @return 无符号整数值扩展为长整数
     */
    public static long unsignedIntToLong(int unsignedValue) {
        // 使用标准 Java 扩展转换到长整数，执行符号扩展，
        // 然后删除符号位的任何副本，以防止 Java 将其视为负值
        long longValue = unsignedValue;
        return longValue & 0xffff_ffffL;
    }

    /**
     * 通过剥离符号位将无符号长整数转换为有符号长整数值。此方法对大于最大长整数值的长整数值进行“翻转”，但确保结果永不为负。
     *
     * @param unsignedValue 要转换的无符号长整数值
     * @return 有符号长整数值
     */
    public static long unsignedLongToSignedLong(long unsignedValue) {
        return unsignedValue & 0x7fff_ffff_ffff_ffffL;
    }

    /**
     * 将十六进制数字字符串解析为字符串，其中每对十六进制数字表示一个 ASCII 字符。
     *
     * @param hexString 十六进制数字序列
     * @return 如果是有效的十六进制，返回对应的字符串；否则返回原始 hexString
     */
    public static String hexStringToString(String hexString) {
        // 奇数长度的字符串无法解析，直接返回
        if (hexString.length() % 2 > 0) {
            return hexString;
        }
        int charAsInt;
        StringBuilder sb = new StringBuilder();
        try {
            for (int pos = 0; pos < hexString.length(); pos += 2) {
                charAsInt = Integer.parseInt(hexString.substring(pos, pos + 2), 16);
                if (charAsInt < 32 || charAsInt > 127) {
                    return hexString;
                }
                sb.append((char) charAsInt);
            }
        } catch (NumberFormatException e) {
            Logger.trace(DEFAULT_LOG_MSG, hexString, e);
            // 十六进制解析失败，返回原始字符串
            return hexString;
        }
        return sb.toString();
    }

    /**
     * 尝试将字符串解析为整数。如果失败，返回默认值。
     *
     * @param s          要解析的字符串
     * @param defaultInt Ditto
     * @param defaultInt 如果解析失败，返回的默认值
     * @return 解析的整数，如果解析失败，返回默认值
     */
    public static int parseIntOrDefault(String s, int defaultInt) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            Logger.trace(DEFAULT_LOG_MSG, s, e);
            return defaultInt;
        }
    }

    /**
     * 尝试将字符串解析为长整数。如果失败，返回默认值。
     *
     * @param s           要解析的字符串
     * @param defaultLong 如果解析失败，返回的默认长整数
     * @return 解析的长整数，如果解析失败，返回默认值
     */
    public static long parseLongOrDefault(String s, long defaultLong) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            Logger.trace(DEFAULT_LOG_MSG, s, e);
            return defaultLong;
        }
    }

    /**
     * 尝试将字符串解析为“无符号”长整数。如果失败，返回默认值。
     *
     * @param s           要解析的字符串
     * @param defaultLong 如果解析失败，返回的默认值
     * @return 解析的长整数，包含与无符号长整数相同的 64 位（可能产生负值）
     */
    public static long parseUnsignedLongOrDefault(String s, long defaultLong) {
        try {
            return new BigInteger(s).longValue();
        } catch (NumberFormatException e) {
            Logger.trace(DEFAULT_LOG_MSG, s, e);
            return defaultLong;
        }
    }

    /**
     * 尝试将字符串解析为双精度浮点数。如果失败，返回默认值。
     *
     * @param s             要解析的字符串
     * @param defaultDouble 如果解析失败，返回的默认双精度浮点数
     * @return 解析的双精度浮点数，如果解析失败，返回默认值
     */
    public static double parseDoubleOrDefault(String s, double defaultDouble) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            Logger.trace(DEFAULT_LOG_MSG, s, e);
            return defaultDouble;
        }
    }

    /**
     * 尝试将形如 [DD-[hh:]]mm:ss[.ddd] 的字符串解析为毫秒数。如果失败，返回默认值。
     *
     * @param s           要解析的字符串
     * @param defaultLong 如果解析失败，返回的默认值
     * @return 解析的秒数，如果解析失败，返回默认值
     */
    public static long parseDHMSOrDefault(String s, long defaultLong) {
        Matcher m = DHMS.matcher(s);
        if (m.matches()) {
            long milliseconds = 0L;
            if (m.group(1) != null) {
                milliseconds += parseLongOrDefault(m.group(1), 0L) * 86_400_000L;
            }
            if (m.group(2) != null) {
                milliseconds += parseLongOrDefault(m.group(2), 0L) * 3_600_000L;
            }
            if (m.group(3) != null) {
                milliseconds += parseLongOrDefault(m.group(3), 0L) * 60_000L;
            }
            milliseconds += parseLongOrDefault(m.group(4), 0L) * 1000L;
            if (m.group(5) != null) {
                milliseconds += (long) (1000 * parseDoubleOrDefault("0." + m.group(5), 0d));
            }
            return milliseconds;
        }
        return defaultLong;
    }

    /**
     * 尝试解析 UUID。如果失败，返回默认值。
     *
     * @param s          要解析的字符串
     * @param defaultStr 如果解析失败，返回的默认值
     * @return 解析的 UUID，如果解析失败，返回默认值
     */
    public static String parseUuidOrDefault(String s, String defaultStr) {
        Matcher m = UUID_PATTERN.matcher(s.toLowerCase(Locale.ROOT));
        if (m.matches()) {
            return m.group(1);
        }
        return defaultStr;
    }

    /**
     * 解析形如 key = 'value' 的字符串（字符串）。
     *
     * @param line 整个字符串
     * @return 单引号之间的值
     */
    public static String getSingleQuoteStringValue(String line) {
        return getStringBetween(line, '\'');
    }

    /**
     * 解析形如 key = "value" 的字符串（字符串）。
     *
     * @param line 整个字符串
     * @return 双引号之间的值
     */
    public static String getDoubleQuoteStringValue(String line) {
        return getStringBetween(line, '"');
    }

    /**
     * 获取两个相同字符之间的值。示例：
     * <ul>
     * <li>"name = 'James Gosling's Java'" 返回 "James Gosling's Java"</li>
     * <li>"pci.name = 'Realtek AC'97 Audio Device'" 返回 "Realtek AC'97 Audio Device"</li>
     * </ul>
     *
     * @param line 要解析的“键-值”对行
     * @param c    字符串行的前后字符
     * @return 字符之间的值
     */
    public static String getStringBetween(String line, char c) {
        int firstOcc = line.indexOf(c);
        if (firstOcc < 0) {
            return Normal.EMPTY;
        }
        return line.substring(firstOcc + 1, line.lastIndexOf(c)).trim();
    }

    /**
     * 解析形如 "10.12.2" 或 "key = 1 (0x1) (int)" 的字符串，查找第一个连续数字集合的整数值。
     *
     * @param line 整个字符串
     * @return 第一个整数值，如果没有则返回 0
     */
    public static int getFirstIntValue(String line) {
        return getNthIntValue(line, 1);
    }

    /**
     * 解析形如 "10.12.2" 或 "key = 1 (0x1) (int)" 的字符串，查找第 n 个连续数字集合的整数值。
     *
     * @param line 整个字符串
     * @param n    要返回的整数集合
     * @return 第 n 个整数值，如果没有则返回 0
     */
    public static int getNthIntValue(String line, int n) {
        // 按非数字拆分字符串，
        String[] split = Pattern.NOT_NUMBERS_PATTERN
                .split(Pattern.WITH_NOT_NUMBERS_PATTERN.matcher(line).replaceFirst(Normal.EMPTY));
        if (split.length >= n) {
            return parseIntOrDefault(split[n - 1], 0);
        }
        return 0;
    }

    /**
     * 从字符串中移除所有匹配的子字符串。比正则表达式更高效。
     *
     * @param original 要从中移除的源字符串
     * @param toRemove 要移除的子字符串
     * @return 移除所有匹配子字符串后的字符串
     */
    public static String removeMatchingString(final String original, final String toRemove) {
        if (original == null || original.isEmpty() || toRemove == null || toRemove.isEmpty()) {
            return original;
        }

        int matchIndex = original.indexOf(toRemove);
        if (matchIndex == -1) {
            return original;
        }

        StringBuilder buffer = new StringBuilder(original.length() - toRemove.length());
        int currIndex = 0;
        do {
            buffer.append(original, currIndex, matchIndex);
            currIndex = matchIndex + toRemove.length();
            matchIndex = original.indexOf(toRemove, currIndex);
        } while (matchIndex != -1);

        buffer.append(original.substring(currIndex));
        return buffer.toString();
    }

    /**
     * 将分隔字符串解析为长整数数组。针对处理可预测长度的数组（如 Linux proc 或 sys 文件系统的可靠格式输出）进行了优化，最大程度减少新对象创建。用户应执行其他数据完整性检查。
     * <p>
     * 特殊情况，列表末尾的非数字字段（如 OpenVZ 中的 UUID）将被忽略。值大于最大长整数值的返回最大长整数值。
     * <p>
     * 索引参数假设指定的长度进行引用，前导字符被忽略。例如，如果字符串为 "foo 12 34 5" 且长度为 3，那么索引 0 为 12，索引 1 为 34，索引 2 为 5。
     *
     * @param s         要解析的字符串
     * @param indices   一个数组，指示最终数组中应填充的索引；其他值将被跳过。此索引假设字符串最右边的分隔字段包含数组，从零开始引用。
     * @param length    字符串数组中的总元素数。字符串的元素数可以超过此值；前导元素将被忽略。应通过 {@link #countStringToLongArray} 为每种文本格式计算一次。
     * @param delimiter 分隔符
     * @return 如果成功，返回解析的长整数数组。如果发生解析错误，将返回零数组。
     */
    public static long[] parseStringToLongArray(String s, int[] indices, int length, char delimiter) {
        // 确保最后一个字符是数字
        s = s.trim();

        long[] parsed = new long[indices.length];
        // 从字符串的右到左迭代
        // 使用索引数组从右到左填充结果数组
        int charIndex = s.length();
        int parsedIndex = indices.length - 1;
        int stringIndex = length - 1;

        int power = 0;
        int c;
        boolean delimCurrent = false;
        boolean numeric = true;
        boolean numberFound = false; // 忽略末尾的非数字
        boolean dashSeen = false; // 标记 UUID 为非数字
        while (--charIndex >= 0 && parsedIndex >= 0) {
            c = s.charAt(charIndex);
            if (c == delimiter) {
                // 第一个可解析的数字？
                if (!numberFound && numeric) {
                    numberFound = true;
                }
                if (!delimCurrent) {
                    if (numberFound && indices[parsedIndex] == stringIndex--) {
                        parsedIndex--;
                    }
                    delimCurrent = true;
                    power = 0;
                    dashSeen = false;
                    numeric = true;
                }
            } else if (indices[parsedIndex] != stringIndex || c == Symbol.C_PLUS || !numeric) {
                // 不影响解析，忽略
                delimCurrent = false;
            } else if (c >= '0' && c <= '9' && !dashSeen) {
                if (power > 18 || power == 17 && c == '9' && parsed[parsedIndex] > 223_372_036_854_775_807L) {
                    parsed[parsedIndex] = Long.MAX_VALUE;
                } else {
                    parsed[parsedIndex] += (c - '0') * Parsing.POWERS_OF_TEN[power++];
                }
                delimCurrent = false;
            } else if (c == Symbol.C_MINUS) {
                parsed[parsedIndex] *= -1L;
                delimCurrent = false;
                dashSeen = true;
            } else {
                // 标记为非数字并继续，除非我们已经看到数字
                // 其他情况下错误
                if (numberFound) {
                    if (!noLog(s)) {
                        Logger.error("Illegal character parsing string '{}' to long array: {}", s, s.charAt(charIndex));
                    }
                    return new long[indices.length];
                }
                parsed[parsedIndex] = 0;
                numeric = false;
            }
        }
        if (parsedIndex > 0) {
            if (!noLog(s)) {
                Logger.error("Not enough fields in string '{}' parsing to long array: {}", s,
                        indices.length - parsedIndex);
            }
            return new long[indices.length];
        }
        return parsed;
    }

    /**
     * 测试是否记录此消息的日志。
     *
     * @param s 要记录的字符串
     * @return 如果字符串以 {@code NOLOG} 开头，返回 true
     */
    private static boolean noLog(String s) {
        return s.startsWith("NOLOG: ");
    }

    /**
     * 解析分隔字符串以计算长整数数组的元素数。旨在为 {@link #parseStringToLongArray} 的 {@code length} 字段调用一次以进行计算。
     * <p>
     * 特殊情况，列表末尾的非数字字段（如 OpenVZ 中的 UUID）将被忽略。
     *
     * @param s         要解析的字符串
     * @param delimiter 分隔符
     * @return 最后一个不可解析值之后的可解析长整数值的数量。
     */
    public static int countStringToLongArray(String s, char delimiter) {
        // 确保最后一个字符是数字
        s = s.trim();

        // 从字符串的右到左迭代
        // 使用索引数组从右到左填充结果数组
        int charIndex = s.length();
        int numbers = 0;

        int c;
        boolean delimCurrent = false;
        boolean numeric = true;
        boolean dashSeen = false; // 标记 UUID 为非数字
        while (--charIndex >= 0) {
            c = s.charAt(charIndex);
            if (c == delimiter) {
                if (!delimCurrent) {
                    if (numeric) {
                        numbers++;
                    }
                    delimCurrent = true;
                    dashSeen = false;
                    numeric = true;
                }
            } else if (c == Symbol.C_PLUS || !numeric) {
                // 不影响解析，忽略
                delimCurrent = false;
            } else if (c >= '0' && c <= '9' && !dashSeen) {
                delimCurrent = false;
            } else if (c == Symbol.C_MINUS) {
                delimCurrent = false;
                dashSeen = true;
            } else {
                // 找到非数字或分隔符。如果不是最后一个字段，退出
                if (numbers > 0) {
                    return numbers;
                }
                // 否则标记为非数字并继续
                numeric = false;
            }
        }
        // 我们到达字符串开头，只有数字，将开始视为分隔符并退出
        return numbers + 1;
    }

    /**
     * 获取文本行中两个标记字符串之间的字符串。
     *
     * @param text   要搜索匹配的文本
     * @param before 在此文本之后开始匹配
     * @param after  在此文本之前结束匹配
     * @return before 和 after 之间的文本，如果任一标记不存在，则返回空字符串
     */
    public static String getTextBetweenStrings(String text, String before, String after) {

        String result = Normal.EMPTY;

        if (text.indexOf(before) >= 0 && text.indexOf(after) >= 0) {
            result = text.substring(text.indexOf(before) + before.length());
            result = result.substring(0, result.indexOf(after));
        }
        return result;
    }

    /**
     * 将表示文件时间（1601 年开始的 100 纳秒）的长整数转换为 1970 年开始的毫秒数。
     *
     * @param filetime 表示 FILETIME 的 64 位值
     * @param local    如果从本地文件时间（PDH 计数器）转换，则为 true；如果已经是 UTC（WMI PerfRawData 类），则为 false
     * @return 等效的毫秒数，自 epoch 以来
     */
    public static long filetimeToUtcMs(long filetime, boolean local) {
        return filetime / 10_000L - EPOCH_DIFF - (local ? TZ_OFFSET : 0L);
    }

    /**
     * 将 MM-DD-YYYY 或 MM/DD/YYYY 格式的日期解析为 YYYY-MM-DD 格式。
     *
     * @param dateString MM DD YYYY 格式的日期
     * @return 如果可解析，返回 ISO YYYY-MM-DD 格式的日期；否则返回原始字符串
     */
    public static String parseMmDdYyyyToYyyyMmDD(String dateString) {
        try {
            // 日期为 MM-DD-YYYY，转换为 YYYY-MM-DD
            return String.format(Locale.ROOT, "%s-%s-%s", dateString.substring(6, 10), dateString.substring(0, 2),
                    dateString.substring(3, 5));
        } catch (StringIndexOutOfBoundsException e) {
            return dateString;
        }
    }

    /**
     * 将 WMI 返回的 CIM 日期格式的字符串（例如，<code>20160513072950.782000-420</code>）转换为 {@link java.time.OffsetDateTime}。
     *
     * @param cimDateTime 非空的 CIM 日期格式的日期时间字符串
     * @return 如果字符串可解析，返回解析的 {@link java.time.OffsetDateTime}；否则返回 {@link Builder#UNIX_EPOCH}。
     */
    public static OffsetDateTime parseCimDateTimeToOffset(String cimDateTime) {
        // 保留前 22 个字符：数字、小数点和 + 或 - 符号
        // 但将最后 3 个字符从分钟偏移更改为 hh:mm
        try {
            // 从 WMI 获取，如 20160513072950.782000-420，
            int tzInMinutes = Integer.parseInt(cimDateTime.substring(22));
            // 修改为 20160513072950.782000-07:00，可解析
            LocalTime offsetAsLocalTime = LocalTime.MIDNIGHT.plusMinutes(tzInMinutes);
            return OffsetDateTime.parse(
                    cimDateTime.substring(0, 22) + offsetAsLocalTime.format(DateTimeFormatter.ISO_LOCAL_TIME),
                    Parsing.CIM_FORMAT);
        } catch (IndexOutOfBoundsException // 如果 cimDate 不是 22+ 个字符
                | NumberFormatException // 如果时区分钟数无法解析
                | DateTimeParseException e) {
            Logger.trace("Unable to parse {} to CIM DateTime.", cimDateTime);
            return Builder.UNIX_EPOCH;
        }
    }

    /**
     * 检查文件路径是否等于或以给定列表中的前缀开头。
     *
     * @param prefixList 路径前缀列表
     * @param path       要检查的字符串路径
     * @return 如果路径完全等于或以 prefixList 中的某个字符串开头，则返回 true
     */
    public static boolean filePathStartsWith(List<String> prefixList, String path) {
        for (String match : prefixList) {
            if (path.equals(match) || path.startsWith(match + "/")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析形如 "53G" 或 "54.904 M" 的字符串为其长整数值。
     *
     * @param count 带乘数的计数，例如 "4096 M"
     * @return 解析为长整数的计数
     */
    public static long parseMultipliedToLongs(String count) {
        Matcher matcher = UNITS_PATTERN.matcher(count.trim());
        String[] mem;
        if (matcher.find() && matcher.groupCount() == 3) {
            mem = new String[2];
            mem[0] = matcher.group(1);
            mem[1] = matcher.group(3);
        } else {
            mem = new String[] { count };
        }

        double number = Parsing.parseDoubleOrDefault(mem[0], 0L);
        if (mem.length == 2 && mem[1] != null && mem[1].length() >= 1) {
            switch (mem[1].charAt(0)) {
            case 'T':
                number *= 1_000_000_000_000L;
                break;
            case 'G':
                number *= 1_000_000_000L;
                break;
            case 'M':
                number *= 1_000_000L;
                break;
            case 'K':
            case 'k':
                number *= 1_000L;
                break;
            default:
            }
        }
        return (long) number;
    }

    /**
     * 解析形如 "4096 MB" 的字符串为其长整数值。用于解析 macOS 和 *nix 内存芯片大小。尽管给定的单位是十进制，但必须解析为二进制单位。
     *
     * @param size 内存大小字符串，例如 "4096 MB"
     * @return 解析为长整数的大小
     */
    public static long parseDecimalMemorySizeToBinary(String size) {
        String[] mem = Regex.SPACES.split(size);
        if (mem.length < 2) {
            // 如果没有空格，使用正则表达式
            Matcher matcher = BYTES_PATTERN.matcher(size.trim());
            if (matcher.find() && matcher.groupCount() == 2) {
                mem = new String[2];
                mem[0] = matcher.group(1);
                mem[1] = matcher.group(2);
            }
        }
        long capacity = Parsing.parseLongOrDefault(mem[0], 0L);
        if (mem.length == 2 && mem[1].length() > 1) {
            switch (mem[1].charAt(0)) {
            case 'T':
                capacity <<= 40;
                break;
            case 'G':
                capacity <<= 30;
                break;
            case 'M':
                capacity <<= 20;
                break;
            case 'K':
            case 'k':
                capacity <<= 10;
                break;
            default:
                break;
            }
        }
        return capacity;
    }

    /**
     * 解析 Windows 设备 ID 以获取供应商 ID、产品 ID 和序列号。
     *
     * @param deviceId 设备 ID
     * @return 一个 {@link Triplet}，第一个元素是供应商 ID，第二个元素是产品 ID，第三个元素是序列号或空字符串（如果解析成功），否则为 {@code null}
     */
    public static Triplet<String, String, String> parseDeviceIdToVendorProductSerial(String deviceId) {
        Matcher m = VENDOR_PRODUCT_ID_SERIAL.matcher(deviceId);
        if (m.matches()) {
            String vendorId = "0x" + m.group(1).toLowerCase(Locale.ROOT);
            String productId = "0x" + m.group(2).toLowerCase(Locale.ROOT);
            String serial = m.group(4);
            return Triplet.of(vendorId, productId,
                    !m.group(3).isEmpty() || serial.contains(Symbol.AND) ? Normal.EMPTY : serial);
        }
        return null;
    }

    /**
     * 解析 Linux lshw 资源字符串以计算内存大小。
     *
     * @param resources 包含一个或多个形如 {@code memory:b00000000-bffffffff} 的元素的字符串
     * @return {@code resources} 字符串中内存消耗的字节数
     */
    public static long parseLshwResourceString(String resources) {
        long bytes = 0L;
        // 首先按空格拆分
        String[] resourceArray = Regex.SPACES.split(resources);
        for (String r : resourceArray) {
            // 移除前缀
            if (r.startsWith("memory:")) {
                // 拆分为低地址和高地址
                String[] mem = r.substring(7).split(Symbol.MINUS);
                if (mem.length == 2) {
                    try {
                        // 解析十六进制字符串
                        bytes += Long.parseLong(mem[1], 16) - Long.parseLong(mem[0], 16) + 1;
                    } catch (NumberFormatException e) {
                        Logger.trace(DEFAULT_LOG_MSG, r, e);
                    }
                }
            }
        }
        return bytes;
    }

    /**
     * 解析 Linux lspci 机器可读行以获取其名称和 ID。
     *
     * @param line 形如 Foo [bar] 的字符串
     * @return 如果找到，分离方括号前和方括号内的字符串对，否则返回 null
     */
    public static Pair<String, String> parseLspciMachineReadable(String line) {
        Matcher matcher = LSPCI_MACHINE_READABLE.matcher(line);
        if (matcher.matches()) {
            return Pair.of(matcher.group(1), matcher.group(2));
        }
        return null;
    }

    /**
     * 解析包含内存大小的 Linux lspci 行。
     *
     * @param line 形如 Foo [size=256M] 的字符串
     * @return 内存大小（字节）
     */
    public static long parseLspciMemorySize(String line) {
        Matcher matcher = LSPCI_MEMORY_SIZE.matcher(line);
        if (matcher.matches()) {
            return parseDecimalMemorySizeToBinary(matcher.group(1) + Symbol.SPACE + matcher.group(2) + "B");
        }
        return 0;
    }

    /**
     * 解析包含连字符范围的以空格分隔的整数列表为仅包含整数的列表。例如，0 1 4-7 解析为包含 0、1、4、5、6 和 7 的列表。还支持逗号分隔的条目，如 0, 2-5, 7-8, 9 解析为包含
     * 0、2、3、4、5、7、8、9 的列表。
     *
     * @param text 包含以空格分隔的整数或连字符范围整数的字符串
     * @return 表示提供范围的整数列表。
     */
    public static List<Integer> parseHyphenatedIntList(String text) {
        List<Integer> result = new ArrayList<>();
        String[] csvTokens = text.split(Symbol.COMMA);
        for (String csvToken : csvTokens) {
            csvToken = csvToken.trim();
            for (String s : Regex.SPACES.split(csvToken)) {
                if (s.contains(Symbol.MINUS)) {
                    int first = getFirstIntValue(s);
                    int last = getNthIntValue(s, 2);
                    for (int i = first; i <= last; i++) {
                        result.add(i);
                    }
                } else {
                    int only = Parsing.parseIntOrDefault(s, -1);
                    if (only >= 0) {
                        result.add(only);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 将大端 IP 格式的整数解析为其表示 IPv4 地址的组件字节。
     *
     * @param ip 作为整数的地址
     * @return 作为四个字节数组的地址
     */
    public static byte[] parseIntToIP(int ip) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ip).array();
    }

    /**
     * 将大端 IP 格式的整数数组解析为其表示 IPv6 地址的组件字节。
     *
     * @param ip6 作为整数数组的地址
     * @return 作为十六个字节数组的地址
     */
    public static byte[] parseIntArrayToIP(int[] ip6) {
        ByteBuffer bb = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
        for (int i : ip6) {
            bb.putInt(i);
        }
        return bb.array();
    }

    /**
     * TCP 网络地址和端口按定义采用大端格式。必须反转 16 位无符号短端口值的两个字节的顺序。
     *
     * @param port 大端顺序的端口号
     * @return 端口号
     * @see <a href= "https://docs.microsoft.com/en-us/windows/win32/api/winsock/nf-winsock-ntohs">ntohs</a>
     */
    public static int bigEndian16ToLittleEndian(int port) {
        // 20480 = 0x5000 应该是 0x0050 = 80
        // 47873 = 0xBB01 应该是 0x01BB = 443
        return port >> 8 & 0xff | port << 8 & 0xff00;
    }

    /**
     * 将整数数组解析为 IPv4 或 IPv6（视情况而定）。
     * <p>
     * 适用于 Utmp 结构的 {@code ut_addr_v6} 元素。
     *
     * @param utAddrV6 表示 IPv6 地址的 4 个整数数组。IPv4 地址仅使用 utAddrV6[0]
     * @return IP 地址的字符串表示。
     */
    public static String parseUtAddrV6toIP(int[] utAddrV6) {
        if (utAddrV6.length != 4) {
            throw new IllegalArgumentException("ut_addr_v6 必须正好有 4 个元素");
        }
        // IPv4 只有第一个元素
        if (utAddrV6[1] == 0 && utAddrV6[2] == 0 && utAddrV6[3] == 0) {
            // 特殊情况，所有为 0
            if (utAddrV6[0] == 0) {
                return "::";
            }
            // 使用 InetAddress 解析
            byte[] ipv4 = ByteBuffer.allocate(4).putInt(utAddrV6[0]).array();
            try {
                return InetAddress.getByAddress(ipv4).getHostAddress();
            } catch (UnknownHostException e) {
                // 对于长度 4 或 16 不应该发生
                return Normal.UNKNOWN;
            }
        }
        // 解析所有 16 个字节
        byte[] ipv6 = ByteBuffer.allocate(16).putInt(utAddrV6[0]).putInt(utAddrV6[1]).putInt(utAddrV6[2])
                .putInt(utAddrV6[3]).array();
        try {
            return InetAddress.getByAddress(ipv6).getHostAddress()
                    .replaceAll("((?:(?:^|:)0+\\b){2,8}):?(?!\\S*\\b\\1:0+\\b)(\\S*)", "::$2");
        } catch (UnknownHostException e) {
            // 对于长度 4 或 16 不应该发生
            return Normal.UNKNOWN;
        }
    }

    /**
     * 将十六进制数字字符串解析为整数值。
     *
     * @param hexString    十六进制数字序列
     * @param defaultValue 如果解析失败，返回的默认值
     * @return 对应的整数值
     */
    public static int hexStringToInt(String hexString, int defaultValue) {
        if (hexString != null) {
            try {
                if (hexString.startsWith("0x")) {
                    return new BigInteger(hexString.substring(2), 16).intValue();
                } else {
                    return new BigInteger(hexString, 16).intValue();
                }
            } catch (NumberFormatException e) {
                Logger.trace(DEFAULT_LOG_MSG, hexString, e);
            }
        }
        // 十六进制解析失败，返回默认整数
        return defaultValue;
    }

    /**
     * 将十六进制数字字符串解析为长整数值。
     *
     * @param hexString    十六进制数字序列
     * @param defaultValue 如果解析失败，返回的默认值
     * @return 对应的长整数值
     */
    public static long hexStringToLong(String hexString, long defaultValue) {
        if (hexString != null) {
            try {
                if (hexString.startsWith("0x")) {
                    return new BigInteger(hexString.substring(2), 16).longValue();
                } else {
                    return new BigInteger(hexString, 16).longValue();
                }
            } catch (NumberFormatException e) {
                Logger.trace(DEFAULT_LOG_MSG, hexString, e);
            }
        }
        // 十六进制解析失败，返回默认长整数
        return defaultValue;
    }

    /**
     * 解析形如 "....foo" 的字符串为 "foo"。
     *
     * @param dotPrefixedStr 可能带有前导点的字符串
     * @return 去除点后的字符串
     */
    public static String removeLeadingDots(String dotPrefixedStr) {
        int pos = 0;
        while (pos < dotPrefixedStr.length() && dotPrefixedStr.charAt(pos) == '.') {
            pos++;
        }
        return pos < dotPrefixedStr.length() ? dotPrefixedStr.substring(pos) : Normal.EMPTY;
    }

    /**
     * 将以空字符分隔的字节数组解析为字符串列表。
     *
     * @param bytes 包含以空字符分隔的字符串的字节数组。两个连续的空字符标记列表的结束。
     * @return 空字符之间的字符串列表。
     */
    public static List<String> parseByteArrayToStrings(byte[] bytes) {
        List<String> strList = new ArrayList<>();
        int start = 0;
        int end = 0;
        // 迭代字符
        do {
            // 如果到达分隔符或数组末尾或换行符（Linux），添加到列表
            if (end == bytes.length || bytes[end] == 0 || bytes[end] == '\n') {
                // 零长度字符串表示两个空字符，完成
                if (start == end) {
                    break;
                }
                // 否则添加字符串并重置开始
                // 故意使用平台默认字符集
                strList.add(new String(bytes, start, end - start, Charset.UTF_8));
                start = end + 1;
            }
        } while (end++ < bytes.length);
        return strList;
    }

    /**
     * 将以空字符分隔的字节数组解析为字符串键值对的映射。
     *
     * @param bytes 包含字符串键值对的字节数组，键和值以 {@code =} 分隔，对以空字符分隔。两个连续的空字符标记映射的结束。
     * @return 空字符之间的字符串键值对映射。
     */
    public static Map<String, String> parseByteArrayToStringMap(byte[] bytes) {
        // API 未指定条目的特定顺序，但保留操作系统提供给最终用户的顺序是合理的
        Map<String, String> strMap = new LinkedHashMap<>();
        int start = 0;
        int end = 0;
        String key = null;
        // 迭代字符
        do {
            // 如果到达分隔符或数组末尾，添加到列表
            if (end == bytes.length || bytes[end] == 0) {
                // 没有键的零长度字符串，完成
                if (start == end && key == null) {
                    break;
                }
                // 否则添加字符串（可能为空）并重置开始
                // 故意使用平台默认字符集
                strMap.put(key, new String(bytes, start, end - start, Charset.UTF_8));
                key = null;
                start = end + 1;
            } else if (bytes[end] == Symbol.C_EQUAL && key == null) {
                key = new String(bytes, start, end - start, Charset.UTF_8);
                start = end + 1;
            }
        } while (end++ < bytes.length);
        return strMap;
    }

    /**
     * 将以空字符分隔的字符数组解析为字符串键值对的映射。
     *
     * @param chars 包含字符串键值对的字符数组，键和值以 {@code =} 分隔，对以空字符分隔。两个连续的空字符标记映射的结束。
     * @return 空字符之间的字符串键值对映射。
     */
    public static Map<String, String> parseCharArrayToStringMap(char[] chars) {
        // API 未指定条目的特定顺序，但保留操作系统提供给最终用户的顺序是合理的
        Map<String, String> strMap = new LinkedHashMap<>();
        int start = 0;
        int end = 0;
        String key = null;
        // 迭代字符
        do {
            // 如果到达分隔符或数组末尾，添加到列表
            if (end == chars.length || chars[end] == 0) {
                // 没有键的零长度字符串，完成
                if (start == end && key == null) {
                    break;
                }
                // 否则添加字符串（可能为空）并重置开始
                // 故意使用平台默认字符集
                strMap.put(key, new String(chars, start, end - start));
                key = null;
                start = end + 1;
            } else if (chars[end] == Symbol.C_EQUAL && key == null) {
                key = new String(chars, start, end - start);
                start = end + 1;
            }
        } while (end++ < chars.length);
        return strMap;
    }

    /**
     * 将分隔字符串解析为枚举映射。多个连续分隔符视为一个。
     *
     * @param <K>    扩展 Enum 的类型
     * @param clazz  枚举类
     * @param values 要解析为映射的分隔字符串
     * @param delim  要使用的分隔符
     * @return 使用分隔字符串值按顺序填充的 EnumMap。如果字符串值少于枚举值，后面的枚举值不被映射。最后的枚举值将包含字符串的剩余部分，包括多余的分隔符。
     */
    public static <K extends Enum<K>> Map<K, String> stringToEnumMap(Class<K> clazz, String values, char delim) {
        EnumMap<K, String> map = new EnumMap<>(clazz);
        int start = 0;
        int len = values.length();
        EnumSet<K> keys = EnumSet.allOf(clazz);
        int keySize = keys.size();
        for (K key : keys) {
            // 如果这是最后一个枚举，将索引放在字符串末尾，否则放在分隔符处
            int idx = --keySize == 0 ? len : values.indexOf(delim, start);
            if (idx >= 0) {
                map.put(key, values.substring(start, idx));
                start = idx;
                do {
                    start++;
                } while (start < len && values.charAt(start) == delim);
            } else {
                map.put(key, values.substring(start));
                break;
            }
        }
        return map;
    }

    /**
     * 检查给定键的映射中是否存在值，并根据是否存在返回值或 unknown。
     *
     * @param map 字符串键值对的映射
     * @param key 为给定键获取值
     * @return 如果键存在于映射中，返回键的值；否则返回 unknown
     */
    public static String getValueOrUnknown(Map<String, String> map, String key) {
        String value = map.getOrDefault(key, Normal.EMPTY);
        return value.isEmpty() ? Normal.UNKNOWN : value;
    }

    /**
     * 检查给定键的映射中是否存在值，并根据是否存在返回值或 unknown。
     *
     * @param map 键可以是任意类型，值为字符串的映射。
     * @param key 要从映射中获取值的键。键可以是与映射键类型兼容的任意类型。
     * @return 如果键存在于映射中且值不为空，返回与键关联的值；否则返回预定义的“unknown”字符串
     */
    public static String getValueOrUnknown(Map<?, String> map, Object key) {
        return getStringValueOrUnknown(map.get(key));
    }

    /**
     * 如果给定字符串值不为空，返回该值；否则返回 {@code Constants.UNKNOWN}。
     *
     * @param value 输入字符串值。
     * @return 如果值非空，返回输入值；否则返回 {@code Constants.UNKNOWN}。
     */
    public static String getStringValueOrUnknown(String value) {
        return (value == null || value.isEmpty()) ? Normal.UNKNOWN : value;
    }

    /**
     * 从给定格式解析日期字符串并将其转换为 epoch 时间（自 epoch 以来的毫秒数）。此方法适用于处理不同操作系统上的日期格式，例如：
     * <ul>
     * <li>{@code yyyyMMdd}</li>
     * <li>{@code dd/MM/yy, HH:mm}</li>
     * </ul>
     *
     * @param dateString  要解析的日期字符串。
     * @param datePattern 预期的日期格式模式（例如，{@code "yyyyMMdd"}）。
     * @return 自 1970 年 1 月 1 日 UTC 以来的毫秒数。如果解析失败，返回 {@code 0}。
     */
    public static long parseDateToEpoch(String dateString, String datePattern) {
        if (dateString == null || dateString.equals(Normal.UNKNOWN) || dateString.isEmpty() || datePattern.isEmpty()) {
            return 0; // 如果日期未知或为空，返回默认值
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(datePattern, Locale.ROOT);
            // 确定模式是否包含时间组件
            if (datePattern.contains("H") || datePattern.contains("m") || datePattern.contains("s")) {
                LocalDateTime localDateTime = LocalDateTime.parse(dateString, formatter);
                return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } else {
                LocalDate localDate = LocalDate.parse(dateString, formatter);
                return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            }
        } catch (DateTimeParseException e) {
            Logger.trace("Unable to parse date string: " + dateString);
            return 0;
        }
    }

}