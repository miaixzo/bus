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
 * FileVisitor implementation for file copying, used to recursively traverse and copy directories. This class is not
 * thread-safe. This class automatically creates non-existent parent directories in the target directory during the
 * traversal and copying process.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CopyVisitor extends SimpleFileVisitor<Path> {

    /**
     * Source Path, or base path, used to calculate the relative path of the file to be copied.
     */
    private final Path source;
    /**
     * Target Path.
     */
    private final Path target;
    /**
     * Copy options, such as skipping existing files.
     */
    private final CopyOption[] copyOptions;

    /**
     * Flag indicating whether the target directory has been created, avoiding repeated checks for its existence.
     */
    private boolean isTargetCreated;

    /**
     * Constructs a {@code CopyVisitor} with the specified source, target, and copy options.
     *
     * @param source      The source Path, or base path, used to calculate the relative path of the file to be copied.
     * @param target      The target Path.
     * @param copyOptions Copy options, such as skipping existing files.
     * @throws IllegalArgumentException if the target exists and is not a directory.
     */
    public CopyVisitor(final Path source, final Path target, final CopyOption... copyOptions) {
        if (PathResolve.exists(target, false) && !PathResolve.isDirectory(target)) {
            throw new IllegalArgumentException("Target must be a directory");
        }
        this.source = source;
        this.target = target;
        this.copyOptions = copyOptions;
    }

    /**
     * Invoked before visiting a directory. Initializes the target directory and copies the current directory to the
     * target.
     *
     * @param dir   The directory to visit.
     * @param attrs The basic file attributes of the directory.
     * @return {@link FileVisitResult#CONTINUE} to continue the file tree traversal.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
        initTargetDir();
        // Convert the current directory relative to the source path to a path relative to the target path.
        final Path targetDir = resolveTarget(dir);

        // The copy method will create a new directory if it does not exist.
        try {
            Files.copy(dir, targetDir, copyOptions);
        } catch (final FileAlreadyExistsException e) {
            if (!Files.isDirectory(targetDir)) {
                // If the target file exists, throw an exception; directories are ignored.
                throw e;
            }
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     * Invoked when a file is visited. Initializes the target directory and copies the current file to the target.
     *
     * @param file  The file to visit.
     * @param attrs The basic file attributes of the file.
     * @return {@link FileVisitResult#CONTINUE} to continue the file tree traversal.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        initTargetDir();
        // If the target exists, whether it's a directory or a file, a FileAlreadyExistsException is thrown.
        // No special handling is done here.
        Files.copy(file, resolveTarget(file), copyOptions);
        return FileVisitResult.CONTINUE;
    }

    /**
     * Generates the target file or directory path based on the source file or directory path. The principle is to first
     * truncate the source path to get the relative path, and then concatenate it with the target path.
     *
     * <p>
     * For example: If the source path is {@code /opt/test/}, the file to be copied is {@code /opt/test/a/a.txt}, the
     * relative path obtained is {@code a/a.txt}. If the target path is {@code /home/}, the final target path will be
     * {@code /home/a/a.txt}.
     *
     * @param file The Path of the file or directory to be copied.
     * @return The target Path.
     */
    private Path resolveTarget(final Path file) {
        return target.resolve(source.relativize(file));
    }

    /**
     * Initializes the target file or directory. This method ensures that the target directory exists before any copy
     * operations.
     */
    private void initTargetDir() {
        if (!this.isTargetCreated) {
            PathResolve.mkdir(this.target);
            this.isTargetCreated = true;
        }
    }

}
