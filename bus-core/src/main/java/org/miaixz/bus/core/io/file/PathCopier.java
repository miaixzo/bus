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

import java.io.IOException;
import java.io.Serial;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.miaixz.bus.core.io.file.visitor.CopyVisitor;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.copier.SrcToDestCopier;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Encapsulates file and directory copying operations. This class provides methods to copy files or directories with
 * various options, including overwriting existing files and handling recursive copies.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PathCopier extends SrcToDestCopier<Path, PathCopier> {

    @Serial
    private static final long serialVersionUID = 2852228290187L;

    /**
     * The copy options, such as {@link StandardCopyOption#REPLACE_EXISTING}.
     */
    private final CopyOption[] options;

    /**
     * Constructs a new {@code PathCopier} instance.
     *
     * @param src     The source file or directory to be copied. Must not be {@code null} and must exist.
     * @param target  The target destination for the copy operation. Must not be {@code null}.
     * @param options An array of {@link CopyOption} specifying how the copy operation should be performed. If
     *                {@code null}, an empty array is used.
     * @throws IllegalArgumentException if the source path is {@code null} or does not exist, or if the target path is
     *                                  {@code null}.
     */
    public PathCopier(final Path src, final Path target, final CopyOption[] options) {
        Assert.notNull(src, "Src path must be not null !");
        if (!PathResolve.exists(src, false)) {
            throw new IllegalArgumentException("Src path does not exist!");
        }
        this.src = src;
        this.target = Assert.notNull(target, "Target path must be not null !");
        this.options = ObjectKit.defaultIfNull(options, new CopyOption[] {});
    }

    /**
     * Creates a file or directory copier with the specified source, target, and overwrite option.
     *
     * @param src        The source file or directory to be copied.
     * @param target     The target destination for the copy operation.
     * @param isOverride Whether to overwrite the target file if it exists. If {@code true},
     *                   {@link StandardCopyOption#REPLACE_EXISTING} is used.
     * @return A new {@code PathCopier} instance.
     */
    public static PathCopier of(final Path src, final Path target, final boolean isOverride) {
        return of(
                src,
                target,
                isOverride ? new CopyOption[] { StandardCopyOption.REPLACE_EXISTING } : new CopyOption[] {});
    }

    /**
     * Creates a file or directory copier with the specified source, target, and copy options.
     *
     * @param src     The source file or directory to be copied.
     * @param target  The target destination for the copy operation.
     * @param options An array of {@link CopyOption} specifying how the copy operation should be performed.
     * @return A new {@code PathCopier} instance.
     */
    public static PathCopier of(final Path src, final Path target, final CopyOption[] options) {
        return new PathCopier(src, target, options);
    }

    /**
     * Copies all files and directories under the source directory to the target directory recursively.
     *
     * @param src     The source directory path.
     * @param target  The target directory.
     * @param options An array of {@link CopyOption} specifying how the copy operation should be performed.
     * @return The target {@link Path} after the copy operation.
     * @throws InternalException if an I/O error occurs during the copy operation.
     */
    private static Path copyContent(final Path src, final Path target, final CopyOption... options)
            throws InternalException {
        try {
            Files.walkFileTree(src, new CopyVisitor(src, target, options));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return target;
    }

    /**
     * Copies a single file using the JDK7+ {@link Files#copy(Path, Path, CopyOption...)} method.
     *
     * @param src     The source file path. Must not be {@code null}.
     * @param target  The target file or directory. If the target is a directory, the source file will be copied into it
     *                with its original name.
     * @param options An array of {@link CopyOption} specifying how the copy operation should be performed.
     * @return The target {@link Path} after the copy operation.
     * @throws InternalException        if an I/O error occurs during the copy operation.
     * @throws IllegalArgumentException if the source or target file is {@code null}.
     */
    private static Path copyFile(final Path src, final Path target, final CopyOption... options)
            throws InternalException {
        Assert.notNull(src, "Source file is null !");
        Assert.notNull(target, "Target file or directory is null !");

        final Path targetPath = PathResolve.isDirectory(target) ? target.resolve(src.getFileName()) : target;
        PathResolve.mkParentDirs(targetPath);
        try {
            return Files.copy(src, targetPath, options);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Copies the source path (file or directory) to the target path. If the source is a directory and the target exists
     * and is a directory, the source directory's content will be copied into a new subdirectory within the target,
     * named after the source directory. If the source is a directory and the target does not exist, the source
     * directory and its content will be copied to the target path.
     *
     * @return The target {@link Path} after the copy operation.
     * @throws InternalException        if an I/O error occurs during the copy operation.
     * @throws IllegalArgumentException if {@code src} is a directory and {@code target} is an existing file.
     */
    @Override
    public Path copy() throws InternalException {
        if (PathResolve.isDirectory(src)) {
            if (PathResolve.exists(target, false)) {
                if (PathResolve.isDirectory(target)) {
                    return copyContent(src, target.resolve(src.getFileName()), options);
                } else {
                    throw new IllegalArgumentException("Cannot copy a directory to a file!");
                }
            } else {
                return copyContent(src, target, options);
            }
        }
        return copyFile(src, target, options);
    }

    /**
     * Copies the content of the source path (file or directory) to the target path. If the source is a directory, its
     * content (files and subdirectories) will be copied directly into the target directory. If the source is a file, it
     * will be copied to the target path.
     *
     * @return The target {@link Path} after the copy operation.
     * @throws InternalException        if an I/O error occurs during the copy operation.
     * @throws IllegalArgumentException if {@code src} is a directory and {@code target} is an existing file.
     */
    public Path copyContent() throws InternalException {
        if (PathResolve.isDirectory(src, false)) {
            return copyContent(src, target, options);
        }
        return copyFile(src, target, options);
    }

}
