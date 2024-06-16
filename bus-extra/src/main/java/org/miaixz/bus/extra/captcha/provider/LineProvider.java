/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.xyz.ColorKit;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.extra.captcha.AbstractProvider;
import org.miaixz.bus.extra.captcha.strategy.CodeStrategy;
import org.miaixz.bus.extra.captcha.strategy.RandomStrategy;
import org.miaixz.bus.extra.image.ImageKit;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 使用干扰线方式生成的图形验证码
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LineProvider extends AbstractProvider {

    private static final long serialVersionUID = -1L;

    /**
     * 构造，默认5位验证码，150条干扰线
     *
     * @param width  图片宽
     * @param height 图片高
     */
    public LineProvider(final int width, final int height) {
        this(width, height, 5, 150);
    }

    /**
     * 构造
     *
     * @param width     图片宽
     * @param height    图片高
     * @param codeCount 字符个数
     * @param lineCount 干扰线条数
     */
    public LineProvider(final int width, final int height, final int codeCount, final int lineCount) {
        this(width, height, new RandomStrategy(codeCount), lineCount);
    }

    /**
     * 构造
     *
     * @param width          图片宽
     * @param height         图片高
     * @param generator      验证码生成器
     * @param interfereCount 验证码干扰元素个数
     */
    public LineProvider(final int width, final int height, final CodeStrategy generator, final int interfereCount) {
        super(width, height, generator, interfereCount);
    }

    /**
     * 构造
     *
     * @param width          图片宽
     * @param height         图片高
     * @param codeCount      字符个数
     * @param interfereCount 验证码干扰元素个数
     * @param sizeBaseHeight 字体的大小 高度的倍数
     */
    public LineProvider(final int width, final int height, final int codeCount, final int interfereCount, final float sizeBaseHeight) {
        super(width, height, new RandomStrategy(codeCount), interfereCount, sizeBaseHeight);
    }

    @Override
    public Image createImage(final String code) {
        // 图像buffer
        final BufferedImage image = new BufferedImage(width, height, (null == this.background) ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = ImageKit.createGraphics(image, this.background);

        try {
            // 干扰线
            drawInterfere(g);

            // 字符串
            drawString(g, code);
        } finally {
            g.dispose();
        }

        return image;
    }

    /**
     * 绘制字符串
     *
     * @param g    {@link Graphics}画笔
     * @param code 验证码
     */
    private void drawString(final Graphics2D g, final String code) {
        // 指定透明度
        if (null != this.textAlpha) {
            g.setComposite(this.textAlpha);
        }
        ImageKit.drawStringColourful(g, code, this.font, this.width, this.height);
    }

    /**
     * 绘制干扰线
     *
     * @param g {@link Graphics2D}画笔
     */
    private void drawInterfere(final Graphics2D g) {
        final ThreadLocalRandom random = RandomKit.getRandom();
        // 干扰线
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
