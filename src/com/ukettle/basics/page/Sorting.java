package com.ukettle.basics.page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * <p>
 * 排序信息.
 * </p>
 * 
 * @author Kimi Liu
 * @Date Apr 23, 2014
 * @Time 20:11:20
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public class Sorting implements Serializable {

	private static final long serialVersionUID = 7341407235162993170L;
	private static String INJECTION_REGEX = "[A-Za-z0-9\\_\\-\\+\\.]+";
	private String column;
	private String sort;

	public Sorting() {
	}

	public Sorting(String column, String sort) {
		super();
		this.column = column;
		this.sort = sort;
	}

	public static List<Sorting> parseSortColumns(String sortColumns) {
		return parseSortColumns(sortColumns, null);
	}

	public static List<Sorting> parseSortColumns(String sortColumns,
			String sortExpression) {
		if (sortColumns == null) {
			return new ArrayList<Sorting>(0);
		}

		List<Sorting> results = new ArrayList<Sorting>();
		String[] sortSegments = sortColumns.trim().split(",");
		for (int i = 0; i < sortSegments.length; i++) {
			String sortSegment = sortSegments[i];
			Sorting sort = parseSortColumn(sortSegment, sortExpression);
			if (sort != null) {
				results.add(sort);
			}
		}
		return results;
	}

	public static Sorting parseSortColumn(String sortSegment) {
		return parseSortColumn(sortSegment, null);
	}

	/**
	 * 
	 * @param segment
	 *            str "id.asc" or "code.desc"
	 * @param expression
	 *            placeholder is "?", in oracle like:
	 *            "nlssort( ? ,'NLS_SORT=SCHINESE_PINYIN_M')". Warning: you must
	 *            prevent SQL injection.
	 * @return
	 */
	public static Sorting parseSortColumn(String sort, String sortExpression) {
		if (sort == null || sort.trim().equals("") || sort.startsWith("null.")
				|| sort.startsWith(".") || isSQLInjection(sort)) {
			return null;
		}
		String[] array = sort.trim().split("\\.");
		Sorting s = new Sorting();
		if (sortExpression != null && sortExpression.indexOf("?") != -1) {
			sortExpression = sortExpression.replaceAll("\\?", "%s");
			array[0] = String.format(sortExpression, array[0]);
		}
		s.setColumn(array[0]);
		s.setSort(array.length == 2 ? array[1] : "asc");
		return s;
	}

	public String toString() {
		return column + (sort == null ? "" : " " + sort);
	}

	public static boolean isSQLInjection(String str) {
		return !Pattern.matches(INJECTION_REGEX, str);
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

}