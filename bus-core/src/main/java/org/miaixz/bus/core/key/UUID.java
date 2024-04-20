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
package org.miaixz.bus.core.key;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.RandomKit;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 提供通用唯一识别码(universally unique identifier)(UUID)实现,UUID表示一个128位的值
 * 此类拷贝自java.util.UUID,用于生成不带-的UUID字符串
 * 这些通用标识符具有不同的变体 此类的方法用于操作 Leach-Salz 变体,不过构造方法允许创建任何 UUID 变体(将在下面进行描述)
 * 变体 2 (Leach-Salz) UUID 的布局如下： long 型数据的最高有效位由以下无符号字段组成：
 *
 * <pre>
 * 0xFFFFFFFF00000000 time_low
 * 0x00000000FFFF0000 time_mid
 * 0x000000000000F000 version
 * 0x0000000000000FFF time_hi
 * </pre>
 * long 型数据的最低有效位由以下无符号字段组成：
 *
 * <pre>
 * 0xC000000000000000 variant
 * 0x3FFF000000000000 clock_seq
 * 0x0000FFFFFFFFFFFF node
 * </pre>
 * variant 字段包含一个表示 UUID 布局的值 以上描述的位布局仅在 UUID 的 variant 值为 2(表示 Leach-Salz 变体)时才有效
 * version 字段保存描述此 UUID 类型的值 有 4 种不同的基本 UUID 类型：基于时间的 UUID、DCE 安全 UUID、基于名称的 UUID 和随机生成的 UUID
 * 这些类型的 version 值分别为 1、2、3 和 4
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UUID implements java.io.Serializable, Comparable<UUID> {

    private static final long serialVersionUID = 1L;
    /**
     * 支持的最小进制数
     */
    private static final int MIN_RADIX = 2;
    private final static String STR_BASE = Normal.UPPER_LOWER_NUMBER;
    private final static char[] DIGITS = STR_BASE.toCharArray();
    /**
     * 支持的最大进制数
     */
    private static final int MAX_RADIX = DIGITS.length;
    private static final Map<Character, Integer> DIGIT_MAP = new HashMap<>();
    private static final SecureRandom numberGenerator = RandomKit.getSecureRandom();

    static {
        for (int i = 0; i < DIGITS.length; i++) {
            DIGIT_MAP.put(DIGITS[i], i);
        }
    }

    /**
     * 此UUID的最高64有效位
     */
    private final long mostSigBits;
    /**
     * 此UUID的最低64有效位
     */
    private final long leastSigBits;

    /**
     * 私有构造
     *
     * @param data 数据
     */
    private UUID(byte[] data) {
        long msb = 0;
        long lsb = 0;
        assert data.length == Normal._16 : "data must be 16 bytes in length";
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (data[i] & 0xff);
        }
        for (int i = 8; i < Normal._16; i++) {
            lsb = (lsb << 8) | (data[i] & 0xff);
        }
        this.mostSigBits = msb;
        this.leastSigBits = lsb;
    }

    /**
     * 使用指定的数据构造新的 UUID
     *
     * @param mostSigBits  用于 {@code UUID} 的最高有效 64 位
     * @param leastSigBits 用于 {@code UUID} 的最低有效 64 位
     */
    public UUID(long mostSigBits, long leastSigBits) {
        this.mostSigBits = mostSigBits;
        this.leastSigBits = leastSigBits;
    }

    /**
     * 根据一个范围,生成一个随机的整数
     *
     * @param min 最小值(包括)
     * @param max 最大值(包括)
     * @return 随机数
     */
    public static int random(int min, int max) {
        return numberGenerator.nextInt(max - min + 1) + min;
    }

    /**
     * 获取类型 4(伪随机生成的)UUID 的静态工厂  使用加密的本地线程伪随机数生成器生成该 UUID
     *
     * @return 随机生成的 {@code UUID}
     */
    public static UUID fastUUID() {
        return randomUUID(false);
    }

    /**
     * 获取类型 4(伪随机生成的)UUID 的静态工厂  使用加密的强伪随机数生成器生成该 UUID
     *
     * @return 随机生成的 {@code UUID}
     */
    public static UUID randomUUID() {
        return randomUUID(true);
    }

    /**
     * 获取32位UUID
     *
     * @return 随机生成ID
     */
    public static String randomUUID32() {
        return java.util.UUID.randomUUID().toString().replace(Symbol.MINUS, Normal.EMPTY);
    }

    /**
     * 获取19位的UUID
     *
     * @return 随机生成ID
     */
    public static String randomUUID19() {
        // 产生UUID
        java.util.UUID uuid = java.util.UUID.randomUUID();
        // 分区转换
        return digits(uuid.getMostSignificantBits() >> Normal._32, 8) +
                digits(uuid.getMostSignificantBits() >> Normal._16, 4) +
                digits(uuid.getMostSignificantBits(), 4) +
                digits(uuid.getLeastSignificantBits() >> 48, 4) +
                digits(uuid.getLeastSignificantBits(), 12);
    }

    /**
     * 获取15位的UUID(精度有所损失)
     *
     * @return 随机生成ID
     */
    public static String randomUUID15() {
        return UUIDMaker.generate();
    }

    /**
     * 获取15位的Long型UUID(精度有所损失)
     *
     * @return 随机生成ID
     */
    public static long randomUUID15Long() {
        return toNumber(randomUUID15(), 10);
    }

    public static String randomUUIDBase64() {
        java.util.UUID uuid = java.util.UUID.randomUUID();
        byte[] byUuid = new byte[Normal._16];
        long least = uuid.getLeastSignificantBits();
        long most = uuid.getMostSignificantBits();
        long2bytes(most, byUuid, 0);
        long2bytes(least, byUuid, 8);

        return Base64.getEncoder().encodeToString(byUuid);
    }

    /**
     * 获取类型 4(伪随机生成的)UUID 的静态工厂  使用加密的强伪随机数生成器生成该 UUID
     *
     * @param isSecure 是否使用{@link SecureRandom}如果是可以获得更安全的随机码,否则可以得到更好的性能
     * @return 随机生成的 {@code UUID}
     */
    public static UUID randomUUID(boolean isSecure) {
        final Random ng = isSecure ? numberGenerator : RandomKit.getRandom();

        byte[] randomBytes = new byte[Normal._16];
        ng.nextBytes(randomBytes);
        randomBytes[6] &= 0x0f; /* clear version */
        randomBytes[6] |= 0x40; /* set to version 4 */
        randomBytes[8] &= 0x3f; /* clear variant */
        randomBytes[8] |= 0x80; /* set to IETF variant */
        return new UUID(randomBytes);
    }

    /**
     * 根据指定的字节数组获取类型 3(基于名称的)UUID 的静态工厂
     *
     * @param name 用于构造 UUID 的字节数组
     * @return 根据指定数组生成的 {@code UUID}
     */
    public static UUID nameUUIDFromBytes(byte[] name) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(Algorithm.MD5.getValue());
        } catch (NoSuchAlgorithmException nsae) {
            throw new InternalError("MD5 not supported");
        }
        byte[] md5Bytes = md.digest(name);
        md5Bytes[6] &= 0x0f; /* clear version */
        md5Bytes[6] |= 0x30; /* set to version 3 */
        md5Bytes[8] &= 0x3f; /* clear variant */
        md5Bytes[8] |= 0x80; /* set to IETF variant */
        return new UUID(md5Bytes);
    }

    /**
     * 根据 {@link #toString()} 方法中描述的字符串标准表示形式创建{@code UUID}
     *
     * @param name 指定 {@code UUID} 字符串
     * @return 具有指定值的 {@code UUID}
     * @throws IllegalArgumentException 如果 name 与 {@link #toString} 中描述的字符串表示形式不符抛出此异常
     */
    public static UUID fromString(String name) {
        String[] components = name.split(Symbol.MINUS);
        if (components.length != 5) {
            throw new IllegalArgumentException("Invalid UUID string: " + name);
        }
        for (int i = 0; i < 5; i++) {
            components[i] = "0x" + components[i];
        }

        long mostSigBits = Long.decode(components[0]).longValue();
        mostSigBits <<= Normal._16;
        mostSigBits |= Long.decode(components[1]).longValue();
        mostSigBits <<= Normal._16;
        mostSigBits |= Long.decode(components[2]).longValue();

        long leastSigBits = Long.decode(components[3]).longValue();
        leastSigBits <<= 48;
        leastSigBits |= Long.decode(components[4]).longValue();

        return new UUID(mostSigBits, leastSigBits);
    }

    /**
     * 返回指定数字对应的hex值
     *
     * @param val    值
     * @param digits 位
     * @return 值
     */
    private static String digits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }

    private static void long2bytes(long value, byte[] bytes, int offset) {
        for (int i = 7; i > -1; i--) {
            bytes[offset++] = (byte) ((value >> 8 * i) & 0xFF);
        }
    }

    /**
     * 将字符串转换为长整型数字
     *
     * @param text  数字字符串
     * @param radix 进制数
     */
    private static long toNumber(String text, int radix) {
        if (null == text) {
            throw new NumberFormatException("The text cannot be null");
        }
        if (radix < MIN_RADIX) {
            throw new NumberFormatException("radix " + radix + " less than Numbers.MIN_RADIX");
        }
        if (radix > MAX_RADIX) {
            throw new NumberFormatException("radix " + radix + " greater than Numbers.MAX_RADIX");
        }

        boolean negative = false;
        Integer digit, i = 0, len = text.length();
        long result = 0, limit = -Long.MAX_VALUE, multmin;
        if (len <= 0) {
            throw forInputString(text);
        }

        char firstChar = text.charAt(0);
        if (firstChar < '0') {
            if (firstChar == Symbol.C_MINUS) {
                negative = true;
                limit = Long.MIN_VALUE;
            } else if (firstChar != Symbol.C_PLUS) {
                throw forInputString(text);
            }
            if (len == 1) {
                throw forInputString(text);
            }
            i++;
        }

        multmin = limit / radix;
        while (i < len) {
            digit = DIGIT_MAP.get(text.charAt(i++));
            if (null == digit || digit < 0 || result < multmin) {
                throw forInputString(text);
            }
            result *= radix;
            if (result < limit + digit) {
                throw forInputString(text);
            }
            result -= digit;
        }

        return negative ? result : -result;
    }

    private static NumberFormatException forInputString(String s) {
        return new NumberFormatException("For input string: " + s);
    }

    /**
     * 返回此 UUID 的 128 位值中的最低有效 64 位
     *
     * @return 此 UUID 的 128 位值中的最低有效 64 位
     */
    public long getLeastSignificantBits() {
        return leastSigBits;
    }

    /**
     * 返回此 UUID 的 128 位值中的最高有效 64 位
     *
     * @return 此 UUID 的 128 位值中最高有效 64 位
     */
    public long getMostSignificantBits() {
        return mostSigBits;
    }

    /**
     * 与此 {@code UUID} 相关联的版本号. 版本号描述此 {@code UUID} 是如何生成的
     * <p>
     * 版本号具有以下含意:
     * <ul>
     * <li>1 基于时间的 UUID
     * <li>2 DCE 安全 UUID
     * <li>3 基于名称的 UUID
     * <li>4 随机生成的 UUID
     * </ul>
     *
     * @return 此 {@code UUID} 的版本号
     */
    public int version() {
        return (int) ((mostSigBits >> 12) & 0x0f);
    }

    /**
     * 与此 {@code UUID} 相关联的变体号 变体号描述 {@code UUID} 的布局
     * <p>
     * 变体号具有以下含意：
     * <ul>
     * <li>0 为 NCS 向后兼容保留
     * <li>2 <a href="http://www.ietf.org/rfc/rfc4122.txt">IETF&nbsp;RFC&nbsp;4122</a>(Leach-Salz), 用于此类
     * <li>6 保留,微软向后兼容
     * <li>7 保留供以后定义使用
     * </ul>
     *
     * @return 此 {@code UUID} 相关联的变体号
     */
    public int variant() {
        return (int) ((leastSigBits >>> (64 - (leastSigBits >>> 62))) & (leastSigBits >> 63));
    }

    /**
     * 与此 UUID 相关联的时间戳值
     *
     * <p>
     * 60 位的时间戳值根据此 {@code UUID} 的 time_low、time_mid 和 time_hi 字段构造
     * 所得到的时间戳以 100 毫微秒为单位,从 UTC(通用协调时间) 1582 年 10 月 15 日零时开始
     *
     * <p>
     * 时间戳值仅在在基于时间的 UUID(其 version 类型为 1)中才有意义
     * 如果此 {@code UUID} 不是基于时间的 UUID,则此方法抛出 UnsupportedOperationException
     *
     * @return the long
     * @throws UnsupportedOperationException 如果此 {@code UUID} 不是 version 为 1 的 UUID
     */
    public long timestamp() throws UnsupportedOperationException {
        checkTimeBase();
        return (mostSigBits & 0x0FFFL) << 48
                | ((mostSigBits >> Normal._16) & 0x0FFFFL) << Normal._32
                | mostSigBits >>> Normal._32;
    }

    /**
     * 与此 UUID 相关联的时钟序列值
     *
     * <p>
     * 14 位的时钟序列值根据此 UUID 的 clock_seq 字段构造 clock_seq 字段用于保证在基于时间的 UUID 中的时间唯一性
     * <p>
     * {@code clockSequence} 值仅在基于时间的 UUID(其 version 类型为 1)中才有意义  如果此 UUID 不是基于时间的 UUID,则此方法抛出 UnsupportedOperationException
     *
     * @return 此 {@code UUID} 的时钟序列
     * @throws UnsupportedOperationException 如果此 UUID 的 version 不为 1
     */
    public int clockSequence() throws UnsupportedOperationException {
        checkTimeBase();
        return (int) ((leastSigBits & 0x3FFF000000000000L) >>> 48);
    }

    /**
     * 与此 UUID 相关的节点值
     *
     * <p>
     * 48 位的节点值根据此 UUID 的 node 字段构造 此字段旨在用于保存机器的 IEEE 802 地址,该地址用于生成此 UUID 以保证空间唯一性
     * <p>
     * 节点值仅在基于时间的 UUID(其 version 类型为 1)中才有意义
     * 如果此 UUID 不是基于时间的 UUID,则此方法抛出 UnsupportedOperationException
     *
     * @return 此 {@code UUID} 的节点值
     * @throws UnsupportedOperationException 如果此 UUID 的 version 不为 1
     */
    public long node() throws UnsupportedOperationException {
        checkTimeBase();
        return leastSigBits & 0x0000FFFFFFFFFFFFL;
    }

    /**
     * 返回此{@code UUID} 的字符串表现形式
     *
     * <p>
     * UUID 的字符串表示形式由此 BNF 描述：
     *
     * <pre>
     * {@code
     * UUID                   = <time_low>-<time_mid>-<time_high_and_version>-<variant_and_sequence>-<node>
     * time_low               = 4*<hexOctet>
     * time_mid               = 2*<hexOctet>
     * time_high_and_version  = 2*<hexOctet>
     * variant_and_sequence   = 2*<hexOctet>
     * node                   = 6*<hexOctet>
     * hexOctet               = <hexDigit><hexDigit>
     * hexDigit               = [0-9a-fA-F]
     * }
     * </pre>
     *
     * @return 此{@code UUID} 的字符串表现形式
     * @see #toString(boolean)
     */
    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * 返回此{@code UUID} 的字符串表现形式
     *
     * <p>
     * UUID 的字符串表示形式由此 BNF 描述：
     *
     * <pre>
     * {@code
     * UUID                   = <time_low>-<time_mid>-<time_high_and_version>-<variant_and_sequence>-<node>
     * time_low               = 4*<hexOctet>
     * time_mid               = 2*<hexOctet>
     * time_high_and_version  = 2*<hexOctet>
     * variant_and_sequence   = 2*<hexOctet>
     * node                   = 6*<hexOctet>
     * hexOctet               = <hexDigit><hexDigit>
     * hexDigit               = [0-9a-fA-F]
     * }
     * </pre>
     *
     * @param isSimple 是否简单模式,简单模式为不带'-'的UUID字符串
     * @return 此{@code UUID} 的字符串表现形式
     */
    public String toString(boolean isSimple) {
        final StringBuilder builder = new StringBuilder(isSimple ? Normal._32 : 36);
        // time_low
        builder.append(digits(mostSigBits >> Normal._32, 8));
        if (false == isSimple) {
            builder.append(Symbol.C_MINUS);
        }
        // time_mid
        builder.append(digits(mostSigBits >> Normal._16, 4));
        if (false == isSimple) {
            builder.append(Symbol.C_MINUS);
        }
        // time_high_and_version
        builder.append(digits(mostSigBits, 4));
        if (false == isSimple) {
            builder.append(Symbol.C_MINUS);
        }
        // variant_and_sequence
        builder.append(digits(leastSigBits >> 48, 4));
        if (false == isSimple) {
            builder.append(Symbol.C_MINUS);
        }
        // node
        builder.append(digits(leastSigBits, 12));

        return builder.toString();
    }

    /**
     * 返回此 UUID 的哈希码
     *
     * @return UUID 的哈希码值
     */
    public int hashCode() {
        long hilo = mostSigBits ^ leastSigBits;
        return ((int) (hilo >> Normal._32)) ^ (int) hilo;
    }

    /**
     * 将此对象与指定对象比较
     * <p>
     * 当且仅当参数不为 {@code null}、而是一个 UUID 对象、具有与此 UUID 相同的 varriant、包含相同的值(每一位均相同)时,结果才为 {@code true}
     *
     * @param object 要与之比较的对象
     * @return 如果对象相同, 则返回 {@code true}；否则返回 {@code false}
     */
    public boolean equals(Object object) {
        if ((null == object) || (object.getClass() != UUID.class)) {
            return false;
        }
        UUID id = (UUID) object;
        return (mostSigBits == id.mostSigBits && leastSigBits == id.leastSigBits);
    }

    /**
     * 将此 UUID 与指定的 UUID 比较
     *
     * <p>
     * 如果两个 UUID 不同,且第一个 UUID 的最高有效字段大于第二个 UUID 的对应字段,则第一个 UUID 大于第二个 UUID
     *
     * @param val 与此 UUID 比较的 UUID
     * @return 在此 UUID 小于、等于或大于 val 时,分别返回 -1、0 或 1
     */
    public int compareTo(UUID val) {
        return this.mostSigBits < val.mostSigBits ? -1 :
                (this.mostSigBits > val.mostSigBits ? 1 :
                        (this.leastSigBits < val.leastSigBits ? -1 :
                                (this.leastSigBits > val.leastSigBits ? 1 : 0)
                        ));
    }

    /**
     * 检查是否为time-based版本UUID
     */
    private void checkTimeBase() {
        if (version() != 1) {
            throw new UnsupportedOperationException("Not a time-based UUID");
        }
    }

    private static class UUIDMaker {

        private final static String PIX_TEXT = Normal.LOWER_NUMBER;
        private final static int PIX_LEN = PIX_TEXT.length();
        private static volatile int pixOne = 0;
        private static volatile int pixTwo = 0;
        private static volatile int pixThree = 0;
        private static volatile int pixFour = 0;

        /**
         * 生成短时间内不会重复的长度为15位的字符串，主要用于模块数据库主键生成使用
         * 生成策略为获取自1970年1月1日零时零分零秒至当前时间的毫秒数的16进制字符串值，该字符串值为11位
         * 并追加四位"0-z"的自增字符串
         * 如果系统时间设置为大于<b>2304-6-27 7:00:26<b/>的时间，将会报错！
         * 由于系统返回的毫秒数与操作系统关系很大，所以本方法并不准确
         * 本方法可以保证在系统返回的一个毫秒数内生成36的4次方个(1679616)ID不重复
         */
        private synchronized static String generate() {
            String hexString = Long.toHexString(System.currentTimeMillis());
            pixFour++;
            if (pixFour == PIX_LEN) {
                pixFour = 0;
                pixThree++;
                if (pixThree == PIX_LEN) {
                    pixThree = 0;
                    pixTwo++;
                    if (pixTwo == PIX_LEN) {
                        pixTwo = 0;
                        pixOne++;
                        if (pixOne == PIX_LEN) {
                            pixOne = 0;
                        }
                    }
                }
            }
            return hexString + PIX_TEXT.charAt(pixOne) + PIX_TEXT.charAt(pixTwo) +
                    PIX_TEXT.charAt(pixThree) + PIX_TEXT.charAt(pixFour);
        }
    }

}
