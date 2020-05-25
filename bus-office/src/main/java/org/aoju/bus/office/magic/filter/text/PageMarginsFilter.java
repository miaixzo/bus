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
import com.sun.star.container.XNameAccess;
import com.sun.star.container.XNameContainer;
import com.sun.star.lang.XComponent;
import com.sun.star.style.XStyle;
import com.sun.star.style.XStyleFamiliesSupplier;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.office.Context;
import org.aoju.bus.office.magic.Lo;
import org.aoju.bus.office.magic.Write;
import org.aoju.bus.office.magic.filter.Filter;
import org.aoju.bus.office.magic.filter.FilterChain;

/**
 * 此筛选器用于设置要转换的文档的页边距.
 *
 * @author Kimi Liu
 * @version 5.9.3
 * @since JDK 1.8+
 */
public class PageMarginsFilter implements Filter {

    private final Integer topMargin;
    private final Integer rightMargin;
    private final Integer bottomMargin;
    private final Integer leftMargin;

    /**
     * 创建一个新的过滤器来设置文档的页边距.
     *
     * @param leftMargin   左边的空白(毫米)可能是空的。如果为空，则左侧空白不改变
     * @param topMargin    顶部边缘(毫米)可能为空。如果为空，则顶部空白不改变.
     * @param rightMargin  右边框(毫米)可能为空。如果为空，则右空白不改变.
     * @param bottomMargin 底部空白(毫米)可能是空的。如果为空，则底部空白不改变.
     */
    public PageMarginsFilter(
            final Integer leftMargin,
            final Integer topMargin,
            final Integer rightMargin,
            final Integer bottomMargin) {
        super();

        this.leftMargin = leftMargin;
        this.topMargin = topMargin;
        this.rightMargin = rightMargin;
        this.bottomMargin = bottomMargin;
    }

    @Override
    public void doFilter(
            final Context context, final XComponent document, final FilterChain chain)
            throws Exception {

        // 此筛选器只能用于文本文档
        if (Write.isText(document)) {
            setMargins(document);
        }

        // 调用链中的下一个过滤器
        chain.doFilter(context, document);
    }

    private void setMargins(final XComponent document) throws Exception {

        // 在XComponent上查询接口XTextDocument(文本接口)
        final XTextDocument docText = Write.getTextDoc(document);

        // 从单元格XText接口创建一个文本光标
        final XTextCursor xTextCursor = docText.getText().createTextCursor();

        // 获取单元格的TextCursor的属性集
        final XPropertySet xTextCursorProps = Lo.qi(XPropertySet.class, xTextCursor);

        // 在光标位置获取页面样式名
        final String pageStyleName = xTextCursorProps.getPropertyValue("PageStyleName").toString();

        // 获取文档的StyleFamiliesSupplier接口
        final XStyleFamiliesSupplier xSupplier = Lo.qi(XStyleFamiliesSupplier.class, docText);

        // 使用StyleFamiliesSupplier接口获得实际样式族的XNameAccess接口
        final XNameAccess xFamilies = Lo.qi(XNameAccess.class, xSupplier.getStyleFamilies());

        // 访问'PageStyles'
        final XNameContainer xFamily = Lo.qi(XNameContainer.class, xFamilies.getByName("PageStyles"));

        // 从PageStyles获取当前页面的样式
        final XStyle xStyle = Lo.qi(XStyle.class, xFamily.getByName(pageStyleName));

        Logger.debug(
                "Changing margins using: [left={}, top={}, right={}, bottom={}]",
                leftMargin,
                topMargin,
                rightMargin,
                bottomMargin);

        // 获取样式的属性集
        final XPropertySet xStyleProps = Lo.qi(XPropertySet.class, xStyle);

        // 改变页边 (1 = 0.01 mm)
        if (leftMargin != null) {
            xStyleProps.setPropertyValue("LeftMargin", leftMargin * 100);
        }
        if (topMargin != null) {
            xStyleProps.setPropertyValue("TopMargin", topMargin * 100);
        }
        if (rightMargin != null) {
            xStyleProps.setPropertyValue("RightMargin", rightMargin * 100);
        }
        if (bottomMargin != null) {
            xStyleProps.setPropertyValue("BottomMargin", bottomMargin * 100);
        }
    }

}
