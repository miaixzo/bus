/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.image;

import org.aoju.bus.core.utils.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * {@link java.awt.Graphics}相关工具类
 *
 * @author Kimi Liu
 * @version 5.2.9
 * @since JDK 1.8+
 */
public class Graphics {

    /**
     * 创建{@link Graphics2D}
     *
     * @param image {@link BufferedImage}
     * @param color {@link Color}背景颜色以及当前画笔颜色,{@code null}表示不设置背景色
     * @return {@link Graphics2D}
     */
    public static Graphics2D createGraphics(BufferedImage image, Color color) {
        final Graphics2D g = image.createGraphics();

        if (null != color) {
            // 填充背景
            g.setColor(color);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
        }

        return g;
    }

    /**
     * 获取文字居中高度的Y坐标（距离上边距距离）
     * 此方法依赖FontMetrics,如果获取失败,默认为背景高度的1/3
     *
     * @param g                {@link Graphics2D}画笔
     * @param backgroundHeight 背景高度
     * @return 最小高度,-1表示无法获取
     */
    public static int getCenterY(java.awt.Graphics g, int backgroundHeight) {
        // 获取允许文字最小高度
        FontMetrics metrics = null;
        try {
            metrics = g.getFontMetrics();
        } catch (Exception e) {
            // 此处报告bug某些情况下会抛出IndexOutOfBoundsException,在此做容错处理
        }
        int y;
        if (null != metrics) {
            y = (backgroundHeight - metrics.getHeight()) / 2 + metrics.getAscent();
        } else {
            y = backgroundHeight / 3;
        }
        return y;
    }

    /**
     * 绘制字符串,使用随机颜色,默认抗锯齿
     *
     * @param g      {@link java.awt.Graphics}画笔
     * @param str    字符串
     * @param font   字体
     * @param width  字符串总宽度
     * @param height 字符串背景高度
     * @return 画笔对象
     */
    public static java.awt.Graphics drawStringColourful(java.awt.Graphics g, String str, Font font, int width, int height) {
        return drawString(g, str, font, null, width, height);
    }

    /**
     * 绘制字符串,默认抗锯齿
     *
     * @param g      {@link java.awt.Graphics}画笔
     * @param str    字符串
     * @param font   字体
     * @param color  字体颜色,{@code null} 表示使用随机颜色（每个字符单独随机）
     * @param width  字符串背景的宽度
     * @param height 字符串背景的高度
     * @return 画笔对象
     */
    public static java.awt.Graphics drawString(java.awt.Graphics g, String str, Font font, Color color, int width, int height) {
        // 抗锯齿
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        // 创建字体
        g.setFont(font);

        // 文字高度（必须在设置字体后调用）
        int midY = Graphics.getCenterY(g, height);
        if (null != color) {
            g.setColor(color);
        }

        final int len = str.length();
        int charWidth = width / len;
        for (int i = 0; i < len; i++) {
            if (null == color) {
                // 产生随机的颜色值,让输出的每个字符的颜色值都将不同
                g.setColor(ImageUtils.randomColor());
            }
            g.drawString(String.valueOf(str.charAt(i)), i * charWidth, midY);
        }
        return g;
    }

}
