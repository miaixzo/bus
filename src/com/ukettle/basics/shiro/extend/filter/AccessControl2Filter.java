package com.ukettle.basics.shiro.extend.filter;

import java.io.Serializable;
import java.util.Deque;
import java.util.LinkedList;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;

import com.ukettle.basics.shiro.entity.Shiro;


public class AccessControl2Filter extends AccessControlFilter {

	// 踢出后到的地址
	private String forcedUrl = "/CN?ForcedExit";
	// 默认踢出之前登录的用户
	private boolean forcedAfter = false;
	// 同一个帐号最大会话数 默认1
	private int maxSession = 1;

	private SessionManager sessionManager;
	private Cache<String, Deque<Serializable>> cache;

	public void setForcedUrl(String forcedUrl) {
		this.forcedUrl = forcedUrl;
	}

	public void setForcedAfter(boolean forcedAfter) {
		this.forcedAfter = forcedAfter;
	}

	public void setMaxSession(int maxSession) {
		this.maxSession = maxSession;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public void setCache(Cache<String, Deque<Serializable>> cache) {
		this.cache = cache;
	}

	@Override
	protected boolean isAccessAllowed(ServletRequest request,
			ServletResponse response, Object mappedValue) throws Exception {
		return false;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cache = cacheManager.getCache("forcedSessionsCache");
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request,
			ServletResponse response) throws Exception {
		Subject subject = getSubject(request, response);
		if (!subject.isAuthenticated() && !subject.isRemembered()) {
			// 如果没有登录，直接进行之后的流程
			return true;
		}

		Session session = subject.getSession();
		Serializable sessionId = session.getId();
		Shiro shiro = (Shiro) subject.getPrincipal();
		String username = shiro.getEmail();
		// 同步控制
		Deque<Serializable> deque = cache.get(username);
		if (null == deque) {
			deque = new LinkedList<Serializable>();
			cache.put(username, deque);
		}

		// 如果队列里没有此sessionId，且用户没有被踢出；放入队列
		if (!deque.contains(sessionId)
				&& null == session.getAttribute("ForcedExit")) {
			deque.push(sessionId);
		}

		// 如果队列里的sessionId数超出最大会话数，开始踢人
		while (deque.size() > maxSession) {
			Serializable exitId = null;
			if (forcedAfter) { // 如果踢出后者
				exitId = deque.removeFirst();
			} else { // 否则踢出前者
				exitId = deque.removeLast();
			}
			try {
				Session exitSession = sessionManager
						.getSession(new DefaultSessionKey(exitId));
				if (null != exitSession) {
					// 设置会话的exit属性表示踢出了
					exitSession.setAttribute("ForcedExit", true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// 如果被踢出了，直接退出，重定向到踢出后的地址
		if (null != session.getAttribute("ForcedExit")) {
			try {
				// 会话被踢出了
				subject.logout();
				saveRequest(request);
				WebUtils.issueRedirect(request, response, forcedUrl);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}

}