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
package org.miaixz.bus.office.excel.style;

import java.awt.*;
import java.io.Serializable;

import org.apache.poi.sl.usermodel.ShapeType;

/**
 * 形状配置 用于在Excel中定义形状的样式，包括形状类型、线条样式、线条宽度、线条颜色、填充颜色等
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ShapeConfig implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 形状类型，如矩形、圆形等，默认直线
     */
    private ShapeType shapeType = ShapeType.LINE;
    /**
     * 线条样式，如实线、虚线等，默认实线
     */
    private LineStyle lineStyle = LineStyle.SOLID;
    /**
     * 线条宽度，以磅为单位
     */
    private int lineWidth = 1;
    /**
     * 线条颜色
     */
    private Color lineColor = Color.BLACK;
    /**
     * 填充颜色，{@code null表示不填充}
     */
    private Color fillColor;

    /**
     * 创建一个形状配置
     *
     * @return this
     */
    public static ShapeConfig of() {
        return new ShapeConfig();
    }

    /**
     * 获取形状类型
     *
     * @return 形状类型
     */
    public ShapeType getShapeType() {
        return shapeType;
    }

    /**
     * 设置形状类型
     *
     * @param shapeType 形状类型
     * @return 当前形状配置对象，用于链式调用
     */
    public ShapeConfig setShapeType(final ShapeType shapeType) {
        this.shapeType = shapeType;
        return this;
    }

    /**
     * 获取线条样式
     *
     * @return 线条样式
     */
    public LineStyle getLineStyle() {
        return lineStyle;
    }

    /**
     * 设置线条样式
     *
     * @param lineStyle 线条样式
     * @return 当前形状配置对象，用于链式调用
     */
    public ShapeConfig setLineStyle(final LineStyle lineStyle) {
        this.lineStyle = lineStyle;
        return this;
    }

    /**
     * 获取线条宽度
     *
     * @return 线条宽度，以磅为单位
     */
    public int getLineWidth() {
        return lineWidth;
    }

    /**
     * 设置线条宽度
     *
     * @param lineWidth 线条宽度，以磅为单位
     * @return 当前形状配置对象，用于链式调用
     */
    public ShapeConfig setLineWidth(final int lineWidth) {
        this.lineWidth = lineWidth;
        return this;
    }

    /**
     * 获取线条颜色
     *
     * @return 线条颜色
     */
    public Color getLineColor() {
        return lineColor;
    }

    /**
     * 设置线条颜色
     *
     * @param lineColor 线条颜色
     * @return 当前形状配置对象，用于链式调用
     */
    public ShapeConfig setLineColor(final Color lineColor) {
        this.lineColor = lineColor;
        return this;
    }

    /**
     * 获取填充颜色，{@code null表示不填充}
     *
     * @return 填充颜色，{@code null表示不填充}
     */
    public Color getFillColor() {
        return fillColor;
    }

    /**
     * 设置填充颜色，{@code null表示不填充}
     *
     * @param fillColor 填充颜色，{@code null表示不填充}
     * @return 当前形状配置对象，用于链式调用
     */
    public ShapeConfig setFillColor(final Color fillColor) {
        this.fillColor = fillColor;
        return this;
    }

}
