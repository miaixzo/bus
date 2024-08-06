/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import java.io.*;
import java.net.URL;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * {@link CharSequence}资源，字符串做为资源
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CharSequenceResource implements Resource, Serializable {

    private static final long serialVersionUID = -1L;

    private final CharSequence data;
    private final CharSequence name;
    private final java.nio.charset.Charset charset;

    /**
     * 构造，使用UTF8编码
     *
     * @param data 资源数据
     */
    public CharSequenceResource(final CharSequence data) {
        this(data, null);
    }

    /**
     * 构造，使用UTF8编码
     *
     * @param data 资源数据
     * @param name 资源名称
     */
    public CharSequenceResource(final CharSequence data, final String name) {
        this(data, name, Charset.UTF_8);
    }

    /**
     * 构造
     *
     * @param data    资源数据
     * @param name    资源名称
     * @param charset 编码
     */
    public CharSequenceResource(final CharSequence data, final CharSequence name,
            final java.nio.charset.Charset charset) {
        this.data = data;
        this.name = name;
        this.charset = charset;
    }

    @Override
    public String getName() {
        return StringKit.toString(this.name);
    }

    @Override
    public URL getUrl() {
        return null;
    }

    @Override
    public long size() {
        return data.length();
    }

    @Override
    public InputStream getStream() {
        return new ByteArrayInputStream(readBytes());
    }

    @Override
    public BufferedReader getReader(final java.nio.charset.Charset charset) {
        return IoKit.toBuffered(new StringReader(this.data.toString()));
    }

    @Override
    public String readString(final java.nio.charset.Charset charset) throws InternalException {
        return this.data.toString();
    }

    @Override
    public byte[] readBytes() throws InternalException {
        return ByteKit.toBytes(this.data, this.charset);
    }

}
