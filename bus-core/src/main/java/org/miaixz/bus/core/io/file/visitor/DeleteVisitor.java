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
