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
package org.miaixz.bus.core.lang.loader;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
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
                        set.add(new URL(url, spec.substring(0, index + Normal.JAR_URL_SEPARATOR.length())));
                    }
                }
                return Collections.enumeration(set);
            }
        }

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
                        URL context = new URL(url, Normal.FILE_URL_PREFIX + UrlEncoder.encodeAll(root, Charset.UTF_8));
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
                        URL context = new URL(url, Normal.JAR_URL_PREFIX + UrlEncoder.encodeAll(root, Charset.UTF_8));
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
