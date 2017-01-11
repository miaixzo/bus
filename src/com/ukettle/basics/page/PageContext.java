package com.ukettle.basics.page;

/**
 * <p>
 * 分页参数上下文.
 * </p>
 * 
 * @author Kimi Liu
 * @Date Aug 21, 2014
 * @Time 10:13:40
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public class PageContext extends Pagination {

	private static final long serialVersionUID = -3294902812084550562L;

	/**
	 * 分页参数上下文，
	 */
	private static final ThreadLocal<PageContext> PAGE_CONTEXT_THREAD_LOCAL = new ThreadLocal<PageContext>();

	/**
	 * 取得当前的分页参数上下文
	 * 
	 * @return 分页参数上下文
	 */
	public static PageContext getPageContext() {
		PageContext ci = PAGE_CONTEXT_THREAD_LOCAL.get();
		if (ci == null) {
			ci = new PageContext();
			PAGE_CONTEXT_THREAD_LOCAL.set(ci);
		}
		return ci;
	}

	/**
	 * 清理分页参数上下文
	 */
	public static void clear() {
		PAGE_CONTEXT_THREAD_LOCAL.remove();
	}

}