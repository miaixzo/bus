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
package org.miaixz.bus.core.io.file;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Wrapper;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.ObjectKit;

import java.io.File;
import java.io.Serializable;

/**
 * 文件包装器，扩展文件对象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileWrapper implements Wrapper<File>, Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 被包装的文件
     */
    protected File file;
    /**
     * 编码
     */
    protected java.nio.charset.Charset charset;

    /**
     * 构造
     *
     * @param file    文件（非{@code null}）
     * @param charset 编码，使用 {@link Charset}，传入{@code null}则使用默认编码{@link Charset#UTF_8}
     */
    public FileWrapper(final File file, final java.nio.charset.Charset charset) {
        this.file = Assert.notNull(file);
        this.charset = ObjectKit.defaultIfNull(charset, Charset.UTF_8);
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
    public FileWrapper setFile(final File file) {
        this.file = file;
        return this;
    }

    /**
     * 获得字符集编码
     *
     * @return 编码
     */
    public java.nio.charset.Charset getCharset() {
        return charset;
    }

    /**
     * 设置字符集编码
     *
     * @param charset 编码
     * @return 自身
     */
    public FileWrapper setCharset(final java.nio.charset.Charset charset) {
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
