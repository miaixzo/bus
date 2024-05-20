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

import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * 资源加载器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class ResourceLoader implements Loader {

    public Enumeration<Resource> load(String path) throws IOException {
        return load(path, false, Filters.ALWAYS);
    }

    public Enumeration<Resource> load(String path, boolean recursively) throws IOException {
        return load(path, recursively, Filters.ALWAYS);
    }

    public Enumeration<Resource> load(String path, Filter filter) throws IOException {
        return load(path, true, filter);
    }

    protected abstract static class ResourceEnumerator implements Enumeration<Resource> {

        protected Resource next;

        public Resource nextElement() {
            if (hasMoreElements()) {
                Resource resource = next;
                next = null;
                return resource;
            } else {
                throw new NoSuchElementException();
            }
        }
    }

}
