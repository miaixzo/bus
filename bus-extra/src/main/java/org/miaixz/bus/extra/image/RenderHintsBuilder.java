/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.Builder;
import org.miaixz.bus.core.lang.Assert;

/**
 * A builder for defining and managing a collection of keys and their associated values for rendering hints. It provides
 * configuration for:
 * <ol>
 * <li>{@link RenderingHints#KEY_ANTIALIASING}</li>
 * <li>{@link RenderingHints#KEY_TEXT_ANTIALIASING}</li>
 * <li>{@link RenderingHints#KEY_COLOR_RENDERING}</li>
 * <li>{@link RenderingHints#KEY_DITHERING}</li>
 * <li>{@link RenderingHints#KEY_FRACTIONALMETRICS}</li>
 * <li>{@link RenderingHints#KEY_INTERPOLATION}</li>
 * <li>{@link RenderingHints#KEY_ALPHA_INTERPOLATION}</li>
 * <li>{@link RenderingHints#KEY_RENDERING}</li>
 * <li>{@link RenderingHints#KEY_STROKE_CONTROL}</li>
 * <li>{@link RenderingHints#KEY_TEXT_LCD_CONTRAST}</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RenderHintsBuilder implements Builder<RenderingHints> {

    /**
     * The map of rendering hints.
     */
    private final Map<RenderingHints.Key, Object> hintsMap;

    /**
     * Private constructor.
     */
    private RenderHintsBuilder() {
        // A total of 10 configuration items
        hintsMap = new HashMap<>(10, 1);
    }

    /**
     * Creates a {@link RenderingHints} builder.
     *
     * @return A {@code RenderingHintsBuilder}.
     */
    public static RenderHintsBuilder of() {
        return new RenderHintsBuilder();
    }

    /**
     * Sets whether to use anti-aliasing.
     *
     * @param antialias The anti-aliasing option. If {@code null}, this option is removed.
     * @return this
     */
    public RenderHintsBuilder setAntialiasing(final Antialias antialias) {
        final RenderingHints.Key key = RenderingHints.KEY_ANTIALIASING;
        if (null == antialias) {
            this.hintsMap.remove(key);
        } else {
            this.hintsMap.put(key, antialias.getValue());
        }
        return this;
    }

    /**
     * Sets whether to use anti-aliasing for text rendering.
     *
     * @param textAntialias The text anti-aliasing option. If {@code null}, this option is removed.
     * @return this
     */
    public RenderHintsBuilder setTextAntialias(final TextAntialias textAntialias) {
        final RenderingHints.Key key = RenderingHints.KEY_TEXT_ANTIALIASING;
        if (null == textAntialias) {
            this.hintsMap.remove(key);
        } else {
            this.hintsMap.put(key, textAntialias.getValue());
        }
        return this;
    }

    /**
     * Sets the rendering method for color rendering.
     *
     * @param colorRender The color rendering method. If {@code null}, this option is removed.
     * @return this
     */
    public RenderHintsBuilder setColorRendering(final ColorRender colorRender) {
        final RenderingHints.Key key = RenderingHints.KEY_COLOR_RENDERING;
        if (null == colorRender) {
            this.hintsMap.remove(key);
        } else {
            this.hintsMap.put(key, colorRender.getValue());
        }
        return this;
    }

    /**
     * Sets how to handle dithering. Dithering is the process of synthesizing a wider range of colors from a limited set
     * by coloring adjacent pixels to create the illusion of a new color.
     *
     * @param dither How to handle dithering. If {@code null}, this option is removed.
     * @return this
     */
    public RenderHintsBuilder setDithering(final Dither dither) {
        final RenderingHints.Key key = RenderingHints.KEY_DITHERING;
        if (null == dither) {
            this.hintsMap.remove(key);
        } else {
            this.hintsMap.put(key, dither.getValue());
        }
        return this;
    }

    /**
     * Sets the font metrics.
     *
     * @param fractionalMetrics The font metrics option. If {@code null}, this option is removed.
     * @return this
     */
    public RenderHintsBuilder setFractionalMetrics(final FractionalMetrics fractionalMetrics) {
        final RenderingHints.Key key = RenderingHints.KEY_FRACTIONALMETRICS;
        if (null == fractionalMetrics) {
            this.hintsMap.remove(key);
        } else {
            this.hintsMap.put(key, fractionalMetrics.getValue());
        }
        return this;
    }

    /**
     * Sets how interpolation is performed. When transforming a source image, the transformed pixels rarely align
     * perfectly with the target pixel positions. In such cases, the color value of each transformed pixel must be
     * determined from the surrounding pixels. Interpolation is the process of achieving this.
     *
     * @param interpolation The interpolation method. If {@code null}, this option is removed.
     * @return this
     */
    public RenderHintsBuilder setInterpolation(final Interpolation interpolation) {
        final RenderingHints.Key key = RenderingHints.KEY_INTERPOLATION;
        if (null == interpolation) {
            this.hintsMap.remove(key);
        } else {
            this.hintsMap.put(key, interpolation.getValue());
        }
        return this;
    }

    /**
     * Sets the alpha compositing adjustment.
     *
     * @param alphaInterpolation The alpha compositing adjustment. If {@code null}, this option is removed.
     * @return this
     */
    public RenderHintsBuilder setAlphaInterpolation(final AlphaInterpolation alphaInterpolation) {
        final RenderingHints.Key key = RenderingHints.KEY_ALPHA_INTERPOLATION;
        if (null == alphaInterpolation) {
            this.hintsMap.remove(key);
        } else {
            this.hintsMap.put(key, alphaInterpolation.getValue());
        }
        return this;
    }

    /**
     * Sets the rendering technique, balancing speed and quality.
     *
     * @param render The rendering technique. If {@code null}, this option is removed.
     * @return this
     */
    public RenderHintsBuilder setRendering(final Render render) {
        final RenderingHints.Key key = RenderingHints.KEY_RENDERING;
        if (null == render) {
            this.hintsMap.remove(key);
        } else {
            this.hintsMap.put(key, render.getValue());
        }
        return this;
    }

    /**
     * Sets the stroke normalization control.
     *
     * @param strokeControl The stroke normalization control. If {@code null}, this option is removed.
     * @return this
     */
    public RenderHintsBuilder setStrokeControl(final StrokeControl strokeControl) {
        final RenderingHints.Key key = RenderingHints.KEY_STROKE_CONTROL;
        if (null == strokeControl) {
            this.hintsMap.remove(key);
        } else {
            this.hintsMap.put(key, strokeControl.getValue());
        }
        return this;
    }

    /**
     * Sets the LCD text contrast rendering. A positive integer between 100 and 250. Typically, the useful range is
     * 140-180.
     *
     * @param textLCDContrast The LCD text contrast rendering, a positive integer between 100 and 250.
     * @return this
     */
    public RenderHintsBuilder setTextLCDContrast(final Integer textLCDContrast) {
        final RenderingHints.Key key = RenderingHints.KEY_TEXT_LCD_CONTRAST;
        if (null == textLCDContrast) {
            this.hintsMap.remove(key);
        } else {
            this.hintsMap.put(key, Assert.checkBetween(textLCDContrast.intValue(), 100, 250));
        }
        return this;
    }

    @Override
    public RenderingHints build() {
        return new RenderingHints(this.hintsMap);
    }

    /**
     * Anti-aliasing options.
     *
     * @see RenderingHints#VALUE_ANTIALIAS_ON
     * @see RenderingHints#VALUE_ANTIALIAS_OFF
     * @see RenderingHints#VALUE_ANTIALIAS_DEFAULT
     */
    public enum Antialias {

        /**
         * Use anti-aliasing.
         */
        ON(RenderingHints.VALUE_ANTIALIAS_ON),
        /**
         * Do not use anti-aliasing.
         */
        OFF(RenderingHints.VALUE_ANTIALIAS_OFF),
        /**
         * Default anti-aliasing.
         */
        DEFAULT(RenderingHints.VALUE_ANTIALIAS_OFF);

        /**
         * The rendering hint value.
         */
        private final Object value;

        Antialias(final Object value) {
            this.value = value;
        }

        /**
         * Gets the value.
         *
         * @return The value.
         */
        public Object getValue() {
            return this.value;
        }
    }

    /**
     * Text anti-aliasing options.
     *
     * @see RenderingHints#VALUE_TEXT_ANTIALIAS_ON
     * @see RenderingHints#VALUE_TEXT_ANTIALIAS_OFF
     * @see RenderingHints#VALUE_TEXT_ANTIALIAS_DEFAULT
     */
    public enum TextAntialias {

        /**
         * Render text with anti-aliasing.
         */
        ON(RenderingHints.VALUE_TEXT_ANTIALIAS_ON),
        /**
         * Render text without anti-aliasing.
         */
        OFF(RenderingHints.VALUE_TEXT_ANTIALIAS_OFF),
        /**
         * Render text using the platform's default anti-aliasing mode.
         */
        DEFAULT(RenderingHints.VALUE_TEXT_ANTIALIAS_OFF),
        /**
         * Automatically decide whether to use anti-aliasing or solid colors based on font information.
         */
        GASP(RenderingHints.VALUE_TEXT_ANTIALIAS_GASP),
        /**
         * Optimize text display for LCD screens with HRGB subpixel layout.
         */
        LCD_HRGB(RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB),
        /**
         * Optimize text display for LCD screens with HBGR subpixel layout.
         */
        LCD_HBGR(RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR),
        /**
         * Optimize text display for LCD screens with VRGB subpixel layout.
         */
        LCD_VRGB(RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB),
        /**
         * Optimize text display for LCD screens with VBGR subpixel layout.
         */
        LCD_VBGR(RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR);

        /**
         * The rendering hint value.
         */
        private final Object value;

        TextAntialias(final Object value) {
            this.value = value;
        }

        /**
         * Gets the value.
         *
         * @return The value.
         */
        public Object getValue() {
            return this.value;
        }
    }

    /**
     * Color rendering methods.
     *
     * @see RenderingHints#VALUE_COLOR_RENDER_SPEED
     * @see RenderingHints#VALUE_COLOR_RENDER_QUALITY
     * @see RenderingHints#VALUE_COLOR_RENDER_DEFAULT
     */
    public enum ColorRender {

        /**
         * Prioritize speed.
         */
        SPEED(RenderingHints.VALUE_COLOR_RENDER_SPEED),
        /**
         * Prioritize quality.
         */
        QUALITY(RenderingHints.VALUE_COLOR_RENDER_QUALITY),
        /**
         * Default rendering method.
         */
        DEFAULT(RenderingHints.VALUE_COLOR_RENDER_DEFAULT);

        /**
         * The rendering hint value.
         */
        private final Object value;

        ColorRender(final Object value) {
            this.value = value;
        }

        /**
         * Gets the value.
         *
         * @return The value.
         */
        public Object getValue() {
            return this.value;
        }
    }

    /**
     * Rendering techniques.
     *
     * @see RenderingHints#VALUE_RENDER_SPEED
     * @see RenderingHints#VALUE_RENDER_QUALITY
     * @see RenderingHints#VALUE_RENDER_DEFAULT
     */
    public enum Render {

        /**
         * Prioritize speed.
         */
        SPEED(RenderingHints.VALUE_RENDER_SPEED),
        /**
         * Prioritize quality.
         */
        QUALITY(RenderingHints.VALUE_RENDER_QUALITY),
        /**
         * Default.
         */
        DEFAULT(RenderingHints.VALUE_RENDER_DEFAULT);

        /**
         * The rendering hint value.
         */
        private final Object value;

        Render(final Object value) {
            this.value = value;
        }

        /**
         * Gets the value.
         *
         * @return The value.
         */
        public Object getValue() {
            return this.value;
        }
    }

    /**
     * Dithering options. Dithering is the process of synthesizing a wider range of colors from a limited set by
     * coloring adjacent pixels to create the illusion of a new color.
     *
     * @see RenderingHints#VALUE_DITHER_ENABLE
     * @see RenderingHints#VALUE_DITHER_DISABLE
     * @see RenderingHints#VALUE_DITHER_DEFAULT
     */
    public enum Dither {

        /**
         * Enable dithering.
         */
        ENABLE(RenderingHints.VALUE_DITHER_ENABLE),
        /**
         * Disable dithering.
         */
        DISABLE(RenderingHints.VALUE_DITHER_DISABLE),
        /**
         * Default.
         */
        DEFAULT(RenderingHints.VALUE_DITHER_DEFAULT);

        /**
         * The rendering hint value.
         */
        private final Object value;

        Dither(final Object value) {
            this.value = value;
        }

        /**
         * Gets the value.
         *
         * @return The value.
         */
        public Object getValue() {
            return this.value;
        }
    }

    /**
     * Font metrics options.
     *
     * @see RenderingHints#VALUE_FRACTIONALMETRICS_ON
     * @see RenderingHints#VALUE_FRACTIONALMETRICS_OFF
     * @see RenderingHints#VALUE_FRACTIONALMETRICS_DEFAULT
     */
    public enum FractionalMetrics {

        /**
         * Enable fractional font metrics.
         */
        ON(RenderingHints.VALUE_FRACTIONALMETRICS_ON),
        /**
         * Disable fractional font metrics.
         */
        OFF(RenderingHints.VALUE_FRACTIONALMETRICS_OFF),
        /**
         * Default.
         */
        DEFAULT(RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);

        /**
         * The rendering hint value.
         */
        private final Object value;

        FractionalMetrics(final Object value) {
            this.value = value;
        }

        /**
         * Gets the value.
         *
         * @return The value.
         */
        public Object getValue() {
            return this.value;
        }
    }

    /**
     * Interpolation methods. When transforming a source image, the transformed pixels rarely align perfectly with the
     * target pixel positions. In such cases, the color value of each transformed pixel must be determined from the
     * surrounding pixels. Interpolation is the process of achieving this.
     *
     * @see RenderingHints#VALUE_INTERPOLATION_BICUBIC
     * @see RenderingHints#VALUE_INTERPOLATION_BILINEAR
     * @see RenderingHints#VALUE_INTERPOLATION_NEAREST_NEIGHBOR
     */
    public enum Interpolation {

        /**
         * Bicubic interpolation.
         */
        BICUBIC(RenderingHints.VALUE_INTERPOLATION_BICUBIC),
        /**
         * Bilinear interpolation.
         */
        BILINEAR(RenderingHints.VALUE_INTERPOLATION_BILINEAR),
        /**
         * Nearest neighbor interpolation.
         */
        NEAREST_NEIGHBOR(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        /**
         * The rendering hint value.
         */
        private final Object value;

        Interpolation(final Object value) {
            this.value = value;
        }

        /**
         * Gets the value.
         *
         * @return The value.
         */
        public Object getValue() {
            return this.value;
        }
    }

    /**
     * Alpha compositing adjustment.
     *
     * @see RenderingHints#VALUE_ALPHA_INTERPOLATION_SPEED
     * @see RenderingHints#VALUE_ALPHA_INTERPOLATION_QUALITY
     * @see RenderingHints#VALUE_ALPHA_INTERPOLATION_DEFAULT
     */
    public enum AlphaInterpolation {

        /**
         * Prioritize speed.
         */
        SPEED(RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED),
        /**
         * Prioritize quality.
         */
        QUALITY(RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY),
        /**
         * Platform default.
         */
        DEFAULT(RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);

        /**
         * The rendering hint value.
         */
        private final Object value;

        AlphaInterpolation(final Object value) {
            this.value = value;
        }

        /**
         * Gets the value.
         *
         * @return The value.
         */
        public Object getValue() {
            return this.value;
        }
    }

    /**
     * Stroke normalization control.
     *
     * @see RenderingHints#VALUE_STROKE_NORMALIZE
     * @see RenderingHints#VALUE_STROKE_PURE
     * @see RenderingHints#VALUE_STROKE_DEFAULT
     */
    public enum StrokeControl {

        /**
         * Normalize strokes for consistency.
         */
        NORMALIZE(RenderingHints.VALUE_STROKE_NORMALIZE),
        /**
         * Use pure strokes for geometric accuracy.
         */
        PURE(RenderingHints.VALUE_STROKE_PURE),
        /**
         * Platform default.
         */
        DEFAULT(RenderingHints.VALUE_STROKE_DEFAULT);

        /**
         * The rendering hint value.
         */
        private final Object value;

        StrokeControl(final Object value) {
            this.value = value;
        }

        /**
         * Gets the value.
         *
         * @return The value.
         */
        public Object getValue() {
            return this.value;
        }
    }

}
