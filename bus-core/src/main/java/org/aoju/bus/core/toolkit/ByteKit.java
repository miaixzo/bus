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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.core.toolkit;

import org.aoju.bus.core.lang.Charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * @author Kimi Liu
 * @version 6.1.6
 * @since JDK 1.8+
 */
public class ByteKit {

    public static byte[] getBytes(int data) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (data & 0xff);
        bytes[1] = (byte) ((data & 0xff00) >> 8);
        bytes[2] = (byte) ((data & 0xff0000) >> 16);
        bytes[3] = (byte) ((data & 0xff000000) >> 24);
        return bytes;
    }

    public static byte[] getBytes(long data) {
        byte[] bytes = new byte[8];
        bytes[0] = (byte) (data & 0xff);
        bytes[1] = (byte) ((data >> 8) & 0xff);
        bytes[2] = (byte) ((data >> 16) & 0xff);
        bytes[3] = (byte) ((data >> 24) & 0xff);
        bytes[4] = (byte) ((data >> 32) & 0xff);
        bytes[5] = (byte) ((data >> 40) & 0xff);
        bytes[6] = (byte) ((data >> 48) & 0xff);
        bytes[7] = (byte) ((data >> 56) & 0xff);
        return bytes;
    }

    public static byte[] getBytes(float data) {
        int intBits = Float.floatToIntBits(data);
        return getBytes(intBits);
    }

    public static byte[] getBytes(double data) {
        long intBits = Double.doubleToLongBits(data);
        return getBytes(intBits);
    }

    public static byte[] getBytes(short data) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (data & 0xff);
        bytes[1] = (byte) ((data & 0xff00) >> 8);
        return bytes;
    }

    public static byte[] getBytes(char data) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (data);
        bytes[1] = (byte) (data >> 8);
        return bytes;
    }

    public static byte[] getBytes(String data) {
        return getBytes(data, Charset.DEFAULT_GBK);
    }

    public static byte[] getBytes(String data, String charsetName) {
        return data.getBytes(Charset.charset(charsetName));
    }

    public static byte[] getBytes(char[] data) {
        CharBuffer cb = CharBuffer.allocate(data.length);
        cb.put(data);
        cb.flip();
        ByteBuffer bb = Charset.UTF_8.encode(cb);
        return bb.array();
    }

    public static int getInt(byte[] bytes) {
        return (0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)) | (0xff0000 & (bytes[2] << 16)) | (0xff000000 & (bytes[3] << 24));
    }

    public static long getLong(byte[] bytes) {
        return (0xffL & (long) bytes[0]) | (0xff00L & ((long) bytes[1] << 8)) | (0xff0000L & ((long) bytes[2] << 16)) | (0xff000000L & ((long) bytes[3] << 24))
                | (0xff00000000L & ((long) bytes[4] << 32)) | (0xff0000000000L & ((long) bytes[5] << 40)) | (0xff000000000000L & ((long) bytes[6] << 48)) | (0xff00000000000000L & ((long) bytes[7] << 56));
    }

    public static float getFloat(byte[] bytes) {
        return Float.intBitsToFloat(getInt(bytes));
    }

    public static double getDouble(byte[] bytes) {
        return Double.longBitsToDouble(getLong(bytes));
    }

    public static short getShort(byte[] bytes) {
        return (short) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
    }

    public static char getChar(byte[] bytes) {
        return (char) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
    }

    public static String getString(byte[] bytes) {
        return getString(bytes, "GBK");
    }

    public static String getString(byte[] bytes, String charsetName) {
        return new String(bytes, Charset.charset(charsetName));
    }

    /**
     * 根据分隔符拆分字节数组
     *
     * @param arr       字节数组
     * @param separator 分隔符
     * @return the byte
     */
    public static byte[][] split(byte[] arr, byte separator) {
        int count = countOf(arr, separator);
        byte[][] result = new byte[count + 1][];
        int index = indexOf(arr, separator, 0, arr.length);
        if (index < 0) {
            result[0] = arr;
        } else {
            result[0] = subArray(arr, 0, index);
            for (int i = 1; i <= count; i++) {
                int from = index + 1;
                index = indexOf(arr, separator, from, arr.length);
                if (index < from) {
                    index = arr.length;
                }
                result[i] = subArray(arr, from, index);
            }
        }
        return result;
    }


    /**
     * 拆分byte数组为几个等份(最后一份可能小于len)
     *
     * @param array 数组
     * @param len   每个小节的长度
     * @return 拆分后的数组
     */
    public static byte[][] split(byte[] array, int len) {
        int x = array.length / len;
        int y = array.length % len;
        int z = 0;
        if (y != 0) {
            z = 1;
        }
        byte[][] arrays = new byte[x + z][];
        byte[] arr;
        for (int i = 0; i < x + z; i++) {
            arr = new byte[len];
            if (i == x + z - 1 && y != 0) {
                System.arraycopy(array, i * len, arr, 0, y);
            } else {
                System.arraycopy(array, i * len, arr, 0, len);
            }
            arrays[i] = arr;
        }
        return arrays;
    }

    /**
     * 截取子数组
     *
     * @param arr  字节数组
     * @param from 开始下标（包含）
     * @return the byte
     */
    public static byte[] subArray(byte[] arr, int from) {
        return subArray(arr, from, arr.length);
    }


    /**
     * 截取子数组
     *
     * @param arr  字节数组
     * @param from 开始下标（包含）
     * @param to   结束下标（不包含）
     * @return the byte
     */
    public static byte[] subArray(byte[] arr, int from, int to) {
        byte[] result = new byte[to - from];
        if (to > from) {
            System.arraycopy(arr, from, result, 0, result.length);
        }
        return result;
    }

    /**
     * 连接多个字节数组
     *
     * @param hyphen 连字符
     * @param arrs   二维数组
     * @return the byte
     */
    public static byte[] join(byte hyphen, byte[]... arrs) {
        int length = 0;
        for (byte[] arr : arrs) {
            length += arr.length;
        }
        byte[] result = new byte[length + arrs.length - 1];
        int index = 0;
        for (int i = 0; i < arrs.length; i++) {
            byte[] arr = arrs[i];
            System.arraycopy(arr, 0, result, index, arr.length);
            index += arr.length;
            if (i < arrs.length - 1) {
                result[index] = hyphen;
                index++;
            }
        }
        return result;
    }

    /**
     * 连接多个字节数组
     *
     * @param arrs 二维数组
     * @return the byte
     */
    public static byte[] join(byte[]... arrs) {
        int length = 0;
        for (byte[] arr : arrs) {
            length += arr.length;
        }
        byte[] result = new byte[length];
        int index = 0;
        for (byte[] arr : arrs) {
            System.arraycopy(arr, 0, result, index, arr.length);
            index += arr.length;
        }
        return result;
    }

    /**
     * 首尾去掉空字符
     *
     * @param src   字节数组
     * @param empty 空数组
     * @return the byte
     */
    public static byte[] trim(byte[] src, byte empty) {
        return trim(src, empty, 0, src.length);
    }

    /**
     * 从 from 到 to 截取子串 并 首尾去掉空字符
     *
     * @param src   字节数组
     * @param empty 空字节
     * @param from  开始下标（包含）
     * @param to    结束下标（不包含）
     * @return the byte
     */
    public static byte[] trim(byte[] src, byte empty, int from, int to) {
        while (from < src.length - 1 && src[from] == empty) {
            from++;
        }
        while (to > from && src[to - 1] == empty) {
            to--;
        }
        return subArray(src, from, to);
    }


    /**
     * 寻找目标字节在字节数组中的下标
     *
     * @param arr    字节数组
     * @param target 目标字节
     * @param from   检索开始下标（包含）
     * @param to     检索结束下标（不包含）
     * @return 找不到则返回-1
     */
    public static int indexOf(byte[] arr, byte target, int from, int to) {
        for (int i = from; i < to; i++) {
            if (arr[i] == target) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 统计目标字节在字节数组中出现的次数
     *
     * @param arr    字节数组
     * @param target 目标字节
     * @return the int
     */
    public static int countOf(byte[] arr, byte target) {
        int count = 0;
        for (byte b : arr) {
            if (b == target) {
                count++;
            }
        }
        return count;
    }


    /**
     * 解析 BCD 码
     *
     * @param src  字节数组
     * @param from 开始下标（包含）
     * @param to   结束下标（不包含）
     * @return the string
     */
    public static String bcd(byte[] src, int from, int to) {
        char[] chars = new char[2 * (to - from)];
        for (int i = from; i < to; i++) {
            int b = unsigned(src[i]);
            chars[2 * (i - from)] = (char) ((b >> 4) + 0x30);
            chars[2 * (i - from) + 1] = (char) ((b & 0xF) + 0x30);
        }
        return new String(chars);
    }

    /**
     * 无符号整数
     *
     * @param value 字节
     * @return the int
     */
    public static int unsigned(byte value) {
        if (value >= 0) {
            return value;
        }
        return 256 + value;
    }

    /**
     * 异或值，返回
     *
     * @param bytes 数组
     * @return 异或值
     */
    public static int xor(byte[] bytes) {
        int temp = 0;
        if (bytes != null) {
            for (int i = 0; i < bytes.length; i++) {
                temp ^= bytes[i];
            }
        }
        return temp;
    }

    public static int bytesToVR(byte[] bytes, int off) {
        return bytesToUShortBE(bytes, off);
    }

    public static int bytesToUShort(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToUShortBE(bytes, off)
                : bytesToUShortLE(bytes, off);
    }

    public static int bytesToUShortBE(byte[] bytes, int off) {
        return ((bytes[off] & 255) << 8) + (bytes[off + 1] & 255);
    }

    public static int bytesToUShortLE(byte[] bytes, int off) {
        return ((bytes[off + 1] & 255) << 8) + (bytes[off] & 255);
    }

    public static int bytesToShortBE(byte[] bytes, int off) {
        return (bytes[off] << 8) + (bytes[off + 1] & 255);
    }

    public static int bytesToShortLE(byte[] bytes, int off) {
        return (bytes[off + 1] << 8) + (bytes[off] & 255);
    }

    public static void bytesToShortLE(byte[] b, short[] s, int off, int len) {
        int boff = 0;
        for (int j = 0; j < len; j++) {
            int b0 = b[boff + 1];
            int b1 = b[boff] & 0xff;
            s[off + j] = (short) ((b0 << 8) | b1);
            boff += 2;
        }
    }

    public static void bytesToShortsBE(byte[] b, short[] s, int off, int len) {
        int boff = 0;
        for (int j = 0; j < len; j++) {
            int b0 = b[boff];
            int b1 = b[boff + 1] & 0xff;
            s[off + j] = (short) ((b0 << 8) | b1);
            boff += 2;
        }
    }

    public static int bytesToShort(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToShortBE(bytes, off)
                : bytesToShortLE(bytes, off);
    }

    public static void bytesToShort(byte[] b, short[] s, int off, int len, boolean bigEndian) {
        if (bigEndian)
            bytesToShortsBE(b, s, off, len);
        else
            bytesToShortLE(b, s, off, len);
    }

    public static int bytesToInt(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToIntBE(bytes, off) : bytesToIntLE(bytes, off);
    }

    public static int bytesToIntBE(byte[] bytes, int off) {
        return (bytes[off] << 24) + ((bytes[off + 1] & 255) << 16)
                + ((bytes[off + 2] & 255) << 8) + (bytes[off + 3] & 255);
    }

    public static int bytesToIntLE(byte[] bytes, int off) {
        return (bytes[off + 3] << 24) + ((bytes[off + 2] & 255) << 16)
                + ((bytes[off + 1] & 255) << 8) + (bytes[off] & 255);
    }

    public static int bytesToTag(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToTagBE(bytes, off) : bytesToTagLE(bytes, off);
    }

    public static int bytesToTagBE(byte[] bytes, int off) {
        return bytesToIntBE(bytes, off);
    }

    public static int bytesToTagLE(byte[] bytes, int off) {
        return (bytes[off + 1] << 24) + ((bytes[off] & 255) << 16)
                + ((bytes[off + 3] & 255) << 8) + (bytes[off + 2] & 255);
    }

    public static float bytesToFloat(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToFloatBE(bytes, off)
                : bytesToFloatLE(bytes, off);
    }

    public static float bytesToFloatBE(byte[] bytes, int off) {
        return Float.intBitsToFloat(bytesToIntBE(bytes, off));
    }

    public static float bytesToFloatLE(byte[] bytes, int off) {
        return Float.intBitsToFloat(bytesToIntLE(bytes, off));
    }

    public static long bytesToLong(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToLongBE(bytes, off)
                : bytesToLongLE(bytes, off);
    }

    public static long bytesToLongBE(byte[] bytes, int off) {
        return ((long) bytes[off] << 56)
                + ((long) (bytes[off + 1] & 255) << 48)
                + ((long) (bytes[off + 2] & 255) << 40)
                + ((long) (bytes[off + 3] & 255) << 32)
                + ((long) (bytes[off + 4] & 255) << 24)
                + ((bytes[off + 5] & 255) << 16)
                + ((bytes[off + 6] & 255) << 8)
                + (bytes[off + 7] & 255);
    }

    public static long bytesToLongLE(byte[] bytes, int off) {
        return ((long) bytes[off + 7] << 56)
                + ((long) (bytes[off + 6] & 255) << 48)
                + ((long) (bytes[off + 5] & 255) << 40)
                + ((long) (bytes[off + 4] & 255) << 32)
                + ((long) (bytes[off + 3] & 255) << 24)
                + ((bytes[off + 2] & 255) << 16)
                + ((bytes[off + 1] & 255) << 8)
                + (bytes[off] & 255);
    }

    public static double bytesToDouble(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToDoubleBE(bytes, off)
                : bytesToDoubleLE(bytes, off);
    }

    public static double bytesToDoubleBE(byte[] bytes, int off) {
        return Double.longBitsToDouble(bytesToLongBE(bytes, off));
    }

    public static double bytesToDoubleLE(byte[] bytes, int off) {
        return Double.longBitsToDouble(bytesToLongLE(bytes, off));
    }

    public static byte[] shortToBytes(int i, byte[] bytes, int off,
                                      boolean bigEndian) {
        return bigEndian ? shortToBytesBE(i, bytes, off)
                : shortToBytesLE(i, bytes, off);
    }

    public static byte[] shortToBytesBE(int i, byte[] bytes, int off) {
        bytes[off] = (byte) (i >> 8);
        bytes[off + 1] = (byte) i;
        return bytes;
    }

    public static byte[] shortToBytesLE(int i, byte[] bytes, int off) {
        bytes[off + 1] = (byte) (i >> 8);
        bytes[off] = (byte) i;
        return bytes;
    }

    public static byte[] intToBytes(int i, byte[] bytes, int off,
                                    boolean bigEndian) {
        return bigEndian ? intToBytesBE(i, bytes, off)
                : intToBytesLE(i, bytes, off);
    }

    public static byte[] intToBytesBE(int i, byte[] bytes, int off) {
        bytes[off] = (byte) (i >> 24);
        bytes[off + 1] = (byte) (i >> 16);
        bytes[off + 2] = (byte) (i >> 8);
        bytes[off + 3] = (byte) i;
        return bytes;
    }

    public static byte[] intToBytesLE(int i, byte[] bytes, int off) {
        bytes[off + 3] = (byte) (i >> 24);
        bytes[off + 2] = (byte) (i >> 16);
        bytes[off + 1] = (byte) (i >> 8);
        bytes[off] = (byte) i;
        return bytes;
    }

    public static byte[] tagToBytes(int i, byte[] bytes, int off,
                                    boolean bigEndian) {
        return bigEndian ? tagToBytesBE(i, bytes, off)
                : tagToBytesLE(i, bytes, off);
    }

    public static byte[] tagToBytesBE(int i, byte[] bytes, int off) {
        return intToBytesBE(i, bytes, off);
    }

    public static byte[] tagToBytesLE(int i, byte[] bytes, int off) {
        bytes[off + 1] = (byte) (i >> 24);
        bytes[off] = (byte) (i >> 16);
        bytes[off + 3] = (byte) (i >> 8);
        bytes[off + 2] = (byte) i;
        return bytes;
    }

    public static byte[] floatToBytes(float f, byte[] bytes, int off,
                                      boolean bigEndian) {
        return bigEndian ? floatToBytesBE(f, bytes, off)
                : floatToBytesLE(f, bytes, off);
    }

    public static byte[] floatToBytesBE(float f, byte[] bytes, int off) {
        return intToBytesBE(Float.floatToIntBits(f), bytes, off);
    }

    public static byte[] floatToBytesLE(float f, byte[] bytes, int off) {
        return intToBytesLE(Float.floatToIntBits(f), bytes, off);
    }

    public static byte[] doubleToBytes(double d, byte[] bytes, int off,
                                       boolean bigEndian) {
        return bigEndian ? doubleToBytesBE(d, bytes, off)
                : doubleToBytesLE(d, bytes, off);
    }

    public static byte[] doubleToBytesBE(double d, byte[] bytes, int off) {
        return longToBytesBE(Double.doubleToLongBits(d), bytes, off);
    }

    public static byte[] doubleToBytesLE(double d, byte[] bytes, int off) {
        return longToBytesLE(Double.doubleToLongBits(d), bytes, off);
    }

    public static byte[] longToBytes(long l, byte[] bytes, int off,
                                     boolean bigEndian) {
        return bigEndian ? longToBytesBE(l, bytes, off)
                : longToBytesLE(l, bytes, off);
    }

    public static byte[] longToBytesBE(long l, byte[] bytes, int off) {
        bytes[off] = (byte) (l >> 56);
        bytes[off + 1] = (byte) (l >> 48);
        bytes[off + 2] = (byte) (l >> 40);
        bytes[off + 3] = (byte) (l >> 32);
        bytes[off + 4] = (byte) (l >> 24);
        bytes[off + 5] = (byte) (l >> 16);
        bytes[off + 6] = (byte) (l >> 8);
        bytes[off + 7] = (byte) l;
        return bytes;
    }

    public static byte[] longToBytesLE(long l, byte[] bytes, int off) {
        bytes[off + 7] = (byte) (l >> 56);
        bytes[off + 6] = (byte) (l >> 48);
        bytes[off + 5] = (byte) (l >> 40);
        bytes[off + 4] = (byte) (l >> 32);
        bytes[off + 3] = (byte) (l >> 24);
        bytes[off + 2] = (byte) (l >> 16);
        bytes[off + 1] = (byte) (l >> 8);
        bytes[off] = (byte) l;
        return bytes;
    }

    public static byte[][] swapShorts(byte[][] bs) {
        int carry = 0;
        for (int i = 0; i < bs.length; i++) {
            byte[] b = bs[i];
            if (carry != 0)
                swapLastFirst(bs[i - 1], b);
            int len = b.length - carry;
            swapShorts(b, carry, len & ~1);
            carry = len & 1;
        }
        return bs;
    }

    public static byte[] swapShorts(byte[] b, int off, int len) {
        checkLength(len, 2);
        for (int i = off, n = off + len; i < n; i += 2)
            swap(b, i, i + 1);
        return b;
    }

    public static byte[] swapInts(byte[] b, int off, int len) {
        checkLength(len, 4);
        for (int i = off, n = off + len; i < n; i += 4) {
            swap(b, i, i + 3);
            swap(b, i + 1, i + 2);
        }
        return b;
    }

    public static byte[] swapLongs(byte[] b, int off, int len) {
        checkLength(len, 8);
        for (int i = off, n = off + len; i < n; i += 8) {
            swap(b, i, i + 7);
            swap(b, i + 1, i + 6);
            swap(b, i + 2, i + 5);
            swap(b, i + 3, i + 4);
        }
        return b;
    }

    private static void checkLength(int len, int numBytes) {
        if (len < 0 || (len % numBytes) != 0)
            throw new IllegalArgumentException("length: " + len);
    }

    private static void swap(byte[] bytes, int a, int b) {
        byte t = bytes[a];
        bytes[a] = bytes[b];
        bytes[b] = t;
    }

    private static void swapLastFirst(byte[] b1, byte[] b2) {
        int last = b1.length - 1;
        byte t = b2[0];
        b2[0] = b1[last];
        b1[last] = t;
    }

    public static byte[] intsToBytesLE(int... values) {
        byte[] ret = new byte[4 * values.length];
        for (int i = 0; i < values.length; i++) {
            intToBytesLE(values[i], ret, 4 * i);
        }
        return ret;
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

}
