/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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

import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.IoKit;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;

/**
 * 字符串资源,字符串做为资源
 *
 * @author Kimi Liu
 * @version 6.1.2
 * @since JDK 1.8+
 */
public class StringResource implements Resource {

    private final String data;
    private final String name;
    private final java.nio.charset.Charset charset;

    /**
     * 构造,使用UTF8编码
     *
     * @param data 资源数据
     */
    public StringResource(String data) {
        this(data, null);
    }

    /**
     * 构造,使用UTF8编码
     *
     * @param data 资源数据
     * @param name 资源名称
     */
    public StringResource(String data, String name) {
        this(data, name, Charset.UTF_8);
    }

    /**
     * 构造
     *
     * @param data    资源数据
     * @param name    资源名称
     * @param charset 编码
     */
    public StringResource(String data, String name, java.nio.charset.Charset charset) {
        this.data = data;
        this.name = name;
        this.charset = charset;
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
    public InputStream getStream() {
        return new ByteArrayInputStream(readBytes());
    }

    @Override
    public BufferedReader getReader(java.nio.charset.Charset charset) {
        return IoKit.getReader(new StringReader(this.data));
    }

    @Override
    public String readString(java.nio.charset.Charset charset) throws InstrumentException {
        return this.data;
    }

    @Override
    public byte[] readBytes() throws InstrumentException {
        return this.data.getBytes(this.charset);
    }

}
