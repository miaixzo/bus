package org.ukettle.basics.page;

import java.io.Serializable;

/**
 * <p>
 * 分页接口.
 * </p>
 * 
 * @author Kimi Liu
 * @Date Aug 20, 2014
 * @Time 10:13:40
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public interface Page extends Serializable {

	/**
	 * 功能描述：获得当前页
	 */
	int getLimit();

	/**
	 * 功能描述：是否有下页
	 */
	boolean isNext();

	/**
	 * 功能描述：是否有上页
	 */
	boolean isPrevious();

	/**
	 * 功能描述：结束页
	 */
	int getEnd();

	/**
	 * 功能描述：分页大小
	 */
	int getSize();

	/**
	 * 功能描述：起始页
	 */
	int getStart();

	/**
	 * 功能描述：总也数
	 */
	int getTotal();

	/**
	 * 功能描述：总行数
	 */
	int getRows();

	/**
	 * 功能描述：设置总行数
	 */
	void setTotal(int i);

	/**
	 * 功能描述：设置当前页
	 */
	void setLimit(int i);

	/**
	 * 功能描述：设置下一页
	 */
	void setNext(boolean b);

	/**
	 * 功能描述：设置上一页
	 */
	void setPrevious(boolean b);

	/**
	 * 功能描述：设置结束行
	 */
	void setEnd(int i);

	/**
	 * 功能描述：设置分页大小
	 */
	void setSize(int i);

	/**
	 * 功能描述：设置起始行
	 */
	void setStart(int i);

	/**
	 * 功能描述：设置总行
	 */
	void setRows(int i);

	/**
	 * 功能描述：初始化分页
	 */
	void init(int rows, int size, int limit);
}
