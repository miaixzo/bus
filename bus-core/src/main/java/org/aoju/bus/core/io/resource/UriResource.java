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
package org.aoju.bus.core.io.resource;

import org.aoju.bus.core.exception.InternalException;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.toolkit.FileKit;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.core.toolkit.UriKit;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * URL资源访问类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UriResource implements Resource {

    protected URL url;
    protected String name;
    private long lastModified = 0;

    /**
     * 构造
     */
    public UriResource() {

    }

    /**
     * 构造
     *
     * @param url URL
     */
    public UriResource(URL url) {
        this(url, null);
    }

    /**
     * 构造
     *
     * @param uri URI
     */
    public UriResource(URI uri) {
        this(UriKit.url(uri), null);
    }

    /**
     * 构造
     *
     * @param url  URL,允许为空
     * @param name 资源名称
     */
    public UriResource(URL url, String name) {
        this.url = url;
        this.name = ObjectKit.defaultIfNull(name, () -> (null != url ? FileKit.getName(url.getPath()) : null));
        if (null != url && UriKit.isFileURL(url)) {
            this.lastModified = FileKit.file(url).lastModified();
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public URL getUrl() {
        return this.url;
    }

    @Override
    public InputStream getStream() {
        if (null == this.url) {
            throw new InternalException("Resource URL is null!");
        }
        return UriKit.getStream(url);
    }

    @Override
    public boolean isModified() {
        return (0 != this.lastModified) && this.lastModified != getFile().lastModified();
    }

    /**
     * 获得File
     *
     * @return {@link File}
     */
    public File getFile() {
        return FileKit.file(this.url);
    }

    /**
     * 返回路径
     *
     * @return 返回URL路径
     */
    @Override
    public String toString() {
        return (null == this.url) ? Normal.NULL : this.url.toString();
    }

}
