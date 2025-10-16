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
package org.miaixz.bus.extra.image;

import java.awt.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents text to be displayed, used for storing text information for drawing on an image, including content, font,
 * size, position, and transparency.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageText implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852293077169L;

    /**
     * The text content.
     */
    private String pressText;
    /**
     * The color of the text.
     */
    private Color color;
    /**
     * The font used to render the text.
     */
    private Font font;
    /**
     * The starting position (top-left) of the text.
     */
    private Point point;
    /**
     * The transparency of the text (0.0f to 1.0f).
     */
    private float alpha;

    /**
     * Constructor.
     *
     * @param text  The text content.
     * @param color The color of the text.
     * @param font  The font for displaying the text.
     * @param point The top-left starting position.
     * @param alpha The transparency.
     */
    public ImageText(final String text, final Color color, final Font font, final Point point, final float alpha) {
        this.pressText = text;
        this.color = color;
        this.font = font;
        this.point = point;
        this.alpha = alpha;
    }

    /**
     * Builds an ImageText object.
     *
     * @param text  The text content.
     * @param color The color of the text.
     * @param font  The font for displaying the text.
     * @param point The top-left starting position.
     * @param alpha The transparency.
     * @return An ImageText object.
     */
    public static ImageText of(
            final String text,
            final Color color,
            final Font font,
            final Point point,
            final float alpha) {
        return new ImageText(text, color, font, point, alpha);
    }

    /**
     * Gets the text content.
     *
     * @return The text content.
     */
    public String getPressText() {
        return pressText;
    }

    /**
     * Sets the text content.
     *
     * @param pressText The text content.
     */
    public void setPressText(final String pressText) {
        this.pressText = pressText;
    }

    /**
     * Gets the text color.
     *
     * @return The text color.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the text color.
     *
     * @param color The text color.
     */
    public void setColor(final Color color) {
        this.color = color;
    }

    /**
     * Gets the font.
     *
     * @return The font.
     */
    public Font getFont() {
        return font;
    }

    /**
     * Sets the font.
     *
     * @param font The font.
     */
    public void setFont(final Font font) {
        this.font = font;
    }

    /**
     * Gets the 2D coordinate point.
     *
     * @return The 2D coordinate point.
     */
    public Point getPoint() {
        return point;
    }

    /**
     * Sets the 2D coordinate point.
     *
     * @param point The 2D coordinate point.
     */
    public void setPoint(final Point point) {
        this.point = point;
    }

    /**
     * Gets the transparency.
     *
     * @return The transparency.
     */
    public float getAlpha() {
        return alpha;
    }

    /**
     * Sets the transparency.
     *
     * @param alpha The transparency.
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
        return Float.compare(alpha, that.alpha) == 0 && Objects.equals(pressText, that.pressText)
                && Objects.equals(color, that.color) && Objects.equals(font, that.font)
                && Objects.equals(point, that.point);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pressText, color, font, point, alpha);
    }

    @Override
    public String toString() {
        return "ImageText{" + "pressText='" + pressText + '\'' + ", color=" + color + ", font=" + font + ", point="
                + point + ", alpha=" + alpha + '}';
    }

}
