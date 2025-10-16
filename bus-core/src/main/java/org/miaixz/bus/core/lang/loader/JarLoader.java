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
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.io.resource.UrlResource;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.url.UrlEncoder;

/**
 * A resource loader for JAR files.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JarLoader extends ResourceLoader implements Loader {

    /**
     * The context URL for resources within the JAR.
     */
    private final URL context;
    /**
     * The JAR file instance.
     */
    private final JarFile jarFile;

    /**
     * Constructs a {@code JarLoader} from a given JAR file.
     *
     * @param file The JAR file to load resources from.
     * @throws IOException If an I/O error occurs.
     */
    public JarLoader(File file) throws IOException {
        this(new URL(Normal.JAR_URL_PREFIX + file.toURI().toURL() + Normal.JAR_URL_SEPARATOR), new JarFile(file));
    }

    /**
     * Constructs a {@code JarLoader} from a given JAR URL.
     *
     * @param jarURL The URL of the JAR file.
     * @throws IOException If an I/O error occurs.
     */
    public JarLoader(URL jarURL) throws IOException {
        this(jarURL, ((JarURLConnection) jarURL.openConnection()).getJarFile());
    }

    /**
     * Constructs a {@code JarLoader} with a specified context URL and JAR file.
     *
     * @param context The context URL for resources within the JAR.
     * @param jarFile The {@link JarFile} instance.
     * @throws IllegalArgumentException If {@code context} or {@code jarFile} is {@code null}.
     */
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

    @Override
    public Enumeration<Resource> load(String path, boolean recursively, Filter filter) {
        while (path.startsWith(Symbol.SLASH))
            path = path.substring(1);
        while (path.endsWith(Symbol.SLASH))
            path = path.substring(0, path.length() - 1);
        return new Enumerator(context, jarFile, path, recursively, null != filter ? filter : Filters.ALWAYS);
    }

    /**
     * An {@link Enumeration} implementation for iterating over resources within a JAR file.
     */
    private static class Enumerator extends ResourceEnumerator implements Enumeration<Resource> {

        /**
         * The context URL for resources within the JAR.
         */
        private final URL context;
        /**
         * The base path to search for resources.
         */
        private final String path;
        /**
         * The folder path, ensuring it ends with a slash for consistent path matching.
         */
        private final String folder;
        /**
         * Whether to search for resources in subdirectories recursively.
         */
        private final boolean recursively;
        /**
         * The filter to apply to resources.
         */
        private final Filter filter;
        /**
         * The enumeration of JAR entries within the JAR file.
         */
        private final Enumeration<JarEntry> entries;

        /**
         * Constructs a new {@code Enumerator}.
         *
         * @param context     The context URL for resources within the JAR.
         * @param jarFile     The {@link JarFile} instance.
         * @param path        The base path to search for resources.
         * @param recursively Whether to search for resources in subdirectories.
         * @param filter      The filter to apply to resources.
         */
        Enumerator(URL context, JarFile jarFile, String path, boolean recursively, Filter filter) {
            this.context = context;
            this.path = path;
            this.folder = path.endsWith(Symbol.SLASH) || path.length() == 0 ? path : path + Symbol.SLASH;
            this.recursively = recursively;
            this.filter = filter;
            this.entries = jarFile.entries();
        }

        @Override
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
                if (name.equals(path) || (recursively && name.startsWith(folder)) || (!recursively
                        && name.startsWith(folder) && name.indexOf(Symbol.SLASH, folder.length()) < 0)) {
                    try {
                        URL url = new URL(context, UrlEncoder.encodeAll(name, Charset.UTF_8));
                        if (filter.filtrate(name, url)) {
                            next = new UrlResource(url, name);
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
