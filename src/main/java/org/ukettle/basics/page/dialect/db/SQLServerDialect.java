package org.ukettle.basics.page.dialect.db;

import java.util.List;

import org.ukettle.basics.page.Sorting;
import org.ukettle.basics.page.dialect.Dialect;
import org.ukettle.basics.page.plugin.BaseParameter;


/**
 * MSSQLServer 数据库实现分页方言
 * 
 * @author Kimi Liu
 * @Date Aug 17, 2014
 * @Time 11:09:56
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public class SQLServerDialect implements Dialect {

	public boolean limit() {
		return true;
	}

	static int getAfterSelectInsertPoint(String sql) {
		int selectIndex = sql.toLowerCase().indexOf("select");
		final int selectDistinctIndex = sql.toLowerCase().indexOf(
				"select distinct");
		return selectIndex + (selectDistinctIndex == selectIndex ? 15 : 6);
	}

	public String getLimitString(String sql, int offset, int limit) {
		return getLimit(sql, offset, limit);
	}

	/**
	 * 将sql变成分页sql语句,提供将offset及limit使用占位符号(placeholder)替换.
	 * 
	 * <pre>
	 * 如mysql
	 * dialect.getLimit("select * from user", 12, ":offset",0,":limit") 将返回
	 * select * from user limit :offset,:limit
	 * </pre>
	 * 
	 * @param sql
	 *            实际SQL语句
	 * @param offset
	 *            分页开始纪录条数
	 * @param limit
	 *            分页每页显示纪录条数
	 * @return 包含占位符的分页sql
	 */
	public String getLimit(String sql, int offset, int limit) {
		if (offset > 0) {
			throw new UnsupportedOperationException("sql server has no offset");
		}
		return new StringBuffer(sql.length() + 8).append(sql)
				.insert(getAfterSelectInsertPoint(sql), " top " + limit)
				.toString();
	}

	@Override
	public String getCount(String sql) {
		return "select count(1) from (" + BaseParameter.removeOrders(sql)
				+ ") as dialect";
	}

	@Override
	public String getSort(String sql, List<Sorting> sort) {
		if (sort == null || sort.isEmpty()) {
			return sql;
		}
		StringBuffer buffer = new StringBuffer("select * from (").append(sql)
				.append(") dialect order by ");
		for (Sorting s : sort) {
			buffer.append(s.getColumn()).append(" ").append(s.getSort())
					.append(", ");
		}
		buffer.delete(buffer.length() - 2, buffer.length());
		return buffer.toString();
	}

}