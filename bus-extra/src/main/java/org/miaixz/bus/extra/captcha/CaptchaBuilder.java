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
package org.miaixz.bus.extra.captcha;

import org.miaixz.bus.extra.captcha.provider.CircleProvider;
import org.miaixz.bus.extra.captcha.provider.GifProvider;
import org.miaixz.bus.extra.captcha.provider.LineProvider;
import org.miaixz.bus.extra.captcha.provider.ShearProvider;

/**
 * Utility class for graphic CAPTCHA generation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CaptchaBuilder {

    /**
     * Creates a CAPTCHA with line interference, default 5 characters, 150 interference lines.
     *
     * @param width  Image width.
     * @param height Image height.
     * @return {@link LineProvider}
     */
    public static LineProvider ofLine(final int width, final int height) {
        return new LineProvider(width, height);
    }

    /**
     * Creates a CAPTCHA with line interference.
     *
     * @param width     Image width.
     * @param height    Image height.
     * @param codeCount Number of characters.
     * @param lineCount Number of interference lines.
     * @return {@link LineProvider}
     */
    public static LineProvider ofLine(final int width, final int height, final int codeCount, final int lineCount) {
        return new LineProvider(width, height, codeCount, lineCount);
    }

    /**
     * Creates a CAPTCHA with line interference.
     *
     * @param width          Image width.
     * @param height         Image height.
     * @param codeCount      Number of characters.
     * @param lineCount      Number of interference lines.
     * @param sizeBaseHeight Font size, a multiple of height.
     * @return {@link LineProvider}
     */
    public static LineProvider ofLine(
            final int width,
            final int height,
            final int codeCount,
            final int lineCount,
            final float sizeBaseHeight) {
        return new LineProvider(width, height, codeCount, lineCount, sizeBaseHeight);
    }

    /**
     * Creates a CAPTCHA with circle interference, default 5 characters, 15 interference circles.
     *
     * @param width  Image width.
     * @param height Image height.
     * @return {@link CircleProvider}
     */
    public static CircleProvider ofCircle(final int width, final int height) {
        return new CircleProvider(width, height);
    }

    /**
     * Creates a CAPTCHA with circle interference.
     *
     * @param width       Image width.
     * @param height      Image height.
     * @param codeCount   Number of characters.
     * @param circleCount Number of interference circles.
     * @return {@link CircleProvider}
     */
    public static CircleProvider ofCircle(
            final int width,
            final int height,
            final int codeCount,
            final int circleCount) {
        return new CircleProvider(width, height, codeCount, circleCount);
    }

    /**
     * Creates a CAPTCHA with circle interference.
     *
     * @param width       Image width.
     * @param height      Image height.
     * @param codeCount   Number of characters.
     * @param circleCount Number of interference circles.
     * @param size        Font size, a multiple of height.
     * @return {@link CircleProvider}
     */
    public static CircleProvider ofCircle(
            final int width,
            final int height,
            final int codeCount,
            final int circleCount,
            final float size) {
        return new CircleProvider(width, height, codeCount, circleCount, size);
    }

    /**
     * Creates a CAPTCHA with shear interference, default 5 characters.
     *
     * @param width  Image width.
     * @param height Image height.
     * @return {@link ShearProvider}
     */
    public static ShearProvider ofShear(final int width, final int height) {
        return new ShearProvider(width, height);
    }

    /**
     * Creates a CAPTCHA with shear interference.
     *
     * @param width     Image width.
     * @param height    Image height.
     * @param codeCount Number of characters.
     * @param thickness Interference line thickness.
     * @return {@link ShearProvider}
     */
    public static ShearProvider ofShear(final int width, final int height, final int codeCount, final int thickness) {
        return new ShearProvider(width, height, codeCount, thickness);
    }

    /**
     * Creates a CAPTCHA with shear interference.
     *
     * @param width          Image width.
     * @param height         Image height.
     * @param codeCount      Number of characters.
     * @param thickness      Interference line thickness.
     * @param sizeBaseHeight Font size, a multiple of height.
     * @return {@link ShearProvider}
     */
    public static ShearProvider ofShear(
            final int width,
            final int height,
            final int codeCount,
            final int thickness,
            final float sizeBaseHeight) {
        return new ShearProvider(width, height, codeCount, thickness, sizeBaseHeight);
    }

    /**
     * Creates a GIF CAPTCHA.
     *
     * @param width  Image width.
     * @param height Image height.
     * @return {@link GifProvider}
     */
    public static GifProvider ofGif(final int width, final int height) {
        return new GifProvider(width, height);
    }

    /**
     * Creates a GIF CAPTCHA.
     *
     * @param width     Image width.
     * @param height    Image height.
     * @param codeCount Number of characters.
     * @return {@link GifProvider}
     */
    public static GifProvider ofGif(final int width, final int height, final int codeCount) {
        return new GifProvider(width, height, codeCount);
    }

    /**
     * Creates a GIF CAPTCHA with circle interference.
     *
     * @param width          Image width.
     * @param height         Image height.
     * @param codeCount      Number of characters.
     * @param thickness      Number of CAPTCHA interference elements.
     * @param sizeBaseHeight Font size, a multiple of height.
     * @return {@link GifProvider}
     */
    public static GifProvider ofGif(
            final int width,
            final int height,
            final int codeCount,
            final int thickness,
            final float sizeBaseHeight) {
        return new GifProvider(width, height, codeCount, thickness, sizeBaseHeight);
    }

}
