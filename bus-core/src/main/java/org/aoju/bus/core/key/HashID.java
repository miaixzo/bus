/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.key;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.NetKit;
import org.aoju.bus.core.toolkit.RuntimeKit;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hashids用于从数字(如YouTube和Bitly)生成短散列,
 * 数据库id,将它们用作忘记密码散列、邀请码、存储碎片号
 *
 * @author Kimi Liu
 * @version 6.2.3
 * @since JDK 1.8+
 */
public class HashID {

    /**
     * Max number that can be encoded with Hashids.
     */
    public static final long MAX_NUMBER = 9007199254740992L;

    private static final String DEFAULT_SEPS = "cfhistuCFHISTU";
    private static final String DEFAULT_SALT = Normal.EMPTY;

    private static final Pattern PATTERN = Pattern.compile("[\\w\\W]{1,12}");

    private static final int DEFAULT_MIN_HASH_LENGTH = 0;
    private static final int MIN_ALPHABET_LENGTH = 16;
    private static final double SEP_DIV = 3.5;
    private static final int GUARD_DIV = 12;

    private final String salt;
    private final int minHashLength;
    private final String alphabet;
    private final String seps;
    private final String guards;

    public HashID() {
        this(DEFAULT_SALT);
    }

    public HashID(String salt) {
        this(salt, 0);
    }

    public HashID(String salt, int minHashLength) {
        this(salt, minHashLength, Normal.UPPER_LOWER_NUMBER);
    }

    public HashID(String salt, int minHashLength, String alphabet) {
        this.salt = null != salt ? salt : DEFAULT_SALT;
        this.minHashLength = minHashLength > 0 ? minHashLength : DEFAULT_MIN_HASH_LENGTH;

        final StringBuilder uniqueAlphabet = new StringBuilder();
        for (int i = 0; i < alphabet.length(); i++) {
            if (uniqueAlphabet.indexOf(String.valueOf(alphabet.charAt(i))) == -1) {
                uniqueAlphabet.append(alphabet.charAt(i));
            }
        }

        alphabet = uniqueAlphabet.toString();

        if (alphabet.length() < MIN_ALPHABET_LENGTH) {
            throw new IllegalArgumentException(
                    "alphabet must contain at least " + MIN_ALPHABET_LENGTH + " unique characters");
        }

        if (alphabet.contains(Symbol.SPACE)) {
            throw new IllegalArgumentException("alphabet cannot contains spaces");
        }

        // seps should contain only characters present in alphabet;
        // alphabet should not contains seps
        String seps = DEFAULT_SEPS;
        for (int i = 0; i < seps.length(); i++) {
            final int j = alphabet.indexOf(seps.charAt(i));
            if (j == -1) {
                seps = seps.substring(0, i) + Symbol.SPACE + seps.substring(i + 1);
            } else {
                alphabet = alphabet.substring(0, j) + Symbol.SPACE + alphabet.substring(j + 1);
            }
        }

        alphabet = alphabet.replaceAll("\\s+", Normal.EMPTY);
        seps = seps.replaceAll("\\s+", Normal.EMPTY);
        seps = consistentShuffle(seps, this.salt);

        if ((seps.isEmpty()) || (((float) alphabet.length() / seps.length()) > SEP_DIV)) {
            int seps_len = (int) Math.ceil(alphabet.length() / SEP_DIV);

            if (seps_len == 1) {
                seps_len++;
            }

            if (seps_len > seps.length()) {
                final int diff = seps_len - seps.length();
                seps += alphabet.substring(0, diff);
                alphabet = alphabet.substring(diff);
            } else {
                seps = seps.substring(0, seps_len);
            }
        }

        alphabet = consistentShuffle(alphabet, this.salt);
        // use double to round up
        final int guardCount = (int) Math.ceil((double) alphabet.length() / GUARD_DIV);

        String guards;
        if (alphabet.length() < 3) {
            guards = seps.substring(0, guardCount);
            seps = seps.substring(guardCount);
        } else {
            guards = alphabet.substring(0, guardCount);
            alphabet = alphabet.substring(guardCount);
        }
        this.guards = guards;
        this.alphabet = alphabet;
        this.seps = seps;
    }

    public static int checkedCast(long value) {
        final int result = (int) value;
        if (result != value) {
            // don't use checkArgument here, to avoid boxing
            throw new IllegalArgumentException("Out of range: " + value);
        }
        return result;
    }

    private static String consistentShuffle(String alphabet, String salt) {
        if (salt.length() <= 0) {
            return alphabet;
        }

        int asc_val, j;
        final char[] tmpArr = alphabet.toCharArray();
        for (int i = tmpArr.length - 1, v = 0, p = 0; i > 0; i--, v++) {
            v %= salt.length();
            asc_val = salt.charAt(v);
            p += asc_val;
            j = (asc_val + v + p) % i;
            final char tmp = tmpArr[j];
            tmpArr[j] = tmpArr[i];
            tmpArr[i] = tmp;
        }

        return new String(tmpArr);
    }

    private static String hash(long input, String alphabet) {
        String hash = Normal.EMPTY;
        final int alphabetLen = alphabet.length();

        do {
            final int index = (int) (input % alphabetLen);
            if (index >= 0 && index < alphabet.length()) {
                hash = alphabet.charAt(index) + hash;
            }
            input /= alphabetLen;
        } while (input > 0);

        return hash;
    }

    private static Long unhash(String input, String alphabet) {
        long number = 0, pos;

        for (int i = 0; i < input.length(); i++) {
            pos = alphabet.indexOf(input.charAt(i));
            number = number * alphabet.length() + pos;
        }

        return number;
    }

    /**
     * Encrypt numbers to string
     *
     * @param numbers the numbers to encrypt
     * @return the encrypt string
     */
    public String encode(long... numbers) {
        if (numbers.length == 0) {
            return Normal.EMPTY;
        }

        for (final long number : numbers) {
            if (number < 0) {
                return Normal.EMPTY;
            }
            if (number > MAX_NUMBER) {
                throw new IllegalArgumentException("number can not be greater than " + MAX_NUMBER + "L");
            }
        }
        return this._encode(numbers);
    }

    /**
     * Decrypt string to numbers
     *
     * @param hash the encrypt string
     * @return decryped numbers
     */
    public long[] decode(String hash) {
        if (hash.isEmpty()) {
            return Normal.EMPTY_LONG_ARRAY;
        }

        String validChars = this.alphabet + this.guards + this.seps;
        for (int i = 0; i < hash.length(); i++) {
            if (validChars.indexOf(hash.charAt(i)) == -1) {
                return Normal.EMPTY_LONG_ARRAY;
            }
        }

        return this._decode(hash, this.alphabet);
    }

    /**
     * Encrypt hexa to string
     *
     * @param hexa the hexa to encrypt
     * @return the encrypt string
     */
    public String encodeHex(String hexa) {
        if (!hexa.matches("^[0-9a-fA-F]+$")) {
            return Normal.EMPTY;
        }

        final List<Long> matched = new ArrayList<>();
        final Matcher matcher = PATTERN.matcher(hexa);

        while (matcher.find()) {
            matched.add(Long.parseLong(Symbol.ONE + matcher.group(), 16));
        }

        // conversion
        final long[] result = new long[matched.size()];
        for (int i = 0; i < matched.size(); i++) {
            result[i] = matched.get(i);
        }

        return this.encode(result);
    }

    /**
     * Decrypt string to numbers
     *
     * @param hash the encrypt string
     * @return decryped numbers
     */
    public String decodeHex(String hash) {
        final StringBuilder result = new StringBuilder();
        final long[] numbers = this.decode(hash);

        for (final long number : numbers) {
            result.append(Long.toHexString(number).substring(1));
        }

        return result.toString();
    }

    private String _encode(long... numbers) {
        long numberHashInt = 0;
        for (int i = 0; i < numbers.length; i++) {
            numberHashInt += (numbers[i] % (i + 100));
        }
        String alphabet = this.alphabet;
        final char ret = alphabet.charAt((int) (numberHashInt % alphabet.length()));

        long num;
        long sepsIndex, guardIndex;
        String buffer;
        final StringBuilder ret_strB = new StringBuilder(this.minHashLength);
        ret_strB.append(ret);
        char guard;

        for (int i = 0; i < numbers.length; i++) {
            num = numbers[i];
            buffer = ret + this.salt + alphabet;

            alphabet = consistentShuffle(alphabet, buffer.substring(0, alphabet.length()));
            final String last = hash(num, alphabet);

            ret_strB.append(last);

            if (i + 1 < numbers.length) {
                if (last.length() > 0) {
                    num %= (last.charAt(0) + i);
                    sepsIndex = (int) (num % this.seps.length());
                } else {
                    sepsIndex = 0;
                }
                ret_strB.append(this.seps.charAt((int) sepsIndex));
            }
        }

        String ret_str = ret_strB.toString();
        if (ret_str.length() < this.minHashLength) {
            guardIndex = (numberHashInt + (ret_str.charAt(0))) % this.guards.length();
            guard = this.guards.charAt((int) guardIndex);

            ret_str = guard + ret_str;

            if (ret_str.length() < this.minHashLength) {
                guardIndex = (numberHashInt + (ret_str.charAt(2))) % this.guards.length();
                guard = this.guards.charAt((int) guardIndex);

                ret_str += guard;
            }
        }

        final int halfLen = alphabet.length() / 2;
        while (ret_str.length() < this.minHashLength) {
            alphabet = consistentShuffle(alphabet, alphabet);
            ret_str = alphabet.substring(halfLen) + ret_str + alphabet.substring(0, halfLen);
            final int excess = ret_str.length() - this.minHashLength;
            if (excess > 0) {
                final int start_pos = excess / 2;
                ret_str = ret_str.substring(start_pos, start_pos + this.minHashLength);
            }
        }

        return ret_str;
    }

    private long[] _decode(String hash, String alphabet) {
        final List<Long> ret = new ArrayList<>();
        String shuffle = alphabet;
        int i = 0;
        final String regexp = Symbol.BRACKET_LEFT + this.guards + Symbol.BRACKET_RIGHT;
        String hashBreakdown = hash.replaceAll(regexp, Symbol.SPACE);
        String[] hashArray = hashBreakdown.split(Symbol.SPACE);

        if (hashArray.length == 3 || hashArray.length == 2) {
            i = 1;
        }

        if (hashArray.length > 0) {
            hashBreakdown = hashArray[i];
            if (!hashBreakdown.isEmpty()) {
                final char lottery = hashBreakdown.charAt(0);

                hashBreakdown = hashBreakdown.substring(1);
                hashBreakdown = hashBreakdown.replaceAll(Symbol.BRACKET_LEFT + this.seps + Symbol.BRACKET_RIGHT, Symbol.SPACE);
                hashArray = hashBreakdown.split(Symbol.SPACE);

                String subHash, buffer;
                for (final String aHashArray : hashArray) {
                    subHash = aHashArray;
                    buffer = lottery + this.salt + shuffle;
                    shuffle = consistentShuffle(shuffle, buffer.substring(0, shuffle.length()));
                    ret.add(unhash(subHash, shuffle));
                }
            }
        }

        // transform from List<Long> to long[]
        long[] arr = new long[ret.size()];
        for (int k = 0; k < arr.length; k++) {
            arr[k] = ret.get(k);
        }

        if (!this.encode(arr).equals(hash)) {
            arr = Normal.EMPTY_LONG_ARRAY;
        }

        return arr;
    }

    /**
     * Get Hashid algorithm version.
     *
     * @return id algorithm version implemented.
     */
    public String getVersion() {
        return "1.0.0";
    }

    public String getSalt() {
        return salt;
    }

    /**
     * 获取数据中心ID,依赖于本地网卡MAC地址
     * <p>
     * 此算法来自于mybatis-plus#Sequence
     * </p>
     *
     * @param maxDatacenterId 最大的中心ID
     * @return 数据中心ID
     */
    public static long getDataCenterId(long maxDatacenterId) {
        long id = 1L;
        final byte[] mac = NetKit.getLocalHardwareAddress();
        if (null != mac) {
            id = ((0x000000FF & (long) mac[mac.length - 2])
                    | (0x0000FF00 & (((long) mac[mac.length - 1]) << 8))) >> 6;
            id = id % (maxDatacenterId + 1);
        }

        return id;
    }

    /**
     * 获取机器ID，使用进程ID配合数据中心ID生成
     * 依赖于本进程ID或进程名的Hash值
     * <p>
     * 此算法来自于mybatis-plus#Sequence
     * </p>
     *
     * @param datacenterId 数据中心ID
     * @param maxWorkerId  最大的机器节点ID
     */
    public static long getWorkerId(long datacenterId, long maxWorkerId) {
        final StringBuilder mpid = new StringBuilder();
        mpid.append(datacenterId);
        try {
            mpid.append(RuntimeKit.getPid());
        } catch (InstantiationError igonre) {
        }
        // MAC + PID 的 hashcode 获取16个低位
        return (mpid.toString().hashCode() & 0xffff) % (maxWorkerId + 1);
    }

}
