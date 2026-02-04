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
package org.miaixz.bus.extra.captcha.provider;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.util.concurrent.ThreadLocalRandom;

import org.miaixz.bus.core.xyz.ColorKit;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.extra.captcha.AbstractProvider;
import org.miaixz.bus.extra.captcha.strategy.CodeStrategy;
import org.miaixz.bus.extra.captcha.strategy.RandomStrategy;
import org.miaixz.bus.extra.image.ImageKit;

/**
 * CAPTCHA provider that uses interfering lines.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LineProvider extends AbstractProvider {

    @Serial
    private static final long serialVersionUID = 2852291851365L;

    /**
     * Constructor with default 5-character code and 150 interfering lines.
     *
     * @param width  Image width.
     * @param height Image height.
     */
    public LineProvider(final int width, final int height) {
        this(width, height, 5, 150);
    }

    /**
     * Constructor.
     *
     * @param width     Image width.
     * @param height    Image height.
     * @param codeCount Number of characters.
     * @param lineCount Number of interfering lines.
     */
    public LineProvider(final int width, final int height, final int codeCount, final int lineCount) {
        this(width, height, new RandomStrategy(codeCount), lineCount);
    }

    /**
     * Constructor.
     *
     * @param width          Image width.
     * @param height         Image height.
     * @param generator      CAPTCHA code generator.
     * @param interfereCount Number of interfering elements.
     */
    public LineProvider(final int width, final int height, final CodeStrategy generator, final int interfereCount) {
        super(width, height, generator, interfereCount);
    }

    /**
     * Constructor.
     *
     * @param width          Image width.
     * @param height         Image height.
     * @param codeCount      Number of characters.
     * @param interfereCount Number of interfering elements.
     * @param sizeBaseHeight Font size as a multiplier of the height.
     */
    public LineProvider(final int width, final int height, final int codeCount, final int interfereCount,
            final float sizeBaseHeight) {
        super(width, height, new RandomStrategy(codeCount), interfereCount, sizeBaseHeight);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Creates a CAPTCHA image with line interference patterns. The image is rendered with random lines as visual
     * interference and the provided code is drawn in colorful text.
     * </p>
     *
     * @param code the CAPTCHA code to render
     * @return the generated CAPTCHA image with line interference
     */
    @Override
    public Image createImage(final String code) {
        // Image buffer
        final BufferedImage image = new BufferedImage(width, height,
                (null == this.background) ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = ImageKit.createGraphics(image, this.background);

        try {
            // Interfering lines
            drawInterfere(g);

            // String
            drawString(g, code);
        } finally {
            g.dispose();
        }

        return image;
    }

    /**
     * Draws the string.
     *
     * @param g    The {@link Graphics2D} object.
     * @param code The CAPTCHA code.
     */
    private void drawString(final Graphics2D g, final String code) {
        // Specify transparency
        if (null != this.textAlpha) {
            g.setComposite(this.textAlpha);
        }
        ImageKit.drawStringColourful(g, code, this.font, this.width, this.height);
    }

    /**
     * Draws interfering lines.
     *
     * @param g The {@link Graphics2D} object.
     */
    private void drawInterfere(final Graphics2D g) {
        final ThreadLocalRandom random = RandomKit.getRandom();
        // Interfering lines
        for (int i = 0; i < this.interfereCount; i++) {
            final int xs = random.nextInt(width);
            final int ys = random.nextInt(height);
            final int xe = xs + random.nextInt(width / 8);
            final int ye = ys + random.nextInt(height / 8);
            g.setColor(ColorKit.randomColor(random));
            g.drawLine(xs, ys, xe, ye);
        }
    }

}
