package org.ukettle.basics.page.proxy;

import org.apache.ibatis.session.RowBounds;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 分页对象.
 * </p>
 * 
 * @author Kimi Liu
 * @Date Aug 22, 2014
 * @Time 10:21:21
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public class Pagination<E> {

	private long total;
	private int page;
	private int offset;
	private int limit;
	private int pageSize;

	private List<E> datas = Collections.emptyList();

	public Pagination() {
		this(1, 15);
	}

	public Pagination(int page) {
		this(page, 15);
	}

	public Pagination(int page, int limit) {
		setPage(page);
		setLimit(limit);
	}

	public Pagination(RowBounds rowBounds) {
		this.limit = rowBounds.getLimit();
		this.offset = rowBounds.getOffset();
		this.page = offset / limit + 1;
	}

	public void setPage(int page) {
		if (page < 0) {
			page = 1;
		}
		this.page = page;
		onInit();
	}

	public void setLimit(int limit) {
		if (limit < 1) {
			limit = 15;
		}
		this.limit = limit;
		onInit();
	}

	protected void onInit() {
		offset = (page - 1) * limit;
	}

	protected void onSetRowsize() {
		pageSize = (int) (total / limit);
		if (total % limit > 0) {
			pageSize++;
		}
		if (page > pageSize) {
			page = pageSize;
			onInit();
		}
	}

	protected void onSetList() {
		if (datas == null || datas.isEmpty()) {
			total = 0;
			page = 1;
			offset = 0;
		}
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long rowSize) {
		this.total = rowSize;
		onSetRowsize();
	}

	public int getPageSize() {
		return pageSize;
	}

	public List<E> getDatas() {
		return datas;
	}

	public void setDatas(List<E> datas) {
		this.datas = datas;
		onSetList();
	}

	public int getOffset() {
		return offset;
	}

	public int getLimit() {
		return limit;
	}

	public int getPage() {
		return page;
	}

}