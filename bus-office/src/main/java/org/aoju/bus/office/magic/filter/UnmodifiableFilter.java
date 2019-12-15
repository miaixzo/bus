package org.aoju.bus.office.magic.filter;

/**
 * FilterChain的不可修改的实现.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class UnmodifiableFilter extends AbstractFilter {

    /**
     * 创建将包含指定筛选器的不可修改筛选器链.
     *
     * @param filters 要添加到链中的过滤器.
     */
    public UnmodifiableFilter(final Filter... filters) {
        super(true, filters);
    }

    @Override
    public FilterChain copy() {
        return new UnmodifiableFilter(filters.toArray(new Filter[0]));
    }

}
