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
 ********************************************************************************/
package org.aoju.bus.office.magic.filter.text;

import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextFrame;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.office.Context;
import org.aoju.bus.office.magic.Lo;
import org.aoju.bus.office.magic.Write;
import org.aoju.bus.office.magic.filter.FilterChain;

import java.awt.*;
import java.util.Map;

/**
 * 筛选器用于将文本插入文档.
 *
 * @author Kimi Liu
 * @version 5.9.2
 * @since JDK 1.8+
 */
public class TextInserterFilter extends AbstractTextContentInserterFilter {

    private final String insertedText;

    /**
     * 创建一个新筛选器，在转换文档时将在指定位置插入指定的文本.
     *
     * @param text               要插入的文本.
     * @param width              要插入的矩形的宽度(毫米).
     * @param height             要插入的矩形的高度(毫米).
     * @param horizontalPosition 在文档上插入文本的水平位置(毫米).
     * @param verticalPosition   在文档上插入文本的垂直位置(毫米).
     */
    public TextInserterFilter(
            final String text,
            final int width,
            final int height,
            final int horizontalPosition,
            final int verticalPosition) {
        super(new Dimension(width, height), horizontalPosition, verticalPosition);

        Assert.notBlank(text);

        this.insertedText = text;
    }

    /**
     * 创建一个新筛选器，在转换文档时将在指定位置插入指定的文本.
     *
     * @param text            要插入的文本.
     * @param width           要插入的矩形的宽度(毫米).
     * @param height          要插入的矩形的高度(毫米).
     * @param shapeProperties 要应用于创建的矩形形状的属性.
     */
    public TextInserterFilter(
            final String text,
            final int width,
            final int height,
            final Map<String, Object> shapeProperties) {
        super(new Dimension(width, height), shapeProperties);

        Assert.notBlank(text);

        this.insertedText = text;
    }

    @Override
    public void doFilter(
            final Context context, final XComponent document, final FilterChain chain)
            throws Exception {

        Logger.debug("Applying the TextInserterFilter");

        // 此筛选器只能用于文本文档
        if (Write.isText(document)) {
            insertText(document);
        }

        // 调用链中的下一个过滤器
        chain.doFilter(context, document);
    }

    private void insertText(final XComponent document) throws Exception {

        // 使用文档的工厂创建一个新的文本框，并立即访问它的XTextFrame接口
        final XTextFrame textFrame =
                Lo.createInstanceMSF(document, XTextFrame.class, "com.sun.star.text.TextFrame");

        // 访问TextFrame的XShape接口
        final XShape shape = Lo.qi(XShape.class, textFrame);

        // 使用XShape设置新文本框的大小'setSize'
        shape.setSize(toOfficeSize(getRectSize()));

        // 访问TextFrame的XPropertySet接口
        final XPropertySet propSet = Lo.qi(XPropertySet.class, textFrame);

        // 分配所有其他属性
        for (final Map.Entry<String, Object> entry : getShapeProperties().entrySet()) {
            propSet.setPropertyValue(entry.getKey(), entry.getValue());
        }

        // 在XComponent上查询接口XTextDocument(文本接口)
        final XTextDocument docText = Write.getTextDoc(document);

        // 访问框架中包含的文本的XText接口
        XText text = docText.getText();
        XTextCursor textCursor = text.createTextCursor();

        // 应用AnchorPageNo修复
        applyAnchorPageNoFix(docText, textCursor);

        // 将新框架插入文档
        Logger.debug("Inserting frame into the document");
        text.insertTextContent(textCursor, textFrame, false);

        // 访问框架中包含的文本的XText接口
        text = textFrame.getText();

        // 在框架的内容上创建一个TextCursor
        textCursor = text.createTextCursor();

        // 将文本插入框架中
        Logger.debug("Writing text to the inserted frame");
        text.insertString(textCursor, insertedText, false);
    }

}
