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
package org.miaixz.bus.core.codec;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.miaixz.bus.core.lang.Symbol;

/**
 * <a href="http://hashids.org/">Hashids</a> protocol implementation, designed to:
 * <ul>
 * <li>Generate short, unique, case-sensitive, and non-sequential hash values.</li>
 * <li>Hash natural numbers.</li>
 * <li>Allow different salts for confidentiality.</li>
 * <li>Support configurable hash length.</li>
 * <li>Produce unpredictable outputs for incrementally increasing inputs.</li>
 * </ul>
 *
 * <p>
 * This implementation is adapted from:
 * <a href="https://github.com/davidafsilva/java-hashids">https://github.com/davidafsilva/java-hashids</a>
 *
 * <p>
 * {@code Hashids} can convert numbers or hexadecimal strings into short, unique, and non-consecutive strings. It uses a
 * bidirectional encoding scheme, for example, it can convert numbers like 347 into strings like "yr8", and also decode
 * strings like "yr8" back into numbers like 347. This encoding algorithm primarily addresses the problem of web
 * crawlers scraping sequential IDs by converting ordered IDs into unordered Hashids, while maintaining a one-to-one
 * correspondence.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Hashids implements Encoder<long[], String>, Decoder<String, long[]> {

    /**
     * The default alphabet used for encoding and decoding. This array contains a mix of lowercase letters, uppercase
     * letters, and digits.
     */
    public static final char[] DEFAULT_ALPHABET = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
            'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', '0' };
    /**
     * A modulus used in the lottery number calculation to determine the initial character.
     */
    private static final int LOTTERY_MOD = 100;
    /**
     * The threshold for determining the number of guard characters. If the alphabet length divided by this threshold is
     * greater than the number of guards, more guards are used.
     */
    private static final double GUARD_THRESHOLD = 12;
    /**
     * The threshold for determining the number of separator characters. If the alphabet length divided by the separator
     * length is greater than this threshold, more separators are used.
     */
    private static final double SEPARATOR_THRESHOLD = 3.5;
    /**
     * The minimum required length for the alphabet used in encoding and decoding.
     */
    private static final int MIN_ALPHABET_LENGTH = 16;
    /**
     * Regular expression pattern to match hexadecimal values for decoding from hex strings.
     */
    private static final Pattern HEX_VALUES_PATTERN = Pattern.compile("[\\w\\W]{1,12}");
    /**
     * The default set of separator characters used to delimit encoded numbers within a hash.
     */
    private static final char[] DEFAULT_SEPARATORS = { 'c', 'f', 'h', 'i', 's', 't', 'u', 'C', 'F', 'H', 'I', 'S', 'T',
            'U' };

    // algorithm properties
    /**
     * The effective alphabet used for encoding and decoding after initialization and filtering.
     */
    private final char[] alphabet;
    /**
     * The characters used as separators between encoded numbers within a hash.
     */
    private final char[] separators;
    /**
     * A set for efficient lookup of separator characters.
     */
    private final Set<Character> separatorsSet;
    /**
     * The salt used to randomize the encoding process, enhancing security and uniqueness.
     */
    private final char[] salt;
    /**
     * Characters used as "guards" to further obfuscate the hash and meet minimum length requirements.
     */
    private final char[] guards;
    /**
     * The minimum desired length of the generated hash. If the encoded hash is shorter, it will be padded.
     */
    private final int minLength;

    /**
     * Constructs a new {@code Hashids} instance with the specified salt, alphabet, and minimum length. This constructor
     * performs internal setup, including filtering and shuffling the alphabet and separators based on the provided
     * parameters to ensure the algorithm's properties are met.
     *
     * @param salt      The salt to use for encoding and decoding. This adds an extra layer of security.
     * @param alphabet  The set of characters to use for generating the hash. Must meet minimum length requirements.
     * @param minLength The minimum length of the generated hash. If the hash is shorter, it will be padded.
     */
    public Hashids(final char[] salt, final char[] alphabet, final int minLength) {
        this.minLength = minLength;
        this.salt = Arrays.copyOf(salt, salt.length);

        // filter and shuffle separators
        char[] tmpSeparators = shuffle(filterSeparators(DEFAULT_SEPARATORS, alphabet), this.salt);

        // validate and filter the alphabet
        char[] tmpAlphabet = validateAndFilterAlphabet(alphabet, tmpSeparators);

        // check separator threshold
        if (tmpSeparators.length == 0 || ((double) (tmpAlphabet.length / tmpSeparators.length)) > SEPARATOR_THRESHOLD) {
            final int minSeparatorsSize = (int) Math.ceil(tmpAlphabet.length / SEPARATOR_THRESHOLD);
            // check minimum size of separators
            if (minSeparatorsSize > tmpSeparators.length) {
                // fill separators from alphabet
                final int missingSeparators = minSeparatorsSize - tmpSeparators.length;
                tmpSeparators = Arrays.copyOf(tmpSeparators, tmpSeparators.length + missingSeparators);
                System.arraycopy(
                        tmpAlphabet,
                        0,
                        tmpSeparators,
                        tmpSeparators.length - missingSeparators,
                        missingSeparators);
                System.arraycopy(
                        tmpAlphabet,
                        0,
                        tmpSeparators,
                        tmpSeparators.length - missingSeparators,
                        missingSeparators);
                tmpAlphabet = Arrays.copyOfRange(tmpAlphabet, missingSeparators, tmpAlphabet.length);
            }
        }

        // shuffle the current alphabet
        shuffle(tmpAlphabet, this.salt);

        // check guards
        this.guards = new char[(int) Math.ceil(tmpAlphabet.length / GUARD_THRESHOLD)];
        if (alphabet.length < 3) {
            System.arraycopy(tmpSeparators, 0, guards, 0, guards.length);
            this.separators = Arrays.copyOfRange(tmpSeparators, guards.length, tmpSeparators.length);
            this.alphabet = tmpAlphabet;
        } else {
            System.arraycopy(tmpAlphabet, 0, guards, 0, guards.length);
            this.separators = tmpSeparators;
            this.alphabet = Arrays.copyOfRange(tmpAlphabet, guards.length, tmpAlphabet.length);
        }

        // create the separators set
        separatorsSet = IntStream.range(0, separators.length).mapToObj(idx -> separators[idx])
                .collect(Collectors.toSet());
    }

    /**
     * Creates a {@code Hashids} instance using the given salt, the {@link #DEFAULT_ALPHABET}, and no minimum hash
     * length restriction.
     *
     * @param salt The salt value to use.
     * @return A new {@code Hashids} instance.
     */
    public static Hashids of(final char[] salt) {
        return of(salt, DEFAULT_ALPHABET, -1);
    }

    /**
     * Creates a {@code Hashids} instance using the given salt, the {@link #DEFAULT_ALPHABET}, and a specified minimum
     * hash length.
     *
     * @param salt      The salt value to use.
     * @param minLength The minimum length of the generated hash. Use -1 for no restriction.
     * @return A new {@code Hashids} instance.
     */
    public static Hashids of(final char[] salt, final int minLength) {
        return of(salt, DEFAULT_ALPHABET, minLength);
    }

    /**
     * Creates a {@code Hashids} instance using the given salt, custom alphabet, and a specified minimum hash length.
     *
     * @param salt      The salt value to use.
     * @param alphabet  The custom alphabet to use for hashing.
     * @param minLength The minimum length of the generated hash. Use -1 for no restriction.
     * @return A new {@code Hashids} instance.
     */
    public static Hashids of(final char[] salt, final char[] alphabet, final int minLength) {
        return new Hashids(salt, alphabet, minLength);
    }

    /**
     * Encodes a hexadecimal string into a Hashids string. The input hexadecimal string can optionally start with "0x"
     * or "0X". Each 12-character chunk of the hexadecimal string is converted to a long and then encoded.
     *
     * @param hexNumbers The hexadecimal string to encode.
     * @return The encoded Hashids string, or {@code null} if {@code hexNumbers} is {@code null}.
     * @throws IllegalArgumentException if any part of the hexadecimal string cannot be converted to a valid number.
     */
    public String encodeFromHex(final String hexNumbers) {
        if (hexNumbers == null) {
            return null;
        }

        // remove the prefix, if present
        final String hex = hexNumbers.startsWith("0x") || hexNumbers.startsWith("0X") ? hexNumbers.substring(2)
                : hexNumbers;

        // get the associated long value and encode it
        LongStream values = LongStream.empty();
        final Matcher matcher = HEX_VALUES_PATTERN.matcher(hex);
        while (matcher.find()) {
            final long value = new BigInteger("1" + matcher.group(), 16).longValue();
            values = LongStream.concat(values, LongStream.of(value));
        }

        return encode(values.toArray());
    }

    /**
     * Encodes an array of long numbers into a Hashids string. This method applies the Hashids algorithm, including
     * salting, shuffling, and padding to meet the minimum length requirement.
     *
     * @param numbers An array of long numbers to be encoded.
     * @return The encoded Hashids string, or {@code null} if {@code numbers} is {@code null}.
     * @throws IllegalArgumentException if any number in the array is negative.
     */
    @Override
    public String encode(final long... numbers) {
        if (numbers == null) {
            return null;
        }

        // copier alphabet
        final char[] currentAlphabet = Arrays.copyOf(alphabet, alphabet.length);

        // determine the lottery number
        final long lotteryId = LongStream.range(0, numbers.length).reduce(0, (state, i) -> {
            final long number = numbers[(int) i];
            if (number < 0) {
                throw new IllegalArgumentException("invalid number: " + number);
            }
            return state + number % (i + LOTTERY_MOD);
        });
        final char lottery = currentAlphabet[(int) (lotteryId % currentAlphabet.length)];

        // encode each number
        final StringBuilder global = new StringBuilder();
        IntStream.range(0, numbers.length).forEach(idx -> {
            // derive alphabet
            deriveNewAlphabet(currentAlphabet, salt, lottery);

            // encode
            final int initialLength = global.length();
            translate(numbers[idx], currentAlphabet, global, initialLength);

            // prepend the lottery
            if (idx == 0) {
                global.insert(0, lottery);
            }

            // append the separator, if more numbers are pending encoding
            if (idx + 1 < numbers.length) {
                final long n = numbers[idx] % (global.charAt(initialLength) + 1);
                global.append(separators[(int) (n % separators.length)]);
            }
        });

        // add the guards, if there's any space left
        if (minLength > global.length()) {
            int guardIdx = (int) ((lotteryId + lottery) % guards.length);
            global.insert(0, guards[guardIdx]);
            if (minLength > global.length()) {
                guardIdx = (int) ((lotteryId + global.charAt(2)) % guards.length);
                global.append(guards[guardIdx]);
            }
        }

        // add the necessary padding
        int paddingLeft = minLength - global.length();
        while (paddingLeft > 0) {
            shuffle(currentAlphabet, Arrays.copyOf(currentAlphabet, currentAlphabet.length));

            final int alphabetHalfSize = currentAlphabet.length / 2;
            final int initialSize = global.length();
            if (paddingLeft > currentAlphabet.length) {
                // entire alphabet with the current encoding in the middle of it
                final int offset = alphabetHalfSize + (currentAlphabet.length % 2 == 0 ? 0 : 1);

                global.insert(0, currentAlphabet, alphabetHalfSize, offset);
                global.insert(offset + initialSize, currentAlphabet, 0, alphabetHalfSize);
                // decrease the padding left
                paddingLeft -= currentAlphabet.length;
            } else {
                // calculate the excess
                final int excess = currentAlphabet.length + global.length() - minLength;
                final int secondHalfStartOffset = alphabetHalfSize + Math.floorDiv(excess, 2);
                final int secondHalfLength = currentAlphabet.length - secondHalfStartOffset;
                final int firstHalfLength = paddingLeft - secondHalfLength;

                global.insert(0, currentAlphabet, secondHalfStartOffset, secondHalfLength);
                global.insert(secondHalfLength + initialSize, currentAlphabet, 0, firstHalfLength);

                paddingLeft = 0;
            }
        }

        return global.toString();
    }

    /**
     * Decodes a Hashids string back into its original hexadecimal representation.
     *
     * @param hash The Hashids string to decode.
     * @return The decoded hexadecimal string, or {@code null} if {@code hash} is {@code null}.
     * @throws IllegalArgumentException if the hash is invalid or cannot be decoded.
     */
    public String decodeToHex(final String hash) {
        if (hash == null) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        Arrays.stream(decode(hash)).mapToObj(Long::toHexString).forEach(hex -> sb.append(hex, 1, hex.length()));
        return sb.toString();
    }

    /**
     * Decodes a Hashids string back into an array of long numbers. This method reverses the encoding process, including
     * handling guards and separators.
     *
     * @param hash The Hashids string to decode.
     * @return An array of decoded long numbers, or {@code null} if {@code hash} is {@code null}.
     * @throws IllegalArgumentException if the hash is invalid or cannot be decoded.
     */
    @Override
    public long[] decode(final String hash) {
        if (hash == null) {
            return null;
        }

        // create a set of the guards
        final Set<Character> guardsSet = IntStream.range(0, guards.length).mapToObj(idx -> guards[idx])
                .collect(Collectors.toSet());
        // count the total guards used
        final int[] guardsIdx = IntStream.range(0, hash.length()).filter(idx -> guardsSet.contains(hash.charAt(idx)))
                .toArray();
        // get the start/end index base on the guards count
        final int startIdx, endIdx;
        if (guardsIdx.length > 0) {
            startIdx = guardsIdx[0] + 1;
            endIdx = guardsIdx.length > 1 ? guardsIdx[1] : hash.length();
        } else {
            startIdx = 0;
            endIdx = hash.length();
        }

        LongStream decoded = LongStream.empty();
        // parse the hash
        if (!hash.isEmpty()) {
            final char lottery = hash.charAt(startIdx);

            // create the initial accumulation string
            final int length = hash.length() - guardsIdx.length - 1;
            StringBuilder block = new StringBuilder(length);

            // create the base salt
            final char[] decodeSalt = new char[alphabet.length];
            decodeSalt[0] = lottery;
            final int saltLength = salt.length >= alphabet.length ? alphabet.length - 1 : salt.length;
            System.arraycopy(salt, 0, decodeSalt, 1, saltLength);
            final int saltLeft = alphabet.length - saltLength - 1;

            // copier alphabet
            final char[] currentAlphabet = Arrays.copyOf(alphabet, alphabet.length);

            for (int i = startIdx + 1; i < endIdx; i++) {
                if (!separatorsSet.contains(hash.charAt(i))) {
                    block.append(hash.charAt(i));
                    // continue if we have not reached the end, yet
                    if (i < endIdx - 1) {
                        continue;
                    }
                }

                if (block.length() > 0) {
                    // create the salt
                    if (saltLeft > 0) {
                        System.arraycopy(currentAlphabet, 0, decodeSalt, alphabet.length - saltLeft, saltLeft);
                    }

                    // shuffle the alphabet
                    shuffle(currentAlphabet, decodeSalt);

                    // prepend the decoded value
                    final long n = translate(block.toString().toCharArray(), currentAlphabet);
                    decoded = LongStream.concat(decoded, LongStream.of(n));

                    // create a new block
                    block = new StringBuilder(length);
                }
            }
        }

        // validate the hash
        final long[] decodedValue = decoded.toArray();
        if (!Objects.equals(hash, encode(decodedValue))) {
            throw new IllegalArgumentException("invalid hash: " + hash);
        }

        return decodedValue;
    }

    /**
     * Translates a long number into a sequence of characters from the given alphabet, appending them to a
     * {@link StringBuilder} in reverse order (least significant character first).
     *
     * @param n        The number to translate.
     * @param alphabet The alphabet to use for translation.
     * @param sb       The {@link StringBuilder} to append the translated characters to.
     * @param start    The starting index in the {@link StringBuilder} where characters should be inserted.
     * @return The {@link StringBuilder} with the translated characters inserted.
     */
    private StringBuilder translate(final long n, final char[] alphabet, final StringBuilder sb, final int start) {
        long input = n;
        do {
            // prepend the chosen char
            sb.insert(start, alphabet[(int) (input % alphabet.length)]);

            // trim the input
            input = input / alphabet.length;
        } while (input > 0);

        return sb;
    }

    /**
     * Translates a character array (representing a hash segment) back into a long number using the provided alphabet.
     * This is the reverse operation of {@link #translate(long, char[], StringBuilder, int)}.
     *
     * @param hash     The character array representing a hash segment.
     * @param alphabet The alphabet used for translation.
     * @return The decoded long number.
     * @throws IllegalArgumentException if a character in the hash is not found in the alphabet.
     */
    private long translate(final char[] hash, final char[] alphabet) {
        long number = 0;

        final Map<Character, Integer> alphabetMapping = IntStream.range(0, alphabet.length)
                .mapToObj(idx -> new Object[] { alphabet[idx], idx }).collect(
                        Collectors.groupingBy(
                                arr -> (Character) arr[0],
                                Collectors.mapping(
                                        arr -> (Integer) arr[1],
                                        Collectors.reducing(null, (a, b) -> a == null ? b : a))));

        for (int i = 0; i < hash.length; ++i) {
            number += alphabetMapping.computeIfAbsent(hash[i], k -> {
                throw new IllegalArgumentException("Invalid alphabet for hash");
            }) * (long) Math.pow(alphabet.length, hash.length - i - 1);
        }

        return number;
    }

    /**
     * Derives a new alphabet by shuffling the original alphabet based on a combination of the lottery character and the
     * salt. This is a crucial step in the Hashids algorithm to ensure non-sequential and randomized output.
     *
     * @param alphabet The base alphabet to be shuffled.
     * @param salt     The salt used in the shuffling process.
     * @param lottery  The lottery character, which influences the initial shuffle.
     * @return The newly shuffled alphabet.
     */
    private char[] deriveNewAlphabet(final char[] alphabet, final char[] salt, final char lottery) {
        // create the new salt
        final char[] newSalt = new char[alphabet.length];

        // 1. lottery
        newSalt[0] = lottery;
        int spaceLeft = newSalt.length - 1;
        int offset = 1;
        // 2. salt
        if (salt.length > 0 && spaceLeft > 0) {
            final int length = Math.min(salt.length, spaceLeft);
            System.arraycopy(salt, 0, newSalt, offset, length);
            spaceLeft -= length;
            offset += length;
        }
        // 3. alphabet
        if (spaceLeft > 0) {
            System.arraycopy(alphabet, 0, newSalt, offset, spaceLeft);
        }

        // shuffle
        return shuffle(alphabet, newSalt);
    }

    /**
     * Validates the provided alphabet and filters out any characters that are also present in the separators list or
     * are space characters. Ensures the alphabet meets minimum length requirements.
     *
     * @param alphabet   The raw alphabet characters provided by the user.
     * @param separators The separator characters that should not be present in the alphabet.
     * @return A new character array containing only the valid and unique alphabet characters.
     * @throws IllegalArgumentException if the alphabet is too short or contains space characters.
     */
    private char[] validateAndFilterAlphabet(final char[] alphabet, final char[] separators) {
        // validate size
        if (alphabet.length < MIN_ALPHABET_LENGTH) {
            throw new IllegalArgumentException(String.format(
                    "alphabet must contain at least %d unique " + "characters: %d",
                    MIN_ALPHABET_LENGTH,
                    alphabet.length));
        }

        final Set<Character> seen = new LinkedHashSet<>(alphabet.length);
        final Set<Character> invalid = IntStream.range(0, separators.length).mapToObj(idx -> separators[idx])
                .collect(Collectors.toSet());

        // add to seen set (without duplicates)
        IntStream.range(0, alphabet.length).forEach(i -> {
            if (alphabet[i] == Symbol.C_SPACE) {
                throw new IllegalArgumentException(String.format("alphabet must not contain spaces: " + "index %d", i));
            }
            final Character c = alphabet[i];
            if (!invalid.contains(c)) {
                seen.add(c);
            }
        });

        // create a new alphabet without the duplicates
        final char[] uniqueAlphabet = new char[seen.size()];
        int idx = 0;
        for (final char c : seen) {
            uniqueAlphabet[idx++] = c;
        }
        return uniqueAlphabet;
    }

    /**
     * Filters the given separator characters, keeping only those that are also present in the provided alphabet. This
     * ensures that separators are valid characters within the encoding scheme.
     *
     * @param separators The array of separator characters to filter.
     * @param alphabet   The alphabet used to determine valid characters.
     * @return A new character array containing only the valid separator characters.
     */
    private char[] filterSeparators(final char[] separators, final char[] alphabet) {
        final Set<Character> valid = IntStream.range(0, alphabet.length).mapToObj(idx -> alphabet[idx])
                .collect(Collectors.toSet());

        return IntStream.range(0, separators.length).mapToObj(idx -> (separators[idx])).filter(valid::contains)
                // ugly way to convert back to char[]
                .map(c -> Character.toString(c)).collect(Collectors.joining()).toCharArray();
    }

    /**
     * Shuffles the given alphabet using a pseudo-random process based on the provided salt. This shuffling is a core
     * part of the Hashids algorithm to ensure that the output is non-sequential and appears random.
     *
     * @param alphabet The character array representing the alphabet to be shuffled.
     * @param salt     The character array representing the salt used for shuffling.
     * @return The shuffled alphabet (the input array is modified in place).
     */
    private char[] shuffle(final char[] alphabet, final char[] salt) {
        for (int i = alphabet.length - 1, v = 0, p = 0, j, z; salt.length > 0 && i > 0; i--, v++) {
            v %= salt.length;
            p += z = salt[v];
            j = (z + v + p) % i;
            final char tmp = alphabet[j];
            alphabet[j] = alphabet[i];
            alphabet[i] = tmp;
        }
        return alphabet;
    }

}
