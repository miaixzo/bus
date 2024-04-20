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

import org.miaixz.bus.core.date.DateTime;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Normal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RandomKit {

    /**
     * 获取随机数生成器对象
     * ThreadLocalRandom是JDK 7之后提供并发产生随机数,能够解决多个线程发生的竞争争夺
     *
     * @return {@link ThreadLocalRandom}
     */
    public static ThreadLocalRandom getRandom() {
        return ThreadLocalRandom.current();
    }

    /**
     * 获取随机数产生器
     *
     * @param isSecure 是否为强随机数生成器 (RNG)
     * @return {@link Random}
     * @see #getSecureRandom()
     * @see #getRandom()
     */
    public static Random getRandom(boolean isSecure) {
        return isSecure ? getSecureRandom() : getRandom();
    }

    /**
     * 获取{@link SecureRandom},类提供加密的强随机数生成器 (RNG)
     *
     * @return {@link SecureRandom}
     */
    public static SecureRandom getSecureRandom() {
        return getSecureRandom(null);
    }

    /**
     * 获取SHA1PRNG的{@link SecureRandom}，类提供加密的强随机数生成器 (RNG)
     * 注意：此方法获取的是伪随机序列发生器PRNG（pseudo-random number generator）
     *
     * @param seed 随机数种子
     * @return {@link SecureRandom}
     */
    public static SecureRandom getSecureRandom(byte[] seed) {
        if (null == seed) {
            return new SecureRandom();
        }

        SecureRandom random;
        try {
            random = SecureRandom.getInstance(Algorithm.SHA1PRNG.getValue());
        } catch (NoSuchAlgorithmException e) {
            throw new InternalException(e);
        }
        if (null != seed) {
            random.setSeed(seed);
        }
        return random;
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param min 最小数(包含)
     * @param max 最大数(不包含)
     * @return 随机数
     */
    public static int randomInt(int min, int max) {
        return getRandom().nextInt(min, max);
    }

    /**
     * 获得随机数[0, 1)
     *
     * @return 随机数
     */
    public static int randomInt() {
        return getRandom().nextInt();
    }

    /**
     * 获得指定范围内的随机数 [0,limit)
     *
     * @param limit 限制随机数的范围,不包括这个数
     * @return 随机数
     */
    public static int randomInt(int limit) {
        return getRandom().nextInt(limit);
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param min 最小数(包含)
     * @param max 最大数(不包含)
     * @return 随机数
     */
    public static long randomLong(long min, long max) {
        return getRandom().nextLong(min, max);
    }

    /**
     * 获得随机数
     *
     * @return 随机数
     */
    public static long randomLong() {
        return getRandom().nextLong();
    }

    /**
     * 获得指定范围内的随机数 [0,limit)
     *
     * @param limit 限制随机数的范围,不包括这个数
     * @return 随机数
     */
    public static long randomLong(long limit) {
        return getRandom().nextLong(limit);
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param min 最小数(包含)
     * @param max 最大数(不包含)
     * @return 随机数
     */
    public static double randomDouble(double min, double max) {
        return getRandom().nextDouble(min, max);
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param min          最小数(包含)
     * @param max          最大数(不包含)
     * @param scale        保留小数位数
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 随机数
     */
    public static double randomDouble(double min, double max, int scale, RoundingMode roundingMode) {
        return MathKit.round(randomDouble(min, max), scale, roundingMode).doubleValue();
    }

    /**
     * 获得随机数[0, 1)
     *
     * @return 随机数
     */
    public static double randomDouble() {
        return getRandom().nextDouble();
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param scale        保留小数位数
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 随机数
     */
    public static double randomDouble(int scale, RoundingMode roundingMode) {
        return MathKit.round(randomDouble(), scale, roundingMode).doubleValue();
    }

    /**
     * 获得指定范围内的随机数 [0,limit)
     *
     * @param limit 限制随机数的范围，不包括这个数
     * @return 随机数
     */
    public static double randomDouble(double limit) {
        return getRandom().nextDouble(limit);
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param limit        限制随机数的范围，不包括这个数
     * @param scale        保留小数位数
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 随机数
     */
    public static double randomDouble(double limit, int scale, RoundingMode roundingMode) {
        return MathKit.round(randomDouble(limit), scale, roundingMode).doubleValue();
    }

    /**
     * 获得指定范围内的随机数[0, 1)
     *
     * @return 随机数
     */
    public static BigDecimal randomBigDecimal() {
        return MathKit.toBigDecimal(getRandom().nextDouble());
    }

    /**
     * 获得指定范围内的随机数 [0,limit)
     *
     * @param limit 最大数(不包含)
     * @return 随机数
     */
    public static BigDecimal randomBigDecimal(BigDecimal limit) {
        return MathKit.toBigDecimal(getRandom().nextDouble(limit.doubleValue()));
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param min 最小数(包含)
     * @param max 最大数(不包含)
     * @return 随机数
     */
    public static BigDecimal randomBigDecimal(BigDecimal min, BigDecimal max) {
        return MathKit.toBigDecimal(getRandom().nextDouble(min.doubleValue(), max.doubleValue()));
    }

    /**
     * 随机bytes
     *
     * @param length 长度
     * @return bytes
     */
    public static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        getRandom().nextBytes(bytes);
        return bytes;
    }

    /**
     * 随机获得列表中的元素
     *
     * @param <T>  元素类型
     * @param list 列表
     * @return 随机元素
     */
    public static <T> T randomEle(List<T> list) {
        return randomEle(list, list.size());
    }

    /**
     * 随机获得列表中的元素
     *
     * @param <T>   元素类型
     * @param list  列表
     * @param limit 限制列表的前N项
     * @return 随机元素
     */
    public static <T> T randomEle(List<T> list, int limit) {
        if (list.size() < limit) {
            limit = list.size();
        }
        return list.get(randomInt(limit));
    }

    /**
     * 随机获得数组中的元素
     *
     * @param <T>   元素类型
     * @param array 列表
     * @return 随机元素
     */
    public static <T> T randomEle(T[] array) {
        return randomEle(array, array.length);
    }

    /**
     * 随机获得数组中的元素
     *
     * @param <T>   元素类型
     * @param array 列表
     * @param limit 限制列表的前N项
     * @return 随机元素
     */
    public static <T> T randomEle(T[] array, int limit) {
        return array[randomInt(limit)];
    }

    /**
     * 随机获得列表中的一定量元素
     *
     * @param <T>   元素类型
     * @param list  列表
     * @param count 随机取出的个数
     * @return 随机元素
     */
    public static <T> List<T> randomEles(List<T> list, int count) {
        final List<T> result = new ArrayList<>(count);
        int limit = list.size();
        while (result.size() < count) {
            result.add(randomEle(list, limit));
        }

        return result;
    }

    /**
     * 随机获得列表中的一定量的元素，返回List
     *
     * @param source 列表
     * @param count  随机取出的个数
     * @param <T>    元素类型
     * @return 随机列表
     */
    public static <T> List<T> randomEleList(List<T> source, int count) {
        if (count >= source.size()) {
            return CollKit.toList(source);
        }
        int[] randomList = ArrayKit.sub(randomInts(source.size()), 0, count);
        List<T> result = new ArrayList<>();
        for (int e : randomList) {
            result.add(source.get(e));
        }
        return result;
    }

    /**
     * 随机获得列表中的一定量的不重复元素,返回Set
     *
     * @param <T>        元素类型
     * @param collection 列表
     * @param count      随机取出的个数
     * @return 随机元素
     * @throws IllegalArgumentException 需要的长度大于给定集合非重复总数
     */
    public static <T> Set<T> randomEleSet(Collection<T> collection, int count) {
        ArrayList<T> source = new ArrayList<>(new HashSet<>(collection));
        if (count > source.size()) {
            throw new IllegalArgumentException("Count is larger than collection distinct size !");
        }

        final Set<T> result = new LinkedHashSet<>(count);
        int limit = collection.size();
        while (result.size() < count) {
            result.add(randomEle(source, limit));
        }

        return result;
    }

    /**
     * 获得一个随机的字符串(只包含数字和字符)
     *
     * @param length 字符串的长度
     * @return 随机字符串
     */
    public static String randomString(int length) {
        return randomString(Normal.LOWER_NUMBER, length);
    }

    /**
     * 获得一个随机的字符串
     *
     * @param text   随机字符选取的样本
     * @param length 字符串的长度
     * @return 随机字符串
     */
    public static String randomString(String text, int length) {
        if (StringKit.isEmpty(text)) {
            return Normal.EMPTY;
        }
        if (length < 1) {
            length = 1;
        }

        final StringBuilder sb = new StringBuilder(length);
        int baseLength = text.length();
        for (int i = 0; i < length; i++) {
            int number = randomInt(baseLength);
            sb.append(text.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 获得一个随机的字符串(只包含数字和大写字符)
     *
     * @param length 字符串的长度
     * @return 随机字符串
     */
    public static String randomStringUpper(int length) {
        return randomString(Normal.LOWER_NUMBER, length).toUpperCase();
    }

    /**
     * 获得一个随机的字符串（只包含数字和小写字母） 并排除指定字符串
     *
     * @param length   字符串的长度
     * @param elemData 要排除的字符串,如：去重容易混淆的字符串，oO0、lL1、q9Q、pP，不区分大小写
     * @return 随机字符串
     */
    public static String randomStringWithout(int length, String elemData) {
        String baseStr = Normal.LOWER_NUMBER;
        baseStr = StringKit.removeAll(baseStr, elemData.toLowerCase().toCharArray());
        return randomString(baseStr, length);
    }

    /**
     * 创建指定长度的随机索引
     *
     * @param length 长度
     * @return 随机索引
     */
    public static int[] randomInts(int length) {
        int[] list = ArrayKit.range(length);
        for (int i = 0; i < length; i++) {
            int random = randomInt(i, length);
            ArrayKit.swap(list, i, random);
        }
        return list;
    }

    /**
     * 获得一个只包含数字的字符串
     *
     * @param length 字符串的长度
     * @return 随机字符串
     */
    public static String randomNumbers(int length) {
        return randomString(Normal.NUMBER, length);
    }

    /**
     * 随机数字,数字为0~9单个数字
     *
     * @return 随机数字字符
     */
    public static int randomNumber() {
        return randomChar(Normal.NUMBER);
    }

    /**
     * 随机字母或数字,小写
     *
     * @return 随机字符
     */
    public static char randomChar() {
        return randomChar(Normal.LOWER_NUMBER);
    }

    /**
     * 随机字符
     *
     * @param baseString 随机字符选取的样本
     * @return 随机字符
     */
    public static char randomChar(String baseString) {
        return baseString.charAt(getRandom().nextInt(baseString.length()));
    }

    /**
     * 以当天为基准，随机产生一个日期
     *
     * @param min 偏移最小天，可以为负数表示过去的时间(包含)
     * @param max 偏移最大天，可以为负数表示过去的时间(不包含)
     * @return 随机日期(随机天 ， 其它时间不变)
     */
    public static DateTime randomDay(int min, int max) {
        return randomDate(DateKit.date(), Fields.Type.DAY_OF_YEAR, min, max);
    }

    /**
     * 以给定日期为基准，随机产生一个日期
     *
     * @param baseDate 基准日期
     * @param type     偏移的时间字段，例如时、分、秒等
     * @param min      偏移最小量，可以为负数表示过去的时间(包含)
     * @param max      偏移最大量，可以为负数表示过去的时间(不包含)
     * @return 随机日期
     */
    public static DateTime randomDate(Date baseDate, Fields.Type type, int min, int max) {
        if (null == baseDate) {
            baseDate = DateKit.date();
        }
        return DateKit.offset(baseDate, type, randomInt(min, max));
    }

    /**
     * 获得随机Boolean值
     *
     * @return true or false
     */
    public static boolean randomBoolean() {
        return 0 == randomInt(2);
    }

    /**
     * 随机汉字（'\u4E00'-'\u9FFF'）
     *
     * @return 随机的汉字字符
     */
    public static char randomChinese() {
        return (char) randomInt('\u4E00', '\u9FFF');
    }

}
