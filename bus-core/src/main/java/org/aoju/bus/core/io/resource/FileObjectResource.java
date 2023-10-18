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
import org.aoju.bus.core.toolkit.IoKit;

import javax.tools.FileObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * {@link FileObject} 资源包装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileObjectResource implements Resource {

    private final FileObject fileObject;

    /**
     * 构造
     *
     * @param fileObject {@link FileObject}
     */
    public FileObjectResource(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    /**
     * 获取原始的{@link FileObject}
     *
     * @return {@link FileObject}
     */
    public FileObject getFileObject() {
        return this.fileObject;
    }

    @Override
    public String getName() {
        return this.fileObject.getName();
    }

    @Override
    public URL getUrl() {
        try {
            return this.fileObject.toUri().toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public InputStream getStream() {
        try {
            return this.fileObject.openInputStream();
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public BufferedReader getReader(Charset charset) {
        try {
            return IoKit.getReader(this.fileObject.openReader(false));
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

}
