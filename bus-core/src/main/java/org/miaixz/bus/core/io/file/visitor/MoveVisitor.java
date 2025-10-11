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
 * FileVisitor implementation for file move operations, used to recursively traverse and move directories and files.
 * This class is not thread-safe. This class automatically creates non-existent parent directories in the target
 * directory during the traversal and moving process.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MoveVisitor extends SimpleFileVisitor<Path> {

    /**
     * The source path for the move operation.
     */
    private final Path source;
    /**
     * The target path for the move operation.
     */
    private final Path target;
    /**
     * Copy options for the move operation.
     */
    private final CopyOption[] copyOptions;
    /**
     * Flag indicating whether the target directory has been created.
     */
    private boolean isTargetCreated;

    /**
     * Constructs a {@code MoveVisitor} with the specified source, target, and copy options.
     *
     * @param source      The source Path.
     * @param target      The target Path.
     * @param copyOptions Copy (move) options.
     * @throws IllegalArgumentException if the target exists and is not a directory.
     */
    public MoveVisitor(final Path source, final Path target, final CopyOption... copyOptions) {
        if (PathResolve.exists(target, false) && !PathResolve.isDirectory(target)) {
            throw new IllegalArgumentException("Target must be a directory");
        }
        this.source = source;
        this.target = target;
        this.copyOptions = copyOptions;
    }

    /**
     * Invoked before visiting a directory. Initializes the target directory and creates corresponding directories in
     * the target path.
     *
     * @param dir   The directory to visit.
     * @param attrs The basic file attributes of the directory.
     * @return {@link FileVisitResult#CONTINUE} to continue the file tree traversal.
     * @throws IOException if an I/O error occurs or if a file already exists at the target directory path.
     */
    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
        initTarget();
        // Convert the current directory relative to the source path to a path relative to the target path.
        final Path targetDir = target.resolve(source.relativize(dir));
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        } else if (!Files.isDirectory(targetDir)) {
            throw new FileAlreadyExistsException(targetDir.toString());
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     * Invoked when a file is visited. Moves the file to the target path.
     *
     * @param file  The file to visit.
     * @param attrs The basic file attributes of the file.
     * @return {@link FileVisitResult#CONTINUE} to continue the file tree traversal.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        initTarget();
        Files.move(file, target.resolve(source.relativize(file)), copyOptions);
        return FileVisitResult.CONTINUE;
    }

    /**
     * Initializes the target file or directory. This method ensures that the target directory exists before any move
     * operations.
     */
    private void initTarget() {
        if (!this.isTargetCreated) {
            PathResolve.mkdir(this.target);
            this.isTargetCreated = true;
        }
    }

}
