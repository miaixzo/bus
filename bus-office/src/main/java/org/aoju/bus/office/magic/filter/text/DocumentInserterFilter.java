/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
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

import com.sun.star.beans.PropertyValue;
import com.sun.star.document.XDocumentInsertable;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import org.aoju.bus.office.Builder;
import org.aoju.bus.office.Context;
import org.aoju.bus.office.magic.Lo;
import org.aoju.bus.office.magic.Write;
import org.aoju.bus.office.magic.filter.Filter;
import org.aoju.bus.office.magic.filter.FilterChain;

import java.io.File;

/**
 * 此筛选器用于在转换的文档末尾插入文档.
 *
 * @author Kimi Liu
 * @version 5.9.2
 * @since JDK 1.8+
 */
public class DocumentInserterFilter implements Filter {

    private final File documentToInsert;

    /**
     * 创建将插入指定文档的新筛选器.
     *
     * @param document 要插入到当前文档末尾的文档.
     */
    public DocumentInserterFilter(final File document) {
        super();
        this.documentToInsert = document;
    }

    @Override
    public void doFilter(
            final Context context, final XComponent document, final FilterChain chain)
            throws Exception {
        // 此筛选器只能用于文本文档
        if (Write.isText(document)) {
            insertDocument(document);
        }

        // 调用链中的下一个过滤器
        chain.doFilter(context, document);
    }

    private void insertDocument(final XComponent document) throws Exception {
        // 在XComponent上查询接口XTextDocument(文本接口).
        final XTextDocument docText = Write.getTextDoc(document);

        // 需要文本光标以便转到文档的末尾.
        final XTextCursor textCursor = docText.getText().createTextCursor();

        // 翻到文件的末尾
        textCursor.gotoEnd(false);

        // 在当前文档的末尾插入要合并的文档.
        final XDocumentInsertable insertable = Lo.qi(XDocumentInsertable.class, textCursor);
        insertable.insertDocumentFromURL(Builder.toUrl(documentToInsert), new PropertyValue[0]);
    }

}
