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
import java.io.ByteArrayOutputStream;
import java.io.Serial;

import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.extra.captcha.AbstractProvider;
import org.miaixz.bus.extra.captcha.strategy.CodeStrategy;
import org.miaixz.bus.extra.captcha.strategy.RandomStrategy;
import org.miaixz.bus.extra.image.ImageKit;
import org.miaixz.bus.extra.image.gif.AnimatedGifEncoder;

/**
 * Gif CAPTCHA Provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GifProvider extends AbstractProvider {

    @Serial
    private static final long serialVersionUID = 2852291762232L;

    /**
     * Quantizer sampling interval - default is 10ms.
     */
    private int quality = 10;
    /**
     * Frame loop count.
     */
    private int repeat = 0;
    /**
     * Minimum color range for random colors.
     */
    private int minColor = 0;
    /**
     * Maximum color range for random colors.
     */
    private int maxColor = 255;

    /**
     * Constructor to set CAPTCHA width and height.
     *
     * @param width  CAPTCHA width.
     * @param height CAPTCHA height.
     */
    public GifProvider(final int width, final int height) {
        this(width, height, 5);
    }

    /**
     * Constructor.
     *
     * @param width     CAPTCHA width.
     * @param height    CAPTCHA height.
     * @param codeCount Number of characters.
     */
    public GifProvider(final int width, final int height, final int codeCount) {
        this(width, height, codeCount, 10);
    }

    /**
     * Constructor.
     *
     * @param width          CAPTCHA width.
     * @param height         CAPTCHA height.
     * @param codeCount      Number of characters.
     * @param interfereCount Number of interfering elements.
     */
    public GifProvider(final int width, final int height, final int codeCount, final int interfereCount) {
        this(width, height, new RandomStrategy(codeCount), interfereCount);
    }

    /**
     * Constructor.
     *
     * @param width          Image width.
     * @param height         Image height.
     * @param generator      CAPTCHA code generator.
     * @param interfereCount Number of interfering elements.
     */
    public GifProvider(final int width, final int height, final CodeStrategy generator, final int interfereCount) {
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
    public GifProvider(final int width, final int height, final int codeCount, final int interfereCount,
            final float sizeBaseHeight) {
        super(width, height, new RandomStrategy(codeCount), interfereCount, sizeBaseHeight);
    }

    /**
     * Sets the image's color quantization (conversion quality to the maximum 256 colors allowed by the GIF
     * specification). Lower values (minimum = 1) produce better colors but are significantly slower to process. 10 is
     * the default and produces good colors at a reasonable speed. Values greater than 20 do not produce significant
     * improvements in speed.
     *
     * @param quality greater than 1.
     * @return this
     */
    public GifProvider setQuality(int quality) {
        if (quality < 1) {
            quality = 1;
        }
        this.quality = quality;
        return this;
    }

    /**
     * Sets the number of times the GIF frames should be played. The default is 0, which means an infinite loop. Must be
     * called before the first image is added.
     *
     * @param repeat must be greater than or equal to 0.
     * @return this
     */
    public GifProvider setRepeat(final int repeat) {
        if (repeat >= 0) {
            this.repeat = repeat;
        }
        return this;
    }

    /**
     * Sets the maximum color range.
     *
     * @param maxColor the color.
     * @return this
     */
    public GifProvider setMaxColor(final int maxColor) {
        this.maxColor = maxColor;
        return this;
    }

    /**
     * Sets the minimum color range.
     *
     * @param minColor the color.
     * @return this
     */
    public GifProvider setMinColor(final int minColor) {
        this.minColor = minColor;
        return this;
    }

    @Override
    public void create() {
        generateCode();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final AnimatedGifEncoder gifEncoder = new AnimatedGifEncoder(); // gif encoding class
        // generate characters
        gifEncoder.start(out);
        gifEncoder.setQuality(quality); // Set quantizer sampling interval
        // frame delay (default 100)
        final int delay = 100;
        gifEncoder.setDelay(delay); // Set frame delay
        gifEncoder.setRepeat(repeat); // Frame loop count
        BufferedImage frame;
        final char[] chars = code.toCharArray();
        final Color[] fontColor = new Color[chars.length];
        for (int i = 0; i < chars.length; i++) {
            fontColor[i] = getRandomColor(minColor, maxColor);
            frame = graphicsImage(chars, fontColor, chars, i);
            gifEncoder.addFrame(frame);
            frame.flush();
        }
        gifEncoder.finish();
        this.imageBytes = out.toByteArray();
    }

    @Override
    protected Image createImage(final String code) {
        // This method is not used in the GIF provider as the image is created in the create() method.
        return null;
    }

    /**
     * Draws the random code image.
     *
     * @param chars     The character array.
     * @param fontColor Random font color.
     * @param words     The character array.
     * @param flag      Used for transparency.
     * @return BufferedImage
     */
    private BufferedImage graphicsImage(
            final char[] chars,
            final Color[] fontColor,
            final char[] words,
            final int flag) {
        final BufferedImage image = new BufferedImage(width, height,
                (null == this.background) ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_INT_RGB);
        // Fill background with specified color
        final Graphics2D g2d = ImageKit.createGraphics(image, this.background);
        try {
            AlphaComposite ac;
            // y-coordinate of the characters
            final float y = (height >> 1) + (font.getSize() >> 1);
            final float m = 1.0f * (width - (chars.length * font.getSize())) / chars.length;
            // x-coordinate of the characters
            final float x = Math.max(m / 2.0f, 2);
            g2d.setFont(font);
            // Specify transparency
            if (null != this.textAlpha) {
                g2d.setComposite(this.textAlpha);
            }
            for (int i = 0; i < chars.length; i++) {
                ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getAlpha(chars.length, flag, i));
                g2d.setComposite(ac);
                g2d.setColor(fontColor[i]);
                g2d.drawOval(
                        RandomKit.randomInt(width),
                        RandomKit.randomInt(height),
                        RandomKit.randomInt(5, 30),
                        5 + RandomKit.randomInt(5, 30)); // Draw oval border
                g2d.drawString(words[i] + "", x + (font.getSize() + m) * i, y);
            }
        } finally {
            g2d.dispose();
        }
        return image;
    }

    /**
     * Gets the transparency, from 0 to 1, and automatically calculates the step.
     *
     * @param v The total number of characters.
     * @param i The current frame index.
     * @param j The current character index.
     * @return float transparency
     */
    private float getAlpha(final int v, final int i, final int j) {
        final int num = i + j;
        final float r = (float) 1 / v;
        final float s = (v + 1) * r;
        return num > v ? (num * r - s) : num * r;
    }

    /**
     * Gets a random color within a given range.
     *
     * @param min The minimum color value.
     * @param max The maximum color value.
     * @return A random color.
     */
    private Color getRandomColor(int min, int max) {
        if (min > 255) {
            min = 255;
        }
        if (max > 255) {
            max = 255;
        }
        if (min < 0) {
            min = 0;
        }
        if (max < 0) {
            max = 0;
        }
        if (min > max) {
            min = 0;
            max = 255;
        }
        return new Color(RandomKit.randomInt(min, max), RandomKit.randomInt(min, max), RandomKit.randomInt(min, max));
    }

}
