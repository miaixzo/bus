package org.aoju.bus.office.magic.filter;

import com.sun.star.lang.XComponent;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.office.Context;

/**
 * FilterChain是负责管理过滤器调用链的对象.
 * 过滤器使用FilterChain来调用链中的下一个过滤器，
 * 或者如果调用过滤器是链中的最后一个过滤器，则结束调用链。
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public interface FilterChain {

    /**
     * 向链中添加一个过滤器.
     *
     * @param filter 过滤器添加在链的末端.
     */
    void addFilter(Filter filter);

    /**
     * 导致调用链中的下一个过滤器，或者如果调用的过滤器是链中的最后一个过滤器，则不执行任何操作.
     *
     * @param context  用于沿链传递的OfficeContext.
     * @param document 被转换为沿链传递的XComponent.
     * @throws InstrumentException 如果处理过滤器时发生错误.
     */
    void doFilter(final Context context, final XComponent document) throws InstrumentException;

    /**
     * 创建并返回此对象的副本。"copy"的确切含义可能取决于链的类别
     *
     * @return 这个链的拷贝.
     */
    FilterChain copy();

}
