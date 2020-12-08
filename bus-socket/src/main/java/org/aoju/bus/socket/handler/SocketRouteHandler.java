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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.socket.handler;

import org.aoju.bus.socket.SocketRequest;
import org.aoju.bus.socket.SocketResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 路由拦截
 *
 * @author Kimi Liu
 * @version 6.1.5
 * @since JDK 1.8+
 */
public final class SocketRouteHandler extends AbstractSocketHandler {

    private final Map<String, AbstractSocketHandler> handleMap = new ConcurrentHashMap<>();

    @Override
    public void doHandle(SocketRequest request, SocketResponse response) {
        String uri = request.getRequestURI();
        AbstractSocketHandler httpHandle = handleMap.get(uri);


        httpHandle.doHandle(request, response);
    }

    /**
     * 配置URL路由
     *
     * @param urlPattern url匹配
     * @param httpHandle 处理handle
     * @return
     */
    public SocketRouteHandler route(String urlPattern, AbstractSocketHandler httpHandle) {
        handleMap.put(urlPattern, httpHandle);
        return this;
    }

}
