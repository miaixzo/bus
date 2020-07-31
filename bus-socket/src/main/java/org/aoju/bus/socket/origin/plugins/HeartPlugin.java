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
package org.aoju.bus.socket.origin.plugins;

import org.aoju.bus.logger.Logger;
import org.aoju.bus.socket.origin.AioSession;
import org.aoju.bus.socket.origin.StateMachine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 心跳插件
 *
 * @author Kimi Liu
 * @version 6.0.2
 * @since JDK 1.8+
 */
public abstract class HeartPlugin<T> extends AbstractPlugin<T> {

    private static Timer timer = new Timer("HeartMonitor Timer", true);
    private Map<AioSession<T>, Long> sessionMap = new HashMap<>();
    private int timeout;

    public HeartPlugin(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public final boolean preProcess(AioSession<T> session, T t) {
        sessionMap.put(session, System.currentTimeMillis());
        //是否心跳响应消息
        if (isHeartMessage(session, t)) {
            //延长心跳监测时间
            return false;
        }
        return true;
    }

    @Override
    public final void stateEvent(StateMachine stateMachineEnum, AioSession<T> session, Throwable throwable) {
        switch (stateMachineEnum) {
            case NEW_SESSION:
                sessionMap.put(session, System.currentTimeMillis());
                registerHeart(session, timeout);
                //注册心跳监测
                break;
            case SESSION_CLOSED:
                //移除心跳监测
                sessionMap.remove(session);
                break;
        }
    }

    /**
     * 自定义心跳消息并发送
     *
     * @param session 会话
     * @throws IOException 异常
     */
    public abstract void sendHeartRequest(AioSession<T> session) throws IOException;

    /**
     * 判断当前收到的消息是否为心跳消息
     * 心跳请求消息与响应消息可能相同,也可能不同,因实际场景而异,故接口定义不做区分
     *
     * @param session 会话
     * @param msg     信息
     * @return true/false
     */
    public abstract boolean isHeartMessage(AioSession<T> session, T msg);

    private void registerHeart(final AioSession<T> session, final int timeout) {
        if (timeout <= 0) {
            Logger.info("sesssion:{} 因心跳超时时间为:{},终止启动心跳监测任务", session, timeout);
            return;
        }
        Logger.info("session:{}注册心跳任务,超时时间:{}", session, timeout);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (session.isInvalid()) {
                    sessionMap.remove(session);
                    Logger.info("session:{} 已失效,移除心跳任务", session);
                    return;
                }
                Long lastTime = sessionMap.get(session);
                if (lastTime == null) {
                    Logger.warn("session:{} timeout is null", session);
                    lastTime = System.currentTimeMillis();
                    sessionMap.put(session, lastTime);
                }
                if (System.currentTimeMillis() - lastTime > timeout) {
                    try {
                        sendHeartRequest(session);
                    } catch (IOException e) {
                        Logger.error("heart exception", e);
                    }
                }
                registerHeart(session, timeout);
            }
        }, timeout);
    }

}
