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

import org.miaixz.bus.core.center.date.DateTime;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.selector.WeightObject;
import org.miaixz.bus.core.lang.selector.WeightRandomSelector;

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
     * 用于随机选的字符和数字（包括大写和小写字母）
     */
    public static final String BASE_CHAR_NUMBER = Normal.ALPHABET.toUpperCase() + Normal.LOWER_ALPHABET_NUMBER;

    /**
     * 获取随机数生成器对象
     * ThreadLocalRandom是JDK 7之后提供并发产生随机数，能够解决多个线程发生的竞争争夺。
     *
     * <p>
     * 注意：此方法返回的{@link ThreadLocalRandom}不可以在多线程环境下共享对象，否则有重复随机数问题。
     * 见：<a href="https://www.jianshu.com/p/89dfe990295c">https://www.jianshu.com/p/89dfe990295c</a>
     * </p>
     *
     * @return {@link ThreadLocalRandom}
     */
    public static ThreadLocalRandom getRandom() {
        return ThreadLocalRandom.current();
    }

    /**
     * 创建{@link SecureRandom}，类提供加密的强随机数生成器 (RNG)
     *
     * @param seed 自定义随机种子
     * @return {@link SecureRandom}
     */
    public static SecureRandom createSecureRandom(final byte[] seed) {
        return (null == seed) ? new SecureRandom() : new SecureRandom(seed);
    }

    /**
     * 获取SHA1PRNG的{@link SecureRandom}，类提供加密的强随机数生成器 (RNG)
     * 注意：此方法获取的是伪随机序列发生器PRNG（pseudo-random number generator）
     *
     * <p>
     * 相关说明见：<a href="https://stackoverflow.com/questions/137212/how-to-solve-slow-java-securerandom">how-to-solve-slow-java-securerandom</a>
     * </p>
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
     * <p>
     * 相关说明见：<a href="https://stackoverflow.com/questions/137212/how-to-solve-slow-java-securerandom">how-to-solve-slow-java-securerandom</a>
     *
     * @param seed 随机数种子
     * @return {@link SecureRandom}
     * @see #createSecureRandom(byte[])
     */
    public static SecureRandom getSecureRandom(final byte[] seed) {
        return createSecureRandom(seed);
    }

    /**
     * 获取SHA1PRNG的{@link SecureRandom}，类提供加密的强随机数生成器 (RNG)
     * 注意：此方法获取的是伪随机序列发生器PRNG（pseudo-random number generator）,在Linux下噪声生成时可能造成较长时间停顿。
     * see: <a href="http://ifeve.com/jvm-random-and-entropy-source/">http://ifeve.com/jvm-random-and-entropy-source/</a>
     *
     * <p>
     * 相关说明见：<a href="https://stackoverflow.com/questions/137212/how-to-solve-slow-java-securerandom">how-to-solve-slow-java-securerandom</a>
     *
     * @param seed 随机数种子
     * @return {@link SecureRandom}
     */
    public static SecureRandom getSHA1PRNGRandom(final byte[] seed) {
        final SecureRandom random;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (final NoSuchAlgorithmException e) {
            throw new InternalException(e);
        }
        if (null != seed) {
            random.setSeed(seed);
        }
        return random;
    }

    /**
     * 获取algorithms/providers中提供的强安全随机生成器
     * 注意：此方法可能造成阻塞或性能问题
     *
     * @return {@link SecureRandom}
     */
    public static SecureRandom getSecureRandomStrong() {
        try {
            return SecureRandom.getInstanceStrong();
        } catch (final NoSuchAlgorithmException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取随机数产生器
     *
     * @param isSecure 是否为强随机数生成器 (RNG)
     * @return {@link Random}
     * @see #getSecureRandom()
     * @see #getRandom()
     */
    public static Random getRandom(final boolean isSecure) {
        return isSecure ? getSecureRandom() : getRandom();
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
     * 随机bytes
     *
     * @param length 长度
     * @return bytes
     */
    public static byte[] randomBytes(final int length) {
        return randomBytes(length, getRandom());
    }

    /**
     * 随机bytes
     *
     * @param length 长度
     * @param random {@link Random}
     * @return bytes
     */
    public static byte[] randomBytes(final int length, Random random) {
        if (null == random) {
            random = getRandom();
        }
        final byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }

    /**
     * 随机汉字（'\u4E00'-'\u9FFF'）
     *
     * @return 随机的汉字字符
     */
    public static char randomChinese() {
        return (char) randomInt('\u4E00', '\u9FFF');
    }

    /**
     * 获得随机数int值
     *
     * @return 随机数
     * @see Random#nextInt()
     */
    public static int randomInt() {
        return getRandom().nextInt();
    }

    /**
     * 获得指定范围内的随机数 [0,limit)
     *
     * @param limitExclude 限制随机数的范围，不包括这个数
     * @return 随机数
     * @see Random#nextInt(int)
     */
    public static int randomInt(final int limitExclude) {
        return getRandom().nextInt(limitExclude);
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param minInclude 最小数（包含）
     * @param maxExclude 最大数（不包含）
     * @return 随机数
     */
    public static int randomInt(final int minInclude, final int maxExclude) {
        return randomInt(minInclude, maxExclude, true, false);
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param min        最小数
     * @param max        最大数
     * @param includeMin 是否包含最小值
     * @param includeMax 是否包含最大值
     * @return 随机数
     */
    public static int randomInt(int min, int max, final boolean includeMin, final boolean includeMax) {
        if (!includeMin) {
            min++;
        }
        if (includeMax) {
            max++;
        }
        return getRandom().nextInt(min, max);
    }

    /**
     * 创建指定长度的随机索引
     *
     * @param length 长度
     * @return 随机索引
     */
    public static int[] randomInts(final int length) {
        final int[] range = MathKit.range(length);
        for (int i = 0; i < length; i++) {
            final int random = randomInt(i, length);
            ArrayKit.swap(range, i, random);
        }
        return range;
    }

    /**
     * 获得随机数
     *
     * @return 随机数
     * @see ThreadLocalRandom#nextLong()
     */
    public static long randomLong() {
        return getRandom().nextLong();
    }

    /**
     * 获得指定范围内的随机数 [0,limit)
     *
     * @param limitExclude 限制随机数的范围，不包括这个数
     * @return 随机数
     * @see ThreadLocalRandom#nextLong(long)
     */
    public static long randomLong(final long limitExclude) {
        return getRandom().nextLong(limitExclude);
    }

    /**
     * 获得指定范围内的随机数[min, max)
     *
     * @param minInclude 最小数（包含）
     * @param maxExclude 最大数（不包含）
     * @return 随机数
     * @see ThreadLocalRandom#nextLong(long, long)
     */
    public static long randomLong(final long minInclude, final long maxExclude) {
        return randomLong(minInclude, maxExclude, true, false);
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param min        最小数
     * @param max        最大数
     * @param includeMin 是否包含最小值
     * @param includeMax 是否包含最大值
     * @return 随机数
     */
    public static long randomLong(long min, long max, final boolean includeMin, final boolean includeMax) {
        if (!includeMin) {
            min++;
        }
        if (includeMax) {
            max++;
        }
        return getRandom().nextLong(min, max);
    }

    /**
     * 获得随机数[0, 1)
     *
     * @return 随机数
     * @see ThreadLocalRandom#nextFloat()
     */
    public static float randomFloat() {
        return getRandom().nextFloat();
    }

    /**
     * 获得指定范围内的随机数 [0,limit)
     *
     * @param limitExclude 限制随机数的范围，不包括这个数
     * @return 随机数
     */
    public static float randomFloat(final float limitExclude) {
        return randomFloat(0, limitExclude);
    }

    /**
     * 获得指定范围内的随机数[min, max)
     *
     * @param minInclude 最小数（包含）
     * @param maxExclude 最大数（不包含）
     * @return 随机数
     * @see ThreadLocalRandom#nextFloat()
     */
    public static float randomFloat(final float minInclude, final float maxExclude) {
        if (minInclude == maxExclude) {
            return minInclude;
        }

        return minInclude + ((maxExclude - minInclude) * getRandom().nextFloat());
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param minInclude 最小数（包含）
     * @param maxExclude 最大数（不包含）
     * @return 随机数
     * @see ThreadLocalRandom#nextDouble(double, double)
     */
    public static double randomDouble(final double minInclude, final double maxExclude) {
        return getRandom().nextDouble(minInclude, maxExclude);
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param minInclude   最小数（包含）
     * @param maxExclude   最大数（不包含）
     * @param scale        保留小数位数
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 随机数
     */
    public static double randomDouble(final double minInclude, final double maxExclude, final int scale,
                                      final RoundingMode roundingMode) {
        return MathKit.round(randomDouble(minInclude, maxExclude), scale, roundingMode).doubleValue();
    }

    /**
     * 获得随机数[0, 1)
     *
     * @return 随机数
     * @see ThreadLocalRandom#nextDouble()
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
    public static double randomDouble(final int scale, final RoundingMode roundingMode) {
        return MathKit.round(randomDouble(), scale, roundingMode).doubleValue();
    }

    /**
     * 获得指定范围内的随机数 [0,limit)
     *
     * @param limit 限制随机数的范围，不包括这个数
     * @return 随机数
     * @see ThreadLocalRandom#nextDouble(double)
     */
    public static double randomDouble(final double limit) {
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
    public static double randomDouble(final double limit, final int scale, final RoundingMode roundingMode) {
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
     * @param limitExclude 最大数（不包含）
     * @return 随机数
     */
    public static BigDecimal randomBigDecimal(final BigDecimal limitExclude) {
        return MathKit.toBigDecimal(getRandom().nextDouble(limitExclude.doubleValue()));
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param minInclude 最小数（包含）
     * @param maxExclude 最大数（不包含）
     * @return 随机数
     */
    public static BigDecimal randomBigDecimal(final BigDecimal minInclude, final BigDecimal maxExclude) {
        return MathKit.toBigDecimal(getRandom().nextDouble(minInclude.doubleValue(), maxExclude.doubleValue()));
    }

    /**
     * 随机获得列表中的元素
     *
     * @param <T>  元素类型
     * @param list 列表
     * @return 随机元素
     */
    public static <T> T randomEle(final List<T> list) {
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
    public static <T> T randomEle(final List<T> list, int limit) {
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
    public static <T> T randomEle(final T[] array) {
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
    public static <T> T randomEle(final T[] array, int limit) {
        if (array.length < limit) {
            limit = array.length;
        }
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
    public static <T> List<T> randomEles(final List<T> list, final int count) {
        final List<T> result = new ArrayList<>(count);
        final int limit = list.size();
        while (result.size() < count) {
            result.add(randomEle(list, limit));
        }

        return result;
    }

    /**
     * 随机获得列表中的一定量的元素，返回List
     * 此方法与{@link #randomEles(List, int)} 不同点在于，不会获取重复位置的元素
     *
     * @param source 列表
     * @param count  随机取出的个数
     * @param <T>    元素类型
     * @return 随机列表
     */
    public static <T> List<T> randomPick(final List<T> source, final int count) {
        if (count >= source.size()) {
            return ListKit.of(source);
        }
        final int[] randomList = ArrayKit.sub(randomInts(source.size()), 0, count);
        final List<T> result = new ArrayList<>();
        for (final int e : randomList) {
            result.add(source.get(e));
        }
        return result;
    }

    /**
     * 生成从种子中获取随机数字
     *
     * @param size 指定产生随机数的个数
     * @param seed 种子，用于取随机数的int池
     * @return 随机int数组
     */
    public static int[] randomPickInts(final int size, final int[] seed) {
        Assert.isTrue(seed.length >= size, "Size is larger than seed size!");

        final int[] ranArr = new int[size];
        // 数量你可以自己定义。
        for (int i = 0; i < size; i++) {
            // 得到一个位置
            final int j = RandomKit.randomInt(seed.length - i);
            // 得到那个位置的数值
            ranArr[i] = seed[j];
            // 将最后一个未用的数字放到这里
            seed[j] = seed[seed.length - 1 - i];
        }
        return ranArr;
    }

    /**
     * 随机获得列表中的一定量的不重复元素，返回Set
     *
     * @param <T>        元素类型
     * @param collection 列表
     * @param count      随机取出的个数
     * @return 随机元素
     * @throws IllegalArgumentException 需要的长度大于给定集合非重复总数
     */
    public static <T> Set<T> randomEleSet(final Collection<T> collection, final int count) {
        final ArrayList<T> source = CollKit.distinct(collection);
        if (count > source.size()) {
            throw new IllegalArgumentException("Count is larger than collection distinct size !");
        }

        final Set<T> result = new LinkedHashSet<>(count);
        final int limit = source.size();
        while (result.size() < count) {
            result.add(randomEle(source, limit));
        }

        return result;
    }

    /**
     * 获得一个随机的字符串（只包含数字和大小写字母）
     *
     * @param length 字符串的长度
     * @return 随机字符串
     */
    public static String randomString(final int length) {
        return randomString(BASE_CHAR_NUMBER, length);
    }

    /**
     * 获得一个随机的字符串（只包含数字和小写字母）
     *
     * @param length 字符串的长度
     * @return 随机字符串
     */
    public static String randomStringLower(final int length) {
        return randomString(Normal.LOWER_ALPHABET_NUMBER, length);
    }

    /**
     * 获得一个随机的字符串（只包含数字和大写字符）
     *
     * @param length 字符串的长度
     * @return 随机字符串
     */
    public static String randomStringUpper(final int length) {
        return randomString(Normal.LOWER_ALPHABET_NUMBER, length).toUpperCase();
    }

    /**
     * 获得一个随机的字符串（只包含数字和字母） 并排除指定字符串
     *
     * @param length   字符串的长度
     * @param elemData 要排除的字符串,如：去重容易混淆的字符串，oO0、lL1、q9Q、pP，区分大小写
     * @return 随机字符串
     */
    public static String randomStringWithoutString(final int length, final String elemData) {
        String baseStr = BASE_CHAR_NUMBER;
        baseStr = StringKit.removeAll(baseStr, elemData.toCharArray());
        return randomString(baseStr, length);
    }

    /**
     * 获得一个随机的字符串（只包含数字和小写字母） 并排除指定字符串
     *
     * @param length   字符串的长度
     * @param elemData 要排除的字符串,如：去重容易混淆的字符串，oO0、lL1、q9Q、pP，不区分大小写
     * @return 随机字符串
     */
    public static String randomStringLowerWithoutString(final int length, final String elemData) {
        String baseStr = Normal.LOWER_ALPHABET_NUMBER;
        baseStr = StringKit.removeAll(baseStr, elemData.toLowerCase().toCharArray());
        return randomString(baseStr, length);
    }

    /**
     * 获得一个只包含数字的字符串
     *
     * @param length 字符串的长度
     * @return 随机字符串
     */
    public static String randomNumbers(final int length) {
        return randomString(Normal.NUMBER, length);
    }

    /**
     * 获得一个随机的字符串
     *
     * @param baseString 随机字符选取的样本
     * @param length     字符串的长度
     * @return 随机字符串
     */
    public static String randomString(final String baseString, int length) {
        if (StringKit.isEmpty(baseString)) {
            return Normal.EMPTY;
        }
        if (length < 1) {
            length = 1;
        }

        final StringBuilder sb = new StringBuilder(length);
        final int baseLength = baseString.length();
        for (int i = 0; i < length; i++) {
            final int number = randomInt(baseLength);
            sb.append(baseString.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 随机数字，数字为0~9单个数字
     *
     * @return 随机数字字符
     */
    public static char randomNumber() {
        return randomChar(Normal.NUMBER);
    }

    /**
     * 随机字母或数字，小写
     *
     * @return 随机字符
     */
    public static char randomChar() {
        return randomChar(Normal.LOWER_ALPHABET_NUMBER);
    }

    /**
     * 随机字符
     *
     * @param baseString 随机字符选取的样本
     * @return 随机字符
     */
    public static char randomChar(final String baseString) {
        return baseString.charAt(randomInt(baseString.length()));
    }

    /**
     * 带有权重的随机生成器
     *
     * @param <T>        随机对象类型
     * @param weightObjs 带有权重的对象列表
     * @return {@link WeightRandomSelector}
     */
    public static <T> WeightRandomSelector<T> weightRandom(final WeightObject<T>[] weightObjs) {
        return new WeightRandomSelector<>(weightObjs);
    }

    /**
     * 带有权重的随机生成器
     *
     * @param <T>        随机对象类型
     * @param weightObjs 带有权重的对象列表
     * @return {@link WeightRandomSelector}
     */
    public static <T> WeightRandomSelector<T> weightRandom(final Iterable<WeightObject<T>> weightObjs) {
        return new WeightRandomSelector<>(weightObjs);
    }

    /**
     * 以当天为基准，随机产生一个日期
     *
     * @param min 偏移最小天，可以为负数表示过去的时间（包含）
     * @param max 偏移最大天，可以为负数表示过去的时间（不包含）
     * @return 随机日期（随机天，其它时间不变）
     */
    public static DateTime randomDay(final int min, final int max) {
        return randomDate(DateKit.now(), Fields.Type.DAY_OF_YEAR, min, max);
    }

    /**
     * 以给定日期为基准，随机产生一个日期
     *
     * @param baseDate 基准日期
     * @param type     偏移的时间字段，例如时、分、秒等
     * @param min      偏移最小量，可以为负数表示过去的时间（包含）
     * @param max      偏移最大量，可以为负数表示过去的时间（不包含）
     * @return 随机日期
     */
    public static DateTime randomDate(Date baseDate, final Fields.Type type, final int min, final int max) {
        if (null == baseDate) {
            baseDate = DateKit.now();
        }

        return DateKit.offset(baseDate, type, randomInt(min, max));
    }

}
