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
package org.miaixz.bus.vortex.registry;

import java.util.function.Consumer;

import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.magic.Limiter;

/**
 * 注册表工厂类，用于创建和配置各种类型的注册表
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RegistryFactory {

    /**
     * 创建自定义注册表
     *
     * @param <T>         注册表存储的对象类型
     * @param registryKey 键生成策略
     * @param initializer 初始化逻辑
     * @return 配置好的注册表实例
     */
    public static <T> AbstractRegistry<T> of(AbstractRegistry.RegistryKey<T> registryKey,
            Consumer<AbstractRegistry<T>> initializer) {

        AbstractRegistry<T> registry = new AbstractRegistry<>() {
            @Override
            public void init() {
                if (initializer != null) {
                    initializer.accept(this);
                }
            }
        };

        registry.setKeyGenerator(registryKey);
        return registry;
    }

    /**
     * 创建资产注册表
     *
     * @param initializer 初始化逻辑
     * @return 配置好的资产注册表实例
     */
    public static AbstractRegistry<Assets> assets(Consumer<AbstractRegistry<Assets>> initializer) {
        return of(assets -> assets.getMethod() + assets.getVersion(), initializer);
    }

    /**
     * 创建限流注册表
     *
     * @param initializer 初始化逻辑
     * @return 配置好的限流注册表实例
     */
    public static AbstractRegistry<Limiter> limiter(Consumer<AbstractRegistry<Limiter>> initializer) {
        return of(limiter -> limiter.getIp() + limiter.getMethod() + limiter.getVersion(), initializer);
    }

}