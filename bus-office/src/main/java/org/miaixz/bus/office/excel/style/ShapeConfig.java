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
import java.io.Serial;
import java.io.Serializable;

import org.apache.poi.sl.usermodel.ShapeType;

/**
 * Shape configuration, used to define the style of shapes in Excel, including shape type, line style, line width, line
 * color, fill color, etc.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ShapeConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852286218800L;

    /**
     * The type of the shape, such as rectangle, circle, etc. Default is a line.
     */
    private ShapeType shapeType = ShapeType.LINE;
    /**
     * The line style, such as solid, dashed, etc. Default is solid.
     */
    private LineStyle lineStyle = LineStyle.SOLID;
    /**
     * The width of the line, in points.
     */
    private int lineWidth = 1;
    /**
     * The color of the line.
     */
    private Color lineColor = Color.BLACK;
    /**
     * The fill color. {@code null} indicates no filling.
     */
    private Color fillColor;

    /**
     * Creates a new shape configuration instance.
     *
     * @return A new {@code ShapeConfig} instance.
     */
    public static ShapeConfig of() {
        return new ShapeConfig();
    }

    /**
     * Gets the shape type.
     *
     * @return The {@link ShapeType}.
     */
    public ShapeType getShapeType() {
        return shapeType;
    }

    /**
     * Sets the shape type.
     *
     * @param shapeType The {@link ShapeType} to set.
     * @return This {@code ShapeConfig} instance, for chaining.
     */
    public ShapeConfig setShapeType(final ShapeType shapeType) {
        this.shapeType = shapeType;
        return this;
    }

    /**
     * Gets the line style.
     *
     * @return The {@link LineStyle}.
     */
    public LineStyle getLineStyle() {
        return lineStyle;
    }

    /**
     * Sets the line style.
     *
     * @param lineStyle The {@link LineStyle} to set.
     * @return This {@code ShapeConfig} instance, for chaining.
     */
    public ShapeConfig setLineStyle(final LineStyle lineStyle) {
        this.lineStyle = lineStyle;
        return this;
    }

    /**
     * Gets the line width.
     *
     * @return The line width in points.
     */
    public int getLineWidth() {
        return lineWidth;
    }

    /**
     * Sets the line width.
     *
     * @param lineWidth The line width in points.
     * @return This {@code ShapeConfig} instance, for chaining.
     */
    public ShapeConfig setLineWidth(final int lineWidth) {
        this.lineWidth = lineWidth;
        return this;
    }

    /**
     * Gets the line color.
     *
     * @return The {@link Color} of the line.
     */
    public Color getLineColor() {
        return lineColor;
    }

    /**
     * Sets the line color.
     *
     * @param lineColor The {@link Color} to set for the line.
     * @return This {@code ShapeConfig} instance, for chaining.
     */
    public ShapeConfig setLineColor(final Color lineColor) {
        this.lineColor = lineColor;
        return this;
    }

    /**
     * Gets the fill color. {@code null} indicates no filling.
     *
     * @return The {@link Color} of the fill, or {@code null} if no filling.
     */
    public Color getFillColor() {
        return fillColor;
    }

    /**
     * Sets the fill color. {@code null} indicates no filling.
     *
     * @param fillColor The {@link Color} to set for the fill, or {@code null} for no filling.
     * @return This {@code ShapeConfig} instance, for chaining.
     */
    public ShapeConfig setFillColor(final Color fillColor) {
        this.fillColor = fillColor;
        return this;
    }

}
