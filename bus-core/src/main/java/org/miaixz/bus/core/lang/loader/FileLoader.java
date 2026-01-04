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
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Queue;

import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.io.resource.UrlResource;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.net.url.UrlDecoder;

/**
 * A resource loader for files within a file system directory.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileLoader extends ResourceLoader implements Loader {

    /**
     * The context URL for resources within the file system.
     */
    private final URL context;
    /**
     * The root directory from which to load files.
     */
    private final File root;

    /**
     * Constructs a {@code FileLoader} with the given root directory.
     *
     * @param root The root directory from which to load files.
     * @throws IOException If an I/O error occurs when converting the file to a URL.
     */
    public FileLoader(File root) throws IOException {
        this(root.toURI().toURL(), root);
    }

    /**
     * Constructs a {@code FileLoader} with the given file URL.
     *
     * @param fileURL The URL of the root directory for file resources.
     */
    public FileLoader(URL fileURL) {
        this(fileURL, new File(UrlDecoder.decode(fileURL.getPath(), Charset.UTF_8)));
    }

    /**
     * Constructs a {@code FileLoader} with a specified context URL and root directory.
     *
     * @param context The context URL for resources within the file system.
     * @param root    The root directory from which to load files.
     * @throws IllegalArgumentException If {@code context} or {@code root} is {@code null}.
     */
    public FileLoader(URL context, File root) {
        if (null == context) {
            throw new IllegalArgumentException("context must not be null");
        }
        if (null == root) {
            throw new IllegalArgumentException("root must not be null");
        }
        this.context = context;
        this.root = root;
    }

    /**
     * Loads resources from the file system.
     *
     * @param path        the base path to search for resources.
     * @param recursively whether to search for resources in subdirectories.
     * @param filter      the filter to apply to resources.
     * @return an enumeration of resources.
     */
    @Override
    public Enumeration<Resource> load(String path, boolean recursively, Filter filter) {
        return new Enumerator(context, root, path, recursively, null != filter ? filter : Filters.ALWAYS);
    }

    /**
     * An {@link Enumeration} implementation for iterating over resources within a file system directory.
     */
    private static class Enumerator extends ResourceEnumerator implements Enumeration<Resource> {

        /**
         * The context URL for resources within the file system.
         */
        private final URL context;
        /**
         * Whether to search for resources in subdirectories recursively.
         */
        private final boolean recursively;
        /**
         * The filter to apply to resources.
         */
        private final Filter filter;
        /**
         * A queue of files and directories to be processed.
         */
        private final Queue<File> queue;

        /**
         * Constructs a new {@code Enumerator}.
         *
         * @param context     The context URL for resources within the file system.
         * @param root        The root directory from which to load files.
         * @param path        The base path to search for resources.
         * @param recursively Whether to search for resources in subdirectories.
         * @param filter      The filter to apply to resources.
         */
        Enumerator(URL context, File root, String path, boolean recursively, Filter filter) {
            this.context = context;
            this.recursively = recursively;
            this.filter = filter;
            this.queue = new LinkedList<>();
            File file = new File(root, path);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (int i = 0; null != files && i < files.length; i++) {
                    queue.offer(files[i]);
                }
            } else {
                queue.offer(file);
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
            }
            while (!queue.isEmpty()) {
                File file = queue.poll();

                if (!file.exists()) {
                    continue;
                }

                if (file.isFile()) {
                    try {
                        String name = context.toURI().relativize(file.toURI()).toString();
                        URL url = URI.create(context + name).toURL();
                        if (filter.filtrate(name, url)) {
                            next = new UrlResource(url, name);
                            return true;
                        }
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }
                if (file.isDirectory() && recursively) {
                    File[] files = file.listFiles();
                    for (int i = 0; null != files && i < files.length; i++) {
                        queue.offer(files[i]);
                    }
                    return hasMoreElements();
                }
            }
            return false;
        }
    }

}
