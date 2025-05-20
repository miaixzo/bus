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
package org.miaixz.bus.http;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.IDN;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.SegmentBuffer;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.http.bodys.ResponseBody;
import org.miaixz.bus.http.metric.Internal;
import org.miaixz.bus.http.metric.http.Http2Header;

/**
 * HTTP 相关的实用工具类
 * <p>
 * 提供处理 HTTP 请求和响应的工具方法，包括数据解析、编码、集合操作、日期格式化等。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    /**
     * 最大日期值（9999年12月31日）
     */
    public static final long MAX_DATE = 253402300799999L;
    /**
     * 空头部
     */
    public static final Headers EMPTY_HEADERS = Headers.of();
    /**
     * 空响应体
     */
    public static final ResponseBody EMPTY_RESPONSE = ResponseBody.create(null, Normal.EMPTY_BYTE_ARRAY);
    /**
     * GMT 时区
     */
    public static final TimeZone UTC = TimeZone.getTimeZone("GMT");
    /**
     * 自然顺序比较器
     */
    public static final Comparator<String> NATURAL_ORDER = String::compareTo;
    /**
     * 引号字符串分隔符
     */
    public static final ByteString QUOTED_STRING_DELIMITERS = ByteString.encodeUtf8("\"\\");
    /**
     * 令牌分隔符
     */
    public static final ByteString TOKEN_DELIMITERS = ByteString.encodeUtf8("\t ,=");
    /**
     * 浏览器兼容的日期格式
     */
    public static final String[] BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS = new String[] {
            "EEE, dd MMM yyyy HH:mm:ss zzz", "EEEE, dd-MMM-yy HH:mm:ss zzz", "EEE MMM d HH:mm:ss yyyy",
            "EEE, dd-MMM-yyyy HH:mm:ss z", "EEE, dd-MMM-yyyy HH-mm-ss z", "EEE, dd MMM yy HH:mm:ss z",
            "EEE dd-MMM-yyyy HH:mm:ss z", "EEE dd MMM yyyy HH:mm:ss z", "EEE dd-MMM-yyyy HH-mm-ss z",
            "EEE dd-MMM-yy HH:mm:ss z", "EEE dd MMM yy HH:mm:ss z", "EEE,dd-MMM-yy HH:mm:ss z",
            "EEE,dd-MMM-yyyy HH:mm:ss z", "EEE, dd-MM-yyyy HH:mm:ss z", "EEE MMM d yyyy HH:mm:ss z", };
    /**
     * 浏览器兼容的日期格式器
     */
    public static final DateFormat[] BROWSER_COMPATIBLE_DATE_FORMATS = new DateFormat[BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS.length];
    /**
     * CONNECT 命令
     */
    public static final String CONNECT = "CONNECT";
    /**
     * CONNECTED 命令
     */
    public static final String CONNECTED = "CONNECTED";
    /**
     * SEND 命令
     */
    public static final String SEND = "SEND";
    /**
     * MESSAGE 命令
     */
    public static final String MESSAGE = "MESSAGE";
    /**
     * SUBSCRIBE 命令
     */
    public static final String SUBSCRIBE = "SUBSCRIBE";
    /**
     * UNSUBSCRIBE 命令
     */
    public static final String UNSUBSCRIBE = "UNSUBSCRIBE";
    /**
     * ACK 命令
     */
    public static final String ACK = "ACK";
    /**
     * UNKNOWN 命令
     */
    public static final String UNKNOWN = "UNKNOWN";
    /**
     * ERROR 命令
     */
    public static final String ERROR = "ERROR";
    /**
     * Unicode BOM 标记
     */
    private static final SegmentBuffer UNICODE_BOMS = SegmentBuffer.of(ByteString.decodeHex("efbbbf"),
            ByteString.decodeHex("feff"), ByteString.decodeHex("fffe"), ByteString.decodeHex("0000ffff"),
            ByteString.decodeHex("ffff0000"));
    /**
     * 异常抑制的反射方法
     */
    private static final Method addSuppressedExceptionMethod;
    /**
     * 标准日期格式器
     */
    private static final ThreadLocal<DateFormat> STANDARD_DATE_FORMAT = ThreadLocal.withInitial(() -> {
        DateFormat rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        rfc1123.setLenient(false);
        rfc1123.setTimeZone(UTC);
        return rfc1123;
    });

    static {
        Method m;
        try {
            m = Throwable.class.getDeclaredMethod("addSuppressed", Throwable.class);
        } catch (Exception e) {
            m = null;
        }
        addSuppressedExceptionMethod = m;
    }

    /**
     * 构造函数
     */
    public Builder() {
    }

    /**
     * 添加抑制的异常
     *
     * @param e          主异常
     * @param suppressed 抑制的异常
     */
    public static void addSuppressedIfPossible(Throwable e, Throwable suppressed) {
        if (addSuppressedExceptionMethod != null) {
            try {
                addSuppressedExceptionMethod.invoke(e, suppressed);
            } catch (InvocationTargetException | IllegalAccessException ignored) {
            }
        }
    }

    /**
     * 检查数组偏移和计数
     *
     * @param arrayLength 数组长度
     * @param offset      偏移量
     * @param count       计数
     * @throws ArrayIndexOutOfBoundsException 如果参数无效
     */
    public static void checkOffsetAndCount(long arrayLength, long offset, long count) {
        if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * 尝试耗尽数据源
     *
     * @param source   数据源
     * @param timeout  超时时间
     * @param timeUnit 时间单位
     * @return true 如果成功耗尽
     */
    public static boolean discard(Source source, int timeout, TimeUnit timeUnit) {
        try {
            return skipAll(source, timeout, timeUnit);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 读取数据源直到耗尽或超时
     *
     * @param source   数据源
     * @param duration 超时时间
     * @param timeUnit 时间单位
     * @return true 如果成功耗尽
     * @throws IOException 如果读取失败
     */
    public static boolean skipAll(Source source, int duration, TimeUnit timeUnit) throws IOException {
        long now = System.nanoTime();
        long originalDuration = source.timeout().hasDeadline() ? source.timeout().deadlineNanoTime() - now
                : Long.MAX_VALUE;
        source.timeout().deadlineNanoTime(now + Math.min(originalDuration, timeUnit.toNanos(duration)));
        try {
            Buffer skipBuffer = new Buffer();
            while (source.read(skipBuffer, 8192) != -1) {
                skipBuffer.clear();
            }
            return true;
        } catch (InterruptedIOException e) {
            return false;
        } finally {
            if (originalDuration == Long.MAX_VALUE) {
                source.timeout().clearDeadline();
            } else {
                source.timeout().deadlineNanoTime(now + originalDuration);
            }
        }
    }

    /**
     * 创建不可修改的列表
     *
     * @param list 原始列表
     * @param <T>  列表元素类型
     * @return 不可修改的列表
     */
    public static <T> List<T> immutableList(List<T> list) {
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    /**
     * 创建不可修改的映射
     *
     * @param map 原始映射
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 不可修改的映射
     */
    public static <K, V> Map<K, V> immutableMap(Map<K, V> map) {
        return map.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }

    /**
     * 创建不可修改的列表
     *
     * @param elements 元素数组
     * @param <T>      元素类型
     * @return 不可修改的列表
     */
    public static <T> List<T> immutableList(T... elements) {
        return Collections.unmodifiableList(Arrays.asList(elements.clone()));
    }

    /**
     * 创建线程工厂
     *
     * @param name   线程名称
     * @param daemon 是否为守护线程
     * @return 线程工厂
     */
    public static ThreadFactory threadFactory(String name, boolean daemon) {
        return runnable -> {
            Thread result = new Thread(runnable, name);
            result.setDaemon(daemon);
            return result;
        };
    }

    /**
     * 获取两个字符串数组的交集
     *
     * @param comparator 比较器
     * @param first      第一个数组
     * @param second     第二个数组
     * @return 交集数组
     */
    public static String[] intersect(Comparator<? super String> comparator, String[] first, String[] second) {
        List<String> result = new ArrayList<>();
        for (String a : first) {
            for (String b : second) {
                if (comparator.compare(a, b) == 0) {
                    result.add(a);
                    break;
                }
            }
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * 检查两个字符串数组是否存在交集
     *
     * @param comparator 比较器
     * @param first      第一个数组
     * @param second     第二个数组
     * @return true 如果存在交集
     */
    public static boolean nonEmptyIntersection(Comparator<String> comparator, String[] first, String[] second) {
        if (first == null || second == null || first.length == 0 || second.length == 0) {
            return false;
        }
        for (String a : first) {
            for (String b : second) {
                if (comparator.compare(a, b) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 生成主机头部
     *
     * @param url                URL
     * @param includeDefaultPort 是否包含默认端口
     * @return 主机头部字符串
     */
    public static String hostHeader(UnoUrl url, boolean includeDefaultPort) {
        String host = url.host().contains(Symbol.COLON) ? "[" + url.host() + "]" : url.host();
        return includeDefaultPort || url.port() != UnoUrl.defaultPort(url.scheme()) ? host + Symbol.COLON + url.port()
                : host;
    }

    /**
     * 查找字符串在数组中的索引
     *
     * @param comparator 比较器
     * @param array      数组
     * @param value      值
     * @return 索引（不存在时为 -1）
     */
    public static int indexOf(Comparator<String> comparator, String[] array, String value) {
        for (int i = 0, size = array.length; i < size; i++) {
            if (comparator.compare(array[i], value) == 0)
                return i;
        }
        return -1;
    }

    /**
     * 拼接字符串到数组
     *
     * @param array 数组
     * @param value 值
     * @return 新数组
     */
    public static String[] concat(String[] array, String value) {
        String[] result = new String[array.length + 1];
        System.arraycopy(array, 0, result, 0, array.length);
        result[result.length - 1] = value;
        return result;
    }

    /**
     * 跳过前导 ASCII 空白字符
     *
     * @param input 输入字符串
     * @param pos   起始位置
     * @param limit 结束位置
     * @return 非空白字符位置
     */
    public static int skipLeadingAsciiWhitespace(String input, int pos, int limit) {
        for (int i = pos; i < limit; i++) {
            switch (input.charAt(i)) {
            case Symbol.C_HT:
            case Symbol.C_LF:
            case '\f':
            case Symbol.C_CR:
            case Symbol.C_SPACE:
                continue;
            default:
                return i;
            }
        }
        return limit;
    }

    /**
     * 跳过尾部 ASCII 空白字符
     *
     * @param input 输入字符串
     * @param pos   起始位置
     * @param limit 结束位置
     * @return 非空白字符位置
     */
    public static int skipTrailingAsciiWhitespace(String input, int pos, int limit) {
        for (int i = limit - 1; i >= pos; i--) {
            switch (input.charAt(i)) {
            case Symbol.C_HT:
            case Symbol.C_LF:
            case '\f':
            case Symbol.C_CR:
            case Symbol.C_SPACE:
                continue;
            default:
                return i + 1;
            }
        }
        return pos;
    }

    /**
     * 修剪字符串
     *
     * @param string 输入字符串
     * @param pos    起始位置
     * @param limit  结束位置
     * @return 修剪后的字符串
     */
    public static String trimSubstring(String string, int pos, int limit) {
        int start = skipLeadingAsciiWhitespace(string, pos, limit);
        int end = skipTrailingAsciiWhitespace(string, start, limit);
        return string.substring(start, end);
    }

    /**
     * 查找分隔符位置
     *
     * @param input      输入字符串
     * @param pos        起始位置
     * @param limit      结束位置
     * @param delimiters 分隔符集合
     * @return 分隔符位置
     */
    public static int delimiterOffset(String input, int pos, int limit, String delimiters) {
        for (int i = pos; i < limit; i++) {
            if (delimiters.indexOf(input.charAt(i)) != -1)
                return i;
        }
        return limit;
    }

    /**
     * 查找单个分隔符位置
     *
     * @param input     输入字符串
     * @param pos       起始位置
     * @param limit     结束位置
     * @param delimiter 分隔符
     * @return 分隔符位置
     */
    public static int delimiterOffset(String input, int pos, int limit, char delimiter) {
        for (int i = pos; i < limit; i++) {
            if (input.charAt(i) == delimiter)
                return i;
        }
        return limit;
    }

    /**
     * 规范化主机名
     *
     * @param host 主机名
     * @return 规范化主机名（无效时为 null）
     */
    public static String canonicalizeHost(String host) {
        if (host.contains(Symbol.COLON)) {
            InetAddress inetAddress = host.startsWith("[") && host.endsWith("]")
                    ? decodeIpv6(host, 1, host.length() - 1)
                    : decodeIpv6(host, 0, host.length());
            if (inetAddress == null)
                return null;
            byte[] address = inetAddress.getAddress();
            if (address.length == 16)
                return inet6AddressToAscii(address);
            if (address.length == 4)
                return inetAddress.getHostAddress();
            throw new AssertionError("Invalid IPv6 address: '" + host + "'");
        }

        try {
            String result = IDN.toASCII(host).toLowerCase(Locale.US);
            if (result.isEmpty())
                return null;

            if (containsInvalidHostnameAsciiCodes(result)) {
                return null;
            }
            return result;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 检查主机名是否包含无效 ASCII 字符
     *
     * @param hostnameAscii 主机名
     * @return true 如果包含无效字符
     */
    private static boolean containsInvalidHostnameAsciiCodes(String hostnameAscii) {
        for (int i = 0; i < hostnameAscii.length(); i++) {
            char c = hostnameAscii.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                return true;
            }

            if (" #%/:?@[\\]".indexOf(c) != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查找控制字符或非 ASCII 字符的索引
     *
     * @param input 输入字符串
     * @return 索引（不存在时为 -1）
     */
    public static int indexOfControlOrNonAscii(String input) {
        for (int i = 0, length = input.length(); i < length; i++) {
            char c = input.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                return i;
            }
        }
        return -1;
    }

    /**
     * 检查是否为 IP 地址
     *
     * @param host 主机名
     * @return true 如果是 IP 地址
     */
    public static boolean verifyAsIpAddress(String host) {
        return Pattern.IP_ADDRESS_PATTERN.matcher(host).matches();
    }

    /**
     * 根据 BOM 选择字符集
     *
     * @param source  数据源
     * @param charset 默认字符集
     * @return 字符集
     * @throws IOException 如果读取失败
     */
    public static Charset bomAwareCharset(BufferSource source, Charset charset) throws IOException {
        switch (source.select(UNICODE_BOMS)) {
        case 0:
            return org.miaixz.bus.core.lang.Charset.UTF_8;
        case 1:
            return org.miaixz.bus.core.lang.Charset.UTF_16_BE;
        case 2:
            return org.miaixz.bus.core.lang.Charset.UTF_16_LE;
        case 3:
            return org.miaixz.bus.core.lang.Charset.UTF_32_BE;
        case 4:
            return org.miaixz.bus.core.lang.Charset.UTF_32_LE;
        case -1:
            return charset;
        default:
            throw new AssertionError();
        }
    }

    /**
     * 检查持续时间
     *
     * @param name     参数名称
     * @param duration 持续时间
     * @param unit     时间单位
     * @return 持续时间（毫秒）
     * @throws IllegalArgumentException 如果参数无效
     */
    public static int checkDuration(String name, long duration, TimeUnit unit) {
        if (duration < 0)
            throw new IllegalArgumentException(name + " < 0");
        if (null == unit)
            throw new NullPointerException("unit == null");
        long millis = unit.toMillis(duration);
        if (millis > Integer.MAX_VALUE)
            throw new IllegalArgumentException(name + " too large.");
        if (millis == 0 && duration > 0)
            throw new IllegalArgumentException(name + " too small.");
        return (int) millis;
    }

    /**
     * 解码十六进制字符
     *
     * @param c 字符
     * @return 解码值（无效时为 -1）
     */
    public static int decodeHexDigit(char c) {
        if (c >= Symbol.C_ZERO && c <= Symbol.C_NINE)
            return c - Symbol.C_ZERO;
        if (c >= 'a' && c <= 'f')
            return c - 'a' + 10;
        if (c >= 'A' && c <= 'F')
            return c - 'A' + 10;
        return -1;
    }

    /**
     * 解码 IPv6 地址
     *
     * @param input 输入字符串
     * @param pos   起始位置
     * @param limit 结束位置
     * @return InetAddress 对象（无效时为 null）
     */
    private static InetAddress decodeIpv6(String input, int pos, int limit) {
        byte[] address = new byte[Normal._16];
        int b = 0;
        int compress = -1;
        int groupOffset = -1;

        for (int i = pos; i < limit;) {
            if (b == address.length)
                return null;

            if (i + 2 <= limit && input.regionMatches(i, Symbol.COLON + Symbol.COLON, 0, 2)) {
                if (compress != -1)
                    return null;
                i += 2;
                b += 2;
                compress = b;
                if (i == limit)
                    break;
            } else if (b != 0) {
                if (input.regionMatches(i, Symbol.COLON, 0, 1)) {
                    i++;
                } else if (input.regionMatches(i, Symbol.DOT, 0, 1)) {
                    if (!decodeIpv4Suffix(input, groupOffset, limit, address, b - 2))
                        return null;
                    b += 2;
                    break;
                } else {
                    return null;
                }
            }

            int value = 0;
            groupOffset = i;
            for (; i < limit; i++) {
                char c = input.charAt(i);
                int hexDigit = decodeHexDigit(c);
                if (hexDigit == -1)
                    break;
                value = (value << 4) + hexDigit;
            }
            int groupLength = i - groupOffset;
            if (groupLength == 0 || groupLength > 4)
                return null;

            address[b++] = (byte) ((value >>> 8) & 0xff);
            address[b++] = (byte) (value & 0xff);
        }

        if (b != address.length) {
            if (compress == -1)
                return null;
            System.arraycopy(address, compress, address, address.length - (b - compress), b - compress);
            Arrays.fill(address, compress, compress + (address.length - b), (byte) 0);
        }

        try {
            return InetAddress.getByAddress(address);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }

    /**
     * 解码 IPv6 地址中的 IPv4 后缀
     *
     * @param input         输入字符串
     * @param pos           起始位置
     * @param limit         结束位置
     * @param address       地址数组
     * @param addressOffset 地址偏移
     * @return true 如果解码成功
     */
    private static boolean decodeIpv4Suffix(String input, int pos, int limit, byte[] address, int addressOffset) {
        int b = addressOffset;

        for (int i = pos; i < limit;) {
            if (b == address.length)
                return false;

            if (b != addressOffset) {
                if (input.charAt(i) != Symbol.C_DOT)
                    return false;
                i++;
            }

            int value = 0;
            int groupOffset = i;
            for (; i < limit; i++) {
                char c = input.charAt(i);
                if (c < Symbol.C_ZERO || c > Symbol.C_NINE)
                    break;
                if (value == 0 && groupOffset != i)
                    return false;
                value = (value * 10) + c - Symbol.C_ZERO;
                if (value > 255)
                    return false;
            }
            int groupLength = i - groupOffset;
            if (groupLength == 0)
                return false;

            address[b++] = (byte) value;
        }

        if (b != addressOffset + 4)
            return false;

        return true;
    }

    /**
     * 将 IPv6 地址编码为 ASCII 格式
     *
     * @param address 地址字节数组
     * @return ASCII 格式的地址
     */
    private static String inet6AddressToAscii(byte[] address) {
        int longestRunOffset = -1;
        int longestRunLength = 0;
        for (int i = 0; i < address.length; i += 2) {
            int currentRunOffset = i;
            while (i < Normal._16 && address[i] == 0 && address[i + 1] == 0) {
                i += 2;
            }
            int currentRunLength = i - currentRunOffset;
            if (currentRunLength > longestRunLength && currentRunLength >= 4) {
                longestRunOffset = currentRunOffset;
                longestRunLength = currentRunLength;
            }
        }

        Buffer result = new Buffer();
        for (int i = 0; i < address.length;) {
            if (i == longestRunOffset) {
                result.writeByte(Symbol.C_COLON);
                i += longestRunLength;
                if (i == Normal._16)
                    result.writeByte(Symbol.C_COLON);
            } else {
                if (i > 0)
                    result.writeByte(Symbol.C_COLON);
                int group = (address[i] & 0xff) << 8 | address[i + 1] & 0xff;
                result.writeHexadecimalUnsignedLong(group);
                i += 2;
            }
        }
        return result.readUtf8();
    }

    /**
     * 将 HTTP/2 头部列表转换为 Headers
     *
     * @param headerBlock HTTP/2 头部列表
     * @return Headers 实例
     */
    public static Headers toHeaders(List<Http2Header> headerBlock) {
        Headers.Builder builder = new Headers.Builder();
        for (Http2Header header : headerBlock) {
            Internal.instance.addLenient(builder, header.name.utf8(), header.value.utf8());
        }
        return builder.build();
    }

    /**
     * 将 Headers 转换为 HTTP/2 头部列表
     *
     * @param headers Headers 实例
     * @return HTTP/2 头部列表
     */
    public static List<Http2Header> toHeaderBlock(Headers headers) {
        List<Http2Header> result = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            result.add(new Http2Header(headers.name(i), headers.value(i)));
        }
        return result;
    }

    /**
     * 检查两个 URL 是否可以复用连接
     *
     * @param a 第一个 URL
     * @param b 第二个 URL
     * @return true 如果可以复用连接
     */
    public static boolean sameConnection(UnoUrl a, UnoUrl b) {
        return a.host().equals(b.host()) && a.port() == b.port() && a.scheme().equals(b.scheme());
    }

    /**
     * 解析日期字符串
     *
     * @param value 日期字符串
     * @return 日期对象（无效时为 null）
     */
    public static Date parse(String value) {
        if (value.length() == 0) {
            return null;
        }

        ParsePosition position = new ParsePosition(0);
        Date result = STANDARD_DATE_FORMAT.get().parse(value, position);
        if (position.getIndex() == value.length()) {
            return result;
        }
        synchronized (BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS) {
            for (int i = 0, count = BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS.length; i < count; i++) {
                DateFormat format = BROWSER_COMPATIBLE_DATE_FORMATS[i];
                if (format == null) {
                    format = new SimpleDateFormat(BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS[i], Locale.US);
                    format.setTimeZone(UTC);
                    BROWSER_COMPATIBLE_DATE_FORMATS[i] = format;
                }
                position.setIndex(0);
                result = format.parse(value, position);
                if (position.getIndex() != 0) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * 格式化日期
     *
     * @param value 日期对象
     * @return 日期字符串
     */
    public static String format(Date value) {
        return STANDARD_DATE_FORMAT.get().format(value);
    }

}