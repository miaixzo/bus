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

import org.miaixz.bus.core.io.stream.ReaderInputStream;
import org.miaixz.bus.core.lang.exception.InternalException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * 基于{@link InputStream}的资源获取器
 * 注意：此对象中getUrl方法始终返回null
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class InputStreamResource implements Resource, Serializable {

    private static final long serialVersionUID = -1L;

    private final InputStream in;
    private final String name;

    /**
     * 构造
     *
     * @param reader  {@link Reader}
     * @param charset 编码
     */
    public InputStreamResource(final Reader reader, final Charset charset) {
        this(new ReaderInputStream(reader, charset));
    }

    /**
     * 构造
     *
     * @param in {@link InputStream}
     */
    public InputStreamResource(final InputStream in) {
        this(in, null);
    }

    /**
     * 构造
     *
     * @param in   {@link InputStream}
     * @param name 资源名称
     */
    public InputStreamResource(final InputStream in, final String name) {
        this.in = in;
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public URL getUrl() {
        return null;
    }

    @Override
    public long size() {
        try {
            return this.in.available();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public InputStream getStream() {
        return this.in;
    }

}
