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
package org.miaixz.bus.core.io.resource;

import java.io.File;
import java.io.InputStream;
import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;

import org.miaixz.bus.core.io.file.FileName;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.UrlKit;

/**
 * URL资源访问类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UrlResource implements Resource, Serializable {

    @Serial
    private static final long serialVersionUID = 2852232772701L;

    /**
     * URL
     */
    protected URL url;
    /**
     * 资源名称
     */
    protected String name;
    private long lastModified = 0;

    /**
     * 构造
     *
     * @param uri URI
     */
    public UrlResource(final URI uri) {
        this(UrlKit.url(uri), null);
    }

    /**
     * 构造
     *
     * @param url URL
     */
    public UrlResource(final URL url) {
        this(url, null);
    }

    /**
     * 构造
     *
     * @param url  URL，允许为空
     * @param name 资源名称
     */
    public UrlResource(final URL url, final String name) {
        this.url = url;
        if (null != url && Normal.URL_PROTOCOL_FILE.equals(url.getProtocol())) {
            this.lastModified = FileKit.file(url).lastModified();
        }
        this.name = ObjectKit.defaultIfNull(name, () -> (null != url ? FileName.getName(url.getPath()) : null));
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
    public long size() {
        return UrlKit.size(this.url);
    }

    @Override
    public InputStream getStream() throws InternalException {
        if (null == this.url) {
            throw new InternalException("Resource URL is null!");
        }
        return UrlKit.getStream(url);
    }

    @Override
    public boolean isModified() {
        // lastModified == 0表示此资源非文件资源
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
        return (null == this.url) ? "null" : this.url.toString();
    }

    /**
     * 获取相对于本资源的资源
     *
     * @param relativePath 相对路径
     * @return 子资源
     */
    public UrlResource createRelative(final String relativePath) {
        return new UrlResource(UrlKit.getURL(getUrl(), relativePath));
    }

}
