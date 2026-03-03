/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.lang.loader;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarFile;

import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.url.UrlDecoder;
import org.miaixz.bus.core.net.url.UrlEncoder;

/**
 * A standard resource loader that uses a {@link ClassLoader} to find and load resources. It supports loading resources
 * from the classpath, including JAR files and directories.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StdLoader extends ResourceLoader implements Loader {

    /**
     * The class loader used by this {@code StdLoader}.
     */
    private final ClassLoader classLoader;

    /**
     * Constructs a new {@code StdLoader} using the current thread's context class loader. If the context class loader
     * is {@code null}, the system class loader is used.
     */
    public StdLoader() {
        this(null != Thread.currentThread().getContextClassLoader() ? Thread.currentThread().getContextClassLoader()
                : ClassLoader.getSystemClassLoader());
    }

    /**
     * Constructs a new {@code StdLoader} with the specified class loader.
     *
     * @param classLoader The class loader to use for loading resources.
     * @throws IllegalArgumentException If the provided class loader is {@code null}.
     */
    public StdLoader(ClassLoader classLoader) {
        if (null == classLoader) {
            throw new IllegalArgumentException("classLoader must not be null");
        }
        this.classLoader = classLoader;
    }

    /**
     * Loads resources from the classpath.
     *
     * @param path        the base path to search for resources.
     * @param recursively whether to search for resources in subdirectories.
     * @param filter      the filter to apply to resources.
     * @return an enumeration of resources.
     * @throws IOException if an I/O error occurs during resource loading.
     */
    @Override
    public Enumeration<Resource> load(String path, boolean recursively, Filter filter) throws IOException {
        while (path.startsWith(Symbol.SLASH))
            path = path.substring(1);
        while (path.endsWith(Symbol.SLASH))
            path = path.substring(0, path.length() - 1);
        return new Enumerator(classLoader, path, recursively, null != filter ? filter : Filters.ALWAYS);
    }

    /**
     * An {@link Enumeration} implementation for iterating over resources found by a {@link StdLoader}.
     */
    private static class Enumerator extends ResourceEnumerator implements Enumeration<Resource> {

        /**
         * The path to search for resources.
         */
        private final String path;
        /**
         * Whether to search for resources in subdirectories recursively.
         */
        private final boolean recursively;
        /**
         * The filter to apply to resources.
         */
        private final Filter filter;
        /**
         * The enumeration of URLs found by the class loader for the given path.
         */
        private final Enumeration<URL> urls;
        /**
         * The current enumeration of resources being processed.
         */
        private Enumeration<Resource> resources;

        /**
         * Constructs a new {@code Enumerator}.
         *
         * @param classLoader The class loader to use.
         * @param path        The path to search for resources.
         * @param recursively Whether to search for resources in subdirectories.
         * @param filter      The filter to apply to resources.
         * @throws IOException If an I/O error occurs during resource loading.
         */
        Enumerator(ClassLoader classLoader, String path, boolean recursively, Filter filter) throws IOException {
            this.path = path;
            this.recursively = recursively;
            this.filter = filter;
            this.urls = load(classLoader, path);
            this.resources = Collections.enumeration(Collections.emptySet());
        }

        /**
         * Loads URLs for the given path using the specified class loader.
         *
         * @param classLoader The class loader to use.
         * @param path        The path to search for.
         * @return An enumeration of URLs found.
         * @throws IOException If an I/O error occurs.
         */
        private Enumeration<URL> load(ClassLoader classLoader, String path) throws IOException {
            if (path.length() > 0) {
                return classLoader.getResources(path);
            } else {
                Set<URL> set = new LinkedHashSet<>();
                set.add(classLoader.getResource(path));
                Enumeration<URL> urls = classLoader.getResources(Normal.META_INF + Symbol.C_SLASH);
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    if (url.getProtocol().equalsIgnoreCase(Normal.URL_PROTOCOL_JAR)) {
                        String spec = url.toString();
                        int index = spec.lastIndexOf(Normal.JAR_URL_SEPARATOR);
                        if (index < 0)
                            continue;
                        set.add(URI.create(spec.substring(0, index + Normal.JAR_URL_SEPARATOR.length())).toURL());
                    }
                }
                return Collections.enumeration(set);
            }
        }

        /**
         * Returns {@code true} if there are more resources to enumerate.
         *
         * @return {@code true} if there are more resources, {@code false} otherwise.
         */
        @Override
        public boolean hasMoreElements() {
            if (null != next) {
                return true;
            } else if (!resources.hasMoreElements() && !urls.hasMoreElements()) {
                return false;
            } else if (resources.hasMoreElements()) {
                next = resources.nextElement();
                return true;
            } else {
                URL url = urls.nextElement();
                String protocol = url.getProtocol();
                if ("file".equalsIgnoreCase(protocol)) {
                    try {
                        String uri = UrlDecoder.decode(url.getPath(), Charset.UTF_8);
                        String root = uri.substring(0, uri.lastIndexOf(path));
                        URL context = URI.create(Normal.FILE_URL_PREFIX + UrlEncoder.encodeAll(root, Charset.UTF_8))
                                .toURL();
                        File file = new File(root);
                        resources = new FileLoader(context, file).load(path, recursively, filter);
                        return hasMoreElements();
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                } else if (Normal.URL_PROTOCOL_JAR.equalsIgnoreCase(protocol)) {
                    try {
                        String uri = UrlDecoder.decode(url.getPath(), Charset.UTF_8);
                        String root = uri.substring(0, uri.lastIndexOf(path));
                        URL context = URI.create(Normal.JAR_URL_PREFIX + UrlEncoder.encodeAll(root, Charset.UTF_8))
                                .toURL();
                        JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                        JarFile jarFile = jarURLConnection.getJarFile();
                        resources = new JarLoader(context, jarFile).load(path, recursively, filter);
                        return hasMoreElements();
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                } else {
                    return hasMoreElements();
                }
            }
        }

    }

}
