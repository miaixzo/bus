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
package org.miaixz.bus.core.io.file.visitor;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * FileVisitor implementation for delete operations, used to recursively traverse and delete directories.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DeleteVisitor extends SimpleFileVisitor<Path> {

    /**
     * Constructs a new DeleteVisitor. Utility class constructor for static access.
     */
    private DeleteVisitor() {
    }

    /**
     * Singleton instance of {@code DeleteVisitor}.
     */
    public static DeleteVisitor INSTANCE = new DeleteVisitor();

    /**
     * Invoked when a file is visited. Deletes the file.
     *
     * @param file  The file to visit.
     * @param attrs The basic file attributes of the file.
     * @return {@link FileVisitResult#CONTINUE} to continue the file tree traversal.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
    }

    /**
     * Invoked after visiting a directory. Deletes the directory after all its children have been visited (and deleted).
     * Theoretically, when this method is executed, the directory should already be empty.
     *
     * @param dir The directory that was visited.
     * @param e   An {@code IOException} if the iteration of the files in the directory terminated prematurely;
     *            otherwise {@code null}.
     * @return {@link FileVisitResult#CONTINUE} to continue the file tree traversal.
     * @throws IOException if an I/O error occurs during deletion or if {@code e} is not {@code null}.
     */
    @Override
    public FileVisitResult postVisitDirectory(final Path dir, final IOException e) throws IOException {
        if (e == null) {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        } else {
            throw e;
        }
    }

}
