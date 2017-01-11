package com.ukettle.basics.page.plugin;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Properties;

import javax.xml.bind.PropertyException;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.plugin.Interceptor;

import com.ukettle.basics.page.Page;
import com.ukettle.basics.page.Pagination;
import com.ukettle.basics.page.Paging;
import com.ukettle.basics.page.dialect.Client;
import com.ukettle.basics.page.dialect.DB;
import com.ukettle.basics.page.dialect.Dialect;
import com.ukettle.www.toolkit.Reflection;
import com.ukettle.www.toolkit.StringUtils;

/**
 * 
 * @author Kimi Liu
 * @Date Mar 12, 2014
 * @Time 09:56:40
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public abstract class BaseInterceptor implements Interceptor, Serializable {

	private static final long serialVersionUID = 4596430444388728543L;

	protected static final String DELEGATE = "delegate";
	protected static final String MAPPED_STATEMENT = "mappedStatement";
	protected Dialect DIALECT;
	protected String SQL_PATTERN = "";

	/**
	 * 对参数进行转换和检查
	 * 
	 * @param parameterObject
	 *            参数对象
	 * @param pageVO
	 *            参数VO
	 * @return 参数VO
	 * @throws NoSuchFieldException
	 *             无法找到参数
	 */
	protected static Page convertParameter(Object parameter, Page page)
			throws NoSuchFieldException {
		if (parameter instanceof Page) {
			page = (Pagination) parameter;
		} else {
			// 参数为某个实体，该实体拥有Page属性
			Paging paging = parameter.getClass().getAnnotation(Paging.class);
			String field = paging.field();
			Field pageField = Reflection.getAccessibleField(parameter, field);
			if (null != pageField) {
				page = (Pagination) Reflection.getFieldValue(parameter, field);
				if (null != page)
					throw new PersistenceException("分页参数不能为空");
				// 通过反射，对实体对象设置分页对象
				Reflection.setFieldValue(parameter, field, page);
			} else {
				throw new NoSuchFieldException(parameter.getClass().getName()
						+ "不存在分页参数属性！");
			}
		}
		return page;
	}

	/**
	 * 设置属性，支持自定义方言类和制定数据库等方式 dialectClass,自定义方言类; sqlPattern 需要拦截的ID;
	 * 
	 * @param properties
	 *            参数对象
	 * @throws PropertyException
	 *             无法找到参数
	 */
	protected void initProperties(Properties properties) {
		String dialectClass = properties.getProperty("dialectClass");
		DB db;
		if (StringUtils.isEmpty(dialectClass)) {
			String dialect = properties.getProperty("dialectType");
			if (StringUtils.isEmpty(dialect)) {
				throw new IllegalArgumentException(
						"dialect property is not found!");
			}
			db = DB.valueOf(dialect.toUpperCase());
			if (null == db) {
				throw new NullPointerException(
						"plugin not super on this database.");
			}
		} else {
			Dialect dialect = (Dialect) Reflection.instance(dialectClass);
			if (null == dialect) {
				throw new NullPointerException("dialectClass is not found!");
			}
			Client.putEx(dialect);
			db = DB.EX;
		}
		DIALECT = Client.getDialect(db);
		String sqlPattern = properties.getProperty("sqlPattern");
		if (!StringUtils.isEmpty(sqlPattern)) {
			SQL_PATTERN = sqlPattern;
		}
	}

}