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

import org.miaixz.bus.core.io.file.visitor.DeleteVisitor;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * 文件删除封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PathDeleter {

    private final Path path;

    /**
     * 构造
     *
     * @param path 文件或目录，不能为{@code null}且必须存在
     */
    public PathDeleter(final Path path) {
        this.path = Assert.notNull(path, "Path must be not null !");
    }

    /**
     * 创建文件或目录移动器
     *
     * @param src 源文件或目录
     * @return {@code PathMover}
     */
    public static PathDeleter of(final Path src) {
        return new PathDeleter(src);
    }

    /**
     * 删除目录
     *
     * @param path 目录路径
     */
    private static void _del(final Path path) {
        try {
            Files.walkFileTree(path, DeleteVisitor.INSTANCE);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 删除文件或空目录，不追踪软链
     *
     * @param path 文件对象
     * @throws InternalException IO异常
     */
    private static void delFile(final Path path) throws InternalException {
        try {
            Files.delete(path);
        } catch (final IOException e) {
            if (e instanceof AccessDeniedException) {
                // 可能遇到只读文件，无法删除.使用 file 方法删除
                if (path.toFile().delete()) {
                    return;
                }
            }
            throw new InternalException(e);
        }
    }

    /**
     * 删除文件或者文件夹，不追踪软链
     * 注意：删除文件夹时不会判断文件夹是否为空，如果不空则递归删除子文件或文件夹
     * 某个文件删除失败会终止删除操作
     *
     * @throws InternalException IO异常
     */
    public void del() throws InternalException {
        final Path path = this.path;
        if (Files.notExists(path)) {
            return;
        }

        if (PathResolve.isDirectory(path)) {
            _del(path);
        } else {
            delFile(path);
        }
    }

    /**
     * 清空目录
     */
    public void clean() {
        try (final Stream<Path> list = Files.list(this.path)) {
            list.forEach(PathResolve::del);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

}
