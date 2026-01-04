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
package org.miaixz.bus.core.xyz;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.miaixz.bus.core.center.date.DateTime;
import org.miaixz.bus.core.center.date.Various;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.selector.WeightObject;
import org.miaixz.bus.core.lang.selector.WeightRandomSelector;

/**
 * Random utility class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RandomKit {

    /**
     * A string of characters and numbers (uppercase and lowercase) for random selection.
     */
    public static final String BASE_CHAR_NUMBER = Normal.ALPHABET.toUpperCase() + Normal.LOWER_ALPHABET_NUMBER;

    /**
     * Gets a `ThreadLocalRandom` instance. This is the preferred method for generating random numbers in a concurrent
     * environment since JDK 7.
     * <p>
     * Note: The `ThreadLocalRandom` returned by this method should not be shared across threads, as it may lead to
     * duplicate random numbers.
     *
     * @return The current thread's {@link ThreadLocalRandom}.
     */
    public static ThreadLocalRandom getRandom() {
        return ThreadLocalRandom.current();
    }

    /**
     * Creates a {@link SecureRandom} instance, which provides a cryptographically strong random number generator (RNG).
     *
     * @param seed A custom random seed.
     * @return A {@link SecureRandom} instance.
     */
    public static SecureRandom createSecureRandom(final byte[] seed) {
        return (null == seed) ? new SecureRandom() : new SecureRandom(seed);
    }

    /**
     * Gets a `SecureRandom` instance using the default algorithm.
     *
     * @return A {@link SecureRandom} instance.
     */
    public static SecureRandom getSecureRandom() {
        return getSecureRandom(null);
    }

    /**
     * Gets a `SecureRandom` instance, optionally seeded.
     *
     * @param seed The random number seed.
     * @return A {@link SecureRandom} instance.
     * @see #createSecureRandom(byte[])
     */
    public static SecureRandom getSecureRandom(final byte[] seed) {
        return createSecureRandom(seed);
    }

    /**
     * Gets a `SecureRandom` instance using the "SHA1PRNG" algorithm. Note: This is a pseudo-random number generator
     * (PRNG) and may block on Linux systems if entropy is low.
     *
     * @param seed The random number seed.
     * @return A {@link SecureRandom} instance.
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
     * Gets a cryptographically strong `SecureRandom` generator. Note: This method may block or have performance
     * implications.
     *
     * @return A strong {@link SecureRandom} instance.
     */
    public static SecureRandom getSecureRandomStrong() {
        try {
            return SecureRandom.getInstanceStrong();
        } catch (final NoSuchAlgorithmException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets a random number generator.
     *
     * @param isSecure If `true`, returns a cryptographically strong `SecureRandom`; otherwise, returns a
     *                 `ThreadLocalRandom`.
     * @return A {@link Random} instance.
     */
    public static Random getRandom(final boolean isSecure) {
        return isSecure ? getSecureRandom() : getRandom();
    }

    /**
     * Gets a random boolean value.
     *
     * @return `true` or `false`.
     */
    public static boolean randomBoolean() {
        return 0 == randomInt(2);
    }

    /**
     * Generates a random byte array.
     *
     * @param length The length of the array.
     * @return The random byte array.
     */
    public static byte[] randomBytes(final int length) {
        return randomBytes(length, getRandom());
    }

    /**
     * Generates a random byte array using a specific `Random` instance.
     *
     * @param length The length of the array.
     * @param random The {@link Random} instance.
     * @return The random byte array.
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
     * Generates a random Chinese character (from '\u4E00' to '\u9FFF').
     *
     * @return A random Chinese character.
     */
    public static char randomChinese() {
        return (char) randomInt('\u4E00', '\u9FFF');
    }

    /**
     * Gets a random integer.
     *
     * @return A random integer.
     */
    public static int randomInt() {
        return getRandom().nextInt();
    }

    /**
     * Gets a random integer within the range [0, limitExclude).
     *
     * @param limitExclude The exclusive upper bound.
     * @return A random integer.
     */
    public static int randomInt(final int limitExclude) {
        return getRandom().nextInt(limitExclude);
    }

    /**
     * Gets a random integer within the range [minInclude, maxExclude).
     *
     * @param minInclude The inclusive lower bound.
     * @param maxExclude The exclusive upper bound.
     * @return A random integer.
     */
    public static int randomInt(final int minInclude, final int maxExclude) {
        return randomInt(minInclude, maxExclude, true, false);
    }

    /**
     * Gets a random integer within a specified range.
     *
     * @param min        The minimum value.
     * @param max        The maximum value.
     * @param includeMin `true` to include the minimum value.
     * @param includeMax `true` to include the maximum value.
     * @return A random integer.
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
     * Creates an array of shuffled indices of a specified length.
     *
     * @param length The length.
     * @return An array of random indices.
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
     * Gets a random long.
     *
     * @return A random long.
     */
    public static long randomLong() {
        return getRandom().nextLong();
    }

    /**
     * Gets a random long within the range [0, limitExclude).
     *
     * @param limitExclude The exclusive upper bound.
     * @return A random long.
     */
    public static long randomLong(final long limitExclude) {
        return getRandom().nextLong(limitExclude);
    }

    /**
     * Gets a random long within the range [minInclude, maxExclude).
     *
     * @param minInclude The inclusive lower bound.
     * @param maxExclude The exclusive upper bound.
     * @return A random long.
     */
    public static long randomLong(final long minInclude, final long maxExclude) {
        return randomLong(minInclude, maxExclude, true, false);
    }

    /**
     * Gets a random long within a specified range.
     *
     * @param min        The minimum value.
     * @param max        The maximum value.
     * @param includeMin `true` to include the minimum value.
     * @param includeMax `true` to include the maximum value.
     * @return A random long.
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
     * Gets a random float within the range [0.0, 1.0).
     *
     * @return A random float.
     */
    public static float randomFloat() {
        return getRandom().nextFloat();
    }

    /**
     * Gets a random float within the range [0, limitExclude).
     *
     * @param limitExclude The exclusive upper bound.
     * @return A random float.
     */
    public static float randomFloat(final float limitExclude) {
        return randomFloat(0, limitExclude);
    }

    /**
     * Gets a random float within the range [minInclude, maxExclude).
     *
     * @param minInclude The inclusive lower bound.
     * @param maxExclude The exclusive upper bound.
     * @return A random float.
     */
    public static float randomFloat(final float minInclude, final float maxExclude) {
        if (minInclude == maxExclude) {
            return minInclude;
        }
        return minInclude + ((maxExclude - minInclude) * getRandom().nextFloat());
    }

    /**
     * Gets a random double within the range [minInclude, maxExclude).
     *
     * @param minInclude The inclusive lower bound.
     * @param maxExclude The exclusive upper bound.
     * @return A random double.
     */
    public static double randomDouble(final double minInclude, final double maxExclude) {
        return getRandom().nextDouble(minInclude, maxExclude);
    }

    /**
     * Gets a random double within a specified range, rounded to a given scale.
     *
     * @param minInclude   The inclusive lower bound.
     * @param maxExclude   The exclusive upper bound.
     * @param scale        The number of decimal places to keep.
     * @param roundingMode The rounding mode.
     * @return A random double.
     */
    public static double randomDouble(
            final double minInclude,
            final double maxExclude,
            final int scale,
            final RoundingMode roundingMode) {
        return MathKit.round(randomDouble(minInclude, maxExclude), scale, roundingMode).doubleValue();
    }

    /**
     * Gets a random double within the range [0.0, 1.0).
     *
     * @return A random double.
     */
    public static double randomDouble() {
        return getRandom().nextDouble();
    }

    /**
     * Gets a random double rounded to a given scale.
     *
     * @param scale        The number of decimal places to keep.
     * @param roundingMode The rounding mode.
     * @return A random double.
     */
    public static double randomDouble(final int scale, final RoundingMode roundingMode) {
        return MathKit.round(randomDouble(), scale, roundingMode).doubleValue();
    }

    /**
     * Gets a random double within the range [0, limit).
     *
     * @param limit The exclusive upper bound.
     * @return A random double.
     */
    public static double randomDouble(final double limit) {
        return getRandom().nextDouble(limit);
    }

    /**
     * Gets a random double within a range, rounded to a given scale.
     *
     * @param limit        The exclusive upper bound.
     * @param scale        The number of decimal places to keep.
     * @param roundingMode The rounding mode.
     * @return A random double.
     */
    public static double randomDouble(final double limit, final int scale, final RoundingMode roundingMode) {
        return MathKit.round(randomDouble(limit), scale, roundingMode).doubleValue();
    }

    /**
     * Gets a random `BigDecimal` within the range [0, 1).
     *
     * @return A random `BigDecimal`.
     */
    public static BigDecimal randomBigDecimal() {
        return MathKit.toBigDecimal(getRandom().nextDouble());
    }

    /**
     * Gets a random `BigDecimal` within the range [0, limitExclude).
     *
     * @param limitExclude The exclusive upper bound.
     * @return A random `BigDecimal`.
     */
    public static BigDecimal randomBigDecimal(final BigDecimal limitExclude) {
        return MathKit.toBigDecimal(getRandom().nextDouble(limitExclude.doubleValue()));
    }

    /**
     * Gets a random `BigDecimal` within a specified range.
     *
     * @param minInclude The inclusive lower bound.
     * @param maxExclude The exclusive upper bound.
     * @return A random `BigDecimal`.
     */
    public static BigDecimal randomBigDecimal(final BigDecimal minInclude, final BigDecimal maxExclude) {
        return MathKit.toBigDecimal(getRandom().nextDouble(minInclude.doubleValue(), maxExclude.doubleValue()));
    }

    /**
     * Gets a random element from a list.
     *
     * @param <T>  The element type.
     * @param list The list.
     * @return A random element.
     */
    public static <T> T randomEle(final List<T> list) {
        return randomEle(list, list.size());
    }

    /**
     * Gets a random element from the first `limit` elements of a list.
     *
     * @param <T>   The element type.
     * @param list  The list.
     * @param limit The upper bound of the index to choose from.
     * @return A random element.
     */
    public static <T> T randomEle(final List<T> list, int limit) {
        if (list.size() < limit) {
            limit = list.size();
        }
        return list.get(randomInt(limit));
    }

    /**
     * Gets a random element from an array.
     *
     * @param <T>   The element type.
     * @param array The array.
     * @return A random element.
     */
    public static <T> T randomEle(final T[] array) {
        return randomEle(array, array.length);
    }

    /**
     * Gets a random element from the first `limit` elements of an array.
     *
     * @param <T>   The element type.
     * @param array The array.
     * @param limit The upper bound of the index to choose from.
     * @return A random element.
     */
    public static <T> T randomEle(final T[] array, int limit) {
        if (array.length < limit) {
            limit = array.length;
        }
        return array[randomInt(limit)];
    }

    /**
     * Gets a specified number of random elements from a list, with replacement.
     *
     * @param <T>   The element type.
     * @param list  The list.
     * @param count The number of elements to get.
     * @return A list of random elements.
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
     * Gets a specified number of unique random elements from a list (without replacement).
     *
     * @param source The source list.
     * @param count  The number of elements to pick.
     * @param <T>    The element type.
     * @return A list of random elements.
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
     * Picks a specified number of random integers from a seed array without replacement.
     *
     * @param size The number of random integers to generate.
     * @param seed The seed array.
     * @return An array of random integers.
     */
    public static int[] randomPickInts(final int size, final int[] seed) {
        Assert.isTrue(seed.length >= size, "Size is larger than seed size!");
        final int[] ranArr = new int[size];
        for (int i = 0; i < size; i++) {
            final int j = RandomKit.randomInt(seed.length - i);
            ranArr[i] = seed[j];
            seed[j] = seed[seed.length - 1 - i];
        }
        return ranArr;
    }

    /**
     * Gets a specified number of unique random elements from a collection, returned as a `Set`.
     *
     * @param <T>        The element type.
     * @param collection The collection.
     * @param count      The number of elements to pick.
     * @return A set of random elements.
     * @throws IllegalArgumentException if `count` is greater than the number of distinct elements.
     */
    public static <T> Set<T> randomEleSet(final Collection<T> collection, final int count) {
        final List<T> source = CollKit.distinct(collection);
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
     * Generates a random string of a specified length from alphanumeric characters.
     *
     * @param length The length of the string.
     * @return The random string.
     */
    public static String randomString(final int length) {
        return randomString(BASE_CHAR_NUMBER, length);
    }

    /**
     * Generates a random string of a specified length from lowercase letters and digits.
     *
     * @param length The length of the string.
     * @return The random string.
     */
    public static String randomStringLower(final int length) {
        return randomString(Normal.LOWER_ALPHABET_NUMBER, length);
    }

    /**
     * Generates a random string of a specified length from uppercase letters and digits.
     *
     * @param length The length of the string.
     * @return The random string.
     */
    public static String randomStringUpper(final int length) {
        return randomString(Normal.LOWER_ALPHABET_NUMBER, length).toUpperCase();
    }

    /**
     * Generates a random alphanumeric string, excluding specified characters.
     *
     * @param length   The length of the string.
     * @param elemData The characters to exclude.
     * @return The random string.
     */
    public static String randomStringWithoutString(final int length, final String elemData) {
        String baseStr = BASE_CHAR_NUMBER;
        baseStr = StringKit.removeAll(baseStr, elemData.toCharArray());
        return randomString(baseStr, length);
    }

    /**
     * Generates a random string of lowercase letters and digits, excluding specified characters.
     *
     * @param length   The length of the string.
     * @param elemData The characters to exclude.
     * @return The random string.
     */
    public static String randomStringLowerWithoutString(final int length, final String elemData) {
        String baseStr = Normal.LOWER_ALPHABET_NUMBER;
        baseStr = StringKit.removeAll(baseStr, elemData.toLowerCase().toCharArray());
        return randomString(baseStr, length);
    }

    /**
     * Generates a random string containing only digits.
     *
     * @param length The length of the string.
     * @return The random numeric string.
     */
    public static String randomNumbers(final int length) {
        return randomString(Normal.NUMBER, length);
    }

    /**
     * Generates a random string from a given base string.
     *
     * @param baseString The characters to choose from.
     * @param length     The length of the string.
     * @return The random string.
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
     * Gets a random digit character ('0'-'9').
     *
     * @return A random digit character.
     */
    public static char randomNumber() {
        return randomChar(Normal.NUMBER);
    }

    /**
     * Gets a random lowercase letter or digit.
     *
     * @return A random character.
     */
    public static char randomChar() {
        return randomChar(Normal.LOWER_ALPHABET_NUMBER);
    }

    /**
     * Gets a random character from a base string.
     *
     * @param baseString The characters to choose from.
     * @return A random character.
     */
    public static char randomChar(final String baseString) {
        return baseString.charAt(randomInt(baseString.length()));
    }

    /**
     * Creates a weighted random selector.
     *
     * @param <T>        The type of the objects.
     * @param weightObjs A list of objects with weights.
     * @return A {@link WeightRandomSelector}.
     */
    public static <T> WeightRandomSelector<T> weightRandom(final WeightObject<T>[] weightObjs) {
        return new WeightRandomSelector<>(weightObjs);
    }

    /**
     * Creates a weighted random selector.
     *
     * @param <T>        The type of the objects.
     * @param weightObjs An iterable of objects with weights.
     * @return A {@link WeightRandomSelector}.
     */
    public static <T> WeightRandomSelector<T> weightRandom(final Iterable<WeightObject<T>> weightObjs) {
        return new WeightRandomSelector<>(weightObjs);
    }

    /**
     * Generates a random date relative to today.
     *
     * @param min The minimum day offset (can be negative).
     * @param max The maximum day offset (exclusive).
     * @return A random date.
     */
    public static DateTime randomDay(final int min, final int max) {
        return randomDate(DateKit.now(), Various.DAY_OF_YEAR, min, max);
    }

    /**
     * Generates a random date relative to a base date.
     *
     * @param baseDate The base date.
     * @param various  The time field to offset (e.g., hour, day, month).
     * @param min      The minimum offset amount (inclusive).
     * @param max      The maximum offset amount (exclusive).
     * @return A random date.
     */
    public static DateTime randomDate(Date baseDate, final Various various, final int min, final int max) {
        if (null == baseDate) {
            baseDate = DateKit.now();
        }
        return DateKit.offset(baseDate, various, randomInt(min, max));
    }

}
