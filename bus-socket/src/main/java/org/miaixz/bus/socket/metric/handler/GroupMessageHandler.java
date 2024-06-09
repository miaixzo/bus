/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org sandao and other contributors.             ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.socket.metric.handler;

import org.miaixz.bus.socket.GroupIo;
import org.miaixz.bus.socket.Handler;
import org.miaixz.bus.socket.Session;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象群组消息处理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
abstract class GroupMessageHandler<T> implements Handler<T>, GroupIo {

    private Map<String, GroupUnit> sessionGroup = new ConcurrentHashMap<>();

    /**
     * 将Session加入群组group
     *
     * @param group 群组
     * @param session 会话
     */
    @Override
    public final synchronized void join(String group, Session session) {
        GroupUnit groupUnit = sessionGroup.get(group);
        if (groupUnit == null) {
            groupUnit = new GroupUnit();
            sessionGroup.put(group, groupUnit);
        }
        groupUnit.groupList.add(session);
    }

    @Override
    public final synchronized void remove(String group, Session session) {
        GroupUnit groupUnit = sessionGroup.get(group);
        if (groupUnit == null) {
            return;
        }
        groupUnit.groupList.remove(session);
        if (groupUnit.groupList.isEmpty()) {
            sessionGroup.remove(group);
        }
    }

    @Override
    public final void remove(Session session) {
        for (String group : sessionGroup.keySet()) {
            remove(group, session);
        }
    }

    @Override
    public void write(String group, byte[] data) {
        GroupUnit groupUnit = sessionGroup.get(group);
        for (Session session : groupUnit.groupList) {
            try {
                session.writeBuffer().write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class GroupUnit {
        Set<Session> groupList = new HashSet<>();
    }

}
