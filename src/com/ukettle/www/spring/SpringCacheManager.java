package com.ukettle.www.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.ehcache.Ehcache;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.util.CollectionUtils;
import org.springframework.cache.support.SimpleValueWrapper;

/**
 * 功能描述：包装Spring cache
 * 
 * @author Kimi Liu
 * @Date Jun 3, 2014
 * @Time 16:15:23
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
@SuppressWarnings("all")
public class SpringCacheManager implements CacheManager {

	private org.springframework.cache.CacheManager cacheManager;

	/**
	 * 设置spring cache manager
	 * 
	 * @param cacheManager
	 */
	public void setCacheManager(
			org.springframework.cache.CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	public <K, V> Cache<K, V> getCache(String name) throws CacheException {
		org.springframework.cache.Cache springCache = cacheManager
				.getCache(name);
		return (Cache<K, V>) new SpringCacheWrapper(springCache);
	}

	static class SpringCacheWrapper implements Cache<Object, Object> {
		private org.springframework.cache.Cache springCache;

		SpringCacheWrapper(org.springframework.cache.Cache springCache) {
			this.springCache = springCache;
		}

		@Override
		public Object get(Object key) throws CacheException {
			Object value = springCache.get(key);
			if (value instanceof SimpleValueWrapper) {
				return ((SimpleValueWrapper) value).get();
			}
			return value;
		}

		@Override
		public Object put(Object key, Object value) throws CacheException {
			springCache.put(key, value);
			return value;
		}

		@Override
		public Object remove(Object key) throws CacheException {
			springCache.evict(key);
			return null;
		}

		@Override
		public void clear() throws CacheException {
			springCache.clear();
		}

		@Override
		public int size() {
			if (springCache.getNativeCache() instanceof Ehcache) {
				Ehcache ehcache = (Ehcache) springCache.getNativeCache();
				return ehcache.getSize();
			}
			throw new UnsupportedOperationException(
					"invoke spring cache abstract size method not supported");
		}

		@Override
		public Set keys() {
			if (springCache.getNativeCache() instanceof Ehcache) {
				Ehcache ehcache = (Ehcache) springCache.getNativeCache();
				return new HashSet(ehcache.getKeys());
			}
			throw new UnsupportedOperationException(
					"invoke spring cache abstract keys method not supported");
		}

		@Override
		public Collection<Object> values() {
			if (springCache.getNativeCache() instanceof Ehcache) {
				Ehcache ehcache = (Ehcache) springCache.getNativeCache();
				List<?> keys = ehcache.getKeys();
				if (!CollectionUtils.isEmpty(keys)) {
					List<Object> values = new ArrayList<Object>(keys.size());
					for (Object key : keys) {
						Object value = get(key);
						if (value != null) {
							values.add(value);
						}
					}
					return Collections.unmodifiableList(values);
				} else {
					return Collections.emptyList();
				}
			}
			throw new UnsupportedOperationException(
					"invoke spring cache abstract values method not supported");
		}
	}

}