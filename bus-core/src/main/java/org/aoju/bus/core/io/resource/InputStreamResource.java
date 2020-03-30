/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
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
 ********************************************************************************/
package org.aoju.bus.core.io.resource;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.IoUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * 基于{@link InputStream}的资源获取器
 * 注意：此对象中getUrl方法始终返回null
 *
 * @author Kimi Liu
 * @version 5.8.1
 * @since JDK 1.8+
 */
public class InputStreamResource implements Resource {

    private InputStream in;
    private String name;

    /**
     * 构造
     *
     * @param in {@link InputStream}
     */
    public InputStreamResource(InputStream in) {
        this(in, null);
    }

    /**
     * 构造
     *
     * @param in   {@link InputStream}
     * @param name 资源名称
     */
    public InputStreamResource(InputStream in, String name) {
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
    public InputStream getStream() {
        return this.in;
    }

    @Override
    public BufferedReader getReader(Charset charset) {
        return IoUtils.getReader(this.in, charset);
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
        return readStr(org.aoju.bus.core.lang.Charset.UTF_8);
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

}
