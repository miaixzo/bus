/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.nimble;

/**
 * 字节查找表类，用于处理DICOM图像中的字节级查找表操作。
 *
 * <p>
 * 该类继承自LookupTable，专门用于处理8位查找表(LUT)操作。 它提供了各种查找方法，支持字节到字节、字节到短整型、短整型到字节以及短整型到短整型的查找转换。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ByteLookupTable extends LookupTable {

    /**
     * 查找表数据
     */
    private final byte[] lut;

    /**
     * 构造方法，使用现有的查找表数据
     *
     * @param inBits  输入位数
     * @param outBits 输出位数
     * @param offset  偏移量
     * @param lut     查找表数据
     */
    ByteLookupTable(StoredValue inBits, int outBits, int offset, byte[] lut) {
        super(inBits, outBits, offset);
        this.lut = lut;
    }

    /**
     * 构造方法，根据指定的参数创建查找表
     *
     * @param inBits  输入位数
     * @param outBits 输出位数
     * @param minOut  最小输出值
     * @param maxOut  最大输出值
     * @param offset  偏移量
     * @param size    查找表大小
     * @param flip    是否翻转
     */
    ByteLookupTable(StoredValue inBits, int outBits, int minOut, int maxOut, int offset, int size, boolean flip) {
        this(inBits, outBits, offset, new byte[minOut == maxOut ? 1 : size]);
        if (lut.length == 1) {
            lut[0] = (byte) minOut;
        } else {
            int maxIndex = size - 1;
            int midIndex = maxIndex / 2;
            int outRange = maxOut - minOut;
            for (int i = 0; i < size; i++)
                lut[flip ? maxIndex - i : i] = (byte) ((i * outRange + midIndex) / maxIndex + minOut);
        }
    }

    /**
     * 获取查找表长度
     *
     * @return 查找表长度
     */
    @Override
    public int length() {
        return lut.length;
    }

    /**
     * 查找转换：字节数组到字节数组
     *
     * @param src     源数组
     * @param srcPos  源起始位置
     * @param dest    目标数组
     * @param destPos 目标起始位置
     * @param length  转换长度
     */
    @Override
    public void lookup(byte[] src, int srcPos, byte[] dest, int destPos, int length) {
        for (int i = srcPos, endPos = srcPos + length, j = destPos; i < endPos;)
            dest[j++] = lut[index(src[i++])];
    }

    /**
     * 计算索引
     *
     * @param pixel 像素值
     * @return 索引值
     */
    private int index(int pixel) {
        int index = inBits.valueOf(pixel) - offset;
        return Math.min(Math.max(0, index), lut.length - 1);
    }

    /**
     * 查找转换：短整型数组到字节数组
     *
     * @param src     源数组
     * @param srcPos  源起始位置
     * @param dest    目标数组
     * @param destPos 目标起始位置
     * @param length  转换长度
     */
    @Override
    public void lookup(short[] src, int srcPos, byte[] dest, int destPos, int length) {
        for (int i = srcPos, endPos = srcPos + length, j = destPos; i < endPos;)
            dest[j++] = lut[index(src[i++])];
    }

    /**
     * 查找转换：字节数组到短整型数组
     *
     * @param src     源数组
     * @param srcPos  源起始位置
     * @param dest    目标数组
     * @param destPos 目标起始位置
     * @param length  转换长度
     */
    @Override
    public void lookup(byte[] src, int srcPos, short[] dest, int destPos, int length) {
        for (int i = srcPos, endPos = srcPos + length, j = destPos; i < endPos;)
            dest[j++] = (short) (lut[index(src[i++])] & 0xff);
    }

    /**
     * 查找转换：短整型数组到短整型数组
     *
     * @param src     源数组
     * @param srcPos  源起始位置
     * @param dest    目标数组
     * @param destPos 目标起始位置
     * @param length  转换长度
     */
    @Override
    public void lookup(short[] src, int srcPos, short[] dest, int destPos, int length) {
        for (int i = srcPos, endPos = srcPos + length, j = destPos; i < endPos;)
            dest[j++] = (short) (lut[index(src[i++])] & 0xff);
    }

    /**
     * 调整输出位数
     *
     * @param outBits 输出位数
     * @return 调整后的查找表
     */
    @Override
    public LookupTable adjustOutBits(int outBits) {
        int diff = outBits - this.outBits;
        if (diff != 0) {
            byte[] lut = this.lut;
            if (outBits > 8) {
                short[] ss = new short[lut.length];
                for (int i = 0; i < lut.length; i++)
                    ss[i] = (short) ((lut[i] & 0xff) << diff);
                return new ShortLookupTable(inBits, outBits, offset, ss);
            }
            if (diff < 0) {
                diff = -diff;
                for (int i = 0; i < lut.length; i++)
                    lut[i] = (byte) ((lut[i] & 0xff) >> diff);
            } else
                for (int i = 0; i < lut.length; i++)
                    lut[i] <<= diff;
            this.outBits = outBits;
        }
        return this;
    }

    /**
     * 反转查找表
     */
    @Override
    public void inverse() {
        byte[] lut = this.lut;
        int maxOut = (1 << outBits) - 1;
        for (int i = 0; i < lut.length; i++)
            lut[i] = (byte) (maxOut - lut[i]);
    }

    /**
     * 组合查找表
     *
     * @param other 另一个查找表
     * @return 组合后的查找表
     */
    @Override
    public LookupTable combine(LookupTable other) {
        byte[] lut = this.lut;
        if (other.outBits > 8) {
            short[] ss = new short[lut.length];
            other.lookup(lut, 0, ss, 0, lut.length);
            return new ShortLookupTable(inBits, other.outBits, offset, ss);
        }
        other.lookup(lut, 0, lut, 0, lut.length);
        this.outBits = other.outBits;
        return this;
    }

}
