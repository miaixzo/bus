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
package org.miaixz.bus.core.io.compress;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * FileVisitor implementation for copying files to a Zip file, appending files to the Zip archive. This class is not
 * thread-safe. This class automatically creates non-existent parent directories in the target directory during the
 * traversal and copying process.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZipCopyVisitor extends SimpleFileVisitor<Path> {

    /**
     * The source Path, or base path, used to calculate the relative path of the file being copied.
     */
    private final Path source;
    /**
     * The target FileSystem, representing the Zip file.
     */
    private final FileSystem fileSystem;
    /**
     * Copy options, such as skipping existing files.
     */
    private final CopyOption[] copyOptions;

    /**
     * Constructs a new ZipCopyVisitor.
     *
     * @param source      The source Path, or base path, used to calculate the relative path of the file being copied.
     * @param fileSystem  The target FileSystem, representing the Zip file.
     * @param copyOptions Copy options, such as skipping existing files.
     */
    public ZipCopyVisitor(final Path source, final FileSystem fileSystem, final CopyOption... copyOptions) {
        this.source = source;
        this.fileSystem = fileSystem;
        this.copyOptions = copyOptions;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
        final Path targetDir = resolveTarget(dir);
        if (StringKit.isNotEmpty(targetDir.toString())) {
            // Create directory in the target Zip file at the relative location.
            try {
                Files.copy(dir, targetDir, copyOptions);
            } catch (final DirectoryNotEmptyException ignore) {
                // Directory already exists, skip.
            } catch (final FileAlreadyExistsException e) {
                if (!Files.isDirectory(targetDir)) {
                    throw e;
                }
                // If the directory is not empty, skip creating it.
            }
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        // If the target exists, whether it's a directory or a file, a FileAlreadyExistsException will be thrown. No
        // special handling is done here.
        Files.copy(file, resolveTarget(file), copyOptions);

        return FileVisitResult.CONTINUE;
    }

    /**
     * Resolves the target file or directory path based on the source file or directory path. The principle is to first
     * truncate the source path to get the relative path, and then concatenate it with the target path.
     *
     * <p>
     * For example: if the source path is /opt/test/, the file to be copied is /opt/test/a/a.txt, the relative path
     * obtained is a/a.txt. If the target path is /home/, the final target path will be /home/a/a.txt.
     *
     * @param file The file or directory Path to be copied.
     * @return The target Path.
     */
    private Path resolveTarget(final Path file) {
        return fileSystem.getPath(source.relativize(file).toString());
    }

}
