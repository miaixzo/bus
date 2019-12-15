package org.aoju.bus.office.magic;

import com.sun.star.lang.XComponent;
import com.sun.star.text.XTextDocument;

/**
 * 实用程序函数，使office文本文档(Writer)更容易使用.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public final class Write {

    /**
     * 获取给定文档是否为文本文档.
     *
     * @param document 要测试的文档.
     * @return 如果文档是文本文档，则为{@code true}，否则为{@code false}.
     */
    public static boolean isText(final XComponent document) {
        return Info.isDocumentType(document, Lo.WRITER_SERVICE);
    }

    /**
     * 将给定的文档转换为{@link XTextDocument}.
     *
     * @param document 要转换的文档.
     * @return 如果文档不是文本文档，则为null.
     */
    public static XTextDocument getTextDoc(final XComponent document) {
        if (document == null) {
            return null;
        }

        return Lo.qi(XTextDocument.class, document);
    }

}
