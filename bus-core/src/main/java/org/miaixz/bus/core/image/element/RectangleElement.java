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
package org.miaixz.bus.core.image.element;

import org.miaixz.bus.core.lang.Scale;

import java.awt.*;

/**
 * 矩形元素
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RectangleElement extends AbstractElement<RectangleElement> {

    /**
     * 绘制宽度
     */
    private Integer width;
    /**
     * 绘制高度
     */
    private Integer height;
    /**
     * 圆角大小
     */
    private Integer roundCorner = 0;
    /**
     * 颜色，默认白色
     */
    private Color color = new Color(255, 255, 255);

    /**
     * 渐变-开始颜色
     */
    private Color fromColor;
    /**
     * 渐变-结束颜色
     */
    private Color toColor;
    /**
     * 开始位置延长（反向，影响渐变效果）
     */
    private Integer fromExtend = 0;
    /**
     * 结束位置延长（正向，影响渐变效果）
     */
    private Integer toExtend = 0;
    /**
     * 渐变方向
     */
    private Scale.Gradient gradient;

    /**
     * @param x      x坐标
     * @param y      y坐标
     * @param width  宽度
     * @param height 高度
     */
    public RectangleElement(int x, int y, int width, int height) {
        this.width = width;
        this.height = height;
        super.setX(x);
        super.setY(y);
    }

    public Integer getWidth() {
        return width;
    }

    public RectangleElement setWidth(Integer width) {
        this.width = width;
        return this;
    }

    public Integer getHeight() {
        return height;
    }

    public RectangleElement setHeight(Integer height) {
        this.height = height;
        return this;
    }

    public Integer getRoundCorner() {
        return roundCorner;
    }

    public RectangleElement setRoundCorner(Integer roundCorner) {
        this.roundCorner = roundCorner;
        return this;
    }

    public Color getColor() {
        return color;
    }

    public RectangleElement setColor(Color color) {
        this.color = color;
        return this;
    }

    public RectangleElement setColor(int r, int g, int b) {
        return setColor(new Color(r, g, b));
    }

    /**
     * 设置渐变
     *
     * @param fromColor 开始颜色
     * @param toColor   结束颜色
     * @param gradient  渐变方向
     * @return this
     */
    public RectangleElement setGradient(Color fromColor, Color toColor, Scale.Gradient gradient) {
        this.fromColor = fromColor;
        this.toColor = toColor;
        this.gradient = gradient;
        return this;
    }

    /**
     * 设置渐变
     *
     * @param fromColor  开始颜色
     * @param toColor    结束颜色
     * @param fromExtend 开始位置延长（影响渐变效果）
     * @param toExtend   结束位置延长（影响渐变效果）
     * @param gradient   渐变方向
     * @return this
     */
    public RectangleElement setGradient(Color fromColor, Color toColor, int fromExtend, int toExtend, Scale.Gradient gradient) {
        this.fromColor = fromColor;
        this.toColor = toColor;
        this.fromExtend = fromExtend;
        this.toExtend = toExtend;
        this.gradient = gradient;
        return this;
    }

    public Color getFromColor() {
        return fromColor;
    }

    public Color getToColor() {
        return toColor;
    }

    public Integer getFromExtend() {
        return fromExtend;
    }

    public Integer getToExtend() {
        return toExtend;
    }

    public Scale.Gradient getGradient() {
        return gradient;
    }

}