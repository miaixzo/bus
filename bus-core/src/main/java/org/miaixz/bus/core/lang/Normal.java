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
package org.miaixz.bus.core.lang;

import org.miaixz.bus.core.io.file.FileType;

import java.net.URL;
import java.util.Date;

/**
 * 默认常量
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Normal {

    /**
     * The number 1024
     * 1.二进制计数的基本计量单位
     * 2.广大程序员的共同节日
     * 3.由于特殊性置顶
     */
    public static final int _1024 = 2 << 10;

    /**
     * The number 32768
     */
    public static final int _32768 = 2 << 14;

    /**
     * The number 16384
     */
    public static final int _16384 = 2 << 13;

    /**
     * The number 8192
     */
    public static final int _8192 = 2 << 12;

    /**
     * The number 2048
     */
    public static final int _2048 = 2 << 11;

    /**
     * The number 512
     */
    public static final int _512 = 2 << 9;

    /**
     * The number 256
     */
    public static final int _256 = 2 << 8;

    /**
     * The number 128
     */
    public static final int _128 = 2 << 7;

    /**
     * The number 64
     */
    public static final int _64 = 2 << 6;

    /**
     * The number 32
     */
    public static final int _32 = 2 << 5;

    /**
     * The number 24
     */
    public static final int _24 = 24;

    /**
     * The number 20
     */
    public static final int _20 = 20;

    /**
     * The number 18
     */
    public static final int _18 = 18;

    /**
     * The number 16
     */
    public static final int _16 = 16;

    /**
     * The number 12
     */
    public static final int _12 = 12;

    /**
     * The number 9
     */
    public static final int _10 = 10;

    /**
     * The number 9
     */
    public static final int _9 = 9;

    /**
     * The number 8
     */
    public static final int _8 = 8;

    /**
     * The number 7
     */
    public static final int _7 = 7;

    /**
     * The number 6
     */
    public static final int _6 = 6;

    /**
     * The number 5
     */
    public static final int _5 = 5;

    /**
     * The number 4
     */
    public static final int _4 = 4;

    /**
     * The number 3
     */
    public static final int _3 = 3;

    /**
     * The number 2
     */
    public static final int _2 = 2;

    /**
     * The number 1
     */
    public static final int _1 = 1;

    /**
     * The number 0
     */
    public static final int _0 = 0;

    /**
     * The number -1
     */
    public static final int __1 = -1;

    /**
     * The number -2
     */
    public static final int __2 = -2;

    /**
     * The number -3
     */
    public static final int __3 = -3;

    /**
     * The number -4
     */
    public static final int __4 = -4;

    /**
     * The number -5
     */
    public static final int __5 = -5;

    /**
     * The number -6
     */
    public static final int __6 = -6;

    /**
     * The number -7
     */
    public static final int __7 = -7;

    /**
     * The number -8
     */
    public static final int __8 = -8;

    /**
     * The number -9
     */
    public static final int __9 = -9;

    /**
     * The number -10
     */
    public static final int __10 = -10;
    /**
     * The number 65535
     */
    public static final int _65535 = 0xFFFF;
    /**
     * Reusable Long constant for zero.
     */
    public static final Long LONG_ZERO = Long.valueOf(0L);

    /**
     * Reusable Long constant for one.
     */
    public static final Long LONG_ONE = Long.valueOf(1L);

    /**
     * Reusable Long constant for minus one.
     */
    public static final Long LONG_MINUS_ONE = Long.valueOf(-1L);

    /**
     * Reusable Integer constant for zero.
     */
    public static final Integer INTEGER_ZERO = Integer.valueOf(0);

    /**
     * Reusable Integer constant for one.
     */
    public static final Integer INTEGER_ONE = Integer.valueOf(1);

    /**
     * Reusable Integer constant for two
     */
    public static final Integer INTEGER_TWO = Integer.valueOf(2);

    /**
     * Reusable Integer constant for minus one.
     */
    public static final Integer INTEGER_MINUS_ONE = Integer.valueOf(-1);

    /**
     * Reusable Short constant for zero.
     */
    public static final Short SHORT_ZERO = Short.valueOf((short) 0);

    /**
     * Reusable Short constant for one.
     */
    public static final Short SHORT_ONE = Short.valueOf((short) 1);

    /**
     * Reusable Short constant for minus one.
     */
    public static final Short SHORT_MINUS_ONE = Short.valueOf((short) -1);

    /**
     * Reusable Byte constant for zero.
     */
    public static final Byte BYTE_ZERO = Byte.valueOf((byte) 0);

    /**
     * Reusable Byte constant for one.
     */
    public static final Byte BYTE_ONE = Byte.valueOf((byte) 1);

    /**
     * Reusable Byte constant for minus one.
     */
    public static final Byte BYTE_MINUS_ONE = Byte.valueOf((byte) -1);

    /**
     * Reusable Double constant for zero.
     */
    public static final Double DOUBLE_ZERO = Double.valueOf(0.0d);

    /**
     * Reusable Double constant for one.
     */
    public static final Double DOUBLE_ONE = Double.valueOf(1.0d);

    /**
     * Reusable Double constant for minus one.
     */
    public static final Double DOUBLE_MINUS_ONE = Double.valueOf(-1.0d);

    /**
     * Reusable Float constant for zero.
     */
    public static final Float FLOAT_ZERO = Float.valueOf(0.0f);

    /**
     * Reusable Float constant for one.
     */
    public static final Float FLOAT_ONE = Float.valueOf(1.0f);

    /**
     * Reusable Float constant for minus one.
     */
    public static final Float FLOAT_MINUS_ONE = Float.valueOf(-1.0f);

    /**
     * {@code Object} array.
     */
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * {@code Class} array.
     */
    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];

    /**
     * {@code String} array.
     */
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * {@code long} array.
     */
    public static final long[] EMPTY_LONG_ARRAY = new long[0];

    /**
     * {@code Long} array.
     */
    public static final Long[] EMPTY_LONG_OBJECT_ARRAY = new Long[0];

    /**
     * {@code int} array.
     */
    public static final int[] EMPTY_INT_ARRAY = new int[0];

    /**
     * {@code Integer} array.
     */
    public static final Integer[] EMPTY_INTEGER_OBJECT_ARRAY = new Integer[0];

    /**
     * {@code short} array.
     */
    public static final short[] EMPTY_SHORT_ARRAY = new short[0];

    /**
     * {@code Short} array.
     */
    public static final Short[] EMPTY_SHORT_OBJECT_ARRAY = new Short[0];

    /**
     * {@code byte} array.
     */
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * {@code Byte} array.
     */
    public static final Byte[] EMPTY_BYTE_OBJECT_ARRAY = new Byte[0];

    /**
     * {@code double} array.
     */
    public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];

    /**
     * {@code Double} array.
     */
    public static final Double[] EMPTY_DOUBLE_OBJECT_ARRAY = new Double[0];

    /**
     * {@code float} array.
     */
    public static final float[] EMPTY_FLOAT_ARRAY = new float[0];

    /**
     * {@code Float} array.
     */
    public static final Float[] EMPTY_FLOAT_OBJECT_ARRAY = new Float[0];

    /**
     * {@code boolean} array.
     */
    public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];

    /**
     * {@code Boolean} array.
     */
    public static final Boolean[] EMPTY_BOOLEAN_OBJECT_ARRAY = new Boolean[0];

    /**
     * {@code char} array.
     */
    public static final char[] EMPTY_CHAR_ARRAY = new char[0];

    /**
     * {@code Character} array.
     */
    public static final Character[] EMPTY_CHARACTER_OBJECT_ARRAY = new Character[0];

    /**
     * {@code Date} array.
     */
    public static final Date[] EMPTY_DATE_OBJECT_ARRAY = new Date[0];

    /**
     * Bytes per Kilobyte(KB).
     */
    public static final long BYTES_PER_KB = _1024;

    /**
     * Bytes per Megabyte(MB).
     */
    public static final long BYTES_PER_MB = BYTES_PER_KB * _1024;

    /**
     * Bytes per Gigabyte(GB).
     */
    public static final long BYTES_PER_GB = BYTES_PER_MB * _1024;

    /**
     * Bytes per Terabyte(TB).
     */
    public static final long BYTES_PER_TB = BYTES_PER_GB * _1024;

    /**
     * 默认增长因子，当Map的size达到 容量*增长因子时，开始扩充Map
     */
    public static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * 字符串: 数字
     */
    public static final String NUMBER = "0123456789";

    /**
     * 字符串: 字母
     */
    public static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

    /**
     * 字符串: 小字母数字
     */
    public static final String LOWER_ALPHABET_NUMBER = ALPHABET + NUMBER;

    /**
     * 字符串: 大字母数字
     */
    public static final String UPPER_ALPHABET_NUMBER = ALPHABET.toUpperCase() + NUMBER;

    /**
     * 字符串:空
     */
    public static final String EMPTY = "";

    /**
     * 字符串:null
     */
    public static final String NULL = "null";

    /**
     * 真/是
     */
    public static final String TRUE = "true";

    /**
     * 假/否
     */
    public static final String FALSE = "false";

    /**
     * 启用
     */
    public static final String ENABLED = "enabled";

    /**
     * 禁用
     */
    public static final String DISABLED = "disabled";

    /**
     * is
     */
    public static final String IS = "is";

    /**
     * set
     */
    public static final String SET = "set";

    /**
     * get
     */
    public static final String GET = "get";

    /**
     * equals
     */
    public static final String EQUALS = "equals";

    /**
     * hashCode
     */
    public static final String HASHCODE = "hashCode";

    /**
     * toString
     */
    public static final String TOSTRING = "toString";

    /**
     * 字符串:unknown
     */
    public static final String UNKNOWN = "unknown";

    /**
     * 字符串:undefined
     */
    public static final String UNDEFINED = "undefined";

    /**
     * URL 前缀表示文件: "file:"
     */
    public static final String FILE_URL_PREFIX = "file:";

    /**
     * URL 前缀表示jar: "jar:"
     */
    public static final String JAR_URL_PREFIX = "jar:";

    /**
     * URL 前缀表示war: "war:"
     */
    public static final String WAR_URL_PREFIX = "war:";

    /**
     * 针对ClassPath路径的伪协议前缀: "classpath:"
     */
    public static final String CLASSPATH = "classpath:";

    /**
     * 元数据: "META-INF"
     */
    public static final String META_INF = "META-INF";

    /**
     * 元数据: "META-INF/services"
     */
    public static final String META_INF_SERVICES = "META-INF/services";

    /**
     * URL 协议表示文件: "file"
     */
    public static final String URL_PROTOCOL_FILE = "file";

    /**
     * URL 协议表示Jar文件: "jar"
     */
    public static final String URL_PROTOCOL_JAR = "jar";

    /**
     * LIB 协议表示lib文件: "lib"
     */
    public static final String LIB_PROTOCOL_JAR = "lib";

    /**
     * URL 协议表示zip文件: "zip"
     */
    public static final String URL_PROTOCOL_ZIP = "zip";

    /**
     * URL 协议表示WebSphere文件: "wsjar"
     */
    public static final String URL_PROTOCOL_WSJAR = "wsjar";

    /**
     * URL 协议表示JBoss zip文件: "vfszip"
     */
    public static final String URL_PROTOCOL_VFSZIP = "vfszip";

    /**
     * URL 协议表示JBoss文件: "vfsfile"
     */
    public static final String URL_PROTOCOL_VFSFILE = "vfsfile";

    /**
     * URL 协议表示JBoss VFS资源: "vfs"
     */
    public static final String URL_PROTOCOL_VFS = "vfs";

    /**
     * Jar路径以及内部文件路径的分界符: "!/"
     */
    public static final String JAR_URL_SEPARATOR = "!/";

    /**
     * WAR路径及内部文件路径分界符
     */
    public static final String WAR_URL_SEPARATOR = "*/";

    /**
     * 十六进制错误
     */
    public static final String HEX_ERROR = "0x%08X";

    /**
     * 简体中文运算符
     */
    public static final char[] OPERATOR_ZH = {
            '加', '减', '乘', '除'
    };

    /**
     * 字节计量单位
     * <pre>
     *     byte        1B     1
     *     kilobyte    1KB    1,024
     *     megabyte    1MB    1,048,576
     *     gigabyte    1GB    1,073,741,824
     *     terabyte    1TB    1,099,511,627,776
     * </pre>
     */
    public static final String[] CAPACITY_NAMES = new String[]{
            "B", "KB", "MB", "GB", "TB", "PB", "EB"
    };

    /**
     * 七色
     */
    public static final String[] COLOR = {
            "白", "黒", "碧", "绿", "黄", "白", "赤", "白", "紫"
    };

    /**
     * 缺省的币种代码，为CNY（人民币）。
     */
    public static final String CNY = "CNY";

    /**
     * 简体中文单位
     */
    public static final String[] SIMPLE_UNITS = {
            "", "十", "百", "千"
    };

    /**
     * 繁体中文单位
     */
    public static final String[] TRADITIONAL_UNITS = {
            "", "拾", "佰", "仟"
    };

    /**
     * 简体中文形式
     */
    public static final String[] SIMPLE_DIGITS = {
            "零", "一", "二", "三", "四", "五", "六", "七", "八", "九"
    };

    /**
     * 繁体中文形式
     */
    public static final String[] TRADITIONAL_DIGITS = {
            "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"
    };

    /**
     * 英文数字1-9
     */
    public static final String[] EN_NUMBER = new String[]{
            "", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN",
            "EIGHT", "NINE"
    };

    /**
     * 英文数字10-19
     */
    public static final String[] EN_NUMBER_TEEN = new String[]{
            "TEN", "ELEVEN", "TWELVE", "THIRTEEN", "FOURTEEN",
            "FIFTEEN", "SIXTEEN", "SEVENTEEN", "EIGHTEEN", "NINETEEN"
    };

    /**
     * 英文数字10-90
     */
    public static final String[] EN_NUMBER_TEN = new String[]{
            "TEN", "TWENTY", "THIRTY", "FORTY", "FIFTY", "SIXTY",
            "SEVENTY", "EIGHTY", "NINETY"
    };

    /**
     * 英文数字千-亿
     */
    public static final String[] EN_NUMBER_MORE = new String[]{
            "", "THOUSAND", "MILLION", "BILLION", "TRILLION"
    };

    /**
     * 表示为真的字符串
     */
    public static final String[] TRUE_ARRAY = {
            "true", "t", "yes", "y", "ok", "1", "on", "是", "真", "正确",
            "对", "對", "√"
    };

    /**
     * 表示为假的字符串
     */
    public static final String[] FALSE_ARRAY = {
            "false", "no", "n", "f", "0", "off", "否", "错", "錯", "假",
            "×"
    };

    /**
     * 用于建立十六进制字符的输出的小写字符数组
     */
    public static final char[] DIGITS_16_LOWER = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f'
    };

    /**
     * 用于建立十六进制字符的输出的大写字符数组
     */
    public static final char[] DIGITS_16_UPPER = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B',
            'C', 'D', 'E', 'F'
    };

    /**
     * base64编码表
     */
    public static final byte[] ENCODE_64_TABLE = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', '+', '/'
    };

    /**
     * base64解码表
     */
    public static final byte[] DECODE_64_TABLE = {
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1,
            62, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1,
            -1, -2, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
            12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1,
            -1, -1, -1, 63, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50,
            51
    };

    /**
     * 提供的URL是否为文件
     * 文件协议包括"file", "vfsfile" 或 "vfs".
     *
     * @param url {@link URL}
     * @return 是否为文件
     */
    public static boolean isFileOrVfsURL(final URL url) {
        Assert.notNull(url, "URL must be not null");
        return isFileURL(url) || isVfsURL(url);
    }

    /**
     * 提供的URL是否为文件
     * 文件协议包括"file".
     *
     * @param url {@link URL}
     * @return 是否为文件
     */
    public static boolean isFileURL(final URL url) {
        Assert.notNull(url, "URL must be not null");
        final String protocol = url.getProtocol();
        return URL_PROTOCOL_FILE.equals(protocol);
    }

    /**
     * 提供的URL是否为文件
     * 文件协议包括"vfsfile" 或 "vfs".
     *
     * @param url {@link URL}
     * @return 是否为文件
     */
    public static boolean isVfsURL(final URL url) {
        Assert.notNull(url, "URL must be not null");
        final String protocol = url.getProtocol();
        return (URL_PROTOCOL_VFSFILE.equals(protocol) ||
                URL_PROTOCOL_VFS.equals(protocol));
    }

    /**
     * 提供的URL是否为jar包URL 协议包括： "jar", "zip", "vfszip" 或 "wsjar".
     *
     * @param url {@link URL}
     * @return 是否为jar包URL
     */
    public static boolean isJarURL(final URL url) {
        Assert.notNull(url, "URL must be not null");
        final String protocol = url.getProtocol();
        return (URL_PROTOCOL_JAR.equals(protocol) ||
                URL_PROTOCOL_ZIP.equals(protocol) ||
                URL_PROTOCOL_VFSZIP.equals(protocol) ||
                URL_PROTOCOL_WSJAR.equals(protocol));
    }

    /**
     * 提供的URL是否为Jar文件URL 判断依据为file协议且扩展名为.jar
     *
     * @param url the URL to check
     * @return whether the URL has been identified as a JAR file URL
     */
    public static boolean isJarFileURL(final URL url) {
        Assert.notNull(url, "URL must be not null");
        return (URL_PROTOCOL_FILE.equals(url.getProtocol()) &&
                url.getPath().toLowerCase().endsWith(FileType.JAR));
    }

}
