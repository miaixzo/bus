package com.ukettle.basics.shiro.extend.session;

import java.util.Collection;
import java.util.Iterator;

import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;

import com.ukettle.www.toolkit.Constant;


public class DefaultSessionManager extends DefaultWebSessionManager {

	private CacheManager cacheManager;

	public CacheManager getCacheManager() {
		return cacheManager;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	public void validateSessions() {
		int invalidCount = 0;
		Collection<?> activeSessions = getActiveSessions();
		if (null != activeSessions && !activeSessions.isEmpty()) {
			for (Iterator<?> i$ = activeSessions.iterator(); i$.hasNext();) {
				Session session = (Session) i$.next();
				try {
					SessionKey key = new DefaultSessionKey(session.getId());
					validate(session, key);
				} catch (InvalidSessionException e) {
					if (null != cacheManager) {
						SimpleSession s = (SimpleSession) session;
						if (null != s.getAttribute(Constant.SESSION_ID))
							cacheManager.getCache(null).remove(
									s.getAttribute(Constant.SESSION_ID));
					}
					invalidCount++;
				}
			}
		}
	}

}