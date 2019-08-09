/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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
     * 简体中文形式
     **/
    public static final String[] SIMPLE_DIGITS = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
    /**
     * 繁体中文形式
     **/
    public static final String[] TRADITIONAL_DIGITS = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};

    /**
     * 简体中文单位
     **/
    public static final String[] SIMPLE_UNITS = {"", "十", "百", "千"};
    /**
     * 繁体中文单位
     **/
    public static final String[] TRADITIONAL_UNITS = {"", "拾", "佰", "仟"};

    public static final String[] EN_NUMBER = new String[]{"", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN",
            "EIGHT", "NINE"};
    public static final String[] NUMBER_TEEN = new String[]{"TEN", "ELEVEN", "TWELEVE", "THIRTEEN", "FOURTEEN",
            "FIFTEEN", "SIXTEEN", "SEVENTEEN", "EIGHTEEN", "NINETEEN"};
    public static final String[] NUMBER_TEN = new String[]{"TEN", "TWENTY", "THIRTY", "FORTY", "FIFTY", "SIXTY",
            "SEVENTY", "EIGHTY", "NINETY"};
    public static final String[] NUMBER_MORE = new String[]{"", "THOUSAND", "MILLION", "BILLION"};

    public static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    /**
     * 字符串: 字母和数字
     */
    public static final String LETTER_NUMBER = LETTER + NUMBER;

    public static final String SETTER_PREFIX = "set";

    public static final String GETTER_PREFIX = "get";

}
