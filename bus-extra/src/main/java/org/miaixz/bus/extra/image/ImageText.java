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
package org.miaixz.bus.extra.image;

import java.awt.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * 显示文本，用于保存在图片上绘图的文本信息，包括内容、字体、大小、位置和透明度等
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageText implements Serializable {

    private static final long serialVersionUID = -1L;
    private String pressText;
    private Color color;
    private Font font;
    private Point point;
    private float alpha;

    /**
     * 构造
     *
     * @param text  文本
     * @param color 文本颜色
     * @param font  文本显示字体
     * @param point 起始左边位置
     * @param alpha 透明度
     */
    public ImageText(final String text, final Color color, final Font font, final Point point, final float alpha) {
        this.pressText = text;
        this.color = color;
        this.font = font;
        this.point = point;
        this.alpha = alpha;
    }

    /**
     * 构建DisplayText
     *
     * @param text  文本
     * @param color 文本颜色
     * @param font  文本显示字体
     * @param point 起始左边位置
     * @param alpha 透明度
     * @return DisplayText
     */
    public static ImageText of(final String text, final Color color, final Font font, final Point point, final float alpha) {
        return new ImageText(text, color, font, point, alpha);
    }

    /**
     * 获取文本
     *
     * @return 获取文本
     */
    public String getPressText() {
        return pressText;
    }

    /**
     * 设置文本
     *
     * @param pressText 文本
     */
    public void setPressText(final String pressText) {
        this.pressText = pressText;
    }

    /**
     * 获取文本颜色
     *
     * @return 文本颜色
     */
    public Color getColor() {
        return color;
    }

    /**
     * 设置文本颜色
     *
     * @param color 文本颜色
     */
    public void setColor(final Color color) {
        this.color = color;
    }

    /**
     * 获取字体
     *
     * @return 字体
     */
    public Font getFont() {
        return font;
    }

    /**
     * 设置字体
     *
     * @param font 字体
     */
    public void setFont(final Font font) {
        this.font = font;
    }

    /**
     * 获取二维坐标点
     *
     * @return 二维坐标点
     */
    public Point getPoint() {
        return point;
    }

    /**
     * 设置二维坐标点
     *
     * @param point 二维坐标点
     */
    public void setPoint(final Point point) {
        this.point = point;
    }

    /**
     * 获取透明度
     *
     * @return 透明度
     */
    public float getAlpha() {
        return alpha;
    }

    /**
     * 设置透明度
     *
     * @param alpha 透明度
     */
    public void setAlpha(final float alpha) {
        this.alpha = alpha;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ImageText that = (ImageText) o;
        return Float.compare(alpha, that.alpha) == 0
                && Objects.equals(pressText, that.pressText)
                && Objects.equals(color, that.color)
                && Objects.equals(font, that.font)
                && Objects.equals(point, that.point);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pressText, color, font, point, alpha);
    }

    @Override
    public String toString() {
        return "ImageText{" +
                "pressText='" + pressText + '\'' +
                ", color=" + color +
                ", font=" + font +
                ", point=" + point +
                ", alpha=" + alpha +
                '}';
    }

}
