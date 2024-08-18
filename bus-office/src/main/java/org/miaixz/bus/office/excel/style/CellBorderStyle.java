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
package org.miaixz.bus.office.excel.style;

import java.io.Serializable;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * 单元格边框样式和颜色封装，边框按照“上右下左”的顺序定义，与CSS一致
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CellBorderStyle implements Serializable {

    private static final long serialVersionUID = -1L;

    private BorderStyle topStyle;
    private Short topColor;
    private BorderStyle rightStyle;
    private Short rightColor;
    private BorderStyle bottomStyle;
    private Short bottomColor;
    private BorderStyle leftStyle;
    private Short leftColor;

    /**
     * 创建单元格边框样式对象，四边框样式保持一致。
     *
     * @param borderStyle 边框样式
     * @param colorIndex  颜色
     * @return 单元格边框样式对象
     */
    public static CellBorderStyle of(final BorderStyle borderStyle, final IndexedColors colorIndex) {
        return new CellBorderStyle().setTopStyle(borderStyle).setTopColor(colorIndex.getIndex())
                .setRightStyle(borderStyle).setRightColor(colorIndex.getIndex()).setBottomStyle(borderStyle)
                .setBottomColor(colorIndex.getIndex()).setLeftStyle(borderStyle).setLeftColor(colorIndex.getIndex());
    }

    /**
     * 获取上边框的样式
     *
     * @return 上边框的样式
     */
    public BorderStyle getTopStyle() {
        return topStyle;
    }

    /**
     * 设置上边框的样式
     *
     * @param topStyle 上边框的样式
     * @return 当前的单元格边框样式对象，支持链式调用。
     */
    public CellBorderStyle setTopStyle(final BorderStyle topStyle) {
        this.topStyle = topStyle;
        return this;
    }

    /**
     * 获取上边框的颜色
     *
     * @return 上边框的颜色
     */
    public Short getTopColor() {
        return topColor;
    }

    /**
     * 设置上边框的颜色
     *
     * @param topColor 上边框的颜色
     * @return 当前的单元格边框样式对象，支持链式调用。
     */
    public CellBorderStyle setTopColor(final Short topColor) {
        this.topColor = topColor;
        return this;
    }

    /**
     * 获取右边框的样式。
     *
     * @return 右边框的样式。
     */
    public BorderStyle getRightStyle() {
        return rightStyle;
    }

    /**
     * 设置右边框的样式
     *
     * @param rightStyle 右边框的样式
     * @return 当前的单元格边框样式对象，支持链式调用。
     */
    public CellBorderStyle setRightStyle(final BorderStyle rightStyle) {
        this.rightStyle = rightStyle;
        return this;
    }

    /**
     * 获取右边框的颜色
     *
     * @return 右边框的颜色
     */
    public Short getRightColor() {
        return rightColor;
    }

    /**
     * 设置右边框的颜色
     *
     * @param rightColor 右边框的颜色
     * @return 当前的单元格边框样式对象，支持链式调用。
     */
    public CellBorderStyle setRightColor(final Short rightColor) {
        this.rightColor = rightColor;
        return this;
    }

    /**
     * 获取底边框的样式
     *
     * @return 底边框的样式
     */
    public BorderStyle getBottomStyle() {
        return bottomStyle;
    }

    /**
     * 设置底边框的样式
     *
     * @param bottomStyle 底边框的样式
     * @return 当前的单元格边框样式对象，支持链式调用。
     */
    public CellBorderStyle setBottomStyle(final BorderStyle bottomStyle) {
        this.bottomStyle = bottomStyle;
        return this;
    }

    /**
     * 获取底边框的颜色
     *
     * @return 底边框的颜色
     */
    public Short getBottomColor() {
        return bottomColor;
    }

    /**
     * 设置底边框的颜色
     *
     * @param bottomColor 底边框的颜色
     * @return 当前的单元格边框样式对象，支持链式调用
     */
    public CellBorderStyle setBottomColor(final Short bottomColor) {
        this.bottomColor = bottomColor;
        return this;
    }

    /**
     * 获取左边框的样式
     *
     * @return 左边框的样式
     */
    public BorderStyle getLeftStyle() {
        return leftStyle;
    }

    /**
     * 设置左边框的样式
     *
     * @param leftStyle 左边框的样式
     * @return 当前的单元格边框样式对象，支持链式调用
     */
    public CellBorderStyle setLeftStyle(final BorderStyle leftStyle) {
        this.leftStyle = leftStyle;
        return this;
    }

    /**
     * 获取左边框的颜色
     *
     * @return 左边框的颜色
     */
    public Short getLeftColor() {
        return leftColor;
    }

    /**
     * 设置左边框的颜色
     *
     * @param leftColor 左边框的颜色
     * @return 当前的单元格边框样式对象，支持链式调用
     */
    public CellBorderStyle setLeftColor(final Short leftColor) {
        this.leftColor = leftColor;
        return this;
    }

    /**
     * 将边框样式和颜色设置到CellStyle中
     *
     * @param cellStyle {@link CellStyle}
     * @return {@link CellStyle}
     */
    public CellStyle setTo(final CellStyle cellStyle) {
        ObjectKit.accept(this.topStyle, cellStyle::setBorderTop);
        ObjectKit.accept(this.topColor, cellStyle::setTopBorderColor);

        ObjectKit.accept(this.rightStyle, cellStyle::setBorderRight);
        ObjectKit.accept(this.rightColor, cellStyle::setRightBorderColor);

        ObjectKit.accept(this.bottomStyle, cellStyle::setBorderBottom);
        ObjectKit.accept(this.bottomColor, cellStyle::setBottomBorderColor);

        ObjectKit.accept(this.leftStyle, cellStyle::setBorderLeft);
        ObjectKit.accept(this.leftColor, cellStyle::setLeftBorderColor);

        return cellStyle;
    }

}
