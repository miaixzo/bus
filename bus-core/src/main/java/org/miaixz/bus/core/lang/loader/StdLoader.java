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
package org.miaixz.bus.core.lang.loader;

import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.url.UrlDecoder;
import org.miaixz.bus.core.net.url.UrlEncoder;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * 标准的资源加载器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StdLoader extends ResourceLoader implements Loader {

    private final ClassLoader classLoader;

    public StdLoader() {
        this(null != Thread.currentThread().getContextClassLoader() ? Thread.currentThread().getContextClassLoader() : ClassLoader.getSystemClassLoader());
    }

    public StdLoader(ClassLoader classLoader) {
        if (null == classLoader) {
            throw new IllegalArgumentException("classLoader must not be null");
        }
        this.classLoader = classLoader;
    }

    public Enumeration<Resource> load(String path, boolean recursively, Filter filter) throws IOException {
        while (path.startsWith(Symbol.SLASH)) path = path.substring(1);
        while (path.endsWith(Symbol.SLASH)) path = path.substring(0, path.length() - 1);
        return new Enumerator(classLoader, path, recursively, null != filter ? filter : Filters.ALWAYS);
    }

    private static class Enumerator extends ResourceEnumerator implements Enumeration<Resource> {

        private final String path;
        private final boolean recursively;
        private final Filter filter;
        private final Enumeration<URL> urls;
        private Enumeration<Resource> resources;

        Enumerator(ClassLoader classLoader, String path, boolean recursively, Filter filter) throws IOException {
            this.path = path;
            this.recursively = recursively;
            this.filter = filter;
            this.urls = load(classLoader, path);
            this.resources = Collections.enumeration(Collections.emptySet());
        }

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
                        if (index < 0) continue;
                        set.add(new URL(url, spec.substring(0, index + Normal.JAR_URL_SEPARATOR.length())));
                    }
                }
                return Collections.enumeration(set);
            }
        }

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
