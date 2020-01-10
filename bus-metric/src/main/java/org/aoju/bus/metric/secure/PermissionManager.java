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
package org.aoju.bus.metric.secure;

import org.aoju.bus.metric.manual.ManagerInitializer;

import java.util.List;

/**
 * 权限管理定义
 *
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8++
 */
public interface PermissionManager extends ManagerInitializer {

    /**
     * 获取客户端拥有的接口
     *
     * @param appKey 标识
     * @return 返回接口列表
     */
    List<String> listClientApi(String appKey);

    /**
     * 能否访问
     *
     * @param appKey  appKey
     * @param name    接口名
     * @param version 版本号
     * @return true：能
     */
    boolean canVisit(String appKey, String name, String version);

    /**
     * 加载权限配置
     */
    void loadPermissionConfig();

    /**
     * 加载权限配置到缓存
     *
     * @param configJson 配置信息
     */
    void loadPermissionCache(String configJson);

}
