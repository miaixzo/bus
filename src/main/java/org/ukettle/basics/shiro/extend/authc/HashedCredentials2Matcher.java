package org.ukettle.basics.shiro.extend.authc;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;

public class HashedCredentials2Matcher extends HashedCredentialsMatcher {

	private Cache<String, AtomicInteger> faultPasswordCache;

	public void setFaultPasswordCache(
			Cache<String, AtomicInteger> faultPasswordCache) {
		this.faultPasswordCache = faultPasswordCache;
	}

	public HashedCredentials2Matcher(CacheManager cacheManager) {
		faultPasswordCache = cacheManager.getCache("faultPasswordCache");
	}

	@Override
	public boolean doCredentialsMatch(AuthenticationToken token,
			AuthenticationInfo info) {
		String username = (String) token.getPrincipal();
		// retry count + 1
		AtomicInteger retryCount = faultPasswordCache.get(username);
		if (null == retryCount) {
			retryCount = new AtomicInteger(0);
			faultPasswordCache.put(username, retryCount);
		}
		if (retryCount.incrementAndGet() > 5) {
			// if retry count > 5 throw
			throw new ExcessiveAttemptsException();
		}

		boolean matches = super.doCredentialsMatch(token, info);
		if (matches) {
			// clear retry count
			faultPasswordCache.remove(username);
		}
		return matches;
	}

}