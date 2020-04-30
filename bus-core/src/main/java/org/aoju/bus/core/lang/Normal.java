/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.core.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 默认常量
 *
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public class Normal {

    /**
     * 字符串:空
     */
    public static final String BUS = "bus";
    /**
     * 字符串:空
     */
    public static final String EMPTY = "";
    /**
     * 字符串:null
     */
    public static final String NULL = "null";
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
    public static final String META_DATA_INF = "META-INF";
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
     * 字符串: 数字
     */
    public static final String NUMBER = "0123456789";
    /**
     * 字符串: 大字母
     */
    public static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /**
     * 字符串: 小字母
     */
    public static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    /**
     * 字符串: 大字母数字
     */
    public static final String UPPER_NUMBER = UPPER + NUMBER;
    /**
     * 字符串: 小字母数字
     */
    public static final String LOWER_NUMBER = LOWER + NUMBER;
    /**
     * 字符串: 小字母数字
     */
    public static final String UPPER_LOWER = UPPER + LOWER;
    /**
     * 字符串: 大小字母数字
     */
    public static final String UPPER_LOWER_NUMBER = UPPER + LOWER + NUMBER;
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
     * 英文数字1-9
     */
    public static final String[] EN_NUMBER = new String[]{
            "", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN",
            "EIGHT", "NINE"
    };
    /**
     * 英文数字10-19
     */
    public static final String[] NUMBER_TEEN = new String[]{
            "TEN", "ELEVEN", "TWELVE", "THIRTEEN", "FOURTEEN", "FIFTEEN",
            "SIXTEEN", "SEVENTEEN", "EIGHTEEN", "NINETEEN"
    };
    /**
     * 英文数字10-90
     */
    public static final String[] NUMBER_TEN = new String[]{
            "TEN", "TWENTY", "THIRTY", "FORTY", "FIFTY", "SIXTY",
            "SEVENTY", "EIGHTY", "NINETY"
    };
    /**
     * 英文数字千-亿
     */
    public static final String[] NUMBER_MORE = new String[]{
            "", "THOUSAND", "MILLION", "BILLION"
    };
    /**
     * 表示为真的字符串
     */
    public static final String[] TRUE_ARRAY = {
            "true", "t", "yes", "y", "ok", "是", "对", "真", "正确"
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
     * URL编码表,将 . 和 / 替换为 - 和 _
     */
    public static final byte[] ENCODE_URL_TABLE = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', '-', '_'
    };
    /**
     * BCrypt编码表
     */
    public static final char[] ENCODE_BCRYPT_TABLE = {
            '.', '/', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
            'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
            'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9'
    };
    /**
     * BCrypt解码表
     */
    public static final byte[] DECODE_BCRYPT_TABLE = {
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, 0, 1, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, -1, -1,
            -1, -1, -1, -1, -1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
            14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, -1,
            -1, -1, -1, -1, -1, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37,
            38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52,
            53, -1, -1, -1, -1, -1
    };
    /**
     * base32编码表
     */
    public static final char[] ENCODE_32_TABLE = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z', '2', '3', '4', '5', '6', '7'
    };
    /**
     * base32解码表
     */
    public static final int[] DECODE_32_TABLE = {
            0xFF, 0xFF, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x00, 0x01, 0x02,
            0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C,
            0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16,
            0x17, 0x18, 0x19, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x00,
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A,
            0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13, 0x14,
            0x15, 0x16, 0x17, 0x18, 0x19, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
    };
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

    @Getter
    @AllArgsConstructor
    public enum Gender {

        /**
         * MALE/FAMALE为正常值,通过{@link Gender#getGender(String)}
         * 方法获取真实的性别UNKNOWN为容错值,部分平台不会返回用户性别,
         * 为了方便统一,使用UNKNOWN标记所有未知或不可测的用户性别信息
         */
        MALE(1, "男"),
        FEMALE(0, "女"),
        UNKNOWN(-1, "未知");

        private int code;
        private String desc;

        public static Gender getGender(String code) {
            if (code == null) {
                return UNKNOWN;
            }
            String[] males = {"M", "男", Symbol.ONE, "MALE"};
            if (Arrays.asList(males).contains(code.toUpperCase())) {
                return MALE;
            }
            String[] females = {"F", "女", Symbol.ZERO, "FEMALE"};
            if (Arrays.asList(females).contains(code.toUpperCase())) {
                return FEMALE;
            }
            return UNKNOWN;
        }

    }

}
