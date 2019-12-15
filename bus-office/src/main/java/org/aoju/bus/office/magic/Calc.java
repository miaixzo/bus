package org.aoju.bus.office.magic;

import com.sun.star.lang.XComponent;
import com.sun.star.sheet.XSpreadsheetDocument;

/**
 * 使Office Calc文档(电子表格)更容易使用的实用函数集合.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public final class Calc {

    /**
     * 获取给定文档是否为电子表格文档.
     *
     * @param document 要测试的文档.
     * @return 如果文档是电子表格文档，则为{@code true}，否则为{@code false}.
     */
    public static boolean isCalc(final XComponent document) {
        return Info.isDocumentType(document, Lo.CALC_SERVICE);
    }

    /**
     * 将给定的文档转换为{@link XSpreadsheetDocument}.
     *
     * @param document 要转换的文档.
     * @return 如果文档不是电子表格文档，则为null.
     */
    public static XSpreadsheetDocument getCalcDoc(final XComponent document) {
        if (document == null) {
            return null;
        }
        return Lo.qi(XSpreadsheetDocument.class, document);
    }

}
