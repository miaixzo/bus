package org.ukettle.basics.page.plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.util.Assert;
import org.ukettle.basics.page.Page;
import org.ukettle.basics.page.dialect.Dialect;

/**
 * 
 * @author Kimi Liu
 * @Date Mar 12, 2014
 * @Time 10:51:23
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
@SuppressWarnings("all")
public class BaseParameter {

	/** Order by 正则表达式 */
	public static final String ORDER_BY_REGEX = "order\\s*by[\\w|\\W|\\s|\\S]*";

	/**
	 * 对SQL参数(?)设值,参考org.apache.ibatis.executor.parameter.
	 * DefaultParameterHandler
	 * 
	 * @param ps
	 *            表示预编译的 SQL 语句的对象。
	 * @param mappedStatement
	 *            MappedStatement
	 * @param boundSql
	 *            SQL
	 * @param parameterObject
	 *            参数对象
	 * @throws java.sql.SQLException
	 *             数据库异常
	 */
	public static void setParameters(PreparedStatement ps,
			MappedStatement mappedStatement, BoundSql boundSql,
			Object parameterObject) throws SQLException {
		ErrorContext.instance().activity("setting parameters")
				.object(mappedStatement.getParameterMap().getId());
		List<ParameterMapping> parameterMappings = boundSql
				.getParameterMappings();
		if (null != parameterMappings) {
			Configuration configuration = mappedStatement.getConfiguration();
			TypeHandlerRegistry typeHandlerRegistry = configuration
					.getTypeHandlerRegistry();
			MetaObject metaObject = parameterObject == null ? null
					: configuration.newMetaObject(parameterObject);
			for (int i = 0; i < parameterMappings.size(); i++) {
				ParameterMapping parameterMapping = parameterMappings.get(i);
				if (parameterMapping.getMode() != ParameterMode.OUT) {
					Object value;
					String propertyName = parameterMapping.getProperty();
					PropertyTokenizer prop = new PropertyTokenizer(propertyName);
					if (null == parameterObject) {
						value = null;
					} else if (typeHandlerRegistry
							.hasTypeHandler(parameterObject.getClass())) {
						value = parameterObject;
					} else if (boundSql.hasAdditionalParameter(propertyName)) {
						value = boundSql.getAdditionalParameter(propertyName);
					} else if (propertyName
							.startsWith(ForEachSqlNode.ITEM_PREFIX)
							&& boundSql.hasAdditionalParameter(prop.getName())) {
						value = boundSql.getAdditionalParameter(prop.getName());
						if (null != value) {
							value = configuration.newMetaObject(value)
									.getValue(
											propertyName.substring(prop
													.getName().length()));
						}
					} else {
						value = metaObject == null ? null : metaObject
								.getValue(propertyName);
					}
					TypeHandler typeHandler = parameterMapping.getTypeHandler();
					if (null == typeHandler) {
						throw new ExecutorException(
								"There was no TypeHandler found for parameter "
										+ propertyName + " of statement "
										+ mappedStatement.getId());
					}
					typeHandler.setParameter(ps, i + 1, value,
							parameterMapping.getJdbcType());
				}
			}
		}
	}

	/**
	 * 查询总纪录数
	 * 
	 * @param sql
	 *            SQL语句
	 * @param connection
	 *            数据库连接
	 * @param mappedStatement
	 *            mapped
	 * @param parameterObject
	 *            参数
	 * @param boundSql
	 *            boundSql
	 * @param dialect
	 *            database dialect
	 * @return 总记录数
	 * @throws java.sql.SQLException
	 *             sql查询错误
	 */
	public static int getCount(final String sql, final Connection connection,
			final MappedStatement mappedStatement,
			final Object parameterObject, final BoundSql boundSql,
			Dialect dialect) throws SQLException {
		final String count_sql = dialect.getCount(sql);
		PreparedStatement countStmt = null;
		ResultSet rs = null;
		try {
			countStmt = connection.prepareStatement(count_sql);
			final BoundSql countBS = new BoundSql(
					mappedStatement.getConfiguration(), count_sql,
					boundSql.getParameterMappings(), parameterObject);
			setParameters(countStmt, mappedStatement, countBS, parameterObject);
			rs = countStmt.executeQuery();
			int count = 0;
			if (rs.next()) {
				count = rs.getInt(1);
			}
			return count;
		} finally {
			if (null != rs) {
				rs.close();
			}
			if (null != countStmt) {
				countStmt.close();
			}
			if (null != connection) {
				connection.close();
			}
		}
	}

	/**
	 * 根据数据库方言，生成特定的分页sql
	 * 
	 * @param sql
	 *            Mapper中的Sql语句
	 * @param page
	 *            分页对象
	 * @param dialect
	 *            方言类型
	 * @return 分页SQL
	 */
	public static String generatePageSql(String sql, Page page, Dialect dialect) {
		if (dialect.limit()) {
			int pageSize = page.getSize();
			int index = (page.getLimit() - 1) * pageSize;
			int start = index < 0 ? 0 : index;
			return dialect.getLimit(sql, start, pageSize);
		} else {
			return sql;
		}
	}

	/**
	 * 去除orderby 子句
	 * 
	 * @param sql
	 *            sql
	 * @return 去掉order by sql
	 */
	public static String removeOrders(String sql) {
		Assert.hasText(sql);
		Pattern p = Pattern.compile(ORDER_BY_REGEX, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(sql);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, "");
		}
		m.appendTail(sb);
		return sb.toString();
	}

}