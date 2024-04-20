/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.http.accord;

import org.miaixz.bus.http.Route;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 创建到目标地址的新连接时要避免的失败路由的黑名单
 * 如果尝试连接到特定IP地址或代理服务器时出现故障，
 * 则会记住该故障并首选备用路由
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RouteDatabase {

    /**
     * 路由记录
     */
    private final Set<Route> failedRoutes = new LinkedHashSet<>();

    /**
     * 记录连接到{@code route}的失败
     *
     * @param route 错误路由信息
     */
    public synchronized void failed(Route route) {
        failedRoutes.add(route);
    }

    /**
     * 成功连接到{@code route}
     *
     * @param route 正确的路由
     */
    public synchronized void connected(Route route) {
        failedRoutes.remove(route);
    }

    /**
     * 如果{@code route}最近失败，应该避免返回true
     *
     * @param route 路由
     * @return the true/false
     */
    public synchronized boolean shouldPostpone(Route route) {
        return failedRoutes.contains(route);
    }

}
