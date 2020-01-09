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

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexAccess;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XDocumentIndex;
import com.sun.star.text.XDocumentIndexesSupplier;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.office.Context;
import org.aoju.bus.office.magic.Lo;
import org.aoju.bus.office.magic.Write;
import org.aoju.bus.office.magic.filter.Filter;
import org.aoju.bus.office.magic.filter.FilterChain;

/**
 * 此筛选器更新文档中的所有索引.
 *
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8+
 */
public class TableOfContentUpdaterFilter implements Filter {

    private final int level;

    /**
     * 创建一个将更新内容表的新筛选器.
     */
    public TableOfContentUpdaterFilter() {
        this(0);
    }

    /**
     * 创建一个新过滤器，该过滤器将更改内容表的级别数并更新它.
     *
     * @param level 内容表中所需的级别数.
     */
    public TableOfContentUpdaterFilter(final int level) {
        super();

        this.level = level;
    }

    @Override
    public void doFilter(
            final Context context, final XComponent document, final FilterChain chain)
            throws Exception {

        Logger.debug("Applying the TableOfContentUpdaterFilter");

        // 此筛选器只能用于文本文档
        if (Write.isText(document)) {
            updateToc(document);
        }

        // 调用链中的下一个过滤器
        chain.doFilter(context, document);
    }

    private void updateToc(final XComponent document) throws Exception {

        // 获取文档的DocumentIndexesSupplier接口
        final XDocumentIndexesSupplier documentIndexesSupplier =
                Lo.qi(XDocumentIndexesSupplier.class, document);

        // 获取DocumentIndexes的XIndexAccess
        final XIndexAccess documentIndexes =
                Lo.qi(XIndexAccess.class, documentIndexesSupplier.getDocumentIndexes());

        for (int i = 0; i < documentIndexes.getCount(); i++) {
            // 更新每个索引
            final XDocumentIndex docIndex = Lo.qi(XDocumentIndex.class, documentIndexes.getByIndex(i));
            // 如果需要，更新级别
            if (level > 0) {
                // 获取ContentIndex的服务接口
                final String indexType = docIndex.getServiceName();
                if (indexType.contains("com.sun.star.text.ContentIndex")) {
                    final XPropertySet index = Lo.qi(XPropertySet.class, docIndex);
                    index.setPropertyValue("Level", (short) level);
                }
            }
            docIndex.update();
        }
    }

}
