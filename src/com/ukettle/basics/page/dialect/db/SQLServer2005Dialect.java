package com.ukettle.basics.page.dialect.db;

import java.util.List;

import com.ukettle.basics.page.Sorting;
import com.ukettle.basics.page.dialect.Dialect;
import com.ukettle.basics.page.plugin.BaseParameter;
import com.ukettle.www.toolkit.StringUtils;


/**
 * Sql 2005的方言实现
 * 
 * @author Kimi Liu
 * @Date Aug 18, 2014
 * @Time 11:50:21
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public class SQLServer2005Dialect implements Dialect {

	@Override
	public boolean limit() {
		return true;
	}

	@Override
	public String getLimit(String sql, int offset, int limit) {
		return getLimit(sql, offset, limit, Integer.toString(limit));
	}

	/**
	 * Add a LIMIT clause to the given SQL SELECT
	 * <p/>
	 * The LIMIT SQL will look like:
	 * <p/>
	 * WITH query AS (SELECT TOP 100 percent ROW_NUMBER() OVER (ORDER BY
	 * CURRENT_TIMESTAMP) as __row_number__, * from table_name) SELECT * FROM
	 * query WHERE __row_number__ BETWEEN :offset and :lastRows ORDER BY
	 * __row_number__
	 * 
	 * @param querySqlString
	 *            The SQL statement to base the limit query off of.
	 * @param offset
	 *            Offset of the first row to be returned by the query
	 *            (zero-based)
	 * @param limit
	 *            Maximum number of rows to be returned by the query
	 * @param limitPlaceholder
	 *            limitPlaceholder
	 * @return A new SQL statement with the LIMIT clause applied.
	 */
	private String getLimit(String querySqlString, int offset, int limit,
			String limitPlaceholder) {
		StringBuilder pagingBuilder = new StringBuilder();
		String orderby = getOrderByPart(querySqlString);
		String distinctStr = "";

		String loweredString = querySqlString.toLowerCase();
		String sqlPartString = querySqlString;
		if (loweredString.trim().startsWith("select")) {
			int index = 6;
			if (loweredString.startsWith("select distinct")) {
				distinctStr = "DISTINCT ";
				index = 15;
			}
			sqlPartString = sqlPartString.substring(index);
		}
		pagingBuilder.append(sqlPartString);

		// if no ORDER BY is specified use fake ORDER BY field to avoid errors
		if (StringUtils.isEmpty(orderby)) {
			orderby = "ORDER BY CURRENT_TIMESTAMP";
		}

		StringBuilder result = new StringBuilder();
		result.append("WITH query AS (SELECT ").append(distinctStr)
				.append("TOP 100 PERCENT ").append(" ROW_NUMBER() OVER (")
				.append(orderby).append(") as __row_number__, ")
				.append(pagingBuilder)
				.append(") SELECT * FROM query WHERE __row_number__ BETWEEN ")
				.append(offset).append(" AND ").append(offset + limit)
				.append(" ORDER BY __row_number__");

		return result.toString();
	}

	static String getOrderByPart(String sql) {
		String loweredString = sql.toLowerCase();
		int orderByIndex = loweredString.indexOf("order by");
		if (orderByIndex != -1) {
			// if we find a new "order by" then we need to ignore
			// the previous one since it was probably used for a subquery
			return sql.substring(orderByIndex);
		} else {
			return "";
		}
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