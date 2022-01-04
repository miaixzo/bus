/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.image.painter;

import org.aoju.bus.core.image.element.AbstractElement;
import org.aoju.bus.core.image.element.TextElement;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.StringKit;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

/**
 * 文本绘制器
 *
 * @author Kimi Liu
 * @version 6.3.3
 * @since JDK 1.8+
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
            textLineElements = this.getBreakLineElements(textElement);
        }

        for (int i = 0; i < textLineElements.size(); i++) {
            TextElement textLineElement = textLineElements.get(i);
            int textWidth = 0;
            // 设置字体、颜色
            g.setFont(textLineElement.getFont());
            g.setColor(textLineElement.getColor());

            // 设置居中（多行的时候，第一行居中，后续行以第一行的x坐标为准）
            if (textLineElement.isCenter()) {
                if (i == 0) {
                    textWidth = this.getFrontWidth(textLineElement.getText(), textLineElement.getFont());
                    int centerX = (canvasWidth - textWidth) / 2;
                    textLineElement.setX(centerX);
                } else {
                    textLineElement.setX(textLineElements.get(0).getX());
                }
            }

            // 旋转
            if (null != textLineElement.getRotate()) {
                if (textWidth == 0) {
                    textWidth = this.getFrontWidth(textLineElement.getText(), textLineElement.getFont());
                }
                g.rotate(Math.toRadians(textLineElement.getRotate()), textLineElement.getX() + textWidth / 2, textLineElement.getY());
            }

            // 设置透明度
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textLineElement.getAlpha()));

            // 带删除线样式的文字要特殊处理
            if (textLineElement.isStrikeThrough() == true) {
                AttributedString as = new AttributedString(textLineElement.getText());
                as.addAttribute(TextAttribute.FONT, textLineElement.getFont());
                as.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON, 0, textLineElement.getText().length());
                g.drawString(as.getIterator(), textLineElement.getX(), textLineElement.getY());
            } else {
                g.drawString(textLineElement.getText(), textLineElement.getX(), textLineElement.getY());
            }

            // 绘制完后反向旋转，以免影响后续元素
            if (null != textLineElement.getRotate()) {
                g.rotate(-Math.toRadians(textLineElement.getRotate()), textLineElement.getX() + textWidth / 2, textLineElement.getY());
            }
        }
    }

    public int getFrontWidth(String text, Font font) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        FontMetrics metrics = image.createGraphics().getFontMetrics(font);

        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            width += metrics.charWidth(text.charAt(i));
        }

        return width;
    }

    private boolean isChineseChar(char c) {
        return String.valueOf(c).matches("[\u4e00-\u9fa5]");
    }

    private List<String> computeLines(String text, Font font, int maxLineWidth) {
        // 最终要返回的多行文本（不超限定宽度）
        List<String> computedLines = new ArrayList<>();
        String strToComputer = Normal.EMPTY;
        // 一个完整单词
        String word = Normal.EMPTY;
        // 是否获得一个完整单词
        boolean hasWord = false;
        char[] chars = text.toCharArray();
        int count = 0;

        // 遍历每个字符，拆解单词（一个中文算一个单词，其他字符直到碰到空格算一个单词）
        for (int i = 0; i < chars.length; i++) {
            if (count++ > 500) {
                // 防止意外情况进入死循环
                break;
            }
            // 当前字符
            char c = chars[i];
            if (isChineseChar(c) || c == Symbol.C_SPACE || i == (chars.length - 1)) {
                // 如果是中文或空格或最后一个字符，一个中文算一个单词, 其他字符遇到空格认为单词结束
                word += c;
                hasWord = true;
            } else {
                // 英文或其他字符，加入word，待组成单词
                word += c;
            }

            // 获得了一个完整单词，加入当前行，并计算限宽
            if (hasWord) {

                // 计算现有文字宽度
                int originWidth = getFrontWidth(strToComputer, font);

                // 计算单个单词宽度（防止一个单词就超限宽的情况）
                int wordWidth = getFrontWidth(word, font);

                // 单词加入待计算字符串
                strToComputer += word;

                // 加入了新单词之后的宽度
                int newWidth = originWidth + wordWidth;

                // 一个单词就超限，要暴力换行
                if (wordWidth > maxLineWidth) {
                    // 按比例计算要取几个字符（不是特别精准）
                    // 本行剩余宽度所占word宽度比例，乘以字符长度（字符不等宽的时候不太准）
                    int fetch = (int) ((float) (maxLineWidth - originWidth) / (float) wordWidth * word.length());
                    // 去除最后的word的后半截
                    strToComputer = strToComputer.substring(0, strToComputer.length() - word.length() + fetch);
                    // 加入计算结果列表
                    computedLines.add(strToComputer);
                    strToComputer = Normal.EMPTY;
                    // 遍历计数器回退word.length()-fetch个
                    i -= (word.length() - fetch);
                }
                // 行宽度超出限宽，则去除最后word，加入计算结果列表
                else if (newWidth > maxLineWidth) {
                    // 去除最后word
                    strToComputer = strToComputer.substring(0, strToComputer.length() - word.length());
                    // 加入计算结果列表
                    computedLines.add(strToComputer);
                    strToComputer = Normal.EMPTY;
                    // 遍历计数器回退word.length()个
                    i -= word.length();
                }

                word = Normal.EMPTY;
                // 重置标记
                hasWord = false;
            }
        }

        if (StringKit.isNotEmpty(strToComputer)) {
            // 加入计算结果列表
            computedLines.add(strToComputer);
        }

        return computedLines;
    }

    public List<TextElement> getBreakLineElements(TextElement textElement) {
        List<TextElement> breakLineElements = new ArrayList<>();
        List<String> breakLineTexts = computeLines(textElement.getText(), textElement.getFont(), textElement.getMaxLineWidth());
        int y = textElement.getY();
        for (int i = 0; i < breakLineTexts.size(); i++) {
            if (i < textElement.getMaxLineCount()) {
                String text = breakLineTexts.get(i);
                // 如果计该行是要取的最后一行，但不是整体最后一行，则加...
                if (i == textElement.getMaxLineCount() - 1 && i < breakLineTexts.size() - 1) {
                    text = text.substring(0, text.length() - 1) + "...";
                }
                TextElement combineTextLine = new TextElement(text, textElement.getFont(), textElement.getX(), y);
                combineTextLine.setColor(textElement.getColor());
                combineTextLine.setStrikeThrough(textElement.isStrikeThrough());
                combineTextLine.setCenter(textElement.isCenter());
                combineTextLine.setAlpha(textElement.getAlpha());
                combineTextLine.setRotate(textElement.getRotate());
                breakLineElements.add(combineTextLine);

                // 累加高度
                y += textElement.getLineHeight();
            } else {
                break;
            }
        }
        return breakLineElements;
    }

}
