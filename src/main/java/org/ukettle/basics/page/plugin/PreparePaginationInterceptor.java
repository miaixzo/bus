package org.ukettle.basics.page.plugin;

import java.sql.Connection;
import java.util.Properties;

import org.apache.ibatis.executor.statement.BaseStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.ukettle.basics.page.Page;
import org.ukettle.www.toolkit.Reflection;

/**
 * <p>
 * Mybatis数据库分页插件. 拦截StatementHandler的prepare方法
 * </p>
 * 
 * @author Kimi Liu
 * @Date Aug 26, 2014
 * @Time 14:12:09
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
@Intercepts({ @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class }) })
public class PreparePaginationInterceptor extends BaseInterceptor {

	private static final long serialVersionUID = -6075937069117597841L;

	public PreparePaginationInterceptor() {
		super();
	}

	@Override
	public Object intercept(Invocation ivk) throws Throwable {
		if (ivk.getTarget().getClass()
				.isAssignableFrom(RoutingStatementHandler.class)) {
			final RoutingStatementHandler statementHandler = (RoutingStatementHandler) ivk
					.getTarget();
			final BaseStatementHandler delegate = (BaseStatementHandler) Reflection
					.getFieldValue(statementHandler, DELEGATE);
			final MappedStatement mappedStatement = (MappedStatement) Reflection
					.getFieldValue(delegate, MAPPED_STATEMENT);

			if (mappedStatement.getId().matches(SQL_PATTERN)) {
				// 拦截需要分页的SQL
				BoundSql boundSql = delegate.getBoundSql();
				// 分页SQL<select>中parameterType属性对应的实体参数，即Mapper接口中执行分页方法的参数,该参数不得为空
				Object parameterObject = boundSql.getParameterObject();
				if (null == parameterObject) {
					throw new NullPointerException("parameterObject尚未实例化！");
				} else {
					final Connection connection = (Connection) ivk.getArgs()[0];
					final String sql = boundSql.getSql();
					// 记录统计
					final int count = BaseParameter
							.getCount(sql, connection, mappedStatement,
									parameterObject, boundSql, DIALECT);
					Page page = null;
					page = convertParameter(parameterObject, page);
					page.init(count, page.getSize(), page.getLimit());
					String pagingSql = BaseParameter.generatePageSql(sql, page,
							DIALECT);
					// 将分页sql语句反射回BoundSql.
					Reflection.setFieldValue(boundSql, "sql", pagingSql);
				}
			}
		}
		return ivk.proceed();
	}

	@Override
	public Object plugin(Object o) {
		return Plugin.wrap(o, this);
	}

	@Override
	public void setProperties(Properties properties) {
		initProperties(properties);
	}

}