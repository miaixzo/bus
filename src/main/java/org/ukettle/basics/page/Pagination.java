package org.ukettle.basics.page;

/**
 * <p>
 * 查询参数.
 * </p>
 * 
 * @author Kimi Liu
 * @Date Mar 21, 2014
 * @Time 10:45:40
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public class Pagination implements Page {
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = 8919076199499894558L;
	/**
	 * 每页默认100条数据
	 */
	protected int size = 100;
	/**
	 * 当前页
	 */
	protected int limit = 1;
	/**
	 * 总页数
	 */
	protected int total = 0;
	/**
	 * 总数据数
	 */
	protected int rows = 0;
	/**
	 * 每页的起始行数
	 */
	protected int start = 0;
	/**
	 * 每页显示数据的终止行数
	 */
	protected int end = 0;
	/**
	 * 是否有下一页
	 */
	boolean next = false;
	/**
	 * 是否有前一页
	 */
	boolean previous = false;

	public Pagination(int rows, int size) {
		this.init(rows, size);
	}

	public Pagination() {

	}

	/**
	 * 初始化分页参数:需要先设置rows
	 */
	public void init(int rows, int size) {
		this.size = size;
		this.rows = rows;
		if ((rows % size) == 0) {
			total = rows / size;
		} else {
			total = rows / size + 1;
		}
	}

	@Override
	public void init(int rows, int size, int limit) {
		this.size = size;
		this.rows = rows;
		if ((rows % size) == 0) {
			total = rows / size;
		} else {
			total = rows / size + 1;
		}
		if (limit != 0)
			toPage(limit);
	}

	/**
	 * 计算当前页的取值范围：start和end
	 */
	private void calculatePage() {
		previous = (limit - 1) > 0;
		next = limit < total;
		if (limit * size < rows) { // 判断是否为最后一页
			end = limit * size;
			start = end - size;
		} else {
			end = rows;
			start = size * (total - 1);
		}
	}

	/**
	 * 直接跳转到指定页数的页面
	 * 
	 * @param page
	 */
	public void toPage(int page) {
		limit = page;
		calculatePage();
	}

	public String debug() {
		return "总数据:" + rows + "\n页大小:" + size + "\n总页数:" + total + "\n当前页:"
				+ limit + "\n前一页:" + previous + "\n下一页:" + next + "\n开始行数:"
				+ start + "\n终止行数:" + end;
	}

	@Override
	public int getLimit() {
		return limit;
	}

	@Override
	public boolean isNext() {
		return next;
	}

	@Override
	public boolean isPrevious() {
		return previous;
	}

	@Override
	public int getEnd() {
		return end;
	}

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public int getStart() {
		return start;
	}

	@Override
	public int getTotal() {
		return total;
	}

	@Override
	public int getRows() {
		return rows;
	}

	@Override
	public void setTotal(int i) {
		total = i;
	}

	@Override
	public void setLimit(int i) {
		limit = i;
	}

	@Override
	public void setNext(boolean b) {
		next = b;
	}

	@Override
	public void setPrevious(boolean b) {
		previous = b;
	}

	@Override
	public void setEnd(int i) {
		end = i;
	}

	@Override
	public void setSize(int i) {
		size = i;
	}

	@Override
	public void setStart(int i) {
		start = i;
	}

	@Override
	public void setRows(int i) {
		rows = i;
	}

}