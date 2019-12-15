package org.aoju.bus.office.magic.filter.text;

import com.sun.star.beans.PropertyValue;
import com.sun.star.document.XDocumentInsertable;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import org.aoju.bus.office.Context;
import org.aoju.bus.office.magic.Lo;
import org.aoju.bus.office.magic.Write;
import org.aoju.bus.office.magic.filter.Filter;
import org.aoju.bus.office.magic.filter.FilterChain;

import java.io.File;

import static org.aoju.bus.office.Builder.toUrl;

/**
 * 此筛选器用于在转换的文档末尾插入文档.
 *
 * @author Kimi Liu
 * @version 3.6.6
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
        insertable.insertDocumentFromURL(toUrl(documentToInsert), new PropertyValue[0]);
    }

}
