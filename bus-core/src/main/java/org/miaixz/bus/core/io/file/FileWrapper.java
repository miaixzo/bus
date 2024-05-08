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
package org.miaixz.bus.core.io.file;

import org.miaixz.bus.core.lang.Wrapper;
import org.miaixz.bus.core.toolkit.FileKit;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;

/**
 * 文件包装器,扩展文件对象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileWrapper implements Wrapper<File>, Serializable {

    /**
     * 默认编码：UTF-8
     */
    public static final Charset DEFAULT_CHARSET = org.miaixz.bus.core.lang.Charset.UTF_8;
    protected File file;
    protected Charset charset;

    /**
     * 构造
     *
     * @param file    文件
     * @param charset 编码
     */
    public FileWrapper(File file, Charset charset) {
        this.file = file;
        this.charset = charset;
    }

    /**
     * 获得文件
     *
     * @return 文件
     */
    @Override
    public File getRaw() {
        return file;
    }

    /**
     * 设置文件
     *
     * @param file 文件
     * @return 自身
     */
    public FileWrapper setFile(File file) {
        this.file = file;
        return this;
    }

    /**
     * 获得字符集编码
     *
     * @return 编码
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * 设置字符集编码
     *
     * @param charset 编码
     * @return 自身
     */
    public FileWrapper setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * 可读的文件大小
     *
     * @return 大小
     */
    public String readableFileSize() {
        return FileKit.readableFileSize(file.length());
    }

}
