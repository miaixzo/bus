/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.core.image.element;

import lombok.Data;

import java.awt.*;

/**
 * 文本元素合成
 *
 * @author Kimi Liu
 * @version 6.1.6
 * @since JDK 1.8+
 */
@Data
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
    private int lineHeight = 50;
    /**
     * 旋转角度
     */
    private Integer rotate;

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

    /**
     * 设置自动换行
     *
     * @param maxLineWidth 最大行宽
     * @param maxLineCount 最大行数
     * @param lineHeight   行高
     * @return 当前对象
     */
    public TextElement setAutoBreakLine(int maxLineWidth, int maxLineCount, int lineHeight) {
        this.autoBreakLine = true;
        this.maxLineWidth = maxLineWidth;
        this.maxLineCount = maxLineCount;
        this.lineHeight = lineHeight;
        return this;
    }

    public String getText() {
        return text;
    }

    public TextElement setText(String text) {
        this.text = text;
        return this;
    }

    public Font getFont() {
        return font;
    }

    public TextElement setFont(Font font) {
        this.font = font;
        return this;
    }

    public Color getColor() {
        return color;
    }

    public TextElement setColor(Color color) {
        this.color = color;
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

    public TextElement setAutoBreakLine(boolean autoBreakLine) {
        this.autoBreakLine = autoBreakLine;
        return this;
    }

    public int getMaxLineWidth() {
        return maxLineWidth;
    }

    public TextElement setMaxLineWidth(int maxLineWidth) {
        this.maxLineWidth = maxLineWidth;
        return this;
    }

    public int getMaxLineCount() {
        return maxLineCount;
    }

    public TextElement setMaxLineCount(int maxLineCount) {
        this.maxLineCount = maxLineCount;
        return this;
    }

    public int getLineHeight() {
        return lineHeight;
    }

    public TextElement setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
        return this;
    }

    public Integer getRotate() {
        return rotate;
    }

    public TextElement setRotate(Integer rotate) {
        this.rotate = rotate;
        return this;
    }

}
