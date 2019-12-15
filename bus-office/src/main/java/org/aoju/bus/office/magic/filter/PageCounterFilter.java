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
 * @version 3.6.6
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
