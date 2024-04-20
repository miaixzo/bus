/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.image.painter;

import org.miaixz.bus.core.image.element.AbstractElement;
import org.miaixz.bus.core.image.element.RectangleElement;
import org.miaixz.bus.core.lang.Scale;

import java.awt.*;

/**
 * 矩形绘制器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RectanglePainter implements Painter {

    @Override
    public void draw(Graphics2D g, AbstractElement element, int canvasWidth) {
        // 强制转成子类
        RectangleElement rectangleElement = (RectangleElement) element;
        // 设置颜色
        g.setColor(rectangleElement.getColor());
        // 设置居中（优先）和绘制方向
        if (rectangleElement.isCenter()) {
            int centerX = (canvasWidth - rectangleElement.getWidth()) / 2;
            rectangleElement.setX(centerX);
        } else if (rectangleElement.getDirection() == Scale.Direction.RIGHT_LEFT) {
            rectangleElement.setX(rectangleElement.getX() - rectangleElement.getWidth());
        } else if (rectangleElement.getDirection() == Scale.Direction.CENTER_LEFT_RIGHT) {
            rectangleElement.setX(rectangleElement.getX() - rectangleElement.getWidth() / 2);
        }

        // 设置渐变
        if (rectangleElement.getFromColor() != null) {
            float fromX = 0, fromY = 0, toX = 0, toY = 0;
            switch (rectangleElement.getGradient()) {
                case TOP_BOTTOM:
                    fromX = rectangleElement.getX() + rectangleElement.getWidth() / 2;
                    fromY = rectangleElement.getY() - rectangleElement.getFromExtend();
                    toX = fromX;
                    toY = rectangleElement.getY() + rectangleElement.getHeight() + rectangleElement.getToExtend();
                    break;
                case LEFT_RIGHT:
                    fromX = rectangleElement.getX() - rectangleElement.getFromExtend();
                    fromY = rectangleElement.getY() + rectangleElement.getHeight() / 2;
                    toX = rectangleElement.getX() + rectangleElement.getWidth() + rectangleElement.getToExtend();
                    toY = fromY;
                    break;
                case LEFT_TOP_TO_RIGHT_BOTTOM:
                    fromX = rectangleElement.getX() - (float) Math.sqrt(rectangleElement.getFromExtend());
                    fromY = rectangleElement.getY() - (float) Math.sqrt(rectangleElement.getFromExtend());
                    toX = rectangleElement.getX() + rectangleElement.getWidth() + (float) Math.sqrt(rectangleElement.getToExtend());
                    toY = rectangleElement.getY() + rectangleElement.getHeight() + (float) Math.sqrt(rectangleElement.getToExtend());
                    break;
                case RIGHT_TOP_TO_LEFT_BOTTOM:
                    fromX = rectangleElement.getX() + rectangleElement.getWidth() + (float) Math.sqrt(rectangleElement.getFromExtend());
                    fromY = rectangleElement.getY() - (float) Math.sqrt(rectangleElement.getFromExtend());
                    toX = rectangleElement.getX() - (float) Math.sqrt(rectangleElement.getToExtend());
                    toY = rectangleElement.getY() + rectangleElement.getHeight() + (float) Math.sqrt(rectangleElement.getToExtend());
                    break;
            }
            g.setPaint(new GradientPaint(fromX, fromY, rectangleElement.getFromColor(), toX, toY, rectangleElement.getToColor()));
        } else {
            g.setPaint(null);
        }

        // 设置透明度
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, rectangleElement.getAlpha()));

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.fillRoundRect(rectangleElement.getX(), rectangleElement.getY(), rectangleElement.getWidth(), rectangleElement.getHeight(), rectangleElement.getRoundCorner(), rectangleElement.getRoundCorner());
    }

    @Override
    public void drawRepeat(Graphics2D g, AbstractElement element, int canvasWidth, int canvasHeight) {
        // 强制转成子类
        RectangleElement rectangleElement = (RectangleElement) element;
        int currentX = element.getX();
        int currentY = element.getY();
        // 起始坐标归位
        while (currentX > 0) {
            currentX = currentX - rectangleElement.getHorizontal() - rectangleElement.getWidth();
        }
        while (currentY > 0) {
            currentY = currentY - rectangleElement.getVertical() - rectangleElement.getHeight();
        }
        int startY = currentY;
        // 从左往右绘制
        while (currentX < canvasWidth) {
            rectangleElement.setX(currentX);
            currentX = currentX + rectangleElement.getHorizontal() + rectangleElement.getWidth();
            // 从上往下绘制
            while (currentY < canvasHeight) {
                rectangleElement.setY(currentY);
                currentY = currentY + rectangleElement.getVertical() + rectangleElement.getHeight();
                draw(g, rectangleElement, canvasWidth);
            }
            // 重置y坐标
            currentY = startY;
        }
    }

}
