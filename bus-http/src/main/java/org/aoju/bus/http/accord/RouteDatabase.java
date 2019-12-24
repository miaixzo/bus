/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aoju.bus.http.accord;

import org.aoju.bus.http.Route;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 创建到目标地址的新连接时要避免的失败路由的黑名单。
 * 如果尝试连接到特定IP地址或代理服务器时出现故障，
 * 则会记住该故障并首选备用路由
 */
public final class RouteDatabase {

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
