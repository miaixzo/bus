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
package org.miaixz.bus.limiter.metric;

import org.miaixz.bus.core.data.UUID;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.limiter.Builder;
import org.miaixz.bus.limiter.Provider;
import org.miaixz.bus.limiter.Supplier;
import org.miaixz.bus.limiter.magic.StrategyMode;
import org.miaixz.bus.limiter.magic.annotation.Limiting;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * REQUEST_LIMIT 模式处理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RequestProvider implements Provider {

    private final ExecutorService cleaner = ThreadKit.newFixedExecutor(1, 5, "L-", false);

    private final Map<Serializable, ResourceManager> map = new ConcurrentHashMap<>();

    /**
     * 默认的user标识提供者
     */
    private Supplier supplier = new Supplier() {

        @Override
        public Serializable get() {
            return UUID.fastUUID();
        }

    };

    /**
     * 设置新的用户标识提供者
     *
     * @param supplier 提供者
     */
    public void setMarkSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    @Override
    public StrategyMode get() {
        return StrategyMode.REQUEST_LIMIT;
    }

    @Override
    public Object process(Object bean, Method method, Object[] args) {
        // 获取当前用户标识
        Serializable mark = supplier.get();
        ResourceManager resourceManager = map.get(mark);

        // 缓存操作
        if (Objects.isNull(resourceManager)) {
            resourceManager = new ResourceManager();
            map.put(mark, resourceManager);
        }

        // 获取方法配置参数
        String name = Builder.resolveMethodName(method);
        Limiting limiting = (Limiting) MethodManager.getAnnoInfo(name).getRight();
        if (!resourceManager.entry(name, limiting)) {
            // 拦截方法
            return supplier.intercept(bean, method, args);
        }

        // 允许执行
        return MethodKit.invoke(bean, method, args);
    }

    /**
     * 清理存在的资源管理
     */
    private void clears() {
        cleaner.submit(this::clear);
    }

    /**
     * 清理资源，最多10个
     */
    private void clear() {
        int count = 0;
        for (Serializable mark : map.keySet()) {
            count++;
            ResourceManager resourceManager = map.get(mark);
            if (Objects.isNull(resourceManager)) continue;
            if (resourceManager.isClear()) map.remove(mark);
            if (count > 9) return;
        }
    }

}
