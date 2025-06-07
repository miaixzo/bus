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
import java.net.URL;
import java.nio.file.Path;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.NotFoundException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.UrlKit;

/**
 * 文件资源访问对象，支持{@link Path} 和 {@link File} 访问
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileResource implements Resource, Serializable {

    @Serial
    private static final long serialVersionUID = 2852230925613L;

    private final File file;
    private final long lastModified;
    private final String name;

    /**
     * 构造
     *
     * @param path 文件绝对路径或相对ClassPath路径，但是这个路径不能指向一个jar包中的文件
     */
    public FileResource(final String path) {
        this(FileKit.file(path));
    }

    /**
     * 构造，文件名使用文件本身的名字，带扩展名
     *
     * @param path 文件
     */
    public FileResource(final Path path) {
        this(path.toFile());
    }

    /**
     * 构造，文件名使用文件本身的名字，带扩展名
     *
     * @param file 文件
     */
    public FileResource(final File file) {
        this(file, null);
    }

    /**
     * 构造
     *
     * @param file     文件
     * @param fileName 文件名，带扩展名，如果为null获取文件本身的文件名
     */
    public FileResource(final File file, final String fileName) {
        this.file = Assert.notNull(file, "File must be not null !");
        this.lastModified = file.lastModified();
        this.name = ObjectKit.defaultIfNull(fileName, file::getName);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public URL getUrl() {
        return UrlKit.getURL(this.file);
    }

    @Override
    public long size() {
        return this.file.length();
    }

    @Override
    public InputStream getStream() throws NotFoundException {
        if (!exists()) {
            throw new NotFoundException("File [{}] not exist!", this.file.getAbsolutePath());
        }
        return FileKit.getInputStream(this.file);
    }

    /**
     * 获取文件
     *
     * @return 文件
     */
    public File getFile() {
        return this.file;
    }

    /**
     * 文件是否存在
     *
     * @return 是否存在
     */
    public boolean exists() {
        return this.file.exists();
    }

    @Override
    public boolean isModified() {
        return this.lastModified != file.lastModified();
    }

    /**
     * 返回路径
     *
     * @return 返回URL路径
     */
    @Override
    public String toString() {
        return this.file.toString();
    }

}
