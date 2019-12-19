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
package org.aoju.bus.office.magic.filter;

import com.sun.star.drawing.XDrawPages;
import com.sun.star.drawing.XDrawPagesSupplier;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
import org.aoju.bus.office.Context;
import org.aoju.bus.office.magic.*;

/**
 * 此筛选器用于计算文档的页数.
 *
 * @author Kimi Liu
 * @version 5.3.3
 * @since JDK 1.8+
 */
public class PageCounterFilter implements Filter {

    private int pageCount;

    @Override
    public void doFilter(
            final Context context, final XComponent document, final FilterChain chain) {

        if (Write.isText(document)) {

            // 保存文档的PageCount属性t.
            pageCount = (Integer) Props.getProperty(Lo.qi(XModel.class, document).getCurrentController(), "PageCount")
                    .orElse(0);
        } else if (Calc.isCalc(document)) {
            throw new UnsupportedOperationException("SpreadsheetDocument not supported yet");
        } else if (Draw.isImpress(document)) {
            throw new UnsupportedOperationException("PresentationDocument not supported yet");
        } else if (Draw.isDraw(document)) {
            final XDrawPages xDrawPages = Lo.qi(XDrawPagesSupplier.class, document).getDrawPages();
            pageCount = xDrawPages.getCount();
        }
        // 调用链中的下一个过滤器
        chain.doFilter(context, document);
    }

    /**
     * 获取调用筛选器时文档中的页数.
     *
     * @return 页数.
     */
    public int getPageCount() {

        return this.pageCount;
    }

}
