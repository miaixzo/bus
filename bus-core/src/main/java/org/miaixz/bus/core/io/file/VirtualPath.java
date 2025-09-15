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
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.StringKit;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * 虚拟路径类，实现Path接口，用于在内存中模拟文件路径。
 * <p>
 * 该类提供了一种在内存中表示文件系统路径的方式，无需实际访问物理文件系统。 它可以用于测试、模拟文件系统操作或在内存中处理文件路径。
 * </p>
 * <p>
 * 虚拟路径可以关联一个{@link Resource}对象，表示该路径下的内容资源。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class VirtualPath implements Path {

    /**
     * 路径字符串
     */
    private final String path;

    /**
     * 路径对应的内容资源
     */
    private final Resource content;

    /**
     * 构造一个虚拟路径。
     *
     * @param path    路径字符串，不能为null
     * @param content 路径对应的内容资源，可以为null
     * @throws IllegalArgumentException 如果path为null
     */
    public VirtualPath(final String path, final Resource content) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        this.path = path;
        this.content = content;
    }

    /**
     * 获取路径内容资源。
     *
     * @return 路径对应的内容资源，可能为null
     */
    public Resource getContent() {
        return this.content;
    }

    /**
     * 获取文件内容字节数组。
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
     * 获取与此路径关联的文件系统。
     * <p>
     * 虚拟路径不支持文件系统操作，因此此方法会抛出UnsupportedOperationException。
     * </p>
     *
     * @return 与此路径关联的文件系统
     * @throws UnsupportedOperationException 总是抛出，因为虚拟路径不支持文件系统操作
     */
    @Override
    public FileSystem getFileSystem() {
        throw new UnsupportedOperationException("VirtualPath does not support FileSystem operations");
    }

    /**
     * 判断此路径是否为绝对路径。
     * <p>
     * 虚拟路径总是返回false，表示不是绝对路径。
     * </p>
     *
     * @return 如果此路径是绝对路径则返回true，否则返回false
     */
    @Override
    public boolean isAbsolute() {
        return false;
    }

    /**
     * 获取此路径的根组件。
     * <p>
     * 虚拟路径没有根组件，因此返回null。
     * </p>
     *
     * @return 表示此路径根组件的路径，如果没有根组件则返回null
     */
    @Override
    public Path getRoot() {
        return null;
    }

    /**
     * 获取此路径的文件名组件。
     * <p>
     * 文件名是路径中最远的层次结构组件。如果路径为空，则返回空路径。
     * </p>
     *
     * @return 表示此路径文件名组件的路径
     */
    @Override
    public Path getFileName() {
        final int index = path.lastIndexOf(Symbol.C_SLASH);
        if (index == -1) {
            return new VirtualPath(path, content);
        }
        return new VirtualPath(path.substring(index + 1), content);
    }

    /**
     * 获取此路径的父路径，如果此路径没有父路径则返回null。
     * <p>
     * 父路径由路径的根组件和除最远组件外的所有组件组成。如果此路径没有父路径， 则返回null。
     * </p>
     *
     * @return 表示此路径父路径的路径，如果没有父路径则返回null
     */
    @Override
    public Path getParent() {
        final int index = path.lastIndexOf(Symbol.C_SLASH);
        if (index == -1) {
            return null;
        }
        return new VirtualPath(path.substring(0, index), null);
    }

    /**
     * 获取路径中的名称元素数量。
     * <p>
     * 空路径("")有一个名称元素。根路径("/")没有名称元素。
     * </p>
     *
     * @return 路径中的元素数量，如果路径为空则返回1
     */
    @Override
    public int getNameCount() {
        if (StringKit.isEmpty(path)) {
            // ""表示一个有效名称
            return 1;
        }
        if (StringKit.equals(path, Symbol.SLASH)) {
            // /表示根路径，无名称
            return 0;
        }
        // 根路径不算名称
        return StringKit.count(path, Symbol.SLASH);
    }

    /**
     * 获取指定索引处的名称元素。
     *
     * @param index 名称元素的索引
     * @return 名称元素
     * @throws IllegalArgumentException 如果index为负数或大于等于名称元素数量
     */
    @Override
    public Path getName(final int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index must be >= 0");
        }
        final List<String> parts = CharsBacker.splitTrim(path, Symbol.SLASH);
        if (index >= parts.size()) {
            throw new IllegalArgumentException("index exceeds name count");
        }
        return new VirtualPath(parts.get(index), index == parts.size() - 1 ? content : null);
    }

    /**
     * 获取此路径的子路径，它是从beginIndex到endIndex-1的名称元素的序列。
     *
     * @param beginIndex 第一个元素的索引（包含）
     * @param endIndex   最后一个元素的索引（不包含）
     * @return 新的子路径
     * @throws IllegalArgumentException 如果beginIndex或endIndex为负数、beginIndex大于等于endIndex或endIndex大于名称元素数量
     */
    @Override
    public Path subpath(final int beginIndex, final int endIndex) {
        if (beginIndex < 0 || endIndex <= beginIndex) {
            throw new IllegalArgumentException("beginIndex or endIndex is invalid");
        }
        final List<String> parts = CharsBacker.splitTrim(path, Symbol.SLASH);
        if (endIndex > parts.size()) {
            throw new IllegalArgumentException("endIndex exceeds name count");
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = beginIndex; i < endIndex; i++) {
            if (!sb.isEmpty()) {
                sb.append(Symbol.C_SLASH);
            }
            sb.append(parts.get(i));
        }
        return new VirtualPath(sb.toString(), endIndex == parts.size() ? content : null);
    }

    /**
     * 测试此路径是否以给定路径开头。
     *
     * @param other 要测试的路径
     * @return 如果此路径以给定路径开头则返回true，否则返回false
     * @throws ClassCastException 如果other不是VirtualPath实例
     */
    @Override
    public boolean startsWith(final Path other) {
        if (!(other instanceof final VirtualPath otherPath)) {
            return false;
        }
        return this.path.startsWith(otherPath.path);
    }

    /**
     * 测试此路径是否以给定路径字符串开头。
     *
     * @param other 要测试的路径字符串
     * @return 如果此路径以给定路径字符串开头则返回true，否则返回false
     */
    @Override
    public boolean startsWith(final String other) {
        return this.path.startsWith(other);
    }

    /**
     * 测试此路径是否以给定路径结尾。
     *
     * @param other 要测试的路径
     * @return 如果此路径以给定路径结尾则返回true，否则返回false
     * @throws ClassCastException 如果other不是VirtualPath实例
     */
    @Override
    public boolean endsWith(final Path other) {
        if (!(other instanceof final VirtualPath otherPath)) {
            return false;
        }
        return this.path.endsWith(otherPath.path);
    }

    /**
     * 测试此路径是否以给定路径字符串结尾。
     *
     * @param other 要测试的路径字符串
     * @return 如果此路径以给定路径字符串结尾则返回true，否则返回false
     */
    @Override
    public boolean endsWith(final String other) {
        return this.path.endsWith(other);
    }

    /**
     * 返回此路径的规范化形式。
     * <p>
     * 虚拟路径的规范化形式就是其自身。
     * </p>
     *
     * @return 规范化路径
     */
    @Override
    public Path normalize() {
        return this;
    }

    /**
     * 根据此路径解析给定路径。
     * <p>
     * 如果给定路径是绝对路径，则返回给定路径。否则，返回此路径与给定路径的连接。
     * </p>
     *
     * @param other 要解析的路径
     * @return 结果路径
     */
    @Override
    public Path resolve(final Path other) {
        if (other.isAbsolute()) {
            return other;
        }
        if (other.toString().isEmpty()) {
            return this;
        }
        final String newPath = this.path + "/" + other;
        return new VirtualPath(newPath, other instanceof VirtualPath ? ((VirtualPath) other).content : null);
    }

    /**
     * 根据此路径解析给定路径字符串。
     * <p>
     * 如果给定路径字符串为空，则返回此路径。否则，返回此路径与给定路径字符串的连接。
     * </p>
     *
     * @param other 要解析的路径字符串
     * @return 结果路径
     */
    @Override
    public Path resolve(final String other) {
        if (other.isEmpty()) {
            return this;
        }
        final String newPath = this.path + Symbol.SLASH + other;
        return new VirtualPath(newPath, null);
    }

    /**
     * 根据此路径的父路径解析给定路径。
     * <p>
     * 如果此路径没有父路径，则返回给定路径。否则，返回此路径的父路径与给定路径的解析结果。
     * </p>
     *
     * @param other 要解析的路径
     * @return 结果路径
     * @throws NullPointerException 如果other为null
     */
    @Override
    public Path resolveSibling(final Path other) {
        if (other == null) {
            throw new NullPointerException("other cannot be null");
        }
        final Path parent = getParent();
        return (parent == null) ? other : parent.resolve(other);
    }

    /**
     * 根据此路径的父路径解析给定路径字符串。
     * <p>
     * 如果此路径没有父路径，则返回给定路径字符串表示的路径。否则，返回此路径的父路径与给定路径字符串的解析结果。
     * </p>
     *
     * @param other 要解析的路径字符串
     * @return 结果路径
     * @throws NullPointerException 如果other为null
     */
    @Override
    public Path resolveSibling(final String other) {
        if (other == null) {
            throw new NullPointerException("other cannot be null");
        }
        final Path parent = getParent();
        return (parent == null) ? new VirtualPath(other, null) : parent.resolve(other);
    }

    /**
     * 构造此路径与给定路径之间的相对路径。
     * <p>
     * 如果此路径为空，则返回给定路径。如果给定路径以此路径开头，则返回剩余部分。
     * </p>
     *
     * @param other 要相对化的路径
     * @return 相对路径
     * @throws IllegalArgumentException 如果other不是VirtualPath实例
     */
    @Override
    public Path relativize(final Path other) {
        if (!(other instanceof final VirtualPath otherPath)) {
            throw new IllegalArgumentException("other must be a VirtualPath");
        }
        if (this.path.isEmpty()) {
            return otherPath;
        }
        if (otherPath.path.startsWith(this.path + "/")) {
            return new VirtualPath(otherPath.path.substring(this.path.length() + 1), otherPath.content);
        }
        return otherPath;
    }

    /**
     * 返回表示此路径的URI。
     * <p>
     * 虚拟路径不支持URI转换，因此此方法会抛出UnsupportedOperationException。
     * </p>
     *
     * @return 表示此路径的URI
     * @throws UnsupportedOperationException 总是抛出，因为虚拟路径不支持URI转换
     */
    @Override
    public URI toUri() {
        throw new UnsupportedOperationException("VirtualPath does not support URI conversion");
    }

    /**
     * 返回此路径的绝对路径。
     * <p>
     * 虚拟路径的绝对路径就是其自身。
     * </p>
     *
     * @return 表示此路径的绝对路径
     */
    @Override
    public Path toAbsolutePath() {
        return this;
    }

    /**
     * 返回此路径的实际路径。
     * <p>
     * 虚拟路径的实际路径就是其自身。
     * </p>
     *
     * @param options 链接选项，指示如何处理符号链接
     * @return 表示此路径的实际路径
     */
    @Override
    public Path toRealPath(final LinkOption... options) {
        return this;
    }

    /**
     * 返回表示此路径的File对象。
     *
     * @return 表示此路径的File对象
     */
    @Override
    public File toFile() {
        return new VirtualFile(path, content);
    }

    /**
     * 使用监视服务注册此路径。
     * <p>
     * 虚拟路径不支持监视服务，因此此方法会抛出UnsupportedOperationException。
     * </p>
     *
     * @param watcher   监视服务
     * @param events    要监视的事件
     * @param modifiers 监视修饰符
     * @return 表示此路径注册的监视键
     * @throws UnsupportedOperationException 总是抛出，因为虚拟路径不支持监视服务
     * @throws IOException                   如果发生I/O错误
     */
    @Override
    public WatchKey register(final WatchService watcher, final WatchEvent.Kind<?>[] events,
            final WatchEvent.Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException("VirtualPath does not support watch service");
    }

    /**
     * 使用监视服务注册此路径。
     * <p>
     * 虚拟路径不支持监视服务，因此此方法会抛出UnsupportedOperationException。
     * </p>
     *
     * @param watcher 监视服务
     * @param events  要监视的事件
     * @return 表示此路径注册的监视键
     * @throws UnsupportedOperationException 总是抛出，因为虚拟路径不支持监视服务
     * @throws IOException                   如果发生I/O错误
     */
    @Override
    public WatchKey register(final WatchService watcher, final WatchEvent.Kind<?>... events) throws IOException {
        throw new UnsupportedOperationException("VirtualPath does not support watch service");
    }

    /**
     * 返回此路径的名称元素的迭代器。
     * <p>
     * 迭代器的第一个元素是路径中最接近根目录的元素，最后一个元素是路径中最远离根目录的元素。
     * </p>
     *
     * @return 此路径的名称元素的迭代器
     */
    @Override
    public Iterator<Path> iterator() {
        return new Iterator<>() {
            private int index = 0;
            private final List<String> parts = CharsBacker.splitTrim(path, Symbol.SLASH);

            @Override
            public boolean hasNext() {
                return index < parts.size();
            }

            @Override
            public Path next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return new VirtualPath(parts.get(index++), index == parts.size() ? content : null);
            }
        };
    }

    /**
     * 比较此路径与给定路径的字典顺序。
     *
     * @param other 要比较的路径
     * @return 如果此路径等于给定路径则返回0；如果此路径小于给定路径则返回小于0的值；如果此路径大于给定路径则返回大于0的值
     * @throws ClassCastException 如果other不是VirtualPath实例
     */
    @Override
    public int compareTo(final Path other) {
        if (!(other instanceof final VirtualPath otherPath)) {
            throw new ClassCastException("Cannot compare VirtualPath with " + other.getClass().getName());
        }
        return this.path.compareTo(otherPath.path);
    }

    /**
     * 测试此路径与给定对象是否相等。
     * <p>
     * 如果给定对象也是VirtualPath，并且两个路径的字符串表示形式相同，则返回true。
     * </p>
     *
     * @param other 要比较的对象
     * @return 如果给定对象也是VirtualPath，并且两个路径的字符串表示形式相同则返回true，否则返回false
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof final VirtualPath otherPath)) {
            return false;
        }
        return this.path.equals(otherPath.path);
    }

    /**
     * 返回此路径的哈希码。
     * <p>
     * 哈希码基于路径字符串的哈希码。
     * </p>
     *
     * @return 此路径的哈希码
     */
    @Override
    public int hashCode() {
        return path.hashCode();
    }

    /**
     * 返回此路径的字符串表示形式。
     * <p>
     * 字符串表示形式就是路径字符串。
     * </p>
     *
     * @return 此路径的字符串表示形式
     */
    @Override
    public String toString() {
        return path;
    }

}