/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.toolkit;

import lombok.Data;
import org.miaixz.bus.core.lang.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 身份证相关工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CitizenIdKit {

    /**
     * 中国公民身份证号码最小长度
     */
    private static final int CHINA_ID_MIN_LENGTH = 15;
    /**
     * 中国公民身份证号码最大长度
     */
    private static final int CHINA_ID_MAX_LENGTH = 18;
    /**
     * 每位加权因子
     */
    private static final int[] WEIGHTING = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    /**
     * 身份证对应区域代码表
     */
    private static final Map<String, String> AREA_CODE = new HashMap<>();
    /**
     * 中国台湾身份证首字母对应数字
     */
    private static final Map<Character, Integer> AREA_TW_CODE = new HashMap<>();

    static {
        AREA_CODE.put("11", "北京");
        AREA_CODE.put("12", "天津");
        AREA_CODE.put("13", "河北");
        AREA_CODE.put("14", "山西");
        AREA_CODE.put("15", "内蒙古");
        AREA_CODE.put("21", "辽宁");
        AREA_CODE.put("22", "吉林");
        AREA_CODE.put("23", "黑龙江");
        AREA_CODE.put("31", "上海");
        AREA_CODE.put("32", "江苏");
        AREA_CODE.put("33", "浙江");
        AREA_CODE.put("34", "安徽");
        AREA_CODE.put("35", "福建");
        AREA_CODE.put("36", "江西");
        AREA_CODE.put("37", "山东");
        AREA_CODE.put("41", "河南");
        AREA_CODE.put("42", "湖北");
        AREA_CODE.put("43", "湖南");
        AREA_CODE.put("44", "广东");
        AREA_CODE.put("45", "广西");
        AREA_CODE.put("46", "海南");
        AREA_CODE.put("50", "重庆");
        AREA_CODE.put("51", "四川");
        AREA_CODE.put("52", "贵州");
        AREA_CODE.put("53", "云南");
        AREA_CODE.put("54", "西藏");
        AREA_CODE.put("61", "陕西");
        AREA_CODE.put("62", "甘肃");
        AREA_CODE.put("63", "青海");
        AREA_CODE.put("64", "宁夏");
        AREA_CODE.put("65", "新疆");
        AREA_CODE.put("71", "台湾");
        AREA_CODE.put("81", "香港");
        AREA_CODE.put("82", "澳门");
        AREA_CODE.put("83", "台湾");
        AREA_CODE.put("91", "国外");

        AREA_TW_CODE.put('A', 10);
        AREA_TW_CODE.put('B', 11);
        AREA_TW_CODE.put('C', 12);
        AREA_TW_CODE.put('D', 13);
        AREA_TW_CODE.put('E', 14);
        AREA_TW_CODE.put('F', 15);
        AREA_TW_CODE.put('G', 16);
        AREA_TW_CODE.put('H', 17);
        AREA_TW_CODE.put('J', 18);
        AREA_TW_CODE.put('K', 19);
        AREA_TW_CODE.put('L', 20);
        AREA_TW_CODE.put('M', 21);
        AREA_TW_CODE.put('N', 22);
        AREA_TW_CODE.put('P', 23);
        AREA_TW_CODE.put('Q', 24);
        AREA_TW_CODE.put('R', 25);
        AREA_TW_CODE.put('S', 26);
        AREA_TW_CODE.put('T', 27);
        AREA_TW_CODE.put('U', 28);
        AREA_TW_CODE.put('V', 29);
        AREA_TW_CODE.put('X', 30);
        AREA_TW_CODE.put('Y', 31);
        AREA_TW_CODE.put('W', 32);
        AREA_TW_CODE.put('Z', 33);
        AREA_TW_CODE.put('I', 34);
        AREA_TW_CODE.put('O', 35);
    }

    /**
     * 是否有效身份证号，忽略X的大小写
     * 如果身份证号码中含有空格始终返回{@code false}
     *
     * @param idcard 身份证号,支持18位、15位和港澳台的10位
     * @return 是否有效
     */
    public static boolean isValidCard(String idcard) {
        if (StringKit.isBlank(idcard)) {
            return false;
        }
        int length = idcard.length();
        switch (length) {
            case 18:// 18位身份证
                return isValidCard18(idcard);
            case 15:// 15位身份证
                return isValidCard15(idcard);
            case 10: {// 10位身份证，港澳台地区
                String[] cardVal = isValidCard10(idcard);
                return null != cardVal && "true".equals(cardVal[2]);
            }
            default:
                return false;
        }
    }

    /**
     * 判断18位身份证的合法性
     * 根据〖中华人民共和国国家标准GB11643-1999〗中有关公民身份号码的规定,公民身份号码是特征组合码,由十七位数字本体码和一位数字校验码组成
     * 排列顺序从左至右依次为：六位数字地址码,八位数字出生日期码,三位数字顺序码和一位数字校验码
     * 顺序码: 表示在同一地址码所标识的区域范围内,对同年、同月、同 日出生的人编定的顺序号,顺序码的奇数分配给男性,偶数分配 给女性
     * <ol>
     * 	     <li>第1、2位数字表示：所在省份的代码</li>
     * 	     <li>第3、4位数字表示：所在城市的代码</li>
     * 	     <li>第5、6位数字表示：所在区县的代码</li>
     * 	     <li>第7~14位数字表示：出生年、月、日</li>
     * 	     <li>第15、16位数字表示：所在地的派出所的代码</li>
     * 	     <li>第17位数字表示性别：奇数表示男性,偶数表示女性</li>
     * 	     <li>第18位数字是校检码,用来检验身份证的正确性 校检码可以是0~9的数字,有时也用x表示</li>
     * </ol>
     * 第十八位数字(校验码)的计算方法为：
     * <ol>
     * 	     <li>将前面的身份证号码17位数分别乘以不同的系数 从第一位到第十七位的系数分别为：7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2</li>
     * 	     <li>将这17位数字和系数相乘的结果相加</li>
     * 	     <li>用加出来和除以11,看余数是多少</li>
     * 	     <li>余数只可能有0 1 2 3 4 5 6 7 8 9 10这11个数字 其分别对应的最后一位身份证的号码为1 0 X 9 8 7 6 5 4 3 2</li>
     * 	     <li>通过上面得知如果余数是2,就会在身份证的第18位数字上出现罗马数字的Ⅹ 如果余数是10,身份证的最后一位号码就是2</li>
     * </ol>
     * <ol>
     * 	     <li>香港人在大陆的身份证，【810000】开头；同样可以直接获取到 性别、出生日期</li>
     * 	     <li>81000019980902013X: 文绎循 男 1998-09-02</li>
     * 	     <li>810000201011210153: 辛烨 男 2010-11-21</li>
     * </ol>
     * 	 <ol>
     *       <li>澳门人在大陆的身份证，【820000】开头；同样可以直接获取到 性别、出生日期</li>
     *       <li>820000200009100032: 黄敬杰 男 2000-09-10</li>
     *  </ol>
     *  <ol>
     *     <li>台湾人在大陆的身份证，【830000】开头；同样可以直接获取到 性别、出生日期</li>
     *     <li>830000200209060065: 王宜妃 女 2002-09-06</li>
     *     <li>830000194609150010: 苏建文 男 1946-09-14</li>
     *     <li>83000019810715006X: 刁婉琇 女 1981-07-15</li>
     * </ol>
     *
     * @param idcard 待验证的身份证
     * @return 是否有效的18位身份证，忽略x的大小写
     */
    public static boolean isValidCard18(String idcard) {
        return isValidCard18(idcard, true);
    }

    /**
     * <p>
     * 判断18位身份证的合法性
     * </p>
     * 根据〖中华人民共和国国家标准GB11643-1999〗中有关公民身份号码的规定，公民身份号码是特征组合码，由十七位数字本体码和一位数字校验码组成
     * 排列顺序从左至右依次为：六位数字地址码，八位数字出生日期码，三位数字顺序码和一位数字校验码。
     * <p>
     * 顺序码: 表示在同一地址码所标识的区域范围内，对同年、同月、同 日出生的人编定的顺序号，顺序码的奇数分配给男性，偶数分配 给女性。
     * </p>
     * <ol>
     * <li>第1、2位数字表示：所在省份的代码</li>
     * <li>第3、4位数字表示：所在城市的代码</li>
     * <li>第5、6位数字表示：所在区县的代码</li>
     * <li>第7~14位数字表示：出生年、月、日</li>
     * <li>第15、16位数字表示：所在地的派出所的代码</li>
     * <li>第17位数字表示性别：奇数表示男性，偶数表示女性</li>
     * <li>第18位数字是校检码，用来检验身份证的正确性。校检码可以是0~9的数字，有时也用x表示</li>
     * </ol>
     * <p>
     * 第十八位数字(校验码)的计算方法为：
     * <ol>
     * <li>将前面的身份证号码17位数分别乘以不同的系数。从第一位到第十七位的系数分别为：7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2</li>
     * <li>将这17位数字和系数相乘的结果相加</li>
     * <li>用加出来和除以11，看余数是多少</li>
     * <li>余数只可能有0 1 2 3 4 5 6 7 8 9 10这11个数字。其分别对应的最后一位身份证的号码为1 0 X 9 8 7 6 5 4 3 2</li>
     * <li>通过上面得知如果余数是2，就会在身份证的第18位数字上出现罗马数字的Ⅹ。如果余数是10，身份证的最后一位号码就是2</li>
     * </ol>
     *
     * @param idcard     待验证的身份证
     * @param ignoreCase 是否忽略大小写
     * @return 是否有效的18位身份证
     */
    public static boolean isValidCard18(String idcard, boolean ignoreCase) {
        if (StringKit.isBlank(idcard) || CHINA_ID_MAX_LENGTH != idcard.length()) {
            return false;
        }

        // 省份
        final String proCode = idcard.substring(0, 2);
        if (null == AREA_CODE.get(proCode)) {
            return false;
        }

        //校验生日
        if (false == Validator.isBirthday(idcard.substring(6, 14))) {
            return false;
        }

        // 前17位
        final String code17 = idcard.substring(0, 17);
        if (PatternKit.isMatch(RegEx.NUMBERS, code17)) {
            // 获取校验位
            char val = getCheckCode18(code17);
            // 第18位
            return CharsKit.equals(val, idcard.charAt(17), ignoreCase);
        }
        return false;
    }

    /**
     * 验证15位身份编码是否合法
     *
     * @param idcard 身份编码
     * @return 是否合法
     */
    public static boolean isValidCard15(String idcard) {
        if (StringKit.isBlank(idcard) || CHINA_ID_MIN_LENGTH != idcard.length()) {
            return false;
        }
        if (PatternKit.isMatch(RegEx.NUMBERS, idcard)) {
            // 省份
            String proCode = idcard.substring(0, 2);
            if (null == AREA_CODE.get(proCode)) {
                return false;
            }

            //校验生日(两位年份，补充为19XX)
            return false != Validator.isBirthday("19" + idcard.substring(6, 12));
        } else {
            return false;
        }
    }

    /**
     * 验证10位身份编码是否合法
     *
     * @param idcard 身份编码
     * @return 身份证信息数组
     * <p>
     * [0] - 台湾、澳门、香港 [1] - 性别(男M,女F,未知N) [2] - 是否合法(合法true,不合法false) 若不是身份证件号码则返回null
     * </p>
     */
    public static String[] isValidCard10(String idcard) {
        if (StringKit.isBlank(idcard)) {
            return null;
        }
        String[] info = new String[3];
        String card = idcard.replaceAll("[()]", Normal.EMPTY);
        if (card.length() != 8 && card.length() != 9 && idcard.length() != 10) {
            return null;
        }
        // 台湾
        if (idcard.matches("^[a-zA-Z][0-9]{9}$")) {
            info[0] = "台湾";
            char char2 = idcard.charAt(1);
            if (Symbol.C_ONE == char2) {
                info[1] = "M";
            } else if (Symbol.C_TWO == char2) {
                info[1] = "F";
            } else {
                info[1] = "N";
                info[2] = "false";
                return info;
            }
            info[2] = isValidTWCard(idcard) ? "true" : "false";
        } else if (idcard.matches("^[157][0-9]{6}\\(?[0-9A-Z]\\)?$")) {
            info[0] = "澳门";
            info[1] = "N";
            info[2] = "true";
        } else if (idcard.matches("^[A-Z]{1,2}[0-9]{6}\\(?[0-9A]\\)?$")) {
            info[0] = "香港";
            info[1] = "N";
            info[2] = isValidHKCard(idcard) ? "true" : "false";
        } else {
            return null;
        }
        return info;
    }

    /**
     * 验证台湾身份证号码
     *
     * @param idcard 身份证号码
     * @return 验证码是否符合
     */
    public static boolean isValidTWCard(String idcard) {
        if (null == idcard || idcard.length() != 10) {
            return false;
        }
        final Integer iStart = AREA_TW_CODE.get(idcard.charAt(0));
        if (null == iStart) {
            return false;
        }
        int sum = iStart / 10 + (iStart % 10) * 9;

        final String mid = idcard.substring(1, 9);
        final char[] chars = mid.toCharArray();
        int iflag = 8;
        for (char c : chars) {
            sum += Integer.parseInt(String.valueOf(c)) * iflag;
            iflag--;
        }

        final String end = idcard.substring(9, 10);
        return (sum % 10 == 0 ? 0 : (10 - sum % 10)) == Integer.parseInt(end);
    }

    /**
     * 验证香港身份证号码(存在Bug，部份特殊身份证无法检查)
     * <p>
     * 身份证前2位为英文字符，如果只出现一个英文字符则表示第一位是空格，对应数字58 前2位英文字符A-Z分别对应数字10-35 最后一位校验码为0-9的数字加上字符"A"，"A"代表10
     * </p>
     * <p>
     * 将身份证号码全部转换为数字，分别对应乘9-1相加的总和，整除11则证件号码有效
     * </p>
     *
     * @param idcard 身份证号码
     * @return 验证码是否符合
     */
    public static boolean isValidHKCard(String idcard) {
        if (StringKit.isBlank(idcard)) {
            return false;
        }
        String card = idcard.replaceAll("[()]", Normal.EMPTY);
        int sum;
        if (card.length() == 9) {
            sum = (Character.toUpperCase(card.charAt(0)) - 55) * 9 + (Character.toUpperCase(card.charAt(1)) - 55) * 8;
            card = card.substring(1, 9);
        } else {
            sum = 522 + (Character.toUpperCase(card.charAt(0)) - 55) * 8;
        }

        // 首字母A-Z，A表示1，以此类推
        String mid = card.substring(1, 7);
        String end = card.substring(7, 8);
        char[] chars = mid.toCharArray();
        int iflag = 7;
        for (char c : chars) {
            sum = sum + Integer.parseInt(String.valueOf(c)) * iflag;
            iflag--;
        }
        if ("A".equalsIgnoreCase(end)) {
            sum += 10;
        } else {
            sum += Integer.parseInt(end);
        }
        return sum % 11 == 0;
    }

    /**
     * 根据身份编号获取生日,只支持15或18位身份证号码
     *
     * @param idcard 身份编号
     * @return 生日(yyyyMMdd)
     */
    public static String getBirthByIdCard(String idcard) {
        Assert.notBlank(idcard, "id card must be not blank!");
        final int len = idcard.length();
        if (len < CHINA_ID_MIN_LENGTH) {
            return null;
        } else if (len == CHINA_ID_MIN_LENGTH) {
            idcard = getIdCardTo18(idcard);
        }
        return Objects.requireNonNull(idcard).substring(6, 14);
    }

    /**
     * 根据身份编号获取年龄,只支持15或18位身份证号码
     *
     * @param idcard 身份编号
     * @return 年龄
     */
    public static int getAgeByIdCard(String idcard) {
        return getAgeByIdCard(idcard, DateKit.date());
    }

    /**
     * 根据身份编号获取指定日期当时的年龄,只支持15或18位身份证号码
     *
     * @param idcard        身份编号
     * @param dateToCompare 以此日期为界,计算年龄
     * @return 年龄
     */
    public static int getAgeByIdCard(String idcard, Date dateToCompare) {
        return DateKit.getAge(DateKit.parse(getBirthByIdCard(idcard), Fields.PURE_DATE_PATTERN), dateToCompare);
    }

    /**
     * 根据身份编号获取生日年,只支持15或18位身份证号码
     *
     * @param idcard 身份编号
     * @return 生日(yyyy)
     */
    public static Short getYearByIdCard(String idcard) {
        final int len = idcard.length();
        if (len < CHINA_ID_MIN_LENGTH) {
            return null;
        } else if (len == CHINA_ID_MIN_LENGTH) {
            idcard = getIdCardTo18(idcard);
        }
        return Short.valueOf(Objects.requireNonNull(idcard).substring(6, 10));
    }

    /**
     * 根据身份编号获取生日月,只支持15或18位身份证号码
     *
     * @param idcard 身份编号
     * @return 生日(MM)
     */
    public static Short getMonthByIdCard(String idcard) {
        final int len = idcard.length();
        if (len < CHINA_ID_MIN_LENGTH) {
            return null;
        } else if (len == CHINA_ID_MIN_LENGTH) {
            idcard = getIdCardTo18(idcard);
        }
        return Short.valueOf(Objects.requireNonNull(idcard).substring(10, 12));
    }

    /**
     * 根据身份编号获取生日天，只支持15或18位身份证号码
     *
     * @param idcard 身份编号
     * @return 生日(dd)
     */
    public static Short getDayByIdCard(String idcard) {
        final int len = idcard.length();
        if (len < CHINA_ID_MIN_LENGTH) {
            return null;
        } else if (len == CHINA_ID_MIN_LENGTH) {
            idcard = getIdCardTo18(idcard);
        }
        return Short.valueOf(Objects.requireNonNull(idcard).substring(12, 14));
    }

    /**
     * 根据身份编号获取性别,只支持15或18位身份证号码
     *
     * @param idcard 身份编号
     * @return 性别(1 : 男, 0 : 女)
     */
    public static int getGenderByIdCard(String idcard) {
        Assert.notBlank(idcard);
        final int len = idcard.length();
        if (len < CHINA_ID_MIN_LENGTH) {
            throw new IllegalArgumentException("ID Card length must be 15 or 18");
        }

        if (len == CHINA_ID_MIN_LENGTH) {
            idcard = getIdCardTo18(idcard);
        }
        char sCardChar = Objects.requireNonNull(idcard).charAt(Normal._16);
        return (sCardChar % 2 != 0) ? 1 : 0;
    }

    /**
     * 根据身份编号获取户籍省份，只支持15或18位身份证号码
     *
     * @param idcard 身份证号
     * @return 省份名称, 省份编码 采用','分割
     */
    public static String getProvinceByIdCard(String idcard) {
        int len = idcard.length();
        if (len == CHINA_ID_MIN_LENGTH || len == CHINA_ID_MAX_LENGTH) {
            String province = idcard.substring(0, 2);
            return AREA_CODE.get(province) + Symbol.COMMA + idcard.substring(0, 5);
        }
        return null;
    }

    /**
     * 根据身份编号获取地市级编码，只支持15或18位身份证号码
     * 获取编码为4位
     *
     * @param idcard 身份编码
     * @return 地市级编码
     */
    public static String getCityCodeByIdCard(String idcard) {
        int len = idcard.length();
        if (len == CHINA_ID_MIN_LENGTH || len == CHINA_ID_MAX_LENGTH) {
            return idcard.substring(0, 4);
        }
        return null;
    }

    /**
     * 根据身份编号获取区县级编码，只支持15或18位身份证号码
     * 获取编码为6位
     *
     * @param idcard 身份编码
     * @return 地市级编码
     */
    public static String getDistrictCodeByIdCard(String idcard) {
        int len = idcard.length();
        if (len == CHINA_ID_MIN_LENGTH || len == CHINA_ID_MAX_LENGTH) {
            return idcard.substring(0, 6);
        }
        return null;
    }

    /**
     * 隐藏指定位置的几个身份证号数字为“*”
     *
     * @param idcard       身份证号
     * @param startInclude 开始位置(包含)
     * @param endExclude   结束位置(不包含)
     * @return 隐藏后的身份证号码
     * @see StringKit#hide(CharSequence, int, int)
     */
    public static String hide(String idcard, int startInclude, int endExclude) {
        return StringKit.hide(idcard, startInclude, endExclude);
    }

    /**
     * 将15位身份证号码转换为18位
     *
     * @param idcard 15位身份编码
     * @return 18位身份编码
     */
    public static String getIdCardTo18(String idcard) {
        StringBuilder idcard18;
        if (idcard.length() != CHINA_ID_MIN_LENGTH) {
            return null;
        }
        if (PatternKit.isMatch(RegEx.NUMBERS, idcard)) {
            // 获取出生年月日
            String birthday = idcard.substring(6, 12);
            Date birthDate = DateKit.parse(birthday, "yyMMdd");
            // 获取出生年(完全表现形式,如：2010)
            int sYear = DateKit.getYear(birthDate);
            if (sYear > 2000) {
                // 2000年之后不存在15位身份证号,此处用于修复此问题的判断
                sYear -= 100;
            }
            idcard18 = StringKit.builder().append(idcard, 0, 6).append(sYear).append(idcard.substring(8));
            // 获取校验位
            char sVal = getCheckCode18(idcard18.toString());
            idcard18.append(sVal);
        } else {
            return null;
        }
        return idcard18.toString();
    }

    /**
     * 获得18位身份证校验码
     *
     * @param code17 18位身份证号中的前17位
     * @return 第18位
     */
    private static char getCheckCode18(String code17) {
        int sum = getPowerSum(code17.toCharArray());
        return getCheckCode18(sum);
    }

    /**
     * 将power和值与11取模获得余数进行校验码判断
     *
     * @param iSum 加权和
     * @return 校验位
     */
    private static char getCheckCode18(int iSum) {
        switch (iSum % 11) {
            case 10:
                return Symbol.C_TWO;
            case 9:
                return Symbol.C_THREE;
            case 8:
                return Symbol.C_FOUR;
            case 7:
                return Symbol.C_FIVE;
            case 6:
                return Symbol.C_SIX;
            case 5:
                return Symbol.C_SEVEN;
            case 4:
                return Symbol.C_EIGHT;
            case 3:
                return Symbol.C_NINE;
            case 2:
                return 'X';
            case 1:
                return Symbol.C_ZERO;
            case 0:
                return Symbol.C_ONE;
            default:
                return Symbol.C_SPACE;
        }
    }

    /**
     * 将身份证的每位和对应位的加权因子相乘之后,再得到和值
     *
     * @param iArr 身份证号码的数组
     * @return 身份证编码
     */
    private static int getPowerSum(char[] iArr) {
        int iSum = 0;
        if (WEIGHTING.length == iArr.length) {
            for (int i = 0; i < iArr.length; i++) {
                iSum += Integer.parseInt(String.valueOf(iArr[i])) * WEIGHTING[i];
            }
        }
        return iSum;
    }

    /**
     * 获取公民身份相关信息
     *
     * @param idcard 身份号码
     * @return 身份信息
     */
    public CitizenInfo getCitizenInfo(String idcard) {
        CitizenInfo citizenInfo = new CitizenInfo();
        if (isValidCard(idcard)) {
            short year = getYearByIdCard(idcard);
            short month = getMonthByIdCard(idcard);
            short day = getDayByIdCard(idcard);
            citizenInfo.setVerify(isValidCard(idcard));
            citizenInfo.setBirthDate(getBirthByIdCard(idcard));
            citizenInfo.setAge(getAgeByIdCard(idcard));
            citizenInfo.setProvince(getProvinceByIdCard(idcard));
            citizenInfo.setBirthYear(year);
            citizenInfo.setBirthMonth(month);
            citizenInfo.setBirthDay(day);
            citizenInfo.setGender(Gender.of(Normal.EMPTY + getGenderByIdCard(idcard)).getDesc());
            citizenInfo.setZodiac(DateKit.getZodiac(month, day));
            citizenInfo.setAnimal(DateKit.getAnimal(year));
        } else {
            citizenInfo.setVerify(false);
        }
        return citizenInfo;
    }

    @Data
    public class CitizenInfo {

        /**
         * 身份证真伪
         */
        private boolean verify;
        /**
         * 出生日期
         */
        private String birthDate;
        /**
         * 出生省份
         */
        private String province;
        /**
         * 出生省份
         */
        private String cityCode;
        /**
         * 出生年
         */
        private Short birthYear;
        /**
         * 出生月
         */
        private Short birthMonth;
        /**
         * 出生日
         */
        private Short birthDay;
        /**
         * 年龄
         */
        private Integer age;
        /**
         * 性别
         */
        private String gender;
        /**
         * 星座
         */
        private String zodiac;
        /**
         * 生肖
         */
        private String animal;

    }

}
