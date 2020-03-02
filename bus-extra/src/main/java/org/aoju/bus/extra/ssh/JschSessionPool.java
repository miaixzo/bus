/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.extra.ssh;

import com.jcraft.jsch.Session;
import org.aoju.bus.core.utils.StringUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Jsch会话池
 *
 * @author Kimi Liu
 * @version 5.6.5
 * @since JDK 1.8+
 */
public enum JschSessionPool {

    INSTANCE;

    /**
     * 锁
     */
    private static final Object lock = new Object();
    /**
     * SSH会话池,key：host,value：Session对象
     */
    private Map<String, Session> sessionPool = new ConcurrentHashMap<>();

    /**
     * 获取Session,不存在返回null
     *
     * @param key 键
     * @return Session
     */
    public Session get(String key) {
        return sessionPool.get(key);
    }

    /**
     * 获得一个SSH跳板机会话,重用已经使用的会话
     *
     * @param sshHost 跳板机主机
     * @param sshPort 跳板机端口
     * @param sshUser 跳板机用户名
     * @param sshPass 跳板机密码
     * @return SSH会话
     */
    public Session getSession(String sshHost, int sshPort, String sshUser, String sshPass) {
        final String key = StringUtils.format("{}@{}:{}", sshUser, sshHost, sshPort);
        Session session = get(key);
        if (null == session || false == session.isConnected()) {
            synchronized (lock) {
                session = get(key);
                if (null == session || false == session.isConnected()) {
                    session = SSHUtils.openSession(sshHost, sshPort, sshUser, sshPass);
                    put(key, session);
                }
            }
        }
        return session;
    }

    /**
     * 加入Session
     *
     * @param key     键
     * @param session Session
     */
    public void put(String key, Session session) {
        this.sessionPool.put(key, session);
    }

    /**
     * 关闭SSH连接会话
     *
     * @param key 主机,格式为user@host:port
     */
    public void close(String key) {
        Session session = sessionPool.get(key);
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        sessionPool.remove(key);
    }

    /**
     * 移除指定Session
     *
     * @param session Session会话
     */
    public void remove(Session session) {
        if (null != session) {
            final Iterator<Entry<String, Session>> iterator = this.sessionPool.entrySet().iterator();
            Entry<String, Session> entry;
            while (iterator.hasNext()) {
                entry = iterator.next();
                if (session.equals(entry.getValue())) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    /**
     * 关闭所有SSH连接会话
     */
    public void closeAll() {
        Collection<Session> sessions = sessionPool.values();
        for (Session session : sessions) {
            if (session.isConnected()) {
                session.disconnect();
            }
        }
        sessionPool.clear();
    }

}
