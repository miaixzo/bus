/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.office.magic.filter.text;

import com.sun.star.datatransfer.XTransferable;
import com.sun.star.datatransfer.XTransferableSupplier;
import com.sun.star.frame.XController;
import com.sun.star.lang.XComponent;
import com.sun.star.text.*;
import com.sun.star.view.XSelectionSupplier;
import org.aoju.bus.office.Context;
import org.aoju.bus.office.magic.Lo;
import org.aoju.bus.office.magic.Write;
import org.aoju.bus.office.magic.filter.Filter;
import org.aoju.bus.office.magic.filter.FilterChain;

/**
 * 此筛选器用于从文档中选择特定页面，以便仅转换所选页面.
 *
 * @author Kimi Liu
 * @version 5.6.1
 * @since JDK 1.8+
 */
public class PageSelectorFilter implements Filter {

    private final int page;

    /**
     * 创建一个新的过滤器，在转换文档时选择指定的页面(只转换给定的页面).
     *
     * @param page 要转换的页码.
     */
    public PageSelectorFilter(final int page) {
        super();
        this.page = page;
    }

    @Override
    public void doFilter(
            final Context context, final XComponent document, final FilterChain chain)
            throws Exception {

        // 此筛选器只能用于文本文档
        if (Write.isText(document)) {
            selectPage(document);
        }

        // 调用链中的下一个过滤器
        chain.doFilter(context, document);
    }

    private void selectPage(final XComponent document) throws Exception {
        // 在XComponent上查询接口XTextDocument(文本接口)
        final XTextDocument docText = Write.getTextDoc(document);

        // 为了选择所需页面的整个内容，需要文本游标和视图游标.
        final XController controller = docText.getCurrentController();
        final XTextCursor textCursor = docText.getText().createTextCursor();
        final XTextViewCursor viewCursor =
                Lo.qi(XTextViewCursorSupplier.class, controller).getViewCursor();

        // 将两个游标重置为文档的开头
        textCursor.gotoStart(false);
        viewCursor.gotoStart(false);

        // 查询视图游标上的XPageCursor接口
        final XPageCursor pageCursor = Lo.qi(XPageCursor.class, viewCursor);

        // 跳转到要选择的页面(第一页为1)，并将文本光标移动到该页的开头.
        pageCursor.jumpToPage((short) page);
        textCursor.gotoRange(viewCursor.getStart(), false);

        // 跳到页面的末尾，将文本光标展开到页面的末尾.
        pageCursor.jumpToEndOfPage();
        textCursor.gotoRange(viewCursor.getStart(), true);

        // 选择整个页面.
        final XSelectionSupplier selectionSupplier = Lo.qi(XSelectionSupplier.class, controller);
        selectionSupplier.select(textCursor);

        // 复制选区(整页).
        final XTransferableSupplier transferableSupplier =
                Lo.qi(XTransferableSupplier.class, controller);
        final XTransferable xTransferable = transferableSupplier.getTransferable();

        // 现在选择整个文档. 从头开始
        textCursor.gotoStart(false);
        // 转到最后，扩展光标的文本范围
        textCursor.gotoEnd(true);
        selectionSupplier.select(textCursor);

        // 粘贴之前复制的页面。这将替换当前选择(整个文档).
        transferableSupplier.insertTransferable(xTransferable);
    }

}
