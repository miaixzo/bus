/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.extra.captcha;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Kimi Liu
 * @version 5.8.2
 * @since JDK 1.8+
 */
public class MathGifCaptcha extends AbstractMathCaptcha {

    public MathGifCaptcha(int width, int height) {
        setWidth(width);
        setHeight(height);
    }

    public MathGifCaptcha(int width, int height, int len) {
        this(width, height);
        setLen(len);
    }

    public MathGifCaptcha(int width, int height, int len, Font font) {
        this(width, height, len);
        setFont(font);
    }

    @Override
    public boolean out(OutputStream os) {
        char[] rands = alphas();
        boolean ok;
        try {
            GifEncoder gifEncoder = new GifEncoder();
            gifEncoder.start(os);
            gifEncoder.setQuality(180);
            gifEncoder.setDelay(100);
            gifEncoder.setRepeat(0);
            BufferedImage frame;
            Color[] fontcolor = new Color[len];
            for (int i = 0; i < len; i++) {
                fontcolor[i] = new Color(20 + num(110), 20 + num(110), 20 + num(110));
            }
            for (int i = 0; i < len; i++) {
                frame = graphicsImage(fontcolor, rands, i);
                gifEncoder.addFrame(frame);
                frame.flush();
            }
            gifEncoder.finish();
            ok = true;
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ok;
    }

    /**
     * 画随机码图
     *
     * @param fontcolor 随机字体颜色
     * @param strs      字符数组
     * @param flag      透明度使用
     * @return BufferedImage
     */
    private BufferedImage graphicsImage(Color[] fontcolor, char[] strs, int flag) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        // 填充背景颜色
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        // 抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        // 画干扰圆圈
        drawOval(4, g2d);
        // 随机画干扰线
        drawLine(2, g2d);
        // 画验证码
        int hp = (height - font.getSize()) >> 1;
        int h = height - hp;
        int w = width / strs.length;
        int sp = (w - font.getSize()) / 2;
        for (int i = 0; i < strs.length; i++) {
            AlphaComposite ac3 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getAlpha(flag, i));
            g2d.setComposite(ac3);
            g2d.setColor(fontcolor[i]);
            // 计算坐标
            int x = i * w + sp + num(3);
            int y = h - num(3, 6);
            // 调整溢出的字
            if (x < 8) {
                x = 8;
            }
            if (x + font.getSize() > width) {
                x = width - font.getSize();
            }
            if (y > height) {
                y = height;
            }
            if (y - font.getSize() < 0) {
                y = font.getSize();
            }
            g2d.setFont(font.deriveFont(num(2) == 0 ? Font.PLAIN : Font.ITALIC));
            g2d.drawString(String.valueOf(strs[i]), x, y);
        }
        g2d.dispose();
        return image;
    }

    /**
     * 获取透明度,从0到1,自动计算步长
     *
     * @param i
     * @param j
     * @return 透明度
     */
    private float getAlpha(int i, int j) {
        int num = i + j;
        float r = (float) 1 / (len - 1);
        float s = len * r;
        return num >= len ? (num * r - s) : num * r;
    }

}
