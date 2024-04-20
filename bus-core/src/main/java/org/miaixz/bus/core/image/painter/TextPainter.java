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
import org.miaixz.bus.core.image.element.TextElement;
import org.miaixz.bus.core.lang.Scale;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

/**
 * 文本绘制器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TextPainter implements Painter {

    @Override
    public void draw(Graphics2D g, AbstractElement element, int canvasWidth) {
        // 强制转成子类
        TextElement textElement = (TextElement) element;
        // 首先计算是否要换行（由于拆行计算比较耗资源，不设置换行则直接用原始对象绘制）
        List<TextElement> textLineElements = new ArrayList<>();
        textLineElements.add(textElement);
        if (textElement.isAutoBreakLine()) {
            textLineElements = textElement.getBreakLineElements();
        }
        for (int i = 0; i < textLineElements.size(); i++) {
            TextElement firstLineElement = textLineElements.get(0);
            TextElement currentLineElement = textLineElements.get(i);
            // 设置字体、颜色
            g.setFont(currentLineElement.getFont());
            g.setColor(currentLineElement.getColor());
            // 设置居中（多行的时候，第一行居中，后续行按对齐方式计算）
            if (currentLineElement.isCenter()) {
                if (i == 0) {
                    currentLineElement.setX((canvasWidth - currentLineElement.getWidth()) / 2);
                } else {
                    switch (textElement.getAlign()) {
                        case LEFT:
                            currentLineElement.setX(firstLineElement.getX());
                            break;
                        case CENTER:
                            currentLineElement.setX((canvasWidth - currentLineElement.getWidth()) / 2);
                            break;
                        case RIGHT:
                            currentLineElement.setX(firstLineElement.getX() + firstLineElement.getWidth() - currentLineElement.getWidth());
                            break;
                    }
                }
            } else {
                if (i == 0) {
                    // 绘制方向（只处理第一个元素即可，后续元素会参照第一个元素的坐标来计算）
                    if (currentLineElement.getDirection() == Scale.Direction.RIGHT_LEFT) {
                        currentLineElement.setX(currentLineElement.getX() - currentLineElement.getWidth());
                    } else if (currentLineElement.getDirection() == Scale.Direction.CENTER_LEFT_RIGHT) {
                        currentLineElement.setX(currentLineElement.getX() - currentLineElement.getWidth() / 2);
                    }
                } else {
                    switch (textElement.getAlign()) {
                        case LEFT:
                            currentLineElement.setX(firstLineElement.getX());
                            break;
                        case CENTER: {
                            currentLineElement.setX(firstLineElement.getX() + (firstLineElement.getWidth() - currentLineElement.getWidth()) / 2);
                            break;
                        }
                        case RIGHT: {
                            currentLineElement.setX(firstLineElement.getX() + firstLineElement.getWidth() - currentLineElement.getWidth());
                            break;
                        }
                    }
                }
            }

            // 旋转
            if (currentLineElement.getRotate() != null) {
                g.rotate(Math.toRadians(currentLineElement.getRotate()), currentLineElement.getX() + currentLineElement.getWidth() / 2, currentLineElement.getDrawY());
            }
            // 设置透明度
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, currentLineElement.getAlpha()));
            // 带删除线样式的文字要特殊处理
            if (currentLineElement.isStrikeThrough() == true) {
                AttributedString as = new AttributedString(currentLineElement.getText());
                as.addAttribute(TextAttribute.FONT, currentLineElement.getFont());
                as.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON, 0, currentLineElement.getText().length());
                g.drawString(as.getIterator(), currentLineElement.getX(), currentLineElement.getDrawY());
            } else {
                g.drawString(currentLineElement.getText(), currentLineElement.getX(), currentLineElement.getDrawY());
            }
            // 绘制完后反向旋转，以免影响后续元素
            if (currentLineElement.getRotate() != null) {
                g.rotate(-Math.toRadians(currentLineElement.getRotate()), currentLineElement.getX() + currentLineElement.getWidth() / 2, currentLineElement.getDrawY());
            }
        }
    }

    @Override
    public void drawRepeat(Graphics2D g, AbstractElement element, int canvasWidth, int canvasHeight) {
        // 强制转成子类
        TextElement textElement = (TextElement) element;
        int currentX = element.getX();
        int currentY = element.getY();
        // 起始坐标归位
        while (currentX > 0) {
            currentX = currentX - textElement.getHorizontal() - textElement.getWidth();
        }
        while (currentY > 0) {
            currentY = currentY - textElement.getVertical() - textElement.getHeight();
        }
        int startY = currentY;
        // 从左往右绘制
        while (currentX < canvasWidth) {
            textElement.setX(currentX);
            currentX = currentX + textElement.getHorizontal() + textElement.getWidth();
            // 从上往下绘制
            while (currentY < canvasHeight) {
                textElement.setY(currentY);
                currentY = currentY + textElement.getVertical() + textElement.getHeight();
                draw(g, textElement, canvasWidth);
            }
            // 重置y坐标
            currentY = startY;
        }
    }

}
