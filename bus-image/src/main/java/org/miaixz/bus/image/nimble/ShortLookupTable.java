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
package org.miaixz.bus.image.nimble;

/**
 * 短整型查找表类，用于图像处理中的像素值转换
 * <p>
 * 该类实现了一个基于短整型数组的查找表，用于将输入像素值映射到输出像素值。 查找表可以用于各种图像处理操作，如亮度调整、对比度调整、颜色映射等。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ShortLookupTable extends LookupTable {

    /**
     * 查找表数组，存储映射后的像素值
     */
    private final short[] lut;

    /**
     * 构造方法
     *
     * @param inBits  输入值的位数
     * @param outBits 输出值的位数
     * @param offset  偏移量
     * @param lut     查找表数组
     */
    ShortLookupTable(StoredValue inBits, int outBits, int offset, short[] lut) {
        super(inBits, outBits, offset);
        this.lut = lut;
    }

    /**
     * 构造方法
     *
     * @param inBits  输入值的位数
     * @param outBits 输出值的位数
     * @param minOut  最小输出值
     * @param maxOut  最大输出值
     * @param offset  偏移量
     * @param size    查找表大小
     * @param flip    是否翻转
     */
    ShortLookupTable(StoredValue inBits, int outBits, int minOut, int maxOut, int offset, int size, boolean flip) {
        this(inBits, outBits, offset, new short[minOut == maxOut ? 1 : size]);
        if (lut.length == 1) {
            lut[0] = (short) minOut;
        } else {
            int outRange = maxOut - minOut;
            int maxIndex = size - 1;
            int midIndex = maxIndex / 2;
            for (int i = 0; i < size; i++)
                lut[flip ? maxIndex - i : i] = (short) ((i * outRange + midIndex) / maxIndex + minOut);
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
     * 查找并转换字节数组
     *
     * @param src     源数组
     * @param srcPos  源数组起始位置
     * @param dest    目标数组
     * @param destPos 目标数组起始位置
     * @param length  转换长度
     */
    @Override
    public void lookup(byte[] src, int srcPos, byte[] dest, int destPos, int length) {
        for (int i = srcPos, endPos = srcPos + length, j = destPos; i < endPos;)
            dest[j++] = (byte) lut[index(src[i++] & 0xff)];
    }

    /**
     * 计算索引
     *
     * @param pixel 像素值
     * @return 索引
     */
    private int index(int pixel) {
        int index = inBits.valueOf(pixel) - offset;
        return Math.min(Math.max(0, index), lut.length - 1);
    }

    /**
     * 查找并转换短整型数组到字节数组
     *
     * @param src     源数组
     * @param srcPos  源数组起始位置
     * @param dest    目标数组
     * @param destPos 目标数组起始位置
     * @param length  转换长度
     */
    @Override
    public void lookup(short[] src, int srcPos, byte[] dest, int destPos, int length) {
        for (int i = srcPos, endPos = srcPos + length, j = destPos; i < endPos;)
            dest[j++] = (byte) lut[index(src[i++] & 0xffff)];
    }

    /**
     * 查找并转换字节数组到短整型数组
     *
     * @param src     源数组
     * @param srcPos  源数组起始位置
     * @param dest    目标数组
     * @param destPos 目标数组起始位置
     * @param length  转换长度
     */
    @Override
    public void lookup(byte[] src, int srcPos, short[] dest, int destPos, int length) {
        for (int i = srcPos, endPos = srcPos + length, j = destPos; i < endPos;)
            dest[j++] = lut[index(src[i++] & 0xff)];
    }

    /**
     * 查找并转换短整型数组
     *
     * @param src     源数组
     * @param srcPos  源数组起始位置
     * @param dest    目标数组
     * @param destPos 目标数组起始位置
     * @param length  转换长度
     */
    @Override
    public void lookup(short[] src, int srcPos, short[] dest, int destPos, int length) {
        for (int i = srcPos, endPos = srcPos + length, j = destPos; i < endPos;)
            dest[j++] = lut[index(src[i++] & 0xffff)];
    }

    /**
     * 调整输出位数
     *
     * @param outBits 输出位数
     * @return 当前查找表
     */
    @Override
    public LookupTable adjustOutBits(int outBits) {
        int diff = outBits - this.outBits;
        if (diff != 0) {
            short[] lut = this.lut;
            if (diff < 0) {
                diff = -diff;
                for (int i = 0; i < lut.length; i++)
                    lut[i] = (short) ((lut[i] & 0xffff) >> diff);
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
        short[] lut = this.lut;
        int maxOut = (1 << outBits) - 1;
        for (int i = 0; i < lut.length; i++)
            lut[i] = (short) (maxOut - lut[i]);
    }

    /**
     * 组合查找表
     *
     * @param other 其他查找表
     * @return 当前查找表
     */
    @Override
    public LookupTable combine(LookupTable other) {
        short[] lut = this.lut;
        other.lookup(lut, 0, lut, 0, lut.length);
        this.outBits = other.outBits;
        return this;
    }

}
