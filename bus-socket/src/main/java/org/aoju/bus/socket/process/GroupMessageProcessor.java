/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org sandao and other contributors.               *
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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.socket.process;

import org.aoju.bus.socket.AioSession;
import org.aoju.bus.socket.GroupIo;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class GroupMessageProcessor<T> implements MessageProcessor<T>, GroupIo {

    private Map<String, GroupUnit> sessionGroup = new ConcurrentHashMap<>();

    /**
     * 将AioSession加入群组group
     *
     * @param group   群组
     * @param session 会话
     */
    @Override
    public final synchronized void join(String group, AioSession session) {
        GroupUnit groupUnit = sessionGroup.get(group);
        if (null == groupUnit) {
            groupUnit = new GroupUnit();
            sessionGroup.put(group, groupUnit);
        }
        groupUnit.groupList.add(session);
    }

    @Override
    public final synchronized void remove(String group, AioSession session) {
        GroupUnit groupUnit = sessionGroup.get(group);
        if (null == groupUnit) {
            return;
        }
        groupUnit.groupList.remove(session);
        if (groupUnit.groupList.isEmpty()) {
            sessionGroup.remove(group);
        }
    }

    @Override
    public final void remove(AioSession session) {
        for (String group : sessionGroup.keySet()) {
            remove(group, session);
        }
    }

    @Override
    public void writeToGroup(String group, byte[] t) {
        GroupUnit groupUnit = sessionGroup.get(group);
        for (AioSession aioSession : groupUnit.groupList) {
            try {
                aioSession.writeBuffer().write(t);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class GroupUnit {
        Set<AioSession> groupList = new HashSet<>();
    }

}
