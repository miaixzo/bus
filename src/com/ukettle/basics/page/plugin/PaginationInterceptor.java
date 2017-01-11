package com.ukettle.basics.page.plugin;

import java.sql.Connection;
import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import com.ukettle.basics.page.Page;
import com.ukettle.basics.page.PageContext;

/**
 * <p>
 * 数据库分页插件，只拦截查询语句.
 * </p>
 * 
 * @author Kimi Liu
 * @Date Mar 26, 2014
 * @Time 15:36:12
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
@Intercepts({ @Signature(type = Executor.class, method = "query", args = {
		MappedStatement.class, Object.class, RowBounds.class,
		ResultHandler.class }) })
public class PaginationInterceptor extends BaseInterceptor {

	private static final long serialVersionUID = 3576678797374122941L;

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		final MappedStatement mappedStatement = (MappedStatement) invocation
				.getArgs()[0];
		// 拦截需要分页的SQL
		if (mappedStatement.getId().matches(SQL_PATTERN)) {
			Object parameter = invocation.getArgs()[1];
			BoundSql boundSql = mappedStatement.getBoundSql(parameter);
			String originalSql = boundSql.getSql().trim();
			if (null == boundSql.getSql() || "".equals(boundSql.getSql()))
				return null;
			// 分页参数--上下文传参
			Page page = null;
			PageContext context = PageContext.getPageContext();
			// map传参每次都将currentPage重置,先判读map再判断context
			if (null != parameter && null == context) {
				page = convertParameter(parameter, page);
			}
			// 分页参数--context参数里的Page传参
			if (null == page) {
				page = context;
			}

			if (null != page) {
				int totalPage = page.getTotal();
				// 得到总记录数
				if (totalPage == 0) {
					Connection connection = mappedStatement.getConfiguration()
							.getEnvironment().getDataSource().getConnection();
					totalPage = BaseParameter.getCount(originalSql, connection,
							mappedStatement, parameter, boundSql, DIALECT);
				}
				// 分页计算
				page.init(totalPage, page.getSize(), page.getLimit());
				invocation.getArgs()[2] = new RowBounds(
						RowBounds.NO_ROW_OFFSET, RowBounds.NO_ROW_LIMIT);
				BoundSql newBoundSql = new BoundSql(
						mappedStatement.getConfiguration(),
						BaseParameter.generatePageSql(originalSql, page,
								DIALECT), boundSql.getParameterMappings(),
						boundSql.getParameterObject());
				MappedStatement newMs = copyFromMappedStatement(
						mappedStatement, new BoundSqlSqlSource(newBoundSql));
				invocation.getArgs()[0] = newMs;
			}
		}
		PageContext.clear();
		return invocation.proceed();
	}

	private MappedStatement copyFromMappedStatement(MappedStatement ms,
			SqlSource newSqlSource) {
		MappedStatement.Builder builder = new MappedStatement.Builder(
				ms.getConfiguration(), ms.getId(), newSqlSource,
				ms.getSqlCommandType());
		builder.resource(ms.getResource());
		builder.fetchSize(ms.getFetchSize());
		builder.statementType(ms.getStatementType());
		builder.keyGenerator(ms.getKeyGenerator());
		if (null != ms.getKeyProperties()) {
			for (String keyProperty : ms.getKeyProperties()) {
				builder.keyProperty(keyProperty);
			}
		}
		builder.timeout(ms.getTimeout());
		builder.parameterMap(ms.getParameterMap());
		builder.resultMaps(ms.getResultMaps());
		builder.cache(ms.getCache());
		return builder.build();
	}

	public static class BoundSqlSqlSource implements SqlSource {
		BoundSql boundSql;

		public BoundSqlSqlSource(BoundSql boundSql) {
			this.boundSql = boundSql;
		}

		public BoundSql getBoundSql(Object parameterObject) {
			return boundSql;
		}
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
		super.initProperties(properties);
	}

}