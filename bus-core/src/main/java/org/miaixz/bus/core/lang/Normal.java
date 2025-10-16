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
package org.miaixz.bus.core.lang;

import java.lang.annotation.*;
import java.math.BigInteger;
import java.net.URL;
import java.util.Date;
import java.util.Set;

import org.miaixz.bus.core.io.file.FileType;
import org.miaixz.bus.core.xyz.SetKit;

/**
 * Provides a collection of common constants and utility methods.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Normal {

    /**
     * The number 1024, representing 2^10.
     */
    public static final int _1024 = 2 << 10;

    /**
     * 1024 bytes, binary prefix KiB.
     */
    public static final long KIBI = 1L << 10;

    /**
     * 1024^2 bytes, binary prefix MiB.
     */
    public static final long MEBI = 1L << 20;

    /**
     * 1024^3 bytes, binary prefix GiB.
     */
    public static final long GIBI = 1L << 30;

    /**
     * 1024^4 bytes, binary prefix TiB.
     */
    public static final long TEBI = 1L << 40;

    /**
     * 1024^5 bytes, binary prefix PiB.
     */
    public static final long PEBI = 1L << 50;

    /**
     * 1024^6 bytes, binary prefix EiB.
     */
    public static final long EXBI = 1L << 60;

    /**
     * 10^6, decimal prefix M.
     */
    public static final long MEGA = 1_000_000L;

    /**
     * 10^9, decimal prefix G.
     */
    public static final long GIGA = 1_000_000_000L;

    /**
     * 10^12, decimal prefix T.
     */
    public static final long TERA = 1_000_000_000_000L;

    /**
     * 10^15, decimal prefix P.
     */
    public static final long PETA = 1_000_000_000_000_000L;

    /**
     * 10^18, decimal prefix E.
     */
    public static final long EXA = 1_000_000_000_000_000_000L;

    /**
     * 1000, decimal prefix K.
     */
    public static final long KILO = 1_000L;

    /**
     * 2^64, reference value for two's complement.
     */
    public static final BigInteger TWOS_COMPLEMENT_REF = BigInteger.ONE.shiftLeft(64);

    /**
     * The number 32768.
     */
    public static final int _32768 = 2 << 14;

    /**
     * The number 16384.
     */
    public static final int _16384 = 2 << 13;

    /**
     * The number 8192.
     */
    public static final int _8192 = 2 << 12;

    /**
     * The number 2048.
     */
    public static final int _2048 = 2 << 11;

    /**
     * The number 512.
     */
    public static final int _512 = 2 << 9;

    /**
     * The number 256.
     */
    public static final int _256 = 2 << 8;

    /**
     * The number 128.
     */
    public static final int _128 = 2 << 7;

    /**
     * The number 64.
     */
    public static final int _64 = 2 << 6;

    /**
     * The number 32.
     */
    public static final int _32 = 2 << 5;

    /**
     * The number 24.
     */
    public static final int _24 = 24;

    /**
     * The number 20.
     */
    public static final int _20 = 20;

    /**
     * The number 18.
     */
    public static final int _18 = 18;

    /**
     * The number 16.
     */
    public static final int _16 = 16;

    /**
     * The number 12.
     */
    public static final int _12 = 12;

    /**
     * The number 10.
     */
    public static final int _10 = 10;

    /**
     * The number 9.
     */
    public static final int _9 = 9;

    /**
     * The number 8.
     */
    public static final int _8 = 8;

    /**
     * The number 7.
     */
    public static final int _7 = 7;

    /**
     * The number 6.
     */
    public static final int _6 = 6;

    /**
     * The number 5.
     */
    public static final int _5 = 5;

    /**
     * The number 4.
     */
    public static final int _4 = 4;

    /**
     * The number 3.
     */
    public static final int _3 = 3;

    /**
     * The number 2.
     */
    public static final int _2 = 2;

    /**
     * The number 1.
     */
    public static final int _1 = 1;

    /**
     * The number 0.
     */
    public static final int _0 = 0;

    /**
     * The number -1.
     */
    public static final int __1 = -1;

    /**
     * The number -2.
     */
    public static final int __2 = -2;

    /**
     * The number -3.
     */
    public static final int __3 = -3;

    /**
     * The number -4.
     */
    public static final int __4 = -4;

    /**
     * The number -5.
     */
    public static final int __5 = -5;

    /**
     * The number -6.
     */
    public static final int __6 = -6;

    /**
     * The number -7.
     */
    public static final int __7 = -7;

    /**
     * The number -8.
     */
    public static final int __8 = -8;

    /**
     * The number -9.
     */
    public static final int __9 = -9;

    /**
     * The number -10.
     */
    public static final int __10 = -10;
    /**
     * The number 65535.
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
     * Reusable Integer constant for two.
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
     * An empty {@code Object} array.
     */
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * An empty {@code Class} array.
     */
    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];

    /**
     * An empty {@code String} array.
     */
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * An empty {@code long} array.
     */
    public static final long[] EMPTY_LONG_ARRAY = new long[0];

    /**
     * An empty {@code Long} array.
     */
    public static final Long[] EMPTY_LONG_OBJECT_ARRAY = new Long[0];

    /**
     * An empty {@code int} array.
     */
    public static final int[] EMPTY_INT_ARRAY = new int[0];

    /**
     * An empty {@code Integer} array.
     */
    public static final Integer[] EMPTY_INTEGER_OBJECT_ARRAY = new Integer[0];

    /**
     * An empty {@code short} array.
     */
    public static final short[] EMPTY_SHORT_ARRAY = new short[0];

    /**
     * An empty {@code Short} array.
     */
    public static final Short[] EMPTY_SHORT_OBJECT_ARRAY = new Short[0];

    /**
     * An empty {@code byte} array.
     */
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * An empty {@code Byte} array.
     */
    public static final Byte[] EMPTY_BYTE_OBJECT_ARRAY = new Byte[0];

    /**
     * An empty {@code double} array.
     */
    public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];

    /**
     * An empty {@code Double} array.
     */
    public static final Double[] EMPTY_DOUBLE_OBJECT_ARRAY = new Double[0];

    /**
     * An empty {@code float} array.
     */
    public static final float[] EMPTY_FLOAT_ARRAY = new float[0];

    /**
     * An empty {@code Float} array.
     */
    public static final Float[] EMPTY_FLOAT_OBJECT_ARRAY = new Float[0];

    /**
     * An empty {@code boolean} array.
     */
    public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];

    /**
     * An empty {@code Boolean} array.
     */
    public static final Boolean[] EMPTY_BOOLEAN_OBJECT_ARRAY = new Boolean[0];

    /**
     * An empty {@code char} array.
     */
    public static final char[] EMPTY_CHAR_ARRAY = new char[0];

    /**
     * An empty {@code Character} array.
     */
    public static final Character[] EMPTY_CHARACTER_OBJECT_ARRAY = new Character[0];

    /**
     * An empty {@code Date} array.
     */
    public static final Date[] EMPTY_DATE_OBJECT_ARRAY = new Date[0];

    /**
     * 64 MiB in bytes.
     */
    public static final long MEBI_64 = MEBI * 64;

    /**
     * 128 MiB in bytes.
     */
    public static final long MEBI_128 = MEBI * 128;

    /**
     * 256 MiB in bytes.
     */
    public static final long MEBI_256 = MEBI * 256;

    /**
     * 512 MiB in bytes.
     */
    public static final long MEBI_512 = MEBI * 512;

    /**
     * 1024 MiB in bytes (which is also 1 GiB).
     */
    public static final long MEBI_1024 = MEBI * 1024;

    /**
     * 2048 MiB in bytes (which is also 2 GiB).
     */
    public static final long MEBI_2048 = MEBI * 2048;

    /**
     * Bytes per Kilobyte (KB).
     */
    public static final long BYTES_PER_KB = _1024;

    /**
     * Bytes per Megabyte (MB).
     */
    public static final long BYTES_PER_MB = BYTES_PER_KB * _1024;

    /**
     * Bytes per Gigabyte (GB).
     */
    public static final long BYTES_PER_GB = BYTES_PER_MB * _1024;

    /**
     * Bytes per Terabyte (TB).
     */
    public static final long BYTES_PER_TB = BYTES_PER_GB * _1024;

    /**
     * Default load factor for maps, used to determine when to resize the map. When the map's size reaches capacity *
     * load factor, the map is expanded.
     */
    public static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * String containing all numeric digits: "0123456789".
     */
    public static final String NUMBER = "0123456789";

    /**
     * String containing all lowercase English alphabet characters: "abcdefghijklmnopqrstuvwxyz".
     */
    public static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

    /**
     * String containing all lowercase English alphabet characters and numeric digits.
     */
    public static final String LOWER_ALPHABET_NUMBER = ALPHABET + NUMBER;

    /**
     * String containing all uppercase English alphabet characters and numeric digits.
     */
    public static final String UPPER_ALPHABET_NUMBER = ALPHABET.toUpperCase() + NUMBER;

    /**
     * String containing all uppercase English alphabet characters and numeric digits.
     */
    public static final String ALL_ALPHABET_NUMBER = ALPHABET.toUpperCase() + ALPHABET + NUMBER;

    /**
     * An empty string: "".
     */
    public static final String EMPTY = "";

    /**
     * The string "none".
     */
    public static final String NONE = "none";

    /**
     * The string "null".
     */
    public static final String NULL = "null";

    /**
     * The string "true".
     */
    public static final String TRUE = "true";

    /**
     * The string "false".
     */
    public static final String FALSE = "false";

    /**
     * The string "enabled".
     */
    public static final String ENABLED = "enabled";

    /**
     * The string "disabled".
     */
    public static final String DISABLED = "disabled";

    /**
     * The string "is".
     */
    public static final String IS = "is";

    /**
     * The string "set".
     */
    public static final String SET = "set";

    /**
     * The string "get".
     */
    public static final String GET = "get";

    /**
     * The string "equals".
     */
    public static final String EQUALS = "equals";

    /**
     * The string "hashCode".
     */
    public static final String HASHCODE = "hashCode";

    /**
     * The string "toString".
     */
    public static final String TOSTRING = "toString";

    /**
     * The string "unknown".
     */
    public static final String UNKNOWN = "unknown";

    /**
     * The string "undefined".
     */
    public static final String UNDEFINED = "undefined";

    /**
     * URL prefix for file resources: "file:".
     */
    public static final String FILE_URL_PREFIX = "file:";

    /**
     * URL prefix for JAR resources: "jar:".
     */
    public static final String JAR_URL_PREFIX = "jar:";

    /**
     * URL prefix for WAR resources: "war:".
     */
    public static final String WAR_URL_PREFIX = "war:";

    /**
     * Pseudo-protocol prefix for ClassPath resources: "classpath:".
     */
    public static final String CLASSPATH = "classpath:";

    /**
     * Pseudo-protocol prefix for project resources: "project:".
     */
    public static final String PROJECT_URL_PREFIX = "project:";

    /**
     * Metadata directory name: "META-INF".
     */
    public static final String META_INF = "META-INF";

    /**
     * Services metadata directory name: "META-INF/services".
     */
    public static final String META_INF_SERVICES = "META-INF/services";

    /**
     * URL protocol for file resources: "file".
     */
    public static final String URL_PROTOCOL_FILE = "file";

    /**
     * URL protocol for Jar file resources: "jar".
     */
    public static final String URL_PROTOCOL_JAR = "jar";

    /**
     * LIB protocol for library files: "lib".
     */
    public static final String LIB_PROTOCOL_JAR = "lib";

    /**
     * URL protocol for zip file resources: "zip".
     */
    public static final String URL_PROTOCOL_ZIP = "zip";

    /**
     * URL protocol for WebSphere JAR files: "wsjar".
     */
    public static final String URL_PROTOCOL_WSJAR = "wsjar";

    /**
     * URL protocol for JBoss VFS zip files: "vfszip".
     */
    public static final String URL_PROTOCOL_VFSZIP = "vfszip";

    /**
     * URL protocol for JBoss VFS files: "vfsfile".
     */
    public static final String URL_PROTOCOL_VFSFILE = "vfsfile";

    /**
     * URL protocol for JBoss VFS resources: "vfs".
     */
    public static final String URL_PROTOCOL_VFS = "vfs";

    /**
     * Separator for JAR file paths and internal file paths: "!/".
     */
    public static final String JAR_URL_SEPARATOR = "!/";

    /**
     * Separator for WAR file paths and internal file paths.
     */
    public static final String WAR_URL_SEPARATOR = "*/";

    /**
     * Hexadecimal error format string.
     */
    public static final String HEX_ERROR = "0x%08X";

    /**
     * Chinese operators for arithmetic operations.
     */
    public static final char[] OPERATOR_ZH = { '加', '减', '乘', '除' };

    /**
     * Byte measurement units.
     * 
     * <pre>
     *     byte        1B     1
     *     kilobyte    1KB    1,024
     *     megabyte    1MB    1,048,576
     *     gigabyte    1GB    1,073,741,824
     *     terabyte    1TB    1,099,511,627,776
     * </pre>
     */
    public static final String[] CAPACITY_NAMES = new String[] { "B", "KB", "MB", "GB", "TB", "PB", "EB" };

    /**
     * Abbreviated unit names for capacity.
     */
    public static final String[] CAPACITY_SIMPLE_NAMES = new String[] { "B", "K", "M", "G", "T", "P", "E" };

    /**
     * Seven colors in Chinese.
     */
    public static final String[] COLOR = { "白", "黑", "碧", "绿", "黄", "白", "赤", "白", "紫" };

    /**
     * Default currency code, CNY (Chinese Yuan).
     */
    public static final String CNY = "CNY";

    /**
     * Simple Chinese numeral units.
     */
    public static final String[] SIMPLE_UNITS = { "", "十", "百", "千" };

    /**
     * Traditional Chinese numeral units.
     */
    public static final String[] TRADITIONAL_UNITS = { "", "拾", "佰", "仟" };

    /**
     * Simple Chinese numeral characters.
     */
    public static final String[] SIMPLE_DIGITS = { "零", "一", "二", "三", "四", "五", "六", "七", "八", "九" };

    /**
     * Traditional Chinese numeral characters.
     */
    public static final String[] TRADITIONAL_DIGITS = { "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖" };

    /**
     * English numbers 1-9.
     */
    public static final String[] EN_NUMBER = new String[] { "", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN",
            "EIGHT", "NINE" };

    /**
     * English numbers 10-19.
     */
    public static final String[] EN_NUMBER_TEEN = new String[] { "TEN", "ELEVEN", "TWELVE", "THIRTEEN", "FOURTEEN",
            "FIFTEEN", "SIXTEEN", "SEVENTEEN", "EIGHTEEN", "NINETEEN" };

    /**
     * English numbers 10, 20, ..., 90.
     */
    public static final String[] EN_NUMBER_TEN = new String[] { "TEN", "TWENTY", "THIRTY", "FORTY", "FIFTY", "SIXTY",
            "SEVENTY", "EIGHTY", "NINETY" };

    /**
     * English number units for thousands, millions, billions, etc.
     */
    public static final String[] EN_NUMBER_MORE = new String[] { "", "THOUSAND", "MILLION", "BILLION", "TRILLION" };

    /**
     * Strings representing a boolean true value.
     */
    public static final String[] TRUE_ARRAY = { "true", "t", "yes", "y", "ok", "1", "on", "是", "真", "正确", "对", "對",
            "√" };

    /**
     * Strings representing a boolean false value.
     */
    public static final String[] FALSE_ARRAY = { "false", "no", "n", "f", "0", "off", "否", "错", "錯", "假", "×" };

    /**
     * Lowercase character array for hexadecimal output.
     */
    public static final char[] DIGITS_16_LOWER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f' };

    /**
     * Uppercase character array for hexadecimal output.
     */
    public static final char[] DIGITS_16_UPPER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F' };

    /**
     * Base64 encoding table.
     */
    public static final byte[] ENCODE_64_TABLE = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
            'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', '+', '/' };

    /**
     * Base64 decoding table.
     */
    public static final byte[] DECODE_64_TABLE = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62,
            -1, 62, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7,
            8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, 63, -1, 26, 27, 28,
            29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51 };

    /**
     * Set of meta-annotations.
     */
    public static final Set<Class<? extends Annotation>> META_ANNOTATIONS = SetKit.of(
            Target.class,
            Retention.class,
            Inherited.class,
            Documented.class,
            SuppressWarnings.class,
            Override.class,
            Deprecated.class,
            Repeatable.class,
            Native.class,
            FunctionalInterface.class);

    /**
     * Checks if the provided URL represents a file resource. File protocols include "file", "vfsfile", or "vfs".
     *
     * @param url The {@link URL} to check.
     * @return {@code true} if the URL is a file or VFS URL, {@code false} otherwise.
     */
    public static boolean isFileOrVfsURL(final URL url) {
        Assert.notNull(url, "URL must be not null");
        return isFileURL(url) || isVfsURL(url);
    }

    /**
     * Checks if the provided URL represents a file resource. File protocol is "file".
     *
     * @param url The {@link URL} to check.
     * @return {@code true} if the URL is a file URL, {@code false} otherwise.
     */
    public static boolean isFileURL(final URL url) {
        Assert.notNull(url, "URL must be not null");
        final String protocol = url.getProtocol();
        return URL_PROTOCOL_FILE.equals(protocol);
    }

    /**
     * Checks if the provided URL represents a VFS (Virtual File System) resource. VFS protocols include "vfsfile" or
     * "vfs".
     *
     * @param url The {@link URL} to check.
     * @return {@code true} if the URL is a VFS URL, {@code false} otherwise.
     */
    public static boolean isVfsURL(final URL url) {
        Assert.notNull(url, "URL must be not null");
        final String protocol = url.getProtocol();
        return (URL_PROTOCOL_VFSFILE.equals(protocol) || URL_PROTOCOL_VFS.equals(protocol));
    }

    /**
     * Checks if the provided URL represents a JAR resource. Protocols include "jar", "zip", "vfszip", or "wsjar".
     *
     * @param url The {@link URL} to check.
     * @return {@code true} if the URL is a JAR URL, {@code false} otherwise.
     */
    public static boolean isJarURL(final URL url) {
        Assert.notNull(url, "URL must be not null");
        final String protocol = url.getProtocol();
        return (URL_PROTOCOL_JAR.equals(protocol) || URL_PROTOCOL_ZIP.equals(protocol)
                || URL_PROTOCOL_VFSZIP.equals(protocol) || URL_PROTOCOL_WSJAR.equals(protocol));
    }

    /**
     * Checks if the provided URL represents a JAR file URL. The criteria are a "file" protocol and a ".jar" file
     * extension.
     *
     * @param url The URL to check.
     * @return {@code true} if the URL has been identified as a JAR file URL, {@code false} otherwise.
     */
    public static boolean isJarFileURL(final URL url) {
        Assert.notNull(url, "URL must be not null");
        return (URL_PROTOCOL_FILE.equals(url.getProtocol()) && url.getPath().toLowerCase().endsWith(FileType.JAR));
    }

}
