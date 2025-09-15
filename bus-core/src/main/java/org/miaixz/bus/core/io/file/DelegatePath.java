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
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.wrapper.SimpleWrapper;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 代理路径类，实现Path接口，用于代理一个实际的Path对象，并提供对Files类功能的访问。
 * <p>
 * 该类包装了一个Path对象，并提供了与Path接口相同的所有方法，同时添加了许多便捷方法来访问Files类的功能。 所有方法调用都会被委托给内部的Path对象，而返回的Path对象会被包装为DelegatePath实例。
 * </p>
 * <p>
 * 该类还实现了Resource接口，使其可以作为资源对象使用，提供了读取文件内容、获取流等便捷方法。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DelegatePath extends SimpleWrapper<Path> implements Path, Resource {

    /**
     * 构造一个代理路径。
     *
     * @param first 路径的第一个元素，不能为null
     * @param more  更多路径元素，可以为null
     * @throws IllegalArgumentException 如果first为null
     */
    public DelegatePath(final String first, final String... more) {
        this(Paths.get(first, more));
    }

    /**
     * 构造一个代理路径。
     *
     * @param uri URI路径，不能为null
     * @throws IllegalArgumentException    如果uri为null
     * @throws FileSystemNotFoundException 如果uri标识的文件系统不存在
     * @throws SecurityException           如果安全管理器拒绝访问文件系统
     */
    public DelegatePath(final URI uri) {
        this(Paths.get(uri));
    }

    /**
     * 构造一个代理路径。
     *
     * @param path 被代理的路径对象，不能为null
     * @throws IllegalArgumentException 如果path为null
     */
    public DelegatePath(final Path path) {
        super(path);
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
    }

    /**
     * 获取被代理的路径对象。
     *
     * @return 被代理的路径对象
     */
    public Path getRawPath() {
        return this.raw;
    }

    /**
     * 返回创建此路径的文件系统。
     *
     * @return 创建此路径的文件系统
     */
    @Override
    public FileSystem getFileSystem() {
        return raw.getFileSystem();
    }

    /**
     * 测试此路径是否为绝对路径。
     *
     * @return 如果此路径是绝对路径则返回true，否则返回false
     */
    @Override
    public boolean isAbsolute() {
        return raw.isAbsolute();
    }

    /**
     * 返回此路径的根组件，如果此路径没有根组件则返回null。
     *
     * @return 表示此路径根组件的路径，如果没有根组件则返回null
     */
    @Override
    public Path getRoot() {
        final Path root = raw.getRoot();
        return root == null ? null : new DelegatePath(root);
    }

    /**
     * 返回此路径的文件名组件，如果此路径没有文件名组件则返回null。
     *
     * @return 表示此路径文件名组件的路径，如果没有文件名组件则返回null
     */
    @Override
    public Path getFileName() {
        final Path fileName = raw.getFileName();
        return fileName == null ? null : new DelegatePath(fileName);
    }

    /**
     * 返回此路径的父路径，如果此路径没有父路径则返回null。
     *
     * @return 表示此路径父路径的路径，如果没有父路径则返回null
     */
    @Override
    public Path getParent() {
        final Path parent = raw.getParent();
        return parent == null ? null : new DelegatePath(parent);
    }

    /**
     * 返回路径中的名称元素数量。
     *
     * @return 路径中的元素数量，如果路径为空则返回0
     */
    @Override
    public int getNameCount() {
        return raw.getNameCount();
    }

    /**
     * 返回指定索引处的名称元素。
     *
     * @param index 名称元素的索引
     * @return 名称元素
     * @throws IllegalArgumentException 如果index为负数或大于等于名称元素数量
     */
    @Override
    public Path getName(final int index) {
        return new DelegatePath(raw.getName(index));
    }

    /**
     * 返回此路径的子路径，它是从beginIndex到endIndex-1的名称元素的序列。
     *
     * @param beginIndex 第一个元素的索引（包含）
     * @param endIndex   最后一个元素的索引（不包含）
     * @return 新的子路径
     * @throws IllegalArgumentException 如果beginIndex或endIndex为负数、beginIndex大于等于endIndex或endIndex大于名称元素数量
     */
    @Override
    public Path subpath(final int beginIndex, final int endIndex) {
        return new DelegatePath(raw.subpath(beginIndex, endIndex));
    }

    /**
     * 测试此路径是否以给定路径开头。
     *
     * @param other 要测试的路径
     * @return 如果此路径以给定路径开头则返回true，否则返回false
     */
    @Override
    public boolean startsWith(final Path other) {
        if (other instanceof DelegatePath) {
            return raw.startsWith(((DelegatePath) other).raw);
        }
        return raw.startsWith(other);
    }

    /**
     * 测试此路径是否以给定路径字符串开头。
     *
     * @param other 要测试的路径字符串
     * @return 如果此路径以给定路径字符串开头则返回true，否则返回false
     */
    @Override
    public boolean startsWith(final String other) {
        return raw.startsWith(other);
    }

    /**
     * 测试此路径是否以给定路径结尾。
     *
     * @param other 要测试的路径
     * @return 如果此路径以给定路径结尾则返回true，否则返回false
     */
    @Override
    public boolean endsWith(final Path other) {
        if (other instanceof DelegatePath) {
            return raw.endsWith(((DelegatePath) other).raw);
        }
        return raw.endsWith(other);
    }

    /**
     * 测试此路径是否以给定路径字符串结尾。
     *
     * @param other 要测试的路径字符串
     * @return 如果此路径以给定路径字符串结尾则返回true，否则返回false
     */
    @Override
    public boolean endsWith(final String other) {
        return raw.endsWith(other);
    }

    /**
     * 返回此路径的规范化形式。
     *
     * @return 规范化路径
     */
    @Override
    public Path normalize() {
        return new DelegatePath(raw.normalize());
    }

    /**
     * 根据此路径解析给定路径。
     *
     * @param other 要解析的路径
     * @return 结果路径
     */
    @Override
    public Path resolve(final Path other) {
        if (other instanceof DelegatePath) {
            return new DelegatePath(raw.resolve(((DelegatePath) other).raw));
        }
        return new DelegatePath(raw.resolve(other));
    }

    /**
     * 根据此路径解析给定路径字符串。
     *
     * @param other 要解析的路径字符串
     * @return 结果路径
     */
    @Override
    public Path resolve(final String other) {
        return new DelegatePath(raw.resolve(other));
    }

    /**
     * 根据此路径的父路径解析给定路径。
     *
     * @param other 要解析的路径
     * @return 结果路径
     */
    @Override
    public Path resolveSibling(final Path other) {
        if (other instanceof DelegatePath) {
            return new DelegatePath(raw.resolveSibling(((DelegatePath) other).raw));
        }
        return new DelegatePath(raw.resolveSibling(other));
    }

    /**
     * 根据此路径的父路径解析给定路径字符串。
     *
     * @param other 要解析的路径字符串
     * @return 结果路径
     */
    @Override
    public Path resolveSibling(final String other) {
        return new DelegatePath(raw.resolveSibling(other));
    }

    /**
     * 构造此路径与给定路径之间的相对路径。
     *
     * @param other 要相对化的路径
     * @return 相对路径
     */
    @Override
    public Path relativize(final Path other) {
        if (other instanceof DelegatePath) {
            return new DelegatePath(raw.relativize(((DelegatePath) other).raw));
        }
        return new DelegatePath(raw.relativize(other));
    }

    /**
     * 返回表示此路径的URI。
     *
     * @return 表示此路径的URI
     * @throws IOError 如果在获取URI时发生I/O错误
     */
    @Override
    public URI toUri() {
        return raw.toUri();
    }

    /**
     * 返回此路径的绝对路径。
     *
     * @return 表示此路径的绝对路径
     * @throws IOError 如果在获取绝对路径时发生I/O错误
     */
    @Override
    public Path toAbsolutePath() {
        return new DelegatePath(raw.toAbsolutePath());
    }

    /**
     * 返回此路径的实际路径。
     *
     * @param options 链接选项，指示如何处理符号链接
     * @return 表示此路径的实际路径
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public Path toRealPath(final LinkOption... options) throws IOException {
        return new DelegatePath(raw.toRealPath(options));
    }

    /**
     * 返回表示此路径的File对象。
     *
     * @return 表示此路径的File对象
     */
    @Override
    public File toFile() {
        return raw.toFile();
    }

    /**
     * 使用监视服务注册此路径。
     *
     * @param watcher   监视服务
     * @param events    要监视的事件
     * @param modifiers 监视修饰符
     * @return 表示此路径注册的监视键
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public WatchKey register(final WatchService watcher, final WatchEvent.Kind<?>[] events,
            final WatchEvent.Modifier... modifiers) throws IOException {
        return raw.register(watcher, events, modifiers);
    }

    /**
     * 使用监视服务注册此路径。
     *
     * @param watcher 监视服务
     * @param events  要监视的事件
     * @return 表示此路径注册的监视键
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public WatchKey register(final WatchService watcher, final WatchEvent.Kind<?>... events) throws IOException {
        return raw.register(watcher, events);
    }

    /**
     * 返回此路径的名称元素的迭代器。
     *
     * @return 此路径的名称元素的迭代器
     */
    @Override
    public Iterator<Path> iterator() {
        return new Iterator<>() {
            private final Iterator<Path> itr = raw.iterator();

            @Override
            public boolean hasNext() {
                return itr.hasNext();
            }

            @Override
            public Path next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return new DelegatePath(itr.next());
            }
        };
    }

    /**
     * 比较此路径与给定路径的字典顺序。
     *
     * @param other 要比较的路径
     * @return 如果此路径等于给定路径则返回0；如果此路径小于给定路径则返回小于0的值；如果此路径大于给定路径则返回大于0的值
     */
    @Override
    public int compareTo(final Path other) {
        if (other instanceof DelegatePath) {
            return raw.compareTo(((DelegatePath) other).raw);
        }
        return raw.compareTo(other);
    }

    /**
     * 测试此路径与给定对象是否相等。
     *
     * @param other 要比较的对象
     * @return 如果给定对象也是Path，并且两个路径相等则返回true，否则返回false
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof DelegatePath) {
            return raw.equals(((DelegatePath) other).raw);
        }
        if (other instanceof Path) {
            return raw.equals(other);
        }
        return false;
    }

    /**
     * 返回此路径的哈希码。
     *
     * @return 此路径的哈希码
     */
    @Override
    public int hashCode() {
        return raw.hashCode();
    }

    /**
     * 返回此路径的字符串表示形式。
     *
     * @return 此路径的字符串表示形式
     */
    @Override
    public String toString() {
        return raw.toString();
    }

    // 添加对Files类功能的便捷访问方法

    /**
     * 检查文件是否存在。
     *
     * @param options 检查选项
     * @return 文件是否存在
     * @see Files#exists(Path, LinkOption...)
     */
    public boolean exists(final LinkOption... options) {
        return Files.exists(raw, options);
    }

    /**
     * 检查文件是否为给定文件的子文件。
     *
     * @param parent 父文件
     * @return 是否为子文件
     */
    public boolean isSubOf(final Path parent) {
        return PathResolve.isSub(parent, this.raw);
    }

    /**
     * 检查文件是否不存在。
     *
     * @param options 检查选项
     * @return 文件是否不存在
     * @see Files#notExists(Path, LinkOption...)
     */
    public boolean notExists(final LinkOption... options) {
        return Files.notExists(raw, options);
    }

    /**
     * 检查文件是否为目录。
     *
     * @param options 检查选项
     * @return 是否为目录
     * @see Files#isDirectory(Path, LinkOption...)
     */
    public boolean isDirectory(final LinkOption... options) {
        return Files.isDirectory(raw, options);
    }

    /**
     * 检查文件是否为普通文件。
     *
     * @param options 检查选项
     * @return 是否为普通文件
     * @see Files#isRegularFile(Path, LinkOption...)
     */
    public boolean isFile(final LinkOption... options) {
        return Files.isRegularFile(raw, options);
    }

    /**
     * 检查文件是否为符号链接。
     *
     * @return 是否为符号链接
     * @see Files#isSymbolicLink(Path)
     */
    public boolean isSymbolicLink() {
        return Files.isSymbolicLink(raw);
    }

    /**
     * 判断是否为其它类型文件，即非文件、非目录、非链接。
     *
     * @return 是否为其他类型
     */
    public boolean isOther() {
        return PathResolve.isOther(this.raw);
    }

    /**
     * 检查文件是否可执行。
     *
     * @return 是否可执行
     * @see Files#isExecutable(Path)
     */
    public boolean isExecutable() {
        return Files.isExecutable(raw);
    }

    /**
     * 检查文件是否可读。
     *
     * @return 是否可读
     * @see Files#isReadable(Path)
     */
    public boolean isReadable() {
        return Files.isReadable(raw);
    }

    /**
     * 检查文件是否可写。
     *
     * @return 是否可写
     * @see Files#isWritable(Path)
     */
    public boolean isWritable() {
        return Files.isWritable(raw);
    }

    /**
     * 获取文件大小。
     *
     * @return 文件大小（字节数）
     * @throws InternalException 如果发生I/O错误
     * @see Files#size(Path)
     */
    @Override
    public long size() {
        try {
            return Files.size(raw);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取文件名。
     *
     * @return 文件名
     */
    @Override
    public String getName() {
        return PathResolve.getName(this.raw);
    }

    /**
     * 删除文件或目录。
     *
     * @throws InternalException 如果发生I/O错误
     * @see Files#delete(Path)
     */
    public void delete() {
        try {
            Files.delete(raw);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 如果存在则删除文件或目录。
     *
     * @return 是否删除成功
     * @throws InternalException 如果发生I/O错误
     * @see Files#deleteIfExists(Path)
     */
    public boolean deleteIfExists() {
        try {
            return Files.deleteIfExists(raw);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 创建目录。
     *
     * @param attrs 文件属性
     * @return 创建的目录路径
     * @throws InternalException 如果发生I/O错误
     * @see Files#createDirectory(Path, FileAttribute[])
     */
    public DelegatePath createDirectory(final FileAttribute<?>... attrs) {
        try {
            return new DelegatePath(Files.createDirectory(raw, attrs));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 创建目录（包括必要时的父目录）。
     *
     * @param attrs 文件属性
     * @return 创建的目录路径
     * @throws InternalException 如果发生I/O错误
     * @see Files#createDirectories(Path, FileAttribute[])
     */
    public DelegatePath createDirectories(final FileAttribute<?>... attrs) {
        try {
            return new DelegatePath(Files.createDirectories(raw, attrs));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 创建文件。
     *
     * @param attrs 文件属性
     * @return 创建的文件路径
     * @throws InternalException 如果发生I/O错误
     * @see Files#createFile(Path, FileAttribute[])
     */
    public DelegatePath createFile(final FileAttribute<?>... attrs) {
        try {
            return new DelegatePath(Files.createFile(raw, attrs));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 创建临时目录。
     *
     * @param prefix 前缀
     * @param attrs  文件属性
     * @return 创建的临时目录路径
     * @throws InternalException 如果发生I/O错误
     * @see Files#createTempDirectory(Path, String, FileAttribute[])
     */
    public DelegatePath createTempDirectory(final String prefix, final FileAttribute<?>... attrs) {
        try {
            return new DelegatePath(Files.createTempDirectory(raw, prefix, attrs));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 创建临时文件。
     *
     * @param prefix 前缀
     * @param suffix 后缀
     * @param attrs  文件属性
     * @return 创建的临时文件路径
     * @throws InternalException 如果发生I/O错误
     * @see Files#createTempFile(Path, String, String, FileAttribute[])
     */
    public DelegatePath createTempFile(final String prefix, final String suffix, final FileAttribute<?>... attrs) {
        try {
            return new DelegatePath(Files.createTempFile(raw, prefix, suffix, attrs));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 复制文件。
     *
     * @param target  目标路径
     * @param options 复制选项
     * @return 目标路径
     * @throws InternalException 如果发生I/O错误
     * @see Files#copy(Path, Path, CopyOption...)
     */
    public DelegatePath copyTo(final Path target, final CopyOption... options) {
        Path actualTarget = target;
        if (target instanceof DelegatePath) {
            actualTarget = ((DelegatePath) target).raw;
        }
        try {
            Files.copy(raw, actualTarget, options);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return new DelegatePath(actualTarget);
    }

    /**
     * 移动文件。
     *
     * @param target  目标路径
     * @param options 移动选项
     * @return 目标路径
     * @throws InternalException 如果发生I/O错误
     * @see Files#move(Path, Path, CopyOption...)
     */
    public DelegatePath moveTo(final Path target, final CopyOption... options) {
        Path actualTarget = target;
        if (target instanceof DelegatePath) {
            actualTarget = ((DelegatePath) target).raw;
        }
        try {
            Files.move(raw, actualTarget, options);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return new DelegatePath(actualTarget);
    }

    /**
     * 判断文件是否为空目录。
     *
     * @return 是否为空目录
     */
    public boolean isDirEmpty() {
        return PathResolve.isDirEmpty(this);
    }

    /**
     * 列出目录中的所有文件（不会递归子目录）。
     *
     * @param filter 文件过滤器，null表示不过滤，返回所有文件
     * @return 文件列表（包含目录）
     */
    public Path[] listFiles(final Predicate<? super Path> filter) {
        return PathResolve.listFiles(this, filter);
    }

    /**
     * 遍历目录中的所有文件（不会递归子目录）。
     *
     * @param options  访问选项
     * @param maxDepth 最大深度
     * @param visitor  {@link FileVisitor} 接口，用于自定义在访问文件时，访问目录前后等节点做的操作
     * @throws InternalException 如果发生I/O错误
     */
    public void walkFiles(final Set<FileVisitOption> options, final int maxDepth,
            final FileVisitor<? super Path> visitor) {
        try {
            Files.walkFileTree(this.raw, options, maxDepth, visitor);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取文件属性。
     *
     * @param options 链接选项
     * @return 文件属性
     */
    public BasicFileAttributes getAttributes(final LinkOption... options) {
        return PathResolve.getAttributes(this.raw, options);
    }

    /**
     * 获取文件输入流。
     *
     * @param options 链接选项
     * @return 文件输入流
     */
    public BufferedInputStream getStream(final LinkOption... options) {
        return PathResolve.getInputStream(this, options);
    }

    /**
     * 获取文件输入流。
     *
     * @return 文件输入流
     */
    @Override
    public InputStream getStream() {
        return getStream(new LinkOption[0]);
    }

    /**
     * 获取文件的URL。
     *
     * @return 文件的URL
     * @throws InternalException 如果URL格式错误
     */
    @Override
    public URL getUrl() {
        try {
            return this.raw.toUri().toURL();
        } catch (final MalformedURLException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取文件字符输入流。
     *
     * @param charset 字符集
     * @param options 链接选项
     * @return 文件字符输入流
     */
    public Reader getReader(final Charset charset, final OpenOption... options) {
        return PathResolve.getReader(this, charset, options);
    }

    /**
     * 读取文件内容为字节数组。
     *
     * @return 文件内容的字节数组
     * @throws InternalException 如果发生I/O错误
     */
    @Override
    public byte[] readBytes() {
        return PathResolve.readBytes(this);
    }

    /**
     * 获取文件输出流。
     *
     * @param options 链接选项
     * @return 文件输出流
     */
    public BufferedOutputStream getOutputStream(final OpenOption... options) {
        return PathResolve.getOutputStream(this, options);
    }

    /**
     * 获取文件的MIME类型。
     *
     * @return MIME类型
     */
    public String getMimeType() {
        return PathResolve.getMimeType(this);
    }

}