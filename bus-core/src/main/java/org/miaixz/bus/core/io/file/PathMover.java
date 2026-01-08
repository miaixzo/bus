/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
import java.nio.file.*;

import org.miaixz.bus.core.io.file.visitor.MoveVisitor;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Encapsulates file and directory moving operations. This class provides methods to move files or directories with
 * various options, including overwriting existing files and handling cross-partition moves.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PathMover {

    /**
     * The source file or directory to be moved.
     */
    private final Path src;
    /**
     * The target destination for the move operation.
     */
    private final Path target;
    /**
     * The move options, such as {@link StandardCopyOption#REPLACE_EXISTING}.
     */
    private final CopyOption[] options;

    /**
     * Constructs a new {@code PathMover} instance.
     *
     * @param src     The source file or directory, must not be {@code null} and must exist.
     * @param target  The target file or directory. Must not be {@code null}.
     * @param options An array of {@link CopyOption} specifying how the move operation should be performed. If
     *                {@code null}, an empty array is used.
     * @throws IllegalArgumentException if the source path is {@code null} or does not exist, or if the target path is
     *                                  {@code null}.
     */
    public PathMover(final Path src, final Path target, final CopyOption[] options) {
        Assert.notNull(src, "Src path must be not null !");
        if (!PathResolve.exists(src, false)) {
            throw new IllegalArgumentException("Src path does not exist!");
        }
        this.src = src;
        this.target = Assert.notNull(target, "Target path must be not null !");
        this.options = ObjectKit.defaultIfNull(options, new CopyOption[] {});
    }

    /**
     * Creates a file or directory mover with the specified source, target, and overwrite option.
     *
     * @param src        The source file or directory to be moved.
     * @param target     The target destination for the move operation.
     * @param isOverride Whether to overwrite the target file if it exists. If {@code true},
     *                   {@link StandardCopyOption#REPLACE_EXISTING} is used.
     * @return A new {@code PathMover} instance.
     */
    public static PathMover of(final Path src, final Path target, final boolean isOverride) {
        return of(
                src,
                target,
                isOverride ? new CopyOption[] { StandardCopyOption.REPLACE_EXISTING } : new CopyOption[] {});
    }

    /**
     * Creates a file or directory mover with the specified source, target, and move options.
     *
     * @param src     The source file or directory to be moved.
     * @param target  The target destination for the move operation.
     * @param options An array of {@link CopyOption} specifying how the move operation should be performed.
     * @return A new {@code PathMover} instance.
     */
    public static PathMover of(final Path src, final Path target, final CopyOption[] options) {
        return new PathMover(src, target, options);
    }

    /**
     * Recursively moves files and directories from the source to the target using a {@link MoveVisitor}. This method is
     * typically used for cross-partition moves or when {@link Files#move(Path, Path, CopyOption...)} fails.
     *
     * @param src     The source directory to move.
     * @param target  The target directory to move to.
     * @param options The move options to apply during the walk.
     * @throws InternalException if an {@link IOException} occurs during the file tree traversal or move operation.
     */
    private static void walkMove(final Path src, final Path target, final CopyOption... options) {
        try {
            Files.walkFileTree(src, new MoveVisitor(src, target, options));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Moves the source file or directory to the specified target location. If the target is a directory, the source
     * will be moved into it, retaining its original name. If the move operation fails due to cross-partition issues, it
     * falls back to a recursive copy-and-delete strategy.
     *
     * @return The target file {@link Path} after the move operation.
     * @throws IllegalArgumentException if the source is a directory and the target is its subdirectory, or if the
     *                                  source and target are the same.
     * @throws InternalException        if an I/O error occurs during the move operation, or if a
     *                                  {@link FileAlreadyExistsException} or {@link AccessDeniedException} occurs.
     */
    public Path move() throws IllegalArgumentException {
        final Path src = this.src;
        Path target = this.target;
        final CopyOption[] options = this.options;

        if (PathResolve.isSub(src, target)) {
            if (PathResolve.equals(src, target)) {
                return target;
            }
            throw new IllegalArgumentException(StringKit.format("Target [{}] is a subpath of src [{}]!", target, src));
        }

        if (PathResolve.isDirectory(target)) {
            target = target.resolve(src.getFileName());
        }

        PathResolve.mkParentDirs(target);
        try {
            return Files.move(src, target, options);
        } catch (final IOException e) {
            if (e instanceof FileAlreadyExistsException || e instanceof AccessDeniedException) {
                throw new InternalException(e);
            }
            // Fallback to recursive move for cross-partition moves.
            walkMove(src, target, options);
            PathResolve.remove(src);
            return target;
        }
    }

    /**
     * Moves the content of the source file or directory to the target location. If the target is an existing file, the
     * source will be moved to replace it. If the target is a directory, the content of the source will be moved into
     * the target directory.
     *
     * @return The target file {@link Path} after the move operation.
     * @throws IllegalArgumentException if the source is a directory and the target is a file.
     * @throws InternalException        if an I/O error occurs during the move operation.
     */
    public Path moveContent() {
        final Path src = this.src;
        final Path target = this.target;

        if (PathResolve.isExistsAndNotDirectory(target, false)) {
            return move();
        }

        if (PathResolve.equals(src, target)) {
            return target;
        }

        final CopyOption[] options = this.options;

        PathResolve.mkParentDirs(target);

        walkMove(src, target, options);
        return target;
    }

}
