/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.loader;

import org.aoju.bus.core.io.resource.Resource;
import org.aoju.bus.core.io.resource.UriResource;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.toolkit.UriKit;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 文件资源加载器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileLoader extends ResourceLoader implements Loader {

    private final URL context;
    private final File root;

    public FileLoader(File root) throws IOException {
        this(root.toURI().toURL(), root);
    }

    public FileLoader(URL fileURL) {
        this(fileURL, new File(UriKit.decode(fileURL.getPath(), Charset.UTF_8)));
    }

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

    public Enumeration<Resource> load(String path, boolean recursively, Filter filter) {
        return new Enumerator(context, root, path, recursively, null != filter ? filter : Filters.ALWAYS);
    }

    private static class Enumerator extends ResourceEnumerator implements Enumeration<Resource> {
        private final URL context;
        private final boolean recursively;
        private final Filter filter;
        private final Queue<File> queue;

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
                        URL url = new URL(context, name);
                        if (filter.filtrate(name, url)) {
                            next = new UriResource(url, name);
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
