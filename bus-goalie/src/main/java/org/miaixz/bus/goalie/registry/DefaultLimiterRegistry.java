/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.goalie.registry;

import org.miaixz.bus.goalie.Assets;
import org.miaixz.bus.goalie.magic.Limiter;

/**
 * 默认限流注册实现类，基于 AbstractRegistry 提供限流配置（Limiter）的注册和管理功能
 *
 * @author Justubborn
 * @since Java 17+
 */
public class DefaultLimiterRegistry extends AbstractRegistry<Limiter> implements LimiterRegistry {

    /**
     * 添加限流配置到注册表，使用 IP、方法名和版本号的组合作为键
     *
     * @param limiter 要添加的限流配置对象
     */
    @Override
    public void addLimiter(Limiter limiter) {
        String nameVersion = limiter.getMethod() + limiter.getVersion();
        String ip = limiter.getIp();
        add(ip + nameVersion, limiter);
    }

    /**
     * 修改注册表中的限流配置（当前为空实现）
     *
     * @param limitCfg 要更新的限流配置对象
     */
    @Override
    public void amendLimiter(Limiter limitCfg) {
        // 空实现，待扩展
    }

    /**
     * 根据方法名和版本号获取对应的资产（当前返回 null）
     *
     * @param method  方法名
     * @param version 版本号
     * @return 当前实现始终返回 null，待扩展
     */
    @Override
    public Assets getLimiter(String method, String version) {
        return null;
    }

    /**
     * 初始化注册表（当前为空实现，子类可根据需要扩展）
     */
    @Override
    public void init() {
        // 空实现，留给子类扩展
    }

}