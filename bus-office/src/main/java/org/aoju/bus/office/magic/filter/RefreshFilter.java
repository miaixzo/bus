package org.aoju.bus.office.magic.filter;

import com.sun.star.lang.XComponent;
import com.sun.star.util.XRefreshable;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.office.Context;
import org.aoju.bus.office.magic.Lo;

/**
 * 此筛选器用于刷新文档.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class RefreshFilter implements Filter {

    /**
     * 刷新过滤器的单例实例，它不会调用链中的下一个过滤器.
     * 仅当您绝对确定它将作为过滤器链中的最后一个过滤器时，才使用此过滤器.
     */
    public static final RefreshFilter INSTANCE = new RefreshFilter(true);
    /**
     * {@link FilterChain}的单例实例，它总是包含一个{@link RefreshFilter}，
     * 它不会调用链中的下一个过滤器。如果一个文档只是从一种格式转换成另一种格式，那么应该使用这个链.
     */
    public static final FilterChain CHAIN = new UnmodifiableFilter(INSTANCE);
    private final boolean lastFilter;

    /**
     * 创建一个新的刷新筛选器.
     */
    public RefreshFilter() {
        this(false);
    }

    /**
     * 创建一个新的刷新筛选器，它将根据指定的参数调用或不调用链中的下一个筛选器.
     *
     * @param lastFilter 如果{@code true}，那么过滤器将不会调用链中的下一个过滤器.
     *                   如果{@code false}，则应用链中的下一个过滤器(如果有).
     */
    public RefreshFilter(final boolean lastFilter) {
        super();

        this.lastFilter = lastFilter;
    }

    @Override
    public void doFilter(
            final Context context,
            final XComponent document,
            final FilterChain chain) {
        Logger.debug("Applying the RefreshFilter");
        Lo.qiOptional(XRefreshable.class, document).ifPresent(XRefreshable::refresh);

        if (!lastFilter) {
            chain.doFilter(context, document);
        }
    }

}
