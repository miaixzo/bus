package org.ukettle.basics.page.proxy;

import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;

/**
 * 
 * @author Kimi Liu
 * @Date Aug 22, 2014
 * @Time 11:31:11
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public class PaginationMapperRegistry extends MapperRegistry {
	public PaginationMapperRegistry(Configuration config) {
		super(config);
	}

	@Override
	public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
		if (!hasMapper(type)) {
			throw new BindingException("Type " + type
					+ " is not known to the MapperRegistry.");
		}
		try {
			return PaginationMapperProxy.newMapperProxy(type, sqlSession);
		} catch (Exception e) {
			throw new BindingException("Error getting mapper instance. Cause: "
					+ e, e);
		}
	}

}