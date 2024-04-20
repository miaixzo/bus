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
package org.miaixz.bus.core.image.element;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Scale;
import org.miaixz.bus.core.lang.Validator;
import org.miaixz.bus.core.toolkit.StringKit;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文本元素
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TextElement extends AbstractElement<TextElement> {

    /**
     * 文本
     */
    private String text;
    /**
     * 字体
     */
    private Font font;
    /**
     * 字间距
     */
    private Float space;
    /**
     * 删除线
     */
    private boolean strikeThrough;
    /**
     * 颜色，默认黑色
     */
    private Color color = new Color(0, 0, 0);
    /**
     * 是否自动换行
     */
    private boolean autoBreakLine = false;
    /**
     * 最大行宽，超出则换行
     */
    private int maxLineWidth = 600;
    /**
     * 最大行数，超出则丢弃多余行
     */
    private int maxLineCount = 2;
    /**
     * 行高，根据字体大小酌情设置
     */
    private Integer lineHeight;
    /**
     * 旋转角度
     */
    private Integer rotate;
    /**
     * 宽度（只读，计算值）
     */
    private Integer width;
    /**
     * 高度（只读，计算值，单行时等于lineHeight，多行时等于lineHeight*行数）
     */
    private Integer height;
    /**
     * 实际绘制用的y（sketch的y与graph2d有所区别，需要换算）
     */
    private Integer drawY;

    /**
     * 行对齐方式，默认左对齐
     */
    private Scale.Align align = Scale.Align.LEFT;
    /**
     * 换过行的元素们
     */
    private List<TextElement> breakLineElements;

    /**
     * @param text 文本内容
     * @param font Font对象
     * @param x    x坐标
     * @param y    y坐标
     */
    public TextElement(String text, Font font, int x, int y) {
        this.text = text;
        this.font = font;
        super.setX(x);
        super.setY(y);
    }

    /**
     * @param text     文本内容
     * @param fontSize 字号
     * @param x        x坐标
     * @param y        y坐标
     */
    public TextElement(String text, int fontSize, int x, int y) {
        this.text = text;
        this.font = new Font("阿里巴巴普惠体", Font.PLAIN, fontSize);
        super.setX(x);
        super.setY(y);
    }

    /**
     * @param text     文本内容
     * @param fontName 字体名称
     * @param fontSize 字号
     * @param x        x坐标
     * @param y        y坐标
     */
    public TextElement(String text, String fontName, int fontSize, int x, int y) {
        this.text = text;
        this.font = new Font(fontName, Font.PLAIN, fontSize);
        super.setX(x);
        super.setY(y);
    }

    public Integer getWidth() {
        if (width == null) {
            width = getFrontWidth(text);
        }
        return width;
    }

    public Integer getHeight() {
        if (height == null) {
            if (autoBreakLine == true) {
                height = getLineHeight() * getBreakLineElements().size();     //如果自动换行，则计算多行高度
            } else {
                height = getLineHeight();
            }
        }
        return height;
    }

    public Integer getDrawY() {
        if (drawY == null) {
            drawY = getY() + (getLineHeight() - getHeight()) / 2;
        }
        return drawY;
    }

    public List<TextElement> getBreakLineElements() {
        if (breakLineElements == null) {
            breakLineElements = computeBreakLineElements();
        }
        return breakLineElements;
    }

    @Override
    public TextElement setY(int y) {
        // textElement的setY会影响drawY的计算，所以要复写基类方法，重置一下计算属性
        this.resetProperties();
        return super.setY(y);
    }

    public String getText() {
        return text;
    }

    public TextElement setText(String text) {
        this.resetProperties();
        this.text = text;
        return this;
    }

    public Font getFont() {
        return font;
    }

    public TextElement setFont(Font font) {
        this.resetProperties();
        this.font = font;
        return this;
    }

    public Float getSpace() {
        return space;
    }

    public TextElement setSpace(Float space) {
        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.TRACKING, space);
        Font font2 = font.deriveFont(attributes);
        this.setFont(font2);
        return this;
    }

    public Integer getRotate() {
        return rotate;
    }

    public TextElement setRotate(Integer rotate) {
        this.rotate = rotate;
        return this;
    }

    public Color getColor() {
        return color;
    }

    public TextElement setColor(Color color) {
        this.color = color;
        return this;
    }

    public TextElement setColor(int r, int g, int b) {
        return setColor(new Color(r, g, b));
    }

    public Integer getLineHeight() {
        if (lineHeight == null) {
            // 未设置lineHeight则默认取文本高度
            lineHeight = this.getHeight();
        }
        return lineHeight;
    }

    public TextElement setLineHeight(Integer lineHeight) {
        this.resetProperties();
        this.lineHeight = lineHeight;
        return this;
    }

    public boolean isStrikeThrough() {
        return strikeThrough;
    }

    public TextElement setStrikeThrough(boolean strikeThrough) {
        this.strikeThrough = strikeThrough;
        return this;
    }

    public boolean isAutoBreakLine() {
        return autoBreakLine;
    }

    /**
     * 设置自动换行（默认左对齐）
     *
     * @param maxLineWidth 最大宽度（超出则换行）
     * @param maxLineCount 最大行数（超出则丢弃）
     * @param lineHeight   行高
     * @return
     */
    public TextElement setAutoBreakLine(int maxLineWidth, int maxLineCount, int lineHeight) {
        this.autoBreakLine = true;
        this.maxLineWidth = maxLineWidth;
        this.maxLineCount = maxLineCount;
        this.lineHeight = lineHeight;
        return this;
    }

    /**
     * 设置自动换行（默认左对齐）
     *
     * @param maxLineWidth 最大宽度（超出则换行）
     * @param maxLineCount 最大行数（超出则丢弃）
     * @return this
     */
    public TextElement setAutoBreakLine(int maxLineWidth, int maxLineCount) {
        this.autoBreakLine = true;
        this.maxLineWidth = maxLineWidth;
        this.maxLineCount = maxLineCount;
        return this;
    }

    /**
     * 设置自动换行
     *
     * @param maxLineWidth 最大宽度（超出则换行）
     * @param maxLineCount 最大行数（超出则丢弃）
     * @param lineHeight   行高
     * @param align        行对齐方式
     * @return this
     */
    public TextElement setAutoBreakLine(int maxLineWidth, int maxLineCount, int lineHeight, Scale.Align align) {
        this.autoBreakLine = true;
        this.maxLineWidth = maxLineWidth;
        this.maxLineCount = maxLineCount;
        this.lineHeight = lineHeight;
        this.align = align;
        return this;
    }

    /**
     * 设置自动换行
     *
     * @param maxLineWidth 最大宽度（超出则换行）
     * @param maxLineCount 最大行数（超出则丢弃）
     * @param align        行对齐方式
     * @return this
     */
    public TextElement setAutoBreakLine(int maxLineWidth, int maxLineCount, Scale.Align align) {
        this.autoBreakLine = true;
        this.maxLineWidth = maxLineWidth;
        this.maxLineCount = maxLineCount;
        this.align = align;
        return this;
    }

    public Scale.Align getAlign() {
        return align;
    }

    private void resetProperties() {
        // 如果设置了影响布局的字段，需要重置这几个计算值（主要为提高性能，计算字段如未受影响，则只计算一次，相当于缓存，有变动再重置重算）
        this.width = null;
        this.height = null;
        this.drawY = null;
        this.breakLineElements = null;
    }

    private List<TextElement> computeBreakLineElements() {
        List<TextElement> breakLineElements = new ArrayList<>();
        List<String> breakLineTexts = computeLines(text);
        int currentY = getY();
        for (int i = 0; i < breakLineTexts.size(); i++) {
            if (i < maxLineCount) {
                String text = breakLineTexts.get(i);
                //如果计该行是要取的最后一行，但不是整体最后一行，则加...
                if (i == maxLineCount - 1 && i < breakLineTexts.size() - 1) {
                    text = text.substring(0, text.length() - 1) + "...";
                }
                TextElement textLineElement = new TextElement(text, font, getX(), currentY);
                textLineElement.setColor(color);
                textLineElement.setStrikeThrough(strikeThrough);
                textLineElement.setCenter(isCenter());
                textLineElement.setAlpha(getAlpha());
                textLineElement.setRotate(rotate);
                textLineElement.setLineHeight(getLineHeight());
                textLineElement.setDirection(getDirection());
                breakLineElements.add(textLineElement);

                currentY += getLineHeight();

            } else {
                break;
            }
        }
        return breakLineElements;
    }

    private List<String> computeLines(String text) {
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
            if (count++ > 2000) {
                // 防止意外情况进入死循环
                break;
            }
            // 当前字符
            char c = chars[i];
            if (Validator.isChinese(StringKit.toString(c)) || c == ' ' || i == (chars.length - 1)) {
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
                int originWidth = getFrontWidth(strToComputer);
                // 计算单个单词宽度（防止一个单词就超限宽的情况）
                int wordWidth = getFrontWidth(word);
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
                //行宽度超出限宽，则去除最后word，加入计算结果列表
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
                hasWord = false;        //重置标记
            }
        }

        if (strToComputer != Normal.EMPTY) {
            // 加入计算结果列表
            computedLines.add(strToComputer);
        }

        return computedLines;
    }

    /**
     * 计算文本宽度
     *
     * @param text 文本元素
     * @return 高度数值
     */
    public int getFrontWidth(String text) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        FontMetrics metrics = image.createGraphics().getFontMetrics(this.font);

        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            width += metrics.charWidth(text.charAt(i));
        }

        return width;
    }

}
