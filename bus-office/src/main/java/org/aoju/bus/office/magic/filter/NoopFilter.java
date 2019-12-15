package org.aoju.bus.office.magic.filter;

import com.sun.star.lang.XComponent;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.office.Context;

/**
 * 这个过滤器除了调用链中的下一个过滤器外什么也不做.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class NoopFilter implements Filter {

    /**
     * 刷新过滤器的单例实例，它不会调用链中的下一个过滤器.
     * 仅当您绝对确定它将作为过滤器链中的最后一个过滤器时，才使用此过滤器.
     */
    public static final NoopFilter NOOP = new NoopFilter();

    /**
     * {@link FilterChain}的单例实例，总是包含一个{@link NoopFilter}，
     * 这个{@link NoopFilter}不会调用链中的下一个过滤器.
     * 如果一个文档只是从一种格式转换成另一种格式，那么应该使用这个链.
     */
    public static final FilterChain CHAIN = new UnmodifiableFilter(NOOP);

    @Override
    public void doFilter(
            final Context context, final XComponent document, final FilterChain chain) {
        Logger.debug("Applying the NoopFilter");
        chain.doFilter(context, document);
    }

}
