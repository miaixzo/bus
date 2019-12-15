package org.aoju.bus.office.magic.filter.text;

import com.sun.star.lang.XComponent;
import com.sun.star.util.XReplaceDescriptor;
import com.sun.star.util.XReplaceable;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.ArrayUtils;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.office.Context;
import org.aoju.bus.office.magic.Lo;
import org.aoju.bus.office.magic.Write;
import org.aoju.bus.office.magic.filter.Filter;
import org.aoju.bus.office.magic.filter.FilterChain;

/**
 * 此筛选器用于替换文档中的文本.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class TextReplacerFilter implements Filter {

    private final String[] searchList;
    private final String[] replacementList;

    /**
     * 使用要替换的指定字符串创建新筛选器.
     *
     * @param searchList      要搜索的字符串，如果为空则为no-op.
     * @param replacementList 要替换它们的字符串，如果为空则为no-op.
     */
    public TextReplacerFilter(final String[] searchList, final String[] replacementList) {
        super();
        this.searchList = ArrayUtils.clone(searchList);
        this.replacementList = ArrayUtils.clone(replacementList);
    }

    @Override
    public void doFilter(
            final Context context, final XComponent document, final FilterChain chain)
            throws InstrumentException {

        Logger.debug("Applying the TextReplacerFilter");

        // 此筛选器只能用于文本文档
        if (Write.isText(document)) {
            replaceText(document);
        }

        // 调用链中的下一个过滤器
        chain.doFilter(context, document);
    }

    private void replaceText(final XComponent document) {

        final XReplaceable replaceable = Lo.qi(XReplaceable.class, document);

        // 我们需要一个描述符来设置Replace的属性
        final XReplaceDescriptor replaceDescr = replaceable.createReplaceDescriptor();

        Logger.debug("Changing all occurrences of ...");
        for (int i = 0; i < searchList.length; i++) {
            Logger.debug("{} -> {}", searchList[i], replacementList[i]);
            // 设置替换方法所需的属性
            replaceDescr.setSearchString(searchList[i]);
            replaceDescr.setReplaceString(replacementList[i]);
            replaceable.replaceAll(replaceDescr);
        }
    }

}
