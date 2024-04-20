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
package org.miaixz.bus.proxy.factory;

import org.miaixz.bus.core.lang.Symbol;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 用于存储基于特定类型{@link AbstractProxyClass}的代理的实现类的缓存。
 * 代理类缓存确保每个{@link AbstractProxyClass}/{@link ClassLoader}/代理类数组组合只有一个类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ProxyClassCache {

    private final Map loaderToClassCache = new WeakHashMap();
    private final ProxyClass proxyClassGenerator;

    public ProxyClassCache(ProxyClass proxyClassGenerator) {
        this.proxyClassGenerator = proxyClassGenerator;
    }

    public synchronized Class getProxyClass(ClassLoader classLoader, Class[] proxyClasses) {
        final Map classCache = getClassCache(classLoader);
        final String key = toClassCacheKey(proxyClasses);
        Class proxyClass;
        WeakReference proxyClassReference = (WeakReference) classCache.get(key);
        if (null == proxyClassReference) {
            proxyClass = proxyClassGenerator.createProxy(classLoader, proxyClasses);
            classCache.put(key, new WeakReference(proxyClass));
        } else {
            synchronized (proxyClassReference) {
                proxyClass = (Class) proxyClassReference.get();
                if (null == proxyClass) {
                    proxyClass = proxyClassGenerator.createProxy(classLoader, proxyClasses);
                    classCache.put(key, new WeakReference(proxyClass));
                }
            }
        }
        return proxyClass;
    }

    private Map getClassCache(ClassLoader classLoader) {
        Map cache = (Map) loaderToClassCache.get(classLoader);
        if (null == cache) {
            cache = new HashMap();
            loaderToClassCache.put(classLoader, cache);
        }
        return cache;
    }

    private String toClassCacheKey(Class[] proxyClasses) {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < proxyClasses.length; i++) {
            Class proxyInterface = proxyClasses[i];
            sb.append(proxyInterface.getName());
            if (i != proxyClasses.length - 1) {
                sb.append(Symbol.COMMA);
            }
        }
        return sb.toString();
    }

}

