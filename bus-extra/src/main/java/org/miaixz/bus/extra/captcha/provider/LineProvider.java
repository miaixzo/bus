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
     * {@inheritDoc}
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
