/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.metric.session;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.aoju.bus.core.lang.exception.AuthorizedException;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.metric.ApiContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.util.concurrent.TimeUnit;

/**
 * session管理,默认存放的是{@link ApiHttpSession}。采用谷歌guava缓存实现。
 *
 * @author Kimi Liu
 * @version 6.0.5
 * @since JDK 1.8++
 */
public class ApiSessionManager implements SessionManager {

    private int sessionTimeout = 20;

    private volatile LoadingCache<String, HttpSession> cache;

    @Override
    public HttpSession getSession(String sessionId) {
        if (sessionId == null) {
            return this.createSession(sessionId);
        }
        try {
            return getCache().get(sessionId);
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
            throw new AuthorizedException("create session error");
        }
    }

    /**
     * 创建一个session
     *
     * @param sessionId 会话信息
     * @return 返回session
     */
    protected HttpSession createSession(String sessionId) {
        ServletContext servletContext = getServletContext();
        HttpSession session = this.newSession(sessionId, servletContext);
        session.setMaxInactiveInterval(getSessionTimeout());
        this.getCache().put(session.getId(), session);
        return session;
    }

    /**
     * 返回新的session实例
     *
     * @param sessionId      会话信息
     * @param servletContext 上下文信息
     * @return 返回session
     */
    protected HttpSession newSession(String sessionId, ServletContext servletContext) {
        return new ApiHttpSession(servletContext, sessionId);
    }

    protected ServletContext getServletContext() {
        return ApiContext.getServletContext();
    }

    protected LoadingCache<String, HttpSession> buildCache() {
        return CacheBuilder.newBuilder().expireAfterAccess(getSessionTimeout(), TimeUnit.MINUTES)
                .build(new CacheLoader<String, HttpSession>() {
                    // 找不到sessionId对应的HttpSession时,进入这个方法
                    // 找不到就新建一个
                    @Override
                    public HttpSession load(String sessionId) {
                        return createSession(sessionId);
                    }
                });
    }

    /**
     * 过期时间,分钟,默认20分钟
     *
     * @return 返回过期时间
     */
    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public LoadingCache<String, HttpSession> getCache() {
        if (cache == null) {
            synchronized (ApiSessionManager.class) {
                if (cache == null) {
                    cache = buildCache();
                }
            }
        }
        return cache;
    }

}
