/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
package org.aoju.bus.core.toolkit;

import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 拼音工具类
 * 注意：部分拼音并不准确,例如：怡
 *
 * @author Kimi Liu
 * @version 5.9.8
 * @since JDK 1.8+
 */
public class PinyinKit {

    public static final String WITH_OUT_TONE = "WITH_OUT_TONE";
    public static final String WITH_TONE_MARK = "WITH_TONE_MARK";
    public static final String WITH_TONE_NUMBER = "WITH_TONE_NUMBER";
    public static final String ALL_UNMARKED_VOWEL = "aeiouv";
    // 所有带声调的拼音字母
    public static final String ALL_MARKED_VOWEL = "āáǎàēéěèīíǐìōóǒòūúǔùǖǘǚǜ";

    public static final Properties DICT_CHINESE = getDict("chinese");
    public static final Properties DICT_PINYIN = getDict("pinyin");
    public static final Properties DICT_POLYPHONE = getDict("polyphone");

    /**
     * 将单个汉字转换为相应格式的拼音
     *
     * @param c      需要转换成拼音的汉字
     * @param format 拼音格式
     *               WITH_TONE_NUMBER 数字声调
     *               WITH_OUT_TONE 不带声调
     *               WITH_TONE_MARK 带声调
     * @return 汉字的拼音
     */
    public static String[] convert(char c, String format) {
        String pinyin = DICT_PINYIN.getProperty(String.valueOf(c));
        if ((pinyin != null) && (!pinyin.equals(Normal.NULL))) {
            return format(pinyin, format);
        }
        return null;
    }

    /**
     * 将单个汉字转换成带声调格式的拼音
     *
     * @param c 需要转换成拼音的汉字
     * @return 字符串的拼音
     */
    public static String[] convert(char c) {
        return convert(c, WITH_TONE_MARK);
    }

    /**
     * 将字符串转换成相应格式的拼音
     *
     * @param str       需要转换的字符串
     * @param separator 拼音分隔符
     * @param format    拼音格式
     *                  WITH_TONE_NUMBER 数字声调
     *                  WITH_OUT_TONE 不带声调
     *                  WITH_TONE_MARK 带声调
     * @return 字符串的拼音
     */
    public static String convert(String str, String separator, String format) {
        str = convertSimplified(str);
        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = str.length(); i < len; i++) {
            char c = str.charAt(i);

            if (isChinese(c) || c == '〇') {
                boolean isFoundFlag = false;
                int rightMove = 3;

                for (int rightIndex = (i + rightMove) < len ? (i + rightMove) : (len - 1); rightIndex > i; rightIndex--) {
                    String cizu = str.substring(i, rightIndex + 1);
                    if (DICT_POLYPHONE.containsKey(cizu)) {
                        String[] pinyinArray = format(DICT_POLYPHONE.getProperty(cizu), format);
                        for (int j = 0, l = pinyinArray.length; j < l; j++) {
                            sb.append(pinyinArray[j]);
                            if (j < l - 1) {
                                sb.append(separator);
                            }
                        }
                        i = rightIndex;
                        isFoundFlag = true;
                        break;
                    }
                }
                if (!isFoundFlag) {
                    String[] pinyinArray = convert(str.charAt(i), format);
                    if (pinyinArray != null) {
                        sb.append(pinyinArray[0]);
                    } else {
                        sb.append(str.charAt(i));
                    }
                }
                if (i < len - 1) {
                    sb.append(separator);
                }
            } else {
                sb.append(c);
                if ((i + 1) < len && isChinese(str.charAt(i + 1))) {
                    sb.append(separator);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 将字符串转换成带声调格式的拼音
     *
     * @param str       需要转换的字符串
     * @param separator 拼音分隔符
     * @return 转换后带声调的拼音
     */
    public static String convert(String str, String separator) {
        return convert(str, separator, WITH_TONE_MARK);
    }

    /**
     * 获取字符串对应拼音的首字母
     *
     * @param str 需要转换的字符串
     * @return 对应拼音的首字母
     */
    public static String convert(String str) {
        String separator = Symbol.SHAPE;
        StringBuilder sb = new StringBuilder();

        char[] charArray = new char[str.length()];
        for (int i = 0, len = str.length(); i < len; i++) {
            char c = str.charAt(i);

            if (!isChinese(c) && c != '〇') {
                charArray[i] = c;
            } else {
                int j = i + 1;
                sb.append(c);

                while (j < len && (isChinese(str.charAt(j)) || str.charAt(j) == '〇')) {
                    sb.append(str.charAt(j));
                    j++;
                }
                String hanziPinyin = convert(sb.toString(), separator, WITH_OUT_TONE);
                String[] pinyinArray = hanziPinyin.split(separator);
                for (String string : pinyinArray) {
                    charArray[i] = string.charAt(0);
                    i++;
                }
                i--;
                sb.delete(0, sb.toString().length());
                sb.trimToSize();
            }
        }
        return String.valueOf(charArray);
    }

    /**
     * 将繁体字转换为简体字
     *
     * @param str 需要转换的繁体字
     * @return 转换后的简体体
     */
    public static String convertSimplified(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = str.length(); i < len; i++) {
            char c = str.charAt(i);
            sb.append(convertSimplified(c));
        }
        return sb.toString();
    }

    /**
     * 将单个繁体字转换为简体字
     *
     * @param c 需要转换的繁体字
     * @return 转换后的简体字
     */
    public static char convertSimplified(char c) {
        if (isTraditional(c)) {
            return DICT_CHINESE.getProperty(String.valueOf(c)).charAt(0);
        }
        return c;
    }

    /**
     * 将简体字转换为繁体字
     *
     * @param str 需要转换的简体字
     * @return 转换后的繁字体
     */
    public static String convertTraditional(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = str.length(); i < len; i++) {
            char c = str.charAt(i);
            sb.append(convertTraditional(c));
        }
        return sb.toString();
    }

    /**
     * 将单个简体字转换为繁体字
     *
     * @param c 需要转换的简体字
     * @return 转换后的繁字体
     */
    public static char convertTraditional(char c) {
        String hanzi = String.valueOf(c);
        if (DICT_CHINESE.containsValue(hanzi)) {
            Iterator<Map.Entry<Object, Object>> itr = DICT_CHINESE.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<Object, Object> e = itr.next();
                if (e.getValue().toString().equals(hanzi)) {
                    return e.getKey().toString().charAt(0);
                }
            }
        }
        return c;
    }

    /**
     * 将带声调格式的拼音转换为数字代表声调格式的拼音
     *
     * @param array 带声调格式的拼音
     * @return 数字代表声调格式的拼音
     */
    private static String[] convertWithTone(String array) {
        String[] pinyinArray = array.split(Symbol.COMMA);
        for (int i = pinyinArray.length - 1; i >= 0; i--) {
            boolean hasMarkedChar = false;
            String originalPinyin = pinyinArray[i].replaceAll("ü", "v");

            for (int j = originalPinyin.length() - 1; j >= 0; j--) {
                char originalChar = originalPinyin.charAt(j);

                if (originalChar < 'a' || originalChar > 'z') {
                    int indexInAllMarked = ALL_MARKED_VOWEL.indexOf(originalChar);
                    int toneNumber = indexInAllMarked % 4 + 1;
                    char replaceChar = ALL_UNMARKED_VOWEL.charAt(((indexInAllMarked - indexInAllMarked % 4)) / 4);
                    pinyinArray[i] = originalPinyin.replaceAll(String.valueOf(originalChar), String.valueOf(replaceChar)) + toneNumber;
                    hasMarkedChar = true;
                    break;
                }
            }
            if (!hasMarkedChar) {
                pinyinArray[i] = originalPinyin + Symbol.FIVE;
            }
        }

        return pinyinArray;
    }

    /**
     * 将带声调格式的拼音转换为不带声调格式的拼音
     *
     * @param array 带声调格式的拼音
     * @return 不带声调的拼音
     */
    private static String[] convertWithoutTone(String array) {
        String[] pinyinArray;
        for (int i = ALL_MARKED_VOWEL.length() - 1; i >= 0; i--) {
            char originalChar = ALL_MARKED_VOWEL.charAt(i);
            char replaceChar = ALL_UNMARKED_VOWEL.charAt(((i - i % 4)) / 4);
            array = array.replaceAll(String.valueOf(originalChar), String.valueOf(replaceChar));
        }
        pinyinArray = array.replaceAll("ü", "v").split(Symbol.COMMA);

        Set<String> pinyinSet = new LinkedHashSet<>();
        for (String pinyin : pinyinArray) {
            pinyinSet.add(pinyin);
        }

        return pinyinSet.toArray(new String[pinyinSet.size()]);
    }

    /**
     * 将带声调的拼音格式化为相应格式的拼音
     *
     * @param str    带声调的拼音
     * @param format 拼音格式：WITH_TONE_NUMBER--数字代表声调,WITHOUT_TONE--不带声调,WITH_TONE_MARK--带声调
     * @return 格式转换后的拼音
     */
    private static String[] format(String str, String format) {
        if (WITH_TONE_MARK.equals(format)) {
            return str.split(Symbol.COMMA);
        } else if (WITH_TONE_NUMBER.equals(format)) {
            return convertWithTone(str);
        } else if (WITH_OUT_TONE.equals(format)) {
            return convertWithoutTone(str);
        }
        return null;
    }

    /**
     * 判断某个字符是否为繁体字
     *
     * @param c 需要判断的字符
     * @return 是繁体字返回true, 否则返回false
     */
    public static boolean isTraditional(char c) {
        return DICT_CHINESE.containsKey(String.valueOf(c));
    }

    /**
     * 判断某个字符是否为汉字
     *
     * @param c 需要判断的字符
     * @return 是汉字返回true, 否则返回false
     */
    public static boolean isChinese(String c) {
        String regex = "[\\u4e00-\\u9fa5]";
        return String.valueOf(c).matches(regex);
    }

    /**
     * 判断某个字符是否为汉字
     *
     * @param c 需要判断的字符
     * @return 是汉字返回true, 否则返回false
     */
    public static boolean isChinese(char c) {
        String regex = "[\\u4e00-\\u9fa5]";
        return String.valueOf(c).matches(regex);
    }

    /**
     * 判断一个汉字是否为多音字
     *
     * @param c 汉字
     * @return 判断结果, 是汉字返回true, 否则返回false
     */
    public static boolean isPolyphone(char c) {
        String[] pinyinArray = convert(c);
        return pinyinArray != null && pinyinArray.length > 1;
    }

    /**
     * 获取汉字对应的ascii码
     *
     * @param chs 汉字
     * @return ascii码
     */
    private static int getChsAscii(String chs) {
        int asc;
        byte[] bytes = chs.getBytes(Charset.GBK);
        switch (bytes.length) {
            case 1:
                // 英文字符
                asc = bytes[0];
                break;
            case 2:
                // 中文字符
                int hightByte = 256 + bytes[0];
                int lowByte = 256 + bytes[1];
                asc = (256 * hightByte + lowByte) - 256 * 256;
                break;
            default:
                throw new InstrumentException("Illegal resource string");
        }
        return asc;
    }

    private static Properties getDict(String type) {
        Properties p = new Properties();
        try {
            String path = Symbol.SLASH + Normal.META_DATA_INF + "/template/" + type + ".dict";
            InputStream is = PinyinKit.class.getResourceAsStream(path);
            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split(Symbol.EQUAL);
                p.setProperty(tokens[0], tokens[1]);
            }
            br.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return p;
    }

}
