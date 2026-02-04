/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
