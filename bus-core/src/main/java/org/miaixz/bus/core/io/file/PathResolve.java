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

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.miaixz.bus.core.io.resource.FileResource;
import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.NotFoundException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.PredicateKit;

/**
 * A utility class that encapsulates operations on NIO {@link Path} objects.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PathResolve {

    /**
     * Joins multiple path strings into a single {@link Path}.
     *
     * @param firstPath The first path component.
     * @param paths     Additional path components.
     * @return The combined {@link Path}.
     * @see Paths#get(String, String...)
     */
    public static Path of(final String firstPath, final String... paths) {
        return Paths.get(firstPath, paths);
    }

    /**
     * Joins multiple {@link Path} objects into a single {@link Path}.
     *
     * @param firstPath The first path.
     * @param paths     Additional paths to resolve against the first path.
     * @return The combined {@link Path}.
     */
    public static Path of(Path firstPath, final Path... paths) {
        if (ArrayKit.isEmpty(paths)) {
            return firstPath;
        }

        for (final Path path : paths) {
            if (null == path) {
                continue;
            }
            if (null == firstPath) {
                firstPath = path;
            } else {
                firstPath = firstPath.resolve(path);
            }
        }
        return firstPath;
    }

    /**
     * Checks if a directory is empty.
     *
     * @param dirPath The directory path.
     * @return {@code true} if the directory is empty.
     * @throws InternalException if an I/O error occurs.
     */
    public static boolean isDirEmpty(final Path dirPath) {
        try (final DirectoryStream<Path> dirStream = Files.newDirectoryStream(dirPath)) {
            return !dirStream.iterator().hasNext();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Lists all files and subdirectories within a given directory.
     *
     * @param dirPath The directory path.
     * @param filter  A filter to apply to the files. If {@code null}, all files are accepted.
     * @return An array of matching paths.
     */
    public static Path[] listFiles(final Path dirPath, Predicate<? super Path> filter) {
        if (null == filter) {
            filter = PredicateKit.alwaysTrue();
        }

        try (final Stream<Path> list = Files.list(dirPath)) {
            return list.filter(filter).toArray(Path[]::new);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Recursively traverses a directory to find all files.
     *
     * @param path       The starting file or directory.
     * @param fileFilter A filter to select which files to include.
     * @return A list of matching files.
     */
    public static List<File> loopFiles(final Path path, final FileFilter fileFilter) {
        return loopFiles(path, -1, fileFilter);
    }

    /**
     * Recursively traverses a directory to find all files up to a maximum depth.
     *
     * @param path       The starting file or directory.
     * @param maxDepth   The maximum depth to traverse (-1 for unlimited).
     * @param fileFilter A filter to select which files to include.
     * @return A list of matching files.
     */
    public static List<File> loopFiles(final Path path, final int maxDepth, final FileFilter fileFilter) {
        return loopFiles(path, maxDepth, false, fileFilter);
    }

    /**
     * Recursively traverses a directory to find all files.
     *
     * @param path          The starting file or directory.
     * @param maxDepth      The maximum depth to traverse (-1 for unlimited).
     * @param isFollowLinks Whether to follow symbolic links.
     * @param fileFilter    A filter to select which files to include.
     * @return A list of matching files.
     */
    public static List<File> loopFiles(
            final Path path,
            final int maxDepth,
            final boolean isFollowLinks,
            final FileFilter fileFilter) {
        final List<File> fileList = new ArrayList<>();

        if (!exists(path, isFollowLinks)) {
            return fileList;
        } else if (!isDirectory(path, isFollowLinks)) {
            final File file = path.toFile();
            if (null == fileFilter || fileFilter.accept(file)) {
                fileList.add(file);
            }
            return fileList;
        }

        walkFiles(path, maxDepth, isFollowLinks, new SimpleFileVisitor<>() {

            /**
             * Visitfile method.
             *
             * @return the FileVisitResult value
             */
            @Override
            public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) {
                final File file = path.toFile();
                if (null == fileFilter || fileFilter.accept(file)) {
                    fileList.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return fileList;
    }

    /**
     * Walks a file tree starting from a given path.
     *
     * @param start   The starting path (must be a directory).
     * @param visitor The {@link FileVisitor} to apply.
     */
    public static void walkFiles(final Path start, final FileVisitor<? super Path> visitor) {
        walkFiles(start, -1, visitor);
    }

    /**
     * Walks a file tree up to a maximum depth.
     *
     * @param start    The starting path.
     * @param maxDepth The maximum depth (-1 for unlimited).
     * @param visitor  The {@link FileVisitor}.
     */
    public static void walkFiles(final Path start, final int maxDepth, final FileVisitor<? super Path> visitor) {
        walkFiles(start, maxDepth, false, visitor);
    }

    /**
     * Walks a file tree.
     *
     * @param start         The starting path.
     * @param maxDepth      The maximum depth.
     * @param visitor       The {@link FileVisitor}.
     * @param isFollowLinks Whether to follow symbolic links.
     */
    public static void walkFiles(
            final Path start,
            int maxDepth,
            final boolean isFollowLinks,
            final FileVisitor<? super Path> visitor) {
        if (maxDepth < 0) {
            maxDepth = Integer.MAX_VALUE;
        }

        try {
            Files.walkFileTree(start, getFileVisitOption(isFollowLinks), maxDepth, visitor);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Deletes a file or directory recursively without following symbolic links.
     *
     * @param path The path to the file or directory.
     * @throws InternalException if an I/O error occurs.
     */
    public static void remove(final Path path) throws InternalException {
        PathRemover.of(path).remove();
    }

    /**
     * Clears all content from a directory without deleting the directory itself.
     *
     * @param path The path to the directory.
     */
    public static void clean(final Path path) {
        PathRemover.of(path).clean();
    }

    /**
     * Copies a resource to a target path.
     *
     * @param src     The source {@link Resource}.
     * @param target  The target path.
     * @param options The copy options.
     * @return The target path.
     * @throws InternalException if an I/O error occurs.
     */
    public static Path copy(final Resource src, final Path target, final CopyOption... options)
            throws InternalException {
        Assert.notNull(src, "Source is null !");
        if (src instanceof FileResource) {
            return copy(((FileResource) src).getFile().toPath(), target, options);
        }
        try (final InputStream stream = src.getStream()) {
            return copy(stream, target, options);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Copies an {@link InputStream} to a target path.
     *
     * @param src     The source stream (not closed after use).
     * @param target  The target path.
     * @param options The copy options.
     * @return The target path.
     * @throws InternalException if an I/O error occurs.
     */
    public static Path copy(final InputStream src, final Path target, final CopyOption... options)
            throws InternalException {
        Assert.notNull(target, "Destination path is null !");
        mkParentDirs(target);
        try {
            Files.copy(src, target, options);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return target;
    }

    /**
     * Copies a file to an {@link OutputStream}.
     *
     * @param src The source path.
     * @param out The target stream.
     * @return The number of bytes copied.
     * @throws InternalException if an I/O error occurs.
     */
    public static long copy(final Path src, final OutputStream out) throws InternalException {
        Assert.notNull(src, "Source is null !");
        try {
            return Files.copy(src, out);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Copies a source file or directory to a target.
     *
     * @param src     The source path.
     * @param target  The target path.
     * @param options The copy options.
     * @return The target path.
     * @throws InternalException if an I/O error occurs.
     */
    public static Path copy(final Path src, final Path target, final CopyOption... options) throws InternalException {
        return PathCopier.of(src, target, options).copy();
    }

    /**
     * Copies the content of a source file or directory to a target.
     *
     * @param src     The source path.
     * @param target  The target path.
     * @param options The copy options.
     * @return The target path.
     * @throws InternalException if an I/O error occurs.
     */
    public static Path copyContent(final Path src, final Path target, final CopyOption... options)
            throws InternalException {
        return PathCopier.of(src, target, options).copyContent();
    }

    /**
     * Checks if the given path is a directory. Does not follow symbolic links.
     *
     * @param path The {@link Path}.
     * @return {@code true} if it is a directory.
     */
    public static boolean isDirectory(final Path path) {
        return isDirectory(path, false);
    }

    /**
     * Checks if the path exists and is not a directory.
     *
     * @param path          The {@link Path}.
     * @param isFollowLinks Whether to follow symbolic links.
     * @return {@code true} if it exists and is not a directory.
     */
    public static boolean isExistsAndNotDirectory(final Path path, final boolean isFollowLinks) {
        return exists(path, isFollowLinks) && !isDirectory(path, isFollowLinks);
    }

    /**
     * Checks if the given path is a directory.
     *
     * @param path          The {@link Path}.
     * @param isFollowLinks Whether to follow symbolic links.
     * @return {@code true} if it is a directory.
     */
    public static boolean isDirectory(final Path path, final boolean isFollowLinks) {
        if (null == path) {
            return false;
        }
        return Files.isDirectory(path, getLinkOptions(isFollowLinks));
    }

    /**
     * Gets a path element (name) at the specified index. Supports negative indices.
     *
     * @param path  The path.
     * @param index The index of the path element.
     * @return The path element.
     */
    public static Path getPathEle(final Path path, final int index) {
        return subPath(path, index, index == -1 ? path.getNameCount() : index + 1);
    }

    /**
     * Gets the last path element.
     *
     * @param path The path.
     * @return The last path element.
     */
    public static Path getLastPathEle(final Path path) {
        return getPathEle(path, path.getNameCount() - 1);
    }

    /**
     * Returns a relative {@code Path} that is a subsequence of the name elements of this path.
     *
     * @param path      The path.
     * @param fromIndex The starting index (inclusive).
     * @param toIndex   The ending index (exclusive).
     * @return The subpath.
     */
    public static Path subPath(final Path path, int fromIndex, int toIndex) {
        if (null == path) {
            return null;
        }
        final int len = path.getNameCount();

        if (fromIndex < 0)
            fromIndex = len + fromIndex;
        if (toIndex < 0)
            toIndex = len + toIndex;
        if (fromIndex > toIndex) {
            int tmp = fromIndex;
            fromIndex = toIndex;
            toIndex = tmp;
        }

        if (fromIndex >= len)
            return null;
        if (toIndex > len)
            toIndex = len;
        if (fromIndex >= toIndex)
            return null;

        return path.subpath(fromIndex, toIndex);
    }

    /**
     * Gets the basic file attributes for a path.
     *
     * @param path          The {@link Path}.
     * @param isFollowLinks Whether to follow symbolic links.
     * @return The file attributes.
     * @throws InternalException if an I/O error occurs.
     */
    public static BasicFileAttributes getAttributes(final Path path, final boolean isFollowLinks)
            throws InternalException {
        return getAttributes(path, getLinkOptions(isFollowLinks));
    }

    /**
     * Gets the basic file attributes for a path.
     *
     * @param path    The {@link Path}.
     * @param options The link options.
     * @return The file attributes.
     * @throws InternalException if an I/O error occurs.
     */
    public static BasicFileAttributes getAttributes(final Path path, final LinkOption... options)
            throws InternalException {
        if (null == path) {
            return null;
        }
        try {
            return Files.readAttributes(path, BasicFileAttributes.class, options);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets a buffered input stream for a path.
     *
     * @param path The path.
     * @return A {@link BufferedInputStream}.
     * @throws InternalException if an I/O error occurs.
     */
    public static BufferedInputStream getInputStream(final Path path) throws InternalException {
        try {
            return IoKit.toBuffered(Files.newInputStream(path));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets a buffered input stream for a path with open options.
     *
     * @param path    The path.
     * @param options The open options.
     * @return A {@link BufferedInputStream}.
     */
    public static BufferedInputStream getInputStream(final Path path, final OpenOption... options) {
        try {
            return IoKit.toBuffered(Files.newInputStream(path, options));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets a buffered reader for a path using UTF-8 encoding.
     *
     * @param path The path.
     * @return A {@link BufferedReader}.
     * @throws InternalException if an I/O error occurs.
     */
    public static BufferedReader getReader(final Path path) throws InternalException {
        return getReader(path, Charset.UTF_8);
    }

    /**
     * Gets a buffered reader for a path with a specific charset.
     *
     * @param path    The path.
     * @param charset The character set.
     * @return A {@link BufferedReader}.
     * @throws InternalException if an I/O error occurs.
     */
    public static BufferedReader getReader(final Path path, final java.nio.charset.Charset charset)
            throws InternalException {
        return IoKit.toReader(getInputStream(path), charset);
    }

    /**
     * Gets a buffered reader for a path with a specific charset and open options.
     *
     * @param path    The path.
     * @param charset The character set.
     * @param options The open options.
     * @return A {@link BufferedReader}.
     */
    public static BufferedReader getReader(
            final Path path,
            final java.nio.charset.Charset charset,
            final OpenOption... options) {
        return IoKit.toReader(getInputStream(path, options), charset);
    }

    /**
     * Reads all bytes from a file.
     *
     * @param path The path to the file.
     * @return A byte array of the file's contents.
     */
    public static byte[] readBytes(final Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets a buffered output stream for a path.
     *
     * @param path    The path.
     * @param options The open options (e.g., {@link StandardOpenOption#APPEND}).
     * @return A {@link BufferedOutputStream}.
     * @throws NotFoundException if the path cannot be opened.
     */
    public static BufferedOutputStream getOutputStream(final Path path, final OpenOption... options)
            throws NotFoundException {
        try {
            return IoKit.toBuffered(Files.newOutputStream(path, options));
        } catch (final IOException e) {
            throw new NotFoundException(e);
        }
    }

    /**
     * Renames a file or directory within its current parent directory.
     *
     * @param path       The path to rename.
     * @param newName    The new name (including extension).
     * @param isOverride Whether to overwrite the destination if it exists.
     * @return The path to the renamed file or directory.
     */
    public static Path rename(final Path path, final String newName, final boolean isOverride) {
        return move(path, path.resolveSibling(newName), isOverride);
    }

    /**
     * Moves a file or directory to a target location.
     *
     * @param src        The source path.
     * @param target     The target path.
     * @param isOverride Whether to overwrite the destination if it exists.
     * @return The target path.
     */
    public static Path move(final Path src, final Path target, final boolean isOverride) {
        return PathMover.of(src, target, isOverride).move();
    }

    /**
     * Moves the content of a file or directory to a target location.
     *
     * @param src        The source path.
     * @param target     The target path.
     * @param isOverride Whether to overwrite the destination if it exists.
     * @return The target path.
     */
    public static Path moveContent(final Path src, final Path target, final boolean isOverride) {
        return PathMover.of(src, target, isOverride).moveContent();
    }

    /**
     * Checks if two paths refer to the same file or directory.
     *
     * @param file1 The first path.
     * @param file2 The second path.
     * @return {@code true} if they refer to the same file.
     * @throws InternalException if an I/O error occurs.
     */
    public static boolean equals(final Path file1, final Path file2) throws InternalException {
        if (null == file1 || null == file2) {
            return null == file1 && null == file2;
        }

        final boolean exists1 = exists(file1, false);
        final boolean exists2 = exists(file2, false);

        if (exists1 && exists2) {
            return isSameFile(file1, file2);
        }

        return ObjectKit.equals(file1, file2);
    }

    /**
     * Checks if two paths refer to the same file.
     *
     * @param file1 The first path.
     * @param file2 The second path.
     * @return {@code true} if they are the same file.
     * @throws InternalException if an I/O error occurs.
     */
    public static boolean isSameFile(final Path file1, final Path file2) throws InternalException {
        try {
            return Files.isSameFile(file1, file2);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Checks if the path is a regular file.
     *
     * @param path          The path.
     * @param isFollowLinks Whether to follow symbolic links.
     * @return {@code true} if it is a regular file.
     */
    public static boolean isFile(final Path path, final boolean isFollowLinks) {
        if (null == path) {
            return false;
        }
        return Files.isRegularFile(path, getLinkOptions(isFollowLinks));
    }

    /**
     * Checks if the path is a symbolic link.
     *
     * @param path The path to check.
     * @return {@code true} if it is a symbolic link.
     */
    public static boolean isSymlink(final Path path) {
        return Files.isSymbolicLink(path);
    }

    /**
     * Checks if the path refers to something other than a file, directory, or symbolic link.
     *
     * @param path The path to check.
     * @return {@code true} if it is an "other" type.
     */
    public static boolean isOther(final Path path) {
        return getAttributes(path, false).isOther();
    }

    /**
     * Checks if a file or directory exists.
     *
     * @param path          The path to check.
     * @param isFollowLinks Whether to follow symbolic links.
     * @return {@code true} if it exists.
     */
    public static boolean exists(final Path path, final boolean isFollowLinks) {
        if (null == path) {
            return false;
        }
        return Files.exists(path, getLinkOptions(isFollowLinks));
    }

    /**
     * Checks if a given path is a subdirectory of a parent path.
     *
     * @param parent The parent path.
     * @param sub    The potential subdirectory.
     * @return {@code true} if {@code sub} is a subdirectory of {@code parent}.
     */
    public static boolean isSub(final Path parent, final Path sub) {
        return toAbsNormal(sub).startsWith(toAbsNormal(parent));
    }

    /**
     * Converts a {@link Path} to a normalized, absolute path.
     *
     * @param path The path to convert.
     * @return The normalized absolute path.
     */
    public static Path toAbsNormal(final Path path) {
        if (null == path) {
            return null;
        }
        return path.toAbsolutePath().normalize();
    }

    /**
     * Gets the real path of a file, resolving symbolic links.
     *
     * @param path The path.
     * @return The real path.
     * @throws InternalException if an I/O error occurs.
     */
    public static Path toRealPath(Path path) throws InternalException {
        if (null != path) {
            try {
                path = path.toRealPath();
            } catch (final IOException e) {
                throw new InternalException(e);
            }
        }
        return path;
    }

    /**
     * Probes the content type (MIME type) of a file.
     *
     * @param file The file.
     * @return The MIME type, or null if it cannot be determined.
     */
    public static String getMimeType(final Path file) {
        try {
            return Files.probeContentType(file);
        } catch (final IOException ignore) {
            return null;
        }
    }

    /**
     * Creates the specified directory, including any necessary parent directories.
     *
     * @param dir The directory to create.
     * @return The created directory path.
     */
    public static Path mkdir(final Path dir) {
        if (null != dir && !exists(dir, false)) {
            try {
                Files.createDirectories(dir);
            } catch (final IOException e) {
                throw new InternalException(e);
            }
        }
        return dir;
    }

    /**
     * Creates the parent directories for the given path.
     *
     * @param path The file or directory path.
     * @return The parent directory path.
     */
    public static Path mkParentDirs(final Path path) {
        return mkdir(path.getParent());
    }

    /**
     * Gets the file name from a {@link Path}.
     *
     * @param path The {@link Path}.
     * @return The file name.
     */
    public static String getName(final Path path) {
        if (null == path) {
            return null;
        }
        return path.getFileName().toString();
    }

    /**
     * Creates a temporary file.
     *
     * @param prefix The prefix for the file name.
     * @param suffix The suffix for the file name.
     * @param dir    The directory to create the file in.
     * @return The path to the temporary file.
     * @throws InternalException if an I/O error occurs.
     */
    public static Path createTempFile(final String prefix, final String suffix, final Path dir)
            throws InternalException {
        int exceptionsCount = 0;
        while (true) {
            try {
                if (null == dir) {
                    return Files.createTempFile(prefix, suffix);
                } else {
                    return Files.createTempFile(mkdir(dir), prefix, suffix);
                }
            } catch (final IOException ioex) {
                if (++exceptionsCount >= 50) {
                    throw new InternalException(ioex);
                }
            }
        }
    }

    /**
     * Builds the appropriate link options based on whether symbolic links should be followed.
     *
     * @param isFollowLinks Whether to follow symbolic links.
     * @return An array of {@link LinkOption}.
     */
    public static LinkOption[] getLinkOptions(final boolean isFollowLinks) {
        return isFollowLinks ? new LinkOption[0] : new LinkOption[] { LinkOption.NOFOLLOW_LINKS };
    }

    /**
     * Builds the appropriate file visit options based on whether symbolic links should be followed.
     *
     * @param isFollowLinks Whether to follow symbolic links.
     * @return A set of {@link FileVisitOption}.
     */
    public static Set<FileVisitOption> getFileVisitOption(final boolean isFollowLinks) {
        return isFollowLinks ? EnumSet.of(FileVisitOption.FOLLOW_LINKS) : EnumSet.noneOf(FileVisitOption.class);
    }

}
