/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
