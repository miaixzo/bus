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
package org.miaixz.bus.core.io.file.visitor;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import org.miaixz.bus.core.io.file.PathResolve;

/**
 * 文件移动操作的FileVisitor实现，用于递归遍历移动目录和文件，此类非线程安全 此类在遍历源目录并移动过程中会自动创建目标目录中不存在的上级目录。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MoveVisitor extends SimpleFileVisitor<Path> {

    private final Path source;
    private final Path target;
    private final CopyOption[] copyOptions;
    private boolean isTargetCreated;

    /**
     * 构造
     *
     * @param source      源Path
     * @param target      目标Path
     * @param copyOptions 拷贝（移动）选项
     */
    public MoveVisitor(final Path source, final Path target, final CopyOption... copyOptions) {
        if (PathResolve.exists(target, false) && !PathResolve.isDirectory(target)) {
            throw new IllegalArgumentException("Target must be a directory");
        }
        this.source = source;
        this.target = target;
        this.copyOptions = copyOptions;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
        initTarget();
        // 将当前目录相对于源路径转换为相对于目标路径
        final Path targetDir = target.resolve(source.relativize(dir));
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        } else if (!Files.isDirectory(targetDir)) {
            throw new FileAlreadyExistsException(targetDir.toString());
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        initTarget();
        Files.move(file, target.resolve(source.relativize(file)), copyOptions);
        return FileVisitResult.CONTINUE;
    }

    /**
     * 初始化目标文件或目录
     */
    private void initTarget() {
        if (!this.isTargetCreated) {
            PathResolve.mkdir(this.target);
            this.isTargetCreated = true;
        }
    }

}
