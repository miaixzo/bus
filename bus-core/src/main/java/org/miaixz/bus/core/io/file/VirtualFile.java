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
package org.miaixz.bus.core.io.file;

import org.miaixz.bus.core.io.resource.Resource;

import java.io.File;
import java.io.Serial;

/**
 * 虚拟文件类，继承自File，用于在内存中模拟文件。
 * <p>
 * 该类提供了一种在内存中表示文件的方式，无需实际访问物理文件系统。 它可以用于测试、模拟文件操作或在内存中处理文件内容。
 * </p>
 * <p>
 * 虚拟文件可以关联一个{@link Resource}对象，表示文件的内容资源。 如果内容资源为null，则表示文件不存在。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class VirtualFile extends File {

    @Serial
    private static final long serialVersionUID = 2852285580251L;

    /**
     * 文件内容资源
     */
    private final Resource content;

    /**
     * 构造一个虚拟文件。
     *
     * @param pathname 文件路径，不能为null
     * @param content  文件内容资源，可以为null（表示文件不存在）
     * @throws IllegalArgumentException 如果pathname为null
     */
    public VirtualFile(final String pathname, final Resource content) {
        super(pathname);
        if (pathname == null) {
            throw new IllegalArgumentException("pathname cannot be null");
        }
        this.content = content;
    }

    /**
     * 构造一个虚拟文件。
     *
     * @param parent  父路径字符串，不能为null
     * @param child   子文件名，不能为null
     * @param content 文件内容资源，可以为null（表示文件不存在）
     * @throws IllegalArgumentException 如果parent或child为null
     */
    public VirtualFile(final String parent, final String child, final Resource content) {
        super(parent, child);
        if (parent == null || child == null) {
            throw new IllegalArgumentException("parent and child cannot be null");
        }
        this.content = content;
    }

    /**
     * 构造一个虚拟文件。
     *
     * @param parent  父文件对象，不能为null
     * @param child   子文件名，不能为null
     * @param content 文件内容资源，可以为null（表示文件不存在）
     * @throws IllegalArgumentException 如果parent或child为null
     */
    public VirtualFile(final File parent, final String child, final Resource content) {
        super(parent, child);
        if (parent == null || child == null) {
            throw new IllegalArgumentException("parent and child cannot be null");
        }
        this.content = content;
    }

    /**
     * 获取文件内容资源。
     *
     * @return 文件内容资源，可能为null（表示文件不存在）
     */
    public Resource getContent() {
        return this.content;
    }

    /**
     * 获取文件内容的字节数组。
     * <p>
     * 如果内容资源不为null，则返回资源内容的字节数组；否则返回null表示无此文件。
     * </p>
     *
     * @return 文件内容的字节数组，如果没有内容则返回null
     */
    public byte[] getBytes() {
        return null != this.content ? this.content.readBytes() : null;
    }

    /**
     * 测试此文件是否存在。
     * <p>
     * 如果内容资源不为null，则返回true表示文件存在；否则返回false。
     * </p>
     *
     * @return 如果文件存在则返回true，否则返回false
     */
    @Override
    public boolean exists() {
        return null != this.content;
    }

    /**
     * 测试此文件是否为普通文件。
     * <p>
     * 虚拟文件总是返回true，表示是普通文件。
     * </p>
     *
     * @return 如果文件是普通文件则返回true
     */
    @Override
    public boolean isFile() {
        return true;
    }

    /**
     * 测试此文件是否为目录。
     * <p>
     * 虚拟文件总是返回false，表示不是目录。
     * </p>
     *
     * @return 如果文件是目录则返回true，否则返回false
     */
    @Override
    public boolean isDirectory() {
        return false;
    }

    /**
     * 获取文件长度（字节数）。
     * <p>
     * 如果内容资源不为null，则返回资源内容的大小；否则返回0。
     * </p>
     *
     * @return 文件长度（字节数），如果文件不存在则返回0
     */
    @Override
    public long length() {
        return null != this.content ? this.content.size() : 0L;
    }

    /**
     * 测试应用程序是否可以读取此文件。
     * <p>
     * 如果文件存在（即内容资源不为null），则返回true；否则返回false。
     * </p>
     *
     * @return 如果应用程序可以读取此文件则返回true，否则返回false
     */
    @Override
    public boolean canRead() {
        return exists();
    }

    /**
     * 测试应用程序是否可以修改此文件。
     * <p>
     * 虚拟文件总是返回false，表示不可写。
     * </p>
     *
     * @return 如果应用程序可以修改此文件则返回true，否则返回false
     */
    @Override
    public boolean canWrite() {
        return false;
    }

    /**
     * 测试应用程序是否可以执行此文件。
     * <p>
     * 虚拟文件总是返回false，表示不可执行。
     * </p>
     *
     * @return 如果应用程序可以执行此文件则返回true，否则返回false
     */
    @Override
    public boolean canExecute() {
        return false;
    }

    /**
     * 获取文件最后修改时间。
     * <p>
     * 虚拟文件总是返回当前系统时间。
     * </p>
     *
     * @return 文件最后修改时间（自1970年1月1日UTC以来的毫秒数）
     */
    @Override
    public long lastModified() {
        return System.currentTimeMillis();
    }

}
