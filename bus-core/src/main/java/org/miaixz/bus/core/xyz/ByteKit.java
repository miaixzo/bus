/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.io.buffer.FastByteBuffer;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

/**
 * 对数字和字节进行转换。
 * 假设数据存储是以大端模式存储的：
 * <ul>
 *     <li>byte: 字节类型 占8位二进制 00000000</li>
 *     <li>char: 字符类型 占2个字节 16位二进制 byte[0] byte[1]</li>
 *     <li>int : 整数类型 占4个字节 32位二进制 byte[0] byte[1] byte[2] byte[3]</li>
 *     <li>long: 长整数类型 占8个字节 64位二进制 byte[0] byte[1] byte[2] byte[3] byte[4] byte[5]</li>
 *     <li>long: 长整数类型 占8个字节 64位二进制 byte[0] byte[1] byte[2] byte[3] byte[4] byte[5] byte[6] byte[7]</li>
 *     <li>float: 浮点数(小数) 占4个字节 32位二进制 byte[0] byte[1] byte[2] byte[3]</li>
 *     <li>double: 双精度浮点数(小数) 占8个字节 64位二进制 byte[0] byte[1] byte[2] byte[3] byte[4]byte[5] byte[6] byte[7]</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ByteKit {

    /**
     * 默认字节序：大端在前，小端在后
     */
    public static final ByteOrder DEFAULT_ORDER = ByteOrder.LITTLE_ENDIAN;
    /**
     * CPU的字节序
     */
    public static final ByteOrder CPU_ENDIAN = "little".equals(System.getProperty("sun.cpu.endian")) ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;

    /**
     * 编码字符串，编码为UTF-8
     *
     * @param text 字符串
     * @return 编码后的字节码
     */
    public static byte[] toBytes(final CharSequence text) {
        return toBytes(text, Charset.UTF_8);
    }

    /**
     * 编码字符串
     *
     * @param text    字符串
     * @param charset 字符集，如果此字段为空，则解码的结果取决于平台
     * @return 编码后的字节码
     */
    public static byte[] toBytes(final CharSequence text, final java.nio.charset.Charset charset) {
        if (text == null) {
            return null;
        }

        if (null == charset) {
            return text.toString().getBytes();
        }
        return text.toString().getBytes(charset);
    }

    /**
     * int转byte
     *
     * @param intValue int值
     * @return byte值
     */
    public static byte toByte(final int intValue) {
        return (byte) intValue;
    }

    /**
     * short转byte数组
     * 默认以小端序转换
     *
     * @param shortValue short值
     * @return byte数组
     */
    public static byte[] toBytes(final short shortValue) {
        return toBytes(shortValue, DEFAULT_ORDER);
    }

    /**
     * short转byte数组
     * 自定义端序
     *
     * @param shortValue short值
     * @param byteOrder  端序
     * @return byte数组
     */
    public static byte[] toBytes(final short shortValue, final ByteOrder byteOrder) {
        final byte[] b = new byte[Short.BYTES];
        if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
            b[0] = (byte) (shortValue & 0xff);
            b[1] = (byte) ((shortValue >> Byte.SIZE) & 0xff);
        } else {
            b[1] = (byte) (shortValue & 0xff);
            b[0] = (byte) ((shortValue >> Byte.SIZE) & 0xff);
        }
        return b;
    }

    /**
     * char转byte数组
     *
     * @param data char值
     * @return the byte
     */
    public static byte[] toBytes(char data) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (data);
        bytes[1] = (byte) (data >> 8);
        return bytes;
    }

    /**
     * char转byte数组
     *
     * @param data char值
     * @return the byte
     */
    public static byte[] toBytes(char[] data) {
        CharBuffer cb = CharBuffer.allocate(data.length);
        cb.put(data);
        cb.flip();
        ByteBuffer bb = Charset.UTF_8.encode(cb);
        return bb.array();
    }

    /**
     * int转byte数组
     * 默认以小端序转换
     *
     * @param intValue int值
     * @return byte数组
     */
    public static byte[] toBytes(final int intValue) {
        return toBytes(intValue, DEFAULT_ORDER);
    }

    /**
     * int转byte数组
     * 自定义端序
     *
     * @param intValue  int值
     * @param byteOrder 端序
     * @return byte数组
     */
    public static byte[] toBytes(final int intValue, final ByteOrder byteOrder) {

        if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
            return new byte[]{
                    (byte) (intValue & 0xFF),
                    (byte) ((intValue >> 8) & 0xFF),
                    (byte) ((intValue >> 16) & 0xFF),
                    (byte) ((intValue >> 24) & 0xFF)
            };

        } else {
            return new byte[]{
                    (byte) ((intValue >> 24) & 0xFF),
                    (byte) ((intValue >> 16) & 0xFF),
                    (byte) ((intValue >> 8) & 0xFF),
                    (byte) (intValue & 0xFF)
            };
        }

    }

    /**
     * long转byte数组
     * 默认以小端序转换
     * from: <a href="https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java">https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java</a>
     *
     * @param longValue long值
     * @return byte数组
     */
    public static byte[] toBytes(final long longValue) {
        return toBytes(longValue, DEFAULT_ORDER);
    }

    /**
     * long转byte数组
     * 自定义端序
     * from: <a href="https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java">https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java</a>
     *
     * @param longValue long值
     * @param byteOrder 端序
     * @return byte数组
     */
    public static byte[] toBytes(final long longValue, final ByteOrder byteOrder) {
        final byte[] result = new byte[Long.BYTES];
        return fill(longValue, 0, byteOrder, result);
    }

    /**
     * 将long值转为bytes并填充到给定的bytes中
     *
     * @param longValue long值
     * @param start     开始位置（包含）
     * @param byteOrder 端续
     * @param bytes     被填充的bytes
     * @return 填充后的bytes
     */
    public static byte[] fill(long longValue, final int start, final ByteOrder byteOrder, final byte[] bytes) {
        if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
            for (int i = start; i < bytes.length; i++) {
                bytes[i] = (byte) (longValue & 0xFF);
                longValue >>= Byte.SIZE;
            }
        } else {
            for (int i = (bytes.length - 1); i >= start; i--) {
                bytes[i] = (byte) (longValue & 0xFF);
                longValue >>= Byte.SIZE;
            }
        }
        return bytes;
    }

    /**
     * float转byte数组，默认以小端序转换
     *
     * @param floatValue float值
     * @return byte数组
     */
    public static byte[] toBytes(final float floatValue) {
        return toBytes(floatValue, DEFAULT_ORDER);
    }

    /**
     * float转byte数组，自定义端序
     *
     * @param floatValue float值
     * @param byteOrder  端序
     * @return byte数组
     */
    public static byte[] toBytes(final float floatValue, final ByteOrder byteOrder) {
        return toBytes(Float.floatToIntBits(floatValue), byteOrder);
    }

    /**
     * double转byte数组
     * 默认以小端序转换
     *
     * @param doubleValue double值
     * @return byte数组
     */
    public static byte[] toBytes(final double doubleValue) {
        return toBytes(doubleValue, DEFAULT_ORDER);
    }

    /**
     * double转byte数组
     * 自定义端序
     * from: <a href="https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java">https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java</a>
     *
     * @param doubleValue double值
     * @param byteOrder   端序
     * @return byte数组
     */
    public static byte[] toBytes(final double doubleValue, final ByteOrder byteOrder) {
        return toBytes(Double.doubleToLongBits(doubleValue), byteOrder);
    }

    /**
     * 将{@link Number}转换为
     *
     * @param number 数字
     * @return bytes
     */
    public static byte[] toBytes(final Number number) {
        return toBytes(number, DEFAULT_ORDER);
    }

    /**
     * 将{@link Number}转换为
     *
     * @param number    数字
     * @param byteOrder 端序
     * @return bytes
     */
    public static byte[] toBytes(final Number number, final ByteOrder byteOrder) {
        if (number instanceof Byte) {
            return new byte[]{number.byteValue()};
        } else if (number instanceof Double) {
            return toBytes(number.doubleValue(), byteOrder);
        } else if (number instanceof Long) {
            return toBytes(number.longValue(), byteOrder);
        } else if (number instanceof Integer) {
            return ByteKit.toBytes(number.intValue(), byteOrder);
        } else if (number instanceof Short) {
            return ByteKit.toBytes(number.shortValue(), byteOrder);
        } else if (number instanceof Float) {
            return toBytes(number.floatValue(), byteOrder);
        } else if (number instanceof BigInteger) {
            return ((BigInteger) number).toByteArray();
        } else {
            return toBytes(number.doubleValue(), byteOrder);
        }
    }

    /**
     * byte数组转short
     * 默认以小端序转换
     *
     * @param bytes byte数组
     * @return short值
     */
    public static short toShort(final byte[] bytes) {
        return toShort(bytes, DEFAULT_ORDER);
    }

    /**
     * byte数组转short
     * 自定义端序
     *
     * @param bytes     byte数组，长度必须为2
     * @param byteOrder 端序
     * @return short值
     */
    public static short toShort(final byte[] bytes, final ByteOrder byteOrder) {
        return toShort(bytes, 0, byteOrder);
    }

    /**
     * byte数组转short
     * 自定义端序
     *
     * @param bytes     byte数组，长度必须大于2
     * @param start     开始位置
     * @param byteOrder 端序
     * @return short值
     */
    public static short toShort(final byte[] bytes, final int start, final ByteOrder byteOrder) {
        if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
            //小端模式，数据的高字节保存在内存的高地址中，而数据的低字节保存在内存的低地址中
            return (short) (bytes[start] & 0xff | (bytes[start + 1] & 0xff) << Byte.SIZE);
        } else {
            return (short) (bytes[start + 1] & 0xff | (bytes[start] & 0xff) << Byte.SIZE);
        }
    }

    /**
     * byte[]转int值
     * 默认以小端序转换
     *
     * @param bytes byte数组
     * @return int值
     */
    public static int toInt(final byte[] bytes) {
        return toInt(bytes, DEFAULT_ORDER);
    }

    /**
     * byte[]转int值
     * 自定义端序
     *
     * @param bytes     byte数组
     * @param byteOrder 端序
     * @return int值
     */
    public static int toInt(final byte[] bytes, final ByteOrder byteOrder) {
        return toInt(bytes, 0, byteOrder);
    }

    /**
     * byte[]转int值
     * 自定义端序
     *
     * @param bytes     byte数组
     * @param start     开始位置（包含）
     * @param byteOrder 端序
     * @return int值
     */
    public static int toInt(final byte[] bytes, final int start, final ByteOrder byteOrder) {
        if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
            return bytes[start] & 0xFF | //
                    (bytes[1 + start] & 0xFF) << 8 |
                    (bytes[2 + start] & 0xFF) << 16 |
                    (bytes[3 + start] & 0xFF) << 24;
        } else {
            return bytes[3 + start] & 0xFF |
                    (bytes[2 + start] & 0xFF) << 8 |
                    (bytes[1 + start] & 0xFF) << 16 |
                    (bytes[start] & 0xFF) << 24;
        }
    }

    /**
     * byte转无符号int
     *
     * @param byteValue byte值
     * @return 无符号int值
     */
    public static int toUnsignedInt(final byte byteValue) {
        // Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return byteValue & 0xFF;
    }

    /**
     * byte数组转long
     * 默认以小端序转换
     * from: <a href="https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java">https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java</a>
     *
     * @param bytes byte数组
     * @return long值
     */
    public static long toLong(final byte[] bytes) {
        return toLong(bytes, DEFAULT_ORDER);
    }

    /**
     * byte数组转long
     * 自定义端序
     * from: <a href="https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java">https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java</a>
     *
     * @param bytes     byte数组
     * @param byteOrder 端序
     * @return long值
     */
    public static long toLong(final byte[] bytes, final ByteOrder byteOrder) {
        return toLong(bytes, 0, byteOrder);
    }

    /**
     * byte数组转long
     * 自定义端序
     * from: <a href="https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java">https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java</a>
     *
     * @param bytes     byte数组
     * @param start     计算数组开始位置
     * @param byteOrder 端序
     * @return long值
     */
    public static long toLong(final byte[] bytes, final int start, final ByteOrder byteOrder) {
        long values = 0;
        if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
            for (int i = (Long.BYTES - 1); i >= 0; i--) {
                values <<= Byte.SIZE;
                values |= (bytes[i + start] & 0xffL);
            }
        } else {
            for (int i = 0; i < Long.BYTES; i++) {
                values <<= Byte.SIZE;
                values |= (bytes[i + start] & 0xffL);
            }
        }

        return values;
    }

    /**
     * byte数组转float
     * 默认以小端序转换
     *
     * @param bytes byte数组
     * @return float值
     */
    public static float toFloat(final byte[] bytes) {
        return toFloat(bytes, DEFAULT_ORDER);
    }

    /**
     * byte数组转float
     * 自定义端序
     *
     * @param bytes     byte数组
     * @param byteOrder 端序
     * @return float值
     */
    public static float toFloat(final byte[] bytes, final ByteOrder byteOrder) {
        return Float.intBitsToFloat(toInt(bytes, byteOrder));
    }

    /**
     * byte数组转Double
     * 默认以小端序转换
     *
     * @param bytes byte数组
     * @return long值
     */
    public static double toDouble(final byte[] bytes) {
        return toDouble(bytes, DEFAULT_ORDER);
    }

    /**
     * byte数组转double
     * 自定义端序
     *
     * @param bytes     byte数组
     * @param byteOrder 端序
     * @return long值
     */
    public static double toDouble(final byte[] bytes, final ByteOrder byteOrder) {
        return Double.longBitsToDouble(toLong(bytes, byteOrder));
    }

    /**
     * byte数组转换为指定类型数字
     *
     * @param <T>         数字类型
     * @param bytes       byte数组
     * @param targetClass 目标数字类型
     * @param byteOrder   端序
     * @return 转换后的数字
     * @throws IllegalArgumentException 不支持的数字类型，如用户自定义数字类型
     */
    public static <T extends Number> T toNumber(final byte[] bytes, final Class<T> targetClass, final ByteOrder byteOrder) throws IllegalArgumentException {
        final Number number;
        if (Byte.class == targetClass) {
            number = bytes[0];
        } else if (Short.class == targetClass) {
            number = toShort(bytes, byteOrder);
        } else if (Integer.class == targetClass) {
            number = toInt(bytes, byteOrder);
        } else if (AtomicInteger.class == targetClass) {
            number = new AtomicInteger(toInt(bytes, byteOrder));
        } else if (Long.class == targetClass) {
            number = toLong(bytes, byteOrder);
        } else if (AtomicLong.class == targetClass) {
            number = new AtomicLong(toLong(bytes, byteOrder));
        } else if (LongAdder.class == targetClass) {
            final LongAdder longValue = new LongAdder();
            longValue.add(toLong(bytes, byteOrder));
            number = longValue;
        } else if (Float.class == targetClass) {
            number = toFloat(bytes, byteOrder);
        } else if (Double.class == targetClass) {
            number = toDouble(bytes, byteOrder);
        } else if (DoubleAdder.class == targetClass) {
            final DoubleAdder doubleAdder = new DoubleAdder();
            doubleAdder.add(toDouble(bytes, byteOrder));
            number = doubleAdder;
        } else if (BigDecimal.class == targetClass) {
            number = MathKit.toBigDecimal(toDouble(bytes, byteOrder));
        } else if (BigInteger.class == targetClass) {
            number = BigInteger.valueOf(toLong(bytes, byteOrder));
        } else if (Number.class == targetClass) {
            // 用户没有明确类型具体类型，默认Double
            number = toDouble(bytes, byteOrder);
        } else {
            // 用户自定义类型不支持
            throw new IllegalArgumentException("Unsupported Number type: " + targetClass.getName());
        }

        return (T) number;
    }

    /**
     * 以无符号字节数组的形式返回传入值。
     *
     * @param value 需要转换的值
     * @return 无符号bytes
     */
    public static byte[] toUnsignedByteArray(final BigInteger value) {
        final byte[] bytes = value.toByteArray();

        if (bytes[0] == 0) {
            final byte[] tmp = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, tmp, 0, tmp.length);

            return tmp;
        }

        return bytes;
    }

    /**
     * 以无符号字节数组的形式返回传入值。
     *
     * @param length bytes长度
     * @param value  需要转换的值
     * @return 无符号bytes
     */
    public static byte[] toUnsignedByteArray(final int length, final BigInteger value) {
        final byte[] bytes = value.toByteArray();
        if (bytes.length == length) {
            return bytes;
        }

        final int start = bytes[0] == 0 ? 1 : 0;
        final int count = bytes.length - start;

        if (count > length) {
            throw new IllegalArgumentException("standard length exceeded for value");
        }

        final byte[] tmp = new byte[length];
        System.arraycopy(bytes, start, tmp, tmp.length - count, count);
        return tmp;
    }

    /**
     * 无符号bytes转{@link BigInteger}
     *
     * @param buf buf 无符号bytes
     * @return {@link BigInteger}
     */
    public static BigInteger fromUnsignedByteArray(final byte[] buf) {
        return new BigInteger(1, buf);
    }

    /**
     * 无符号bytes转{@link BigInteger}
     *
     * @param buf    无符号bytes
     * @param off    起始位置
     * @param length 长度
     * @return {@link BigInteger}
     */
    public static BigInteger fromUnsignedByteArray(final byte[] buf, final int off, final int length) {
        byte[] mag = buf;
        if (off != 0 || length != buf.length) {
            mag = new byte[length];
            System.arraycopy(buf, off, mag, 0, length);
        }
        return new BigInteger(1, mag);
    }

    /**
     * 连接多个byte[]
     *
     * @param byteArrays 多个byte[]
     * @return 连接后的byte[]
     */
    public static byte[] concat(final byte[]... byteArrays) {
        int totalLength = 0;
        for (final byte[] byteArray : byteArrays) {
            totalLength += byteArray.length;
        }

        final FastByteBuffer buffer = new FastByteBuffer(totalLength);
        for (final byte[] byteArray : byteArrays) {
            buffer.append(byteArray);
        }
        return buffer.toArrayZeroCopyIfPossible();
    }

    /**
     * 统计byte中位数为1的个数
     *
     * @param buf 无符号bytes
     * @return 为 1 的个数
     * @see Integer#bitCount(int)
     */
    public static int bitCount(final byte[] buf) {
        int sum = 0;
        for (final byte b : buf) {
            sum += Integer.bitCount((b & 0xFF));
        }
        return sum;
    }

    /**
     * 统计无符号bytes转为bit位数为1的索引集合
     *
     * @param bytes 无符号bytes
     * @return 位数为1的索引集合
     */
    public static List<Integer> toUnsignedBitIndex(final byte[] bytes) {
        final List<Integer> idxList = new LinkedList<>();
        final StringBuilder sb = new StringBuilder();
        for (final byte b : bytes) {
            sb.append(StringKit.padPre(Integer.toBinaryString((b & 0xFF)), 8, "0"));
        }
        final String bitStr = sb.toString();
        for (int i = 0; i < bitStr.length(); i++) {
            if (bitStr.charAt(i) == '1') {
                idxList.add(i);
            }
        }
        return idxList;
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      byte数组
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the int
     */
    public static int bytesToInt(byte[] data, int off, boolean bigEndian) {
        return bigEndian ? bytesToIntBE(data, off) : bytesToIntLE(data, off);
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the int
     */
    public static int bytesToIntBE(byte[] data, int off) {
        return (data[off] << 24) + ((data[off + 1] & 255) << 16)
                + ((data[off + 2] & 255) << 8) + (data[off + 3] & 255);
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the int
     */
    public static int bytesToIntLE(byte[] data, int off) {
        return (data[off + 3] << 24) + ((data[off + 2] & 255) << 16)
                + ((data[off + 1] & 255) << 8) + (data[off] & 255);
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      byte数组
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the int
     */
    public static int bytesToShort(byte[] data, int off, boolean bigEndian) {
        return bigEndian ? bytesToShortBE(data, off) : bytesToShortLE(data, off);
    }

    /**
     * byte数组处理
     * 排序: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      long值
     * @param s         字符
     * @param off       偏移量
     * @param len       字符长度
     * @param bigEndian 是否大字节序列
     */
    public static void bytesToShort(byte[] data, short[] s, int off, int len, boolean bigEndian) {
        if (bigEndian)
            bytesToShortsBE(data, s, off, len);
        else
            bytesToShortLE(data, s, off, len);
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the int
     */
    public static int bytesToShortBE(byte[] data, int off) {
        return (data[off] << 8) + (data[off + 1] & 255);
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the int
     */
    public static int bytesToShortLE(byte[] data, int off) {
        return (data[off + 1] << 8) + (data[off] & 255);
    }

    /**
     * byte数组处理
     * 默认排序: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data double值
     * @param s    字符
     * @param off  偏移量
     * @param len  字符长度
     */
    public static void bytesToShortsBE(byte[] data, short[] s, int off, int len) {
        int boff = 0;
        for (int j = 0; j < len; j++) {
            int b0 = data[boff];
            int b1 = data[boff + 1] & 0xff;
            s[off + j] = (short) ((b0 << 8) | b1);
            boff += 2;
        }
    }

    /**
     * byte数组处理
     * 默认排序: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data double值
     * @param s    字符
     * @param off  偏移量
     * @param len  字符长度
     */
    public static void bytesToShortLE(byte[] data, short[] s, int off, int len) {
        int boff = 0;
        for (int j = 0; j < len; j++) {
            int b0 = data[boff + 1];
            int b1 = data[boff] & 0xff;
            s[off + j] = (short) ((b0 << 8) | b1);
            boff += 2;
        }
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data      byte数组
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the int
     */
    public static int bytesToUShort(byte[] data, int off, boolean bigEndian) {
        return bigEndian ? bytesToUShortBE(data, off) : bytesToUShortLE(data, off);
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the int
     */
    public static int bytesToUShortBE(byte[] data, int off) {
        return ((data[off] & 255) << 8) + (data[off + 1] & 255);
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the int
     */
    public static int bytesToUShortLE(byte[] data, int off) {
        return ((data[off + 1] & 255) << 8) + (data[off] & 255);
    }

    /**
     * byte数组转float
     * 默认以: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      byte数组
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the float
     */
    public static float bytesToFloat(byte[] data, int off, boolean bigEndian) {
        return bigEndian ? bytesToFloatBE(data, off) : bytesToFloatLE(data, off);
    }

    /**
     * byte数组转float
     * 默认以: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the float
     */
    public static float bytesToFloatBE(byte[] data, int off) {
        return Float.intBitsToFloat(bytesToIntBE(data, off));
    }

    /**
     * byte数组转float
     * 默认以: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the float
     */
    public static float bytesToFloatLE(byte[] data, int off) {
        return Float.intBitsToFloat(bytesToIntLE(data, off));
    }

    /**
     * byte数组转long
     * 默认以: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      byte数组
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the long
     */
    public static long bytesToLong(byte[] data, int off, boolean bigEndian) {
        return bigEndian ? bytesToLongBE(data, off) : bytesToLongLE(data, off);
    }

    /**
     * byte数组转long
     * 默认以: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the long
     */
    public static long bytesToLongBE(byte[] data, int off) {
        return ((long) data[off] << 56)
                + ((long) (data[off + 1] & 255) << 48)
                + ((long) (data[off + 2] & 255) << 40)
                + ((long) (data[off + 3] & 255) << Normal._32)
                + ((long) (data[off + 4] & 255) << 24)
                + ((data[off + 5] & 255) << Normal._16)
                + ((data[off + 6] & 255) << 8)
                + (data[off + 7] & 255);
    }

    /**
     * byte数组转long
     * 默认以: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the long
     */
    public static long bytesToLongLE(byte[] data, int off) {
        return ((long) data[off + 7] << 56)
                + ((long) (data[off + 6] & 255) << 48)
                + ((long) (data[off + 5] & 255) << 40)
                + ((long) (data[off + 4] & 255) << Normal._32)
                + ((long) (data[off + 3] & 255) << 24)
                + ((data[off + 2] & 255) << Normal._16)
                + ((data[off + 1] & 255) << 8)
                + (data[off] & 255);
    }

    /**
     * byte数组转double
     * 默认以: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      byte数组
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the double
     */
    public static double bytesToDouble(byte[] data, int off, boolean bigEndian) {
        return bigEndian ? bytesToDoubleBE(data, off) : bytesToDoubleLE(data, off);
    }

    /**
     * byte数组转double
     * 默认以: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the double
     */
    public static double bytesToDoubleBE(byte[] data, int off) {
        return Double.longBitsToDouble(bytesToLongBE(data, off));
    }

    /**
     * byte数组转double
     * 默认以: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the double
     */
    public static double bytesToDoubleLE(byte[] data, int off) {
        return Double.longBitsToDouble(bytesToLongLE(data, off));
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the int
     */
    public static int bytesToVR(byte[] data, int off) {
        return bytesToUShortBE(data, off);
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      byte数组
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the int
     */
    public static int bytesToTag(byte[] data, int off, boolean bigEndian) {
        return bigEndian ? bytesToTagBE(data, off) : bytesToTagLE(data, off);
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the int
     */
    public static int bytesToTagBE(byte[] data, int off) {
        return bytesToIntBE(data, off);
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the int
     */
    public static int bytesToTagLE(byte[] data, int off) {
        return (data[off + 1] << 24) + ((data[off] & 255) << 16)
                + ((data[off + 3] & 255) << 8) + (data[off + 2] & 255);
    }

    /**
     * int转byte数组
     * 排序: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      float值
     * @param bytes     目标字节
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the byte
     */
    public static byte[] intToBytes(int data, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? intToBytesBE(data, bytes, off) : intToBytesLE(data, bytes, off);
    }

    /**
     * int转byte数组
     * 默认排序: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data  int值
     * @param bytes 目标字节
     * @param off   偏移量
     * @return the byte
     */
    public static byte[] intToBytesBE(int data, byte[] bytes, int off) {
        bytes[off] = (byte) (data >> 24);
        bytes[off + 1] = (byte) (data >> 16);
        bytes[off + 2] = (byte) (data >> 8);
        bytes[off + 3] = (byte) data;
        return bytes;
    }

    /**
     * int转byte数组
     * 默认排序: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data  int值
     * @param bytes 目标字节
     * @param off   偏移量
     * @return the byte
     */
    public static byte[] intToBytesLE(int data, byte[] bytes, int off) {
        bytes[off + 3] = (byte) (data >> 24);
        bytes[off + 2] = (byte) (data >> 16);
        bytes[off + 1] = (byte) (data >> 8);
        bytes[off] = (byte) data;
        return bytes;
    }

    /**
     * int转byte数组
     * 排序: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      int值
     * @param bytes     目标字节
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the byte
     */
    public static byte[] shortToBytes(int data, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? shortToBytesBE(data, bytes, off) : shortToBytesLE(data, bytes, off);
    }

    /**
     * int转byte数组
     * 默认排序: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data  int值
     * @param bytes 目标字节
     * @param off   偏移量
     * @return the byte
     */
    public static byte[] shortToBytesBE(int data, byte[] bytes, int off) {
        bytes[off] = (byte) (data >> 8);
        bytes[off + 1] = (byte) data;
        return bytes;
    }

    /**
     * int转byte数组
     * 默认排序: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data  int值
     * @param bytes 目标字节
     * @param off   偏移量
     * @return the byte
     */
    public static byte[] shortToBytesLE(int data, byte[] bytes, int off) {
        bytes[off + 1] = (byte) (data >> 8);
        bytes[off] = (byte) data;
        return bytes;
    }

    /**
     * long转byte数组
     * 排序: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      long值
     * @param bytes     目标字节
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the byte
     */
    public static byte[] longToBytes(long data, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? longToBytesBE(data, bytes, off) : longToBytesLE(data, bytes, off);
    }

    /**
     * long转byte数组
     * 默认排序: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data  long值
     * @param bytes 目标字节
     * @param off   偏移量
     * @return the byte
     */
    public static byte[] longToBytesBE(long data, byte[] bytes, int off) {
        bytes[off] = (byte) (data >> 56);
        bytes[off + 1] = (byte) (data >> 48);
        bytes[off + 2] = (byte) (data >> 40);
        bytes[off + 3] = (byte) (data >> 32);
        bytes[off + 4] = (byte) (data >> 24);
        bytes[off + 5] = (byte) (data >> 16);
        bytes[off + 6] = (byte) (data >> 8);
        bytes[off + 7] = (byte) data;
        return bytes;
    }

    /**
     * long转byte数组
     * 默认排序: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data  long值
     * @param bytes 目标字节
     * @param off   偏移量
     * @return the byte
     */
    public static byte[] longToBytesLE(long data, byte[] bytes, int off) {
        bytes[off + 7] = (byte) (data >> 56);
        bytes[off + 6] = (byte) (data >> 48);
        bytes[off + 5] = (byte) (data >> 40);
        bytes[off + 4] = (byte) (data >> 32);
        bytes[off + 3] = (byte) (data >> 24);
        bytes[off + 2] = (byte) (data >> 16);
        bytes[off + 1] = (byte) (data >> 8);
        bytes[off] = (byte) data;
        return bytes;
    }

    /**
     * float转byte数组
     * 排序: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      float值
     * @param bytes     目标字节
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the byte
     */
    public static byte[] floatToBytes(float data, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? floatToBytesBE(data, bytes, off) : floatToBytesLE(data, bytes, off);
    }

    /**
     * float转byte数组
     * 默认排序: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data  double值
     * @param bytes 目标字节
     * @param off   偏移量
     * @return the byte
     */
    public static byte[] floatToBytesBE(float data, byte[] bytes, int off) {
        return intToBytesBE(Float.floatToIntBits(data), bytes, off);
    }

    /**
     * float转byte数组
     * 默认排序: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data  double值
     * @param bytes 目标字节
     * @param off   偏移量
     * @return the byte
     */
    public static byte[] floatToBytesLE(float data, byte[] bytes, int off) {
        return intToBytesLE(Float.floatToIntBits(data), bytes, off);
    }

    /**
     * double转byte数组
     * 排序: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      double值
     * @param bytes     目标字节
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the byte
     */
    public static byte[] doubleToBytes(double data, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? doubleToBytesBE(data, bytes, off) : doubleToBytesLE(data, bytes, off);
    }

    /**
     * double转byte数组
     * 默认排序: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data  double值
     * @param bytes 目标字节
     * @param off   偏移量
     * @return the byte
     */
    public static byte[] doubleToBytesBE(double data, byte[] bytes, int off) {
        return longToBytesBE(Double.doubleToLongBits(data), bytes, off);
    }

    /**
     * double转byte数组
     * 默认排序: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data  double值
     * @param bytes 目标字节
     * @param off   偏移量
     * @return the byte
     */
    public static byte[] doubleToBytesLE(double data, byte[] bytes, int off) {
        return longToBytesLE(Double.doubleToLongBits(data), bytes, off);
    }


    /**
     * int转byte数组
     * 排序: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      float值
     * @param bytes     目标字节
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the byte
     */
    public static byte[] tagToBytes(int data, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? tagToBytesBE(data, bytes, off) : tagToBytesLE(data, bytes, off);
    }

    /**
     * int转byte数组
     * 默认排序: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data  int值
     * @param bytes 目标字节
     * @param off   偏移量
     * @return the byte
     */
    public static byte[] tagToBytesBE(int data, byte[] bytes, int off) {
        return intToBytesBE(data, bytes, off);
    }

    /**
     * int转byte数组
     * 默认排序: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data  int值
     * @param bytes 目标字节
     * @param off   偏移量
     * @return the byte
     */
    public static byte[] tagToBytesLE(int data, byte[] bytes, int off) {
        bytes[off + 1] = (byte) (data >> 24);
        bytes[off] = (byte) (data >> 16);
        bytes[off + 3] = (byte) (data >> 8);
        bytes[off + 2] = (byte) data;
        return bytes;
    }

    /**
     * 字符交换
     *
     * @param data byte值
     * @param off  偏移量
     * @param len  长度
     * @return the byte
     */
    public static byte[] swapInts(byte[] data, int off, int len) {
        checkLength(len, 4);
        for (int i = off, n = off + len; i < n; i += 4) {
            swap(data, i, i + 3);
            swap(data, i + 1, i + 2);
        }
        return data;
    }

    /**
     * 字符交换
     *
     * @param data byte值
     * @param off  偏移量
     * @param len  长度
     * @return the byte
     */
    public static byte[] swapLongs(byte[] data, int off, int len) {
        checkLength(len, 8);
        for (int i = off, n = off + len; i < n; i += 8) {
            swap(data, i, i + 7);
            swap(data, i + 1, i + 6);
            swap(data, i + 2, i + 5);
            swap(data, i + 3, i + 4);
        }
        return data;
    }

    /**
     * 字符交换
     *
     * @param data byte值
     * @return the byte
     */
    public static byte[][] swapShorts(byte[][] data) {
        int carry = 0;
        for (int i = 0; i < data.length; i++) {
            byte[] b = data[i];
            if (carry != 0)
                swapLastFirst(data[i - 1], b);
            int len = b.length - carry;
            swapShorts(b, carry, len & ~1);
            carry = len & 1;
        }
        return data;
    }

    /**
     * 字符交换
     *
     * @param data byte值
     * @param off  偏移量
     * @param len  长度
     * @return the byte
     */
    public static byte[] swapShorts(byte[] data, int off, int len) {
        checkLength(len, 2);
        for (int i = off, n = off + len; i < n; i += 2)
            swap(data, i, i + 1);
        return data;
    }


    /**
     * 寻找目标字节在字节数组中的下标
     *
     * @param data   字节数组
     * @param target 目标字节
     * @param from   检索开始下标（包含）
     * @param to     检索结束下标（不包含）
     * @return 找不到则返回-1
     */
    public static int indexOf(byte[] data, byte target, int from, int to) {
        for (int i = from; i < to; i++) {
            if (data[i] == target) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 统计目标字节在字节数组中出现的次数
     *
     * @param data   字节数组
     * @param target 目标字节
     * @return the int
     */
    public static int countOf(byte[] data, byte target) {
        int count = 0;
        for (byte b : data) {
            if (b == target) {
                count++;
            }
        }
        return count;
    }

    /**
     * 解析 BCD 码
     *
     * @param data 字节数组
     * @param from 开始下标（包含）
     * @param to   结束下标（不包含）
     * @return the string
     */
    public static String bcd(byte[] data, int from, int to) {
        char[] chars = new char[2 * (to - from)];
        for (int i = from; i < to; i++) {
            int b = unsigned(data[i]);
            chars[2 * (i - from)] = (char) ((b >> 4) + 0x30);
            chars[2 * (i - from) + 1] = (char) ((b & 0xF) + 0x30);
        }
        return new String(chars);
    }

    /**
     * 无符号整数
     *
     * @param data 字节
     * @return the int
     */
    public static int unsigned(byte data) {
        if (data >= 0) {
            return data;
        }
        return Normal._256 + data;
    }

    /**
     * 异或值，返回
     *
     * @param data 数组
     * @return 异或值
     */
    public static int xor(byte[] data) {
        int temp = 0;
        if (null != data) {
            for (int i = 0; i < data.length; i++) {
                temp ^= data[i];
            }
        }
        return temp;
    }

    /**
     * 将两个字节数组连接到一个新的字节数组
     *
     * @param buf1 字节数组
     * @param buf2 字节数组
     * @return the byte
     */
    public static byte[] concat(byte[] buf1, byte[] buf2) {
        byte[] buffer = new byte[buf1.length + buf2.length];
        int offset = 0;
        System.arraycopy(buf1, 0, buffer, offset, buf1.length);
        offset += buf1.length;
        System.arraycopy(buf2, 0, buffer, offset, buf2.length);
        return buffer;
    }

    /**
     * Parse a byte array into a string of hexadecimal digits including all array bytes as digits
     *
     * @param bytes The byte array to represent
     * @return A string of hex characters corresponding to the bytes. The string is upper case.
     */
    public static String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b & 0xf0) >>> 4, 16));
            sb.append(Character.forDigit(b & 0x0f, 16));
        }
        return sb.toString().toUpperCase(Locale.ROOT);
    }

    /**
     * Parse a string of hexadecimal digits into a byte array
     *
     * @param digits The string to be parsed
     * @return a byte array with each pair of characters converted to a byte, or empty array if the string is not valid
     * hex
     */
    public static byte[] hexStringToByteArray(String digits) {
        int len = digits.length();
        // Check if string is valid hex
        if (!Pattern.VALID_HEX_PATTERN.matcher(digits).matches() || (len & 0x1) != 0) {
            return new byte[0];
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) (Character.digit(digits.charAt(i), 16) << 4
                    | Character.digit(digits.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * 字符交换
     *
     * @param len      长度
     * @param numBytes 字符
     */
    private static void checkLength(int len, int numBytes) {
        if (len < 0 || (len % numBytes) != 0)
            throw new IllegalArgumentException("length: " + len);
    }

    /**
     * 字符交换
     *
     * @param data byte值
     * @param a    字符A
     * @param b    字符B
     */
    private static void swap(byte[] data, int a, int b) {
        byte t = data[a];
        data[a] = data[b];
        data[b] = t;
    }

    /**
     * 字符交换
     *
     * @param b1 byte值
     * @param b2 byte值
     */
    private static void swapLastFirst(byte[] b1, byte[] b2) {
        int last = b1.length - 1;
        byte t = b2[0];
        b2[0] = b1[last];
        b1[last] = t;
    }

}
