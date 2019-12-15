package org.aoju.bus.office.magic;

import com.sun.star.lang.XComponent;

/**
 * 使office draw文档(Drawing)更容易使用的实用函数集合
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public final class Draw {

    /**
     * 获取给定文档是否为draw文档.
     *
     * @param document 要测试的文档.
     * @return 如果文档是draw文档，则{@code true}，否则{@code false}.
     */
    public static boolean isDraw(final XComponent document) {
        return Info.isDocumentType(document, Lo.DRAW_SERVICE);
    }

    /**
     * 获取给定文档是否为演示文档.
     *
     * @param document 要测试的文档.
     * @return 如果文档是演示文档，则{@code true}，否则{@code false}.
     */
    public static boolean isImpress(final XComponent document) {
        return Info.isDocumentType(document, Lo.IMPRESS_SERVICE);
    }

}
