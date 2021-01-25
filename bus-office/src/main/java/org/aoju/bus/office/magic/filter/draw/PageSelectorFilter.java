/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.office.magic.filter.draw;

import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XDrawPages;
import com.sun.star.drawing.XDrawPagesSupplier;
import com.sun.star.lang.XComponent;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.office.Context;
import org.aoju.bus.office.magic.Draw;
import org.aoju.bus.office.magic.Lo;
import org.aoju.bus.office.magic.filter.Filter;
import org.aoju.bus.office.magic.filter.FilterChain;

/**
 * 此筛选器用于从文档中选择特定页面，以便仅转换所选页面
 *
 * @author Kimi Liu
 * @version 6.1.8
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

        Logger.debug("Applying the PageSelectorFilter");

        // 此筛选器只能用于绘制文档
        if (Draw.isDraw(document)) {
            selectPage(document);
        }

        // 用链中的下一个过滤器
        chain.doFilter(context, document);
    }

    private void selectPage(final XComponent document) throws Exception {

        final XDrawPages drawPages = Lo.qi(XDrawPagesSupplier.class, document).getDrawPages();
        final int pageCount = drawPages.getCount();

        // 删除除要选择的页面之外的所有页面.
        int seekIdx = Math.min(pageCount, Math.max(0, page - 1));
        for (int i = 0; i < pageCount; i++) {
            XDrawPage drawPage = null;
            if (i < seekIdx) {
                drawPage = Lo.qi(XDrawPage.class, drawPages.getByIndex(0));
            } else if (i > seekIdx) {
                drawPage = Lo.qi(XDrawPage.class, drawPages.getByIndex(1));
            }
            if (drawPage != null) {
                drawPages.remove(drawPage);
            }
        }
    }

}
