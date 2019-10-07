/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.consts;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 默认常量
 *
 * @author Kimi Liu
 * @version 3.6.5
 * @since JDK 1.8
 */
public class Normal {

    /**
     * 字符串:空
     */
    public static final String EMPTY = "";
    /**
     * 字符串:null
     */
    public static final String NULL = "null";
    /**
     * 字符串: 数字
     */
    public static final String NUMBER = "0123456789";
    /**
     * 字符串: 小字母
     */
    public static final String LETTER = "abcdefghijklmnopqrstuvwxyz";
    /**
     * 字符串: 大小字母
     */
    public static final String LETTER_LOWER_UPPER = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz";

    /**
     * 字符串: 小字母数字
     */
    public static final String LETTER_LOWER_NO = "abcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * 字符串: 大字母数字
     */
    public static final String LETTER_UPPER_NO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * 字符串: 大小字母数字
     */
    public static final String LETTER_LOWER_UPPER_NO = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789";

    /**
     * 简体中文形式
     **/
    public static final String[] SIMPLE_DIGITS = {
            "零", "一", "二", "三", "四", "五",
            "六", "七", "八", "九"
    };
    /**
     * 繁体中文形式
     **/
    public static final String[] TRADITIONAL_DIGITS = {
            "零", "壹", "贰", "叁", "肆", "伍",
            "陆", "柒", "捌", "玖"
    };

    /**
     * 简体中文单位
     **/
    public static final String[] SIMPLE_UNITS = {"", "十", "百", "千"};
    /**
     * 繁体中文单位
     **/
    public static final String[] TRADITIONAL_UNITS = {"", "拾", "佰", "仟"};

    /**
     * 英文数字1-9
     **/
    public static final String[] EN_NUMBER = new String[]{
            "", "ONE", "TWO", "THREE", "FOUR", "FIVE",
            "SIX", "SEVEN", "EIGHT", "NINE"
    };
    /**
     * 英文数字10-19
     **/
    public static final String[] NUMBER_TEEN = new String[]{
            "TEN", "ELEVEN", "TWELEVE", "THIRTEEN", "FOURTEEN",
            "FIFTEEN", "SIXTEEN", "SEVENTEEN", "EIGHTEEN", "NINETEEN"
    };
    /**
     * 英文数字10-90
     **/
    public static final String[] NUMBER_TEN = new String[]{
            "TEN", "TWENTY", "THIRTY", "FORTY", "FIFTY",
            "SIXTY", "SEVENTY", "EIGHTY", "NINETY"
    };
    /**
     * 英文数字千-亿
     **/
    public static final String[] NUMBER_MORE = new String[]{
            "", "THOUSAND", "MILLION", "BILLION"
    };

    /**
     * 将数字表示为字符串的所有字符
     */
    public static final char[] HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B',
            'C', 'D', 'E', 'F'
    };

    /**
     * 将数字表示为字符串的所有字符
     */
    public final static byte[] HEX_DIGITS_ALL = {
            '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f', 'g', 'h',
            'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F',
            'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R',
            'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z'
    };

    /**
     * 字符串: 字母和数字
     */
    public static final String LETTER_NUMBER = LETTER + NUMBER;

    public static final String SETTER_PREFIX = "set";

    public static final String GETTER_PREFIX = "get";

    /**
     * 针对ClassPath路径的伪协议前缀（兼容Spring）: "classpath:"
     */
    public static final String CLASSPATH_URL_PREFIX = "classpath:";
    /**
     * 针对ClassPath路径的伪协议前缀（兼容Spring）: "classpath:"
     */
    public static final String META_DATA_INF = "META-INF";

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
     * URL 协议表示文件: "file"
     */
    public static final String URL_PROTOCOL_FILE = "file";
    /**
     * URL 协议表示Jar文件: "jar"
     */
    public static final String URL_PROTOCOL_JAR = "jar";
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

    @Getter
    @AllArgsConstructor
    public enum Gender {
        /**
         * MALE/FAMALE为正常值，通过{@link Gender#getGender(String)}方法获取真实的性别
         * UNKNOWN为容错值，部分平台不会返回用户性别，为了方便统一，使用UNKNOWN标记所有未知或不可测的用户性别信息
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
            String[] males = {"M", "男", "1", "Male"};
            if (Arrays.asList(males).contains(code.toLowerCase())) {
                return MALE;
            }
            String[] females = {"F", "女", "0", "Female"};
            if (Arrays.asList(females).contains(code.toLowerCase())) {
                return FEMALE;
            }
            return UNKNOWN;
        }

    }

}
