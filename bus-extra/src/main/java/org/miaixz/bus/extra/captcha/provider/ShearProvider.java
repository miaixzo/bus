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

import org.miaixz.bus.core.xyz.ColorKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.extra.captcha.AbstractProvider;
import org.miaixz.bus.extra.captcha.strategy.CodeStrategy;
import org.miaixz.bus.extra.captcha.strategy.RandomStrategy;
import org.miaixz.bus.extra.image.ImageKit;

/**
 * 扭曲干扰验证码
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ShearProvider extends AbstractProvider {

    private static final long serialVersionUID = -1L;

    /**
     * 构造
     *
     * @param width  图片宽
     * @param height 图片高
     */
    public ShearProvider(final int width, final int height) {
        this(width, height, 5);
    }

    /**
     * 构造
     *
     * @param width     图片宽
     * @param height    图片高
     * @param codeCount 字符个数
     */
    public ShearProvider(final int width, final int height, final int codeCount) {
        this(width, height, codeCount, 4);
    }

    /**
     * 构造
     *
     * @param width     图片宽
     * @param height    图片高
     * @param codeCount 字符个数
     * @param thickness 干扰线宽度
     */
    public ShearProvider(final int width, final int height, final int codeCount, final int thickness) {
        this(width, height, new RandomStrategy(codeCount), thickness);
    }

    /**
     * 构造
     *
     * @param width          图片宽
     * @param height         图片高
     * @param generator      验证码生成器
     * @param interfereCount 验证码干扰元素个数
     */
    public ShearProvider(final int width, final int height, final CodeStrategy generator, final int interfereCount) {
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
    public ShearProvider(final int width, final int height, final int codeCount, final int interfereCount,
            final float sizeBaseHeight) {
        super(width, height, new RandomStrategy(codeCount), interfereCount, sizeBaseHeight);
    }

    @Override
    public Image createImage(final String code) {
        final BufferedImage image = new BufferedImage(width, height,
                (null == this.background) ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = ImageKit.createGraphics(image, this.background);

        try {
            // 画字符串
            drawString(g, code);
            // 扭曲
            shear(g, this.width, this.height, ObjectKit.defaultIfNull(this.background, Color.WHITE));
            // 画干扰线
            drawInterfere(g, 0, RandomKit.randomInt(this.height) + 1, this.width, RandomKit.randomInt(this.height) + 1,
                    this.interfereCount, ColorKit.randomColor());
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
     * 扭曲
     *
     * @param g     {@link Graphics}
     * @param w1    w1
     * @param h1    h1
     * @param color 颜色
     */
    private void shear(final Graphics g, final int w1, final int h1, final Color color) {
        shearX(g, w1, h1, color);
        shearY(g, w1, h1, color);
    }

    /**
     * X坐标扭曲
     *
     * @param g     {@link Graphics}
     * @param w1    宽
     * @param h1    高
     * @param color 颜色
     */
    private void shearX(final Graphics g, final int w1, final int h1, final Color color) {

        final int period = RandomKit.randomInt(this.width);

        final int frames = 1;
        final int phase = RandomKit.randomInt(2);

        for (int i = 0; i < h1; i++) {
            final double d = (double) (period >> 1)
                    * Math.sin((double) i / (double) period + (6.2831853071795862D * (double) phase) / (double) frames);
            g.copyArea(0, i, w1, 1, (int) d, 0);
            g.setColor(color);
            g.drawLine((int) d, i, 0, i);
            g.drawLine((int) d + w1, i, w1, i);
        }

    }

    /**
     * Y坐标扭曲
     *
     * @param g     {@link Graphics}
     * @param w1    宽
     * @param h1    高
     * @param color 颜色
     */
    private void shearY(final Graphics g, final int w1, final int h1, final Color color) {

        final int period = RandomKit.randomInt(this.height >> 1);

        final int frames = 20;
        final int phase = 7;
        for (int i = 0; i < w1; i++) {
            final double d = (double) (period >> 1)
                    * Math.sin((double) i / (double) period + (6.2831853071795862D * (double) phase) / (double) frames);
            g.copyArea(i, 0, 1, h1, 0, (int) d);
            g.setColor(color);
            // 擦除原位置的痕迹
            g.drawLine(i, (int) d, i, 0);
            g.drawLine(i, (int) d + h1, i, h1);
        }

    }

    /**
     * 干扰线
     *
     * @param g         {@link Graphics}
     * @param x1        x1
     * @param y1        y1
     * @param x2        x2
     * @param y2        y2
     * @param thickness 粗细
     * @param c         颜色
     */
    private void drawInterfere(final Graphics g, final int x1, final int y1, final int x2, final int y2,
            final int thickness, final Color c) {

        // The thick line is in fact a filled polygon
        g.setColor(c);
        final int dX = x2 - x1;
        final int dY = y2 - y1;
        // line length
        final double lineLength = Math.sqrt(dX * dX + dY * dY);

        final double scale = (double) (thickness) / (2 * lineLength);

        // The x and y increments from an endpoint needed to create a
        // rectangle...
        double ddx = -scale * (double) dY;
        double ddy = scale * (double) dX;
        ddx += (ddx > 0) ? 0.5 : -0.5;
        ddy += (ddy > 0) ? 0.5 : -0.5;
        final int dx = (int) ddx;
        final int dy = (int) ddy;

        // Now we can compute the corner points...
        final int[] xPoints = new int[4];
        final int[] yPoints = new int[4];

        xPoints[0] = x1 + dx;
        yPoints[0] = y1 + dy;
        xPoints[1] = x1 - dx;
        yPoints[1] = y1 - dy;
        xPoints[2] = x2 - dx;
        yPoints[2] = y2 - dy;
        xPoints[3] = x2 + dx;
        yPoints[3] = y2 + dy;

        g.fillPolygon(xPoints, yPoints, 4);
    }

}
