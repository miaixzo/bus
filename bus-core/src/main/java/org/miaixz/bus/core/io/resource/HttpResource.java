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
package org.miaixz.bus.core.io.resource;

import org.miaixz.bus.core.lang.Assert;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;

/**
 * HTTP资源，用于自定义表单数据，可自定义Content-Type
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HttpResource implements Resource, Serializable {

    private static final long serialVersionUID = -1L;

    private final Resource resource;
    private final String contentType;

    /**
     * 构造
     *
     * @param resource    资源，非空
     * @param contentType Content-Type类型，{@code null}表示不设置
     */
    public HttpResource(final Resource resource, final String contentType) {
        this.resource = Assert.notNull(resource, "Resource must be not null !");
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        return resource.getName();
    }

    @Override
    public URL getUrl() {
        return resource.getUrl();
    }

    @Override
    public long size() {
        return resource.size();
    }

    @Override
    public InputStream getStream() {
        return resource.getStream();
    }

    /**
     * 获取自定义Content-Type类型
     *
     * @return Content-Type类型
     */
    public String getContentType() {
        return this.contentType;
    }

}
