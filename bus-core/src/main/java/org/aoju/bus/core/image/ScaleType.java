/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.image;

import java.awt.Image;

/**
 * 图片缩略算法类型
 *
 * @author Kimi Liu
 * @version 3.6.8
 * @since JDK 1.8+
 */
public enum ScaleType {

    /**
     * 默认
     */
    DEFAULT(Image.SCALE_DEFAULT),
    /**
     * 快速
     */
    FAST(Image.SCALE_FAST),
    /**
     * 平滑
     */
    SMOOTH(Image.SCALE_SMOOTH),
    /**
     * 使用 ReplicateScaleFilter 类中包含的图像缩放算法
     */
    REPLICATE(Image.SCALE_REPLICATE),
    /**
     * Area Averaging算法
     */
    AREA_AVERAGING(Image.SCALE_AREA_AVERAGING);

    private int value;

    /**
     * 构造
     *
     * @param value 缩放方式
     * @see Image#SCALE_DEFAULT
     * @see Image#SCALE_FAST
     * @see Image#SCALE_SMOOTH
     * @see Image#SCALE_REPLICATE
     * @see Image#SCALE_AREA_AVERAGING
     */
    ScaleType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
