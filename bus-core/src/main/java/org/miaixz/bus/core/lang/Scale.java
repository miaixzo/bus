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
package org.miaixz.bus.core.lang;

import java.awt.*;

/**
 * 缩放常量信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Scale {

    /**
     * 图片缩略模式
     */
    public enum Zoom {
        /**
         * 原始比例，不缩放
         */
        ORIGIN,
        /**
         * 指定宽度，高度按比例
         */
        WIDTH,
        /**
         * 指定高度，宽度按比例
         */
        HEIGHT,
        /**
         * 自定义高度和宽度，强制缩放
         */
        OPTIONAL
    }

    /**
     * 图片缩略类型
     *
     * @author Kimi Liu
     * @since Java 17+
     */
    public enum Type {
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

        private final int value;

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
        Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

    }

    /**
     * 渐变方向
     */
    public enum Gradient {
        /**
         * 上到下
         */
        TOP_BOTTOM,
        /**
         * 左到右
         */
        LEFT_RIGHT,
        /**
         * 左上到右下
         */
        LEFT_TOP_TO_RIGHT_BOTTOM,
        /**
         * 右上到左下
         */
        RIGHT_TOP_TO_LEFT_BOTTOM
    }

    /**
     * 绘制方向
     */
    public enum Direction {
        /**
         * 左到右
         */
        LEFT_RIGHT,
        /**
         * 右到左
         */
        RIGHT_LEFT,
        /**
         * 中间到两边
         */
        CENTER_LEFT_RIGHT
    }

    /**
     * 自动换行时，多行文本的对齐方式
     */
    public enum Align {
        /**
         * 左对齐
         */
        LEFT,
        /**
         * 居中对齐
         */
        CENTER,
        /**
         * 右对齐
         */
        RIGHT
    }


}
