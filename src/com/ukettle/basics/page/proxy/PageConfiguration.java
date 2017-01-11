package com.ukettle.basics.page.proxy;

import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 自定义Mybatis的配置，扩展.
 * </p>
 * 
 * @author Kimi Liu
 * @Date Aug 25, 2014
 * @Time 10:13:40
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
@SuppressWarnings("all")
@Component
public class PageConfiguration extends Configuration {

	protected MapperRegistry mapperRegistry = new PaginationMapperRegistry(this);

	public <T> void addMapper(Class<T> type) {
		mapperRegistry.addMapper(type);
	}

	public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
		return mapperRegistry.getMapper(type, sqlSession);
	}

	public boolean hasMapper(Class type) {
		return mapperRegistry.hasMapper(type);
	}

}