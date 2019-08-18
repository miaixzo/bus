/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.io.resource;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.FileUtils;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.UriUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * URL资源访问类
 *
 * @author Kimi Liu
 * @version 3.0.9
 * @since JDK 1.8
 */
public class UriResource implements Resource {

    protected URL url;
    protected String name;

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
     * @param url  URL，允许为空
     * @param name 资源名称
     */
    public UriResource(URL url, String name) {
        this.url = url;
        this.name = ObjectUtils.defaultIfNull(name, (null != url) ? FileUtils.getName(url.getPath()) : null);
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
            throw new InstrumentException("Resource [{" + this.url + "}] not exist!");
        }
        return UriUtils.getStream(url);
    }

    /**
     * 获得Reader
     *
     * @param charset 编码
     * @return {@link BufferedReader}
     * @since 3.0.1
     */
    public BufferedReader getReader(Charset charset) {
        return UriUtils.getReader(this.url, charset);
    }

    @Override
    public String readStr(Charset charset) throws InstrumentException {
        BufferedReader reader = null;
        try {
            reader = getReader(charset);
            return IoUtils.read(reader);
        } finally {
            IoUtils.close(reader);
        }
    }

    @Override
    public String readUtf8Str() throws InstrumentException {
        return readStr(org.aoju.bus.core.consts.Charset.UTF_8);
    }

    @Override
    public byte[] readBytes() throws InstrumentException {
        InputStream in = null;
        try {
            in = getStream();
            return IoUtils.readBytes(in);
        } finally {
            IoUtils.close(in);
        }
    }

    /**
     * 获得File
     *
     * @return {@link File}
     */
    public File getFile() {
        return FileUtils.file(this.url);
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

}
