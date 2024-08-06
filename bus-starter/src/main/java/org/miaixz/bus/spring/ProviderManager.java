/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.Provider;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

/**
 * Spring策略模式
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Component
public class ProviderManager {

    public static final Map<Class<Provider<?>>, Collection<Provider<?>>> CACHED_PROVIDERS = new ConcurrentHashMap<>();
    /**
     * Spring的IOC容器,默认为空
     */
    public static ConfigurableApplicationContext context;

    public ProviderManager() {

    }

    /**
     * 加载 Provider
     *
     * @param providerClass 给定的Class
     * @param support       支持的策略
     * @return 最终的支持策略的 Provider
     */
    public static <T extends Provider<S>, S> T load(Class<T> providerClass, S support) {

        Collection<T> providers = loadProvider(providerClass);
        for (Provider<?> provider : providers) {
            if (Objects.equals(provider.type(), support)) {
                return (T) provider;
            }
        }

        return null;
    }

    /**
     * 返回所有的 Provider
     *
     * @param providerClass 给定的Class
     * @return 所有的 Provider
     */
    public static <T extends Provider<?>> Collection<T> all(Class<T> providerClass) {
        return loadProvider(providerClass);
    }

    /**
     * 通过class获取所有的 beans
     */
    private static <T extends Provider<?>> Collection<T> loadProvider(Class<T> providerClass) {
        return (Collection<T>) CACHED_PROVIDERS.computeIfAbsent((Class<Provider<?>>) providerClass, key -> {
            if (context == null) {
                context = SpringBuilder.getContext();
            }
            Map<String, T> beansOfType = context.getBeansOfType(providerClass);
            Collection<T> values = beansOfType.values();
            ArrayList<T> ts = new ArrayList<>(values);
            AnnotationAwareOrderComparator.sort(ts);
            return (Collection<Provider<?>>) ts;
        });
    }

}