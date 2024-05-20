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
package org.miaixz.bus.tracer;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.tracer.backend.TraceBackendProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Tracer {

    public static Backend getBackend() {
        return getBackend(new Resolver());
    }

    /**
     * 返回Backend类路径上必须只有一个Tracer实现
     * 对该方法的调用可能最初会阻塞以使用{@link java.util.ServiceLoader}查找实现
     * 从具有不同类加载器上下文的多个线程对该方法的调用最初可能很慢
     * 因为高速缓存写入可能在并发情况下互相覆盖，并且某些类加载器上下文可能需要多次查找,允许查找机制完全避免同步
     * TODO：如果运行嵌套的类加载器环境（例如servlet容器），并且Tracee Api位于顶层
     * 类加载器中，而Tracee实现位于子类加载器中，则子类加载器可能不会卸载*直到出现内存不足的情况（因为SoftReference将BackendProvider保留在内存中）
     * 可以将SoftReference更改为WeakReference，但让TraceeBackend对它的TraceeBackendProvider保持强引用
     *
     * @param resolver 解析器
     * @return the Backend
     */
    protected static Backend getBackend(final Resolver resolver) {
        final Set<TraceBackendProvider> backendProviders;
        try {
            backendProviders = resolver.getBackendProviders();
        } catch (RuntimeException e) {
            throw new InternalException("Unable to load available backend providers", e);
        }
        if (backendProviders.isEmpty()) {
            final Set<TraceBackendProvider> defaultProvider = resolver.getDefaultTraceBackendProvider();
            if (defaultProvider.isEmpty()) {
                throw new InternalException("Unable to find a Builder backend provider. Make sure that you have " +
                        "Tracer(for slf4j) or any other backend implementation on the classpath.");
            }
            return defaultProvider.iterator().next().provideBackend();
        }
        if (backendProviders.size() > 1) {
            final List<Class<?>> providerClasses = new ArrayList<>(backendProviders.size());
            for (TraceBackendProvider backendProvider : backendProviders) {
                providerClasses.add(backendProvider.getClass());
            }
            final String providerClassNames = Arrays.toString(providerClasses.toArray());
            throw new InternalException("Multiple Builder backend providers found. Don't know which one of the following to use: "
                    + providerClassNames);
        }
        return backendProviders.iterator().next().provideBackend();
    }

}
