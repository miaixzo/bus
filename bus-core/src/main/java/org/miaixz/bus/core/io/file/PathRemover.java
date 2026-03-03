/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.io.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.miaixz.bus.core.io.file.visitor.DeleteVisitor;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Encapsulates file and directory deletion operations. This class provides methods to remove files or directories,
 * including recursive deletion for directories and handling of read-only files.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PathRemover {

    /**
     * The path of the file or directory to be deleted or cleaned.
     */
    private final Path path;

    /**
     * Constructs a new {@code PathRemover} instance.
     *
     * @param path The {@link Path} of the file or directory to operate on. Must not be {@code null}.
     * @throws NullPointerException if the provided {@code path} is {@code null}.
     */
    public PathRemover(final Path path) {
        this.path = Assert.notNull(path, "Path must be not null !");
    }

    /**
     * Creates a new {@code PathRemover} instance for the given source path.
     *
     * @param src The source {@link Path} of the file or directory.
     * @return A new {@code PathRemover} instance.
     */
    public static PathRemover of(final Path src) {
        return new PathRemover(src);
    }

    /**
     * Recursively deletes a directory and its contents. This method uses a {@link DeleteVisitor} to traverse and delete
     * the file tree.
     *
     * @param path The {@link Path} to the directory to be deleted.
     * @throws InternalException if an {@link IOException} occurs during the file tree traversal or deletion.
     */
    private static void remove(final Path path) {
        try {
            Files.walkFileTree(path, DeleteVisitor.INSTANCE);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Deletes a single file or an empty directory. Handles {@link AccessDeniedException} by attempting to delete the
     * file using {@link File#delete()}.
     *
     * @param path The {@link Path} to the file or empty directory to be deleted.
     * @throws InternalException if an I/O error occurs during deletion, and the fallback deletion also fails.
     */
    private static void removeFile(final Path path) throws InternalException {
        try {
            Files.delete(path);
        } catch (final IOException e) {
            if (e instanceof AccessDeniedException) {
                // Fallback for read-only files or permission issues.
                if (path.toFile().delete()) {
                    return;
                }
            }
            throw new InternalException(e);
        }
    }

    /**
     * Deletes the file or directory associated with this {@code PathRemover}. If the path points to a directory, it
     * will be deleted recursively along with its contents. If the path points to a file, it will be deleted. If the
     * path does not exist, this method does nothing.
     *
     * @throws InternalException if an I/O error occurs during the deletion process.
     */
    public void remove() throws InternalException {
        final Path path = this.path;
        if (Files.notExists(path)) {
            return;
        }

        if (PathResolve.isDirectory(path)) {
            remove(path);
        } else {
            removeFile(path);
        }
    }

    /**
     * Clears the contents of the directory associated with this {@code PathRemover} without deleting the directory
     * itself. All files and subdirectories within the target directory will be deleted.
     *
     * @throws InternalException if an I/O error occurs during the cleaning process.
     */
    public void clean() {
        try (final Stream<Path> list = Files.list(this.path)) {
            list.forEach(PathResolve::remove);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

}
