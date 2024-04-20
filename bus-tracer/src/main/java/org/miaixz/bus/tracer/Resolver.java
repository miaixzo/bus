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

import org.miaixz.bus.tracer.backend.TraceBackendProvider;

import java.lang.reflect.InvocationTargetException;
import java.security.PrivilegedAction;
import java.util.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Resolver {

    private static volatile Map<ClassLoader, Set<TraceBackendProvider>> providersPerClassloader = new WeakHashMap<>();

    public Set<TraceBackendProvider> getBackendProviders() {
        final Map<ClassLoader, Set<TraceBackendProvider>> cacheCopy = providersPerClassloader;
        final Set<TraceBackendProvider> providerFromContextClassLoader = getTraceProviderFromClassloader(cacheCopy,
                GetClassLoader.fromContext());
        if (!providerFromContextClassLoader.isEmpty()) {
            return providerFromContextClassLoader;
        } else {
            return getTraceProviderFromClassloader(cacheCopy, GetClassLoader.fromClass(Resolver.class));
        }
    }

    Set<TraceBackendProvider> getDefaultTraceBackendProvider() {
        try {
            final ClassLoader classLoader = GetClassLoader.fromContext();
            final Class<?> slf4jTraceBackendProviderClass = Class.forName("org.miaixz.bus.tracer.backend.Slf4jTraceBackendProvider", true, classLoader);
            final TraceBackendProvider instance = (TraceBackendProvider) slf4jTraceBackendProviderClass.getConstructor().newInstance();
            updatedCache(classLoader, Collections.singleton(instance));
            return Collections.singleton(instance);
        } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException | ClassCastException e) {
            return Collections.emptySet();
        }
    }

    private Set<TraceBackendProvider> getTraceProviderFromClassloader(
            final Map<ClassLoader, Set<TraceBackendProvider>> cacheCopy,
            final ClassLoader classLoader) {
        Set<TraceBackendProvider> classLoaderProviders = cacheCopy.get(classLoader);
        if (isLookupNeeded(classLoaderProviders)) {
            classLoaderProviders = loadProviders(classLoader);
            updatedCache(classLoader, classLoaderProviders);
        }
        return classLoaderProviders;
    }

    boolean isLookupNeeded(Set<TraceBackendProvider> classLoaderProviders) {
        return null == classLoaderProviders || !(classLoaderProviders instanceof EmptyBackendProviderSet) && classLoaderProviders.isEmpty();
    }

    private void updatedCache(final ClassLoader classLoader, final Set<TraceBackendProvider> provider) {
        final Map<ClassLoader, Set<TraceBackendProvider>> copyOnWriteMap = new WeakHashMap<>(providersPerClassloader);
        if (!provider.isEmpty()) {
            copyOnWriteMap.put(classLoader, new Provider(provider));
        } else {
            copyOnWriteMap.put(classLoader, new EmptyBackendProviderSet());
        }
        providersPerClassloader = copyOnWriteMap;
    }

    private Set<TraceBackendProvider> loadProviders(ClassLoader classloader) {
        final ServiceLoader<TraceBackendProvider> loader = ServiceLoader.load(TraceBackendProvider.class, classloader);
        final Iterator<TraceBackendProvider> providerIterator = loader.iterator();
        final Set<TraceBackendProvider> TraceProvider = new HashSet<>();
        while (providerIterator.hasNext()) {
            try {
                TraceProvider.add(providerIterator.next());
            } catch (ServiceConfigurationError ignored) {
            }
        }
        return TraceProvider;
    }

    static final class GetClassLoader implements PrivilegedAction<ClassLoader> {

        private final Class<?> clazz;

        private GetClassLoader(final Class<?> clazz) {
            this.clazz = clazz;
        }

        public static ClassLoader fromContext() {
            return doPrivileged(new GetClassLoader(null));
        }

        public static ClassLoader fromClass(Class<?> clazz) {
            if (null == clazz) {
                throw new IllegalArgumentException("Class is null");
            }
            return doPrivileged(new GetClassLoader(clazz));
        }

        private static ClassLoader doPrivileged(GetClassLoader action) {
            return action.run();
        }

        public ClassLoader run() {
            if (null != clazz) {
                return clazz.getClassLoader();
            } else {
                return Thread.currentThread().getContextClassLoader();
            }
        }

    }

    static final class EmptyBackendProviderSet extends AbstractSet<TraceBackendProvider> {

        @Override
        public Iterator<TraceBackendProvider> iterator() {
            return Collections.<TraceBackendProvider>emptyList().iterator();
        }

        @Override
        public int size() {
            return 0;
        }

    }

}
