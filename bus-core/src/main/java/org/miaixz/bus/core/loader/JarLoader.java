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
package org.miaixz.bus.core.loader;

import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.io.resource.UriResource;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.UriKit;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Jar包资源加载器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JarLoader extends ResourceLoader implements Loader {

    private final URL context;
    private final JarFile jarFile;

    public JarLoader(File file) throws IOException {
        this(new URL(Normal.JAR_URL_PREFIX + file.toURI().toURL() + Normal.JAR_URL_SEPARATOR), new JarFile(file));
    }

    public JarLoader(URL jarURL) throws IOException {
        this(jarURL, ((JarURLConnection) jarURL.openConnection()).getJarFile());
    }

    public JarLoader(URL context, JarFile jarFile) {
        if (null == context) {
            throw new IllegalArgumentException("context must not be null");
        }
        if (null == jarFile) {
            throw new IllegalArgumentException("jarFile must not be null");
        }
        this.context = context;
        this.jarFile = jarFile;
    }

    public Enumeration<Resource> load(String path, boolean recursively, Filter filter) {
        while (path.startsWith(Symbol.SLASH)) path = path.substring(1);
        while (path.endsWith(Symbol.SLASH)) path = path.substring(0, path.length() - 1);
        return new Enumerator(context, jarFile, path, recursively, null != filter ? filter : Filters.ALWAYS);
    }

    private static class Enumerator extends ResourceEnumerator implements Enumeration<Resource> {

        private final URL context;
        private final String path;
        private final String folder;
        private final boolean recursively;
        private final Filter filter;
        private final Enumeration<JarEntry> entries;

        Enumerator(URL context, JarFile jarFile, String path, boolean recursively, Filter filter) {
            this.context = context;
            this.path = path;
            this.folder = path.endsWith(Symbol.SLASH) || path.length() == 0 ? path : path + Symbol.SLASH;
            this.recursively = recursively;
            this.filter = filter;
            this.entries = jarFile.entries();
        }

        public boolean hasMoreElements() {
            if (null != next) {
                return true;
            }
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (jarEntry.isDirectory()) {
                    continue;
                }
                String name = jarEntry.getName();
                if (name.equals(path)
                        || (recursively && name.startsWith(folder))
                        || (!recursively && name.startsWith(folder) && name.indexOf(Symbol.SLASH, folder.length()) < 0)) {
                    try {
                        URL url = new URL(context, UriKit.encodePath(name, Charset.UTF_8));
                        if (filter.filtrate(name, url)) {
                            next = new UriResource(url, name);
                            return true;
                        }
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
            return false;
        }

    }

}
