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

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.FileSystem;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;

import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.wrapper.SimpleWrapper;

/**
 * Delegate Path class, implementing the {@link Path} interface, used to wrap an actual {@link Path} object and provide
 * convenient access to {@link Files} class functionalities.
 * <p>
 * This class wraps a {@link Path} object and provides all methods identical to the {@link Path} interface, while also
 * adding many convenience methods to access functionalities of the {@link Files} class. All method calls are delegated
 * to the internal {@link Path} object, and returned {@link Path} objects are wrapped as {@code DelegatePath} instances.
 * <p>
 * This class also implements the {@link Resource} interface, allowing it to be used as a resource object, providing
 * convenient methods for reading file content, obtaining streams, and more.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DelegatePath extends SimpleWrapper<Path> implements Path, Resource {

    /**
     * Constructs a new {@code DelegatePath} instance from a sequence of path name elements.
     *
     * @param first The first path element. Must not be {@code null}.
     * @param more  Additional path elements. Can be {@code null} or empty.
     * @throws IllegalArgumentException If {@code first} is {@code null}.
     */
    public DelegatePath(final String first, final String... more) {
        this(Paths.get(first, more));
    }

    /**
     * Constructs a new {@code DelegatePath} instance from a {@link URI}.
     *
     * @param uri The {@link URI} to convert to a {@link Path}. Must not be {@code null}.
     * @throws IllegalArgumentException    If {@code uri} is {@code null}.
     * @throws FileSystemNotFoundException If the file system identified by the {@code uri} does not exist.
     * @throws SecurityException           If a security manager exists and its {@code checkRead} method denies access
     *                                     to the file system.
     */
    public DelegatePath(final URI uri) {
        this(Paths.get(uri));
    }

    /**
     * Constructs a new {@code DelegatePath} instance by wrapping an existing {@link Path} object.
     *
     * @param path The {@link Path} object to be wrapped. Must not be {@code null}.
     * @throws IllegalArgumentException If {@code path} is {@code null}.
     */
    public DelegatePath(final Path path) {
        super(path);
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
    }

    /**
     * Returns the raw, unwrapped {@link Path} object that this {@code DelegatePath} is wrapping.
     *
     * @return The underlying {@link Path} object.
     */
    public Path getRawPath() {
        return this.raw;
    }

    /**
     * Returns the file system that created this path.
     *
     * @return The file system that created this path.
     */
    @Override
    public FileSystem getFileSystem() {
        return raw.getFileSystem();
    }

    /**
     * Tests if this path is absolute.
     *
     * @return {@code true} if this path is absolute; {@code false} otherwise.
     */
    @Override
    public boolean isAbsolute() {
        return raw.isAbsolute();
    }

    /**
     * Returns the root component of this path, or {@code null} if this path does not have a root component.
     *
     * @return A {@code DelegatePath} object representing the root component of this path, or {@code null}.
     */
    @Override
    public Path getRoot() {
        final Path root = raw.getRoot();
        return root == null ? null : new DelegatePath(root);
    }

    /**
     * Returns the name of the file or directory denoted by this path. The file name is the farthest element from the
     * root in the directory hierarchy.
     *
     * @return A {@code DelegatePath} object representing the file name, or {@code null} if this path has zero elements.
     */
    @Override
    public Path getFileName() {
        final Path fileName = raw.getFileName();
        return fileName == null ? null : new DelegatePath(fileName);
    }

    /**
     * Returns the parent path, or {@code null} if this path has no parent. The parent path consists of this path's root
     * component, if any, and each element in the path except for the farthest element from the root.
     *
     * @return A {@code DelegatePath} object representing the parent path, or {@code null}.
     */
    @Override
    public Path getParent() {
        final Path parent = raw.getParent();
        return parent == null ? null : new DelegatePath(parent);
    }

    /**
     * Returns the number of name elements in the path.
     *
     * @return The number of elements in the path. An empty path has zero elements.
     */
    @Override
    public int getNameCount() {
        return raw.getNameCount();
    }

    /**
     * Returns a name element of this path as a {@code DelegatePath} object.
     *
     * @param index The index of the element.
     * @return A {@code DelegatePath} object representing the name element.
     * @throws IllegalArgumentException If {@code index} is negative, or greater than or equal to the number of
     *                                  elements.
     */
    @Override
    public Path getName(final int index) {
        return new DelegatePath(raw.getName(index));
    }

    /**
     * Returns a relative {@code DelegatePath} that is a subsequence of the name elements of this path.
     *
     * @param beginIndex The index of the first element, inclusive.
     * @param endIndex   The index of the last element, exclusive.
     * @return A new {@code DelegatePath} object that is a subsequence of the name elements of this path.
     * @throws IllegalArgumentException If {@code beginIndex} or {@code endIndex} are negative, or {@code beginIndex} is
     *                                  greater than or equal to {@code endIndex}, or {@code endIndex} is greater than
     *                                  the number of elements.
     */
    @Override
    public Path subpath(final int beginIndex, final int endIndex) {
        return new DelegatePath(raw.subpath(beginIndex, endIndex));
    }

    /**
     * Tests if this path starts with the given path.
     *
     * @param other The path to test against.
     * @return {@code true} if this path starts with the given path; {@code false} otherwise.
     */
    @Override
    public boolean startsWith(final Path other) {
        if (other instanceof DelegatePath) {
            return raw.startsWith(((DelegatePath) other).raw);
        }
        return raw.startsWith(other);
    }

    /**
     * Tests if this path starts with the given path string.
     *
     * @param other The path string to test against.
     * @return {@code true} if this path starts with the given path string; {@code false} otherwise.
     */
    @Override
    public boolean startsWith(final String other) {
        return raw.startsWith(other);
    }

    /**
     * Tests if this path ends with the given path.
     *
     * @param other The path to test against.
     * @return {@code true} if this path ends with the given path; {@code false} otherwise.
     */
    @Override
    public boolean endsWith(final Path other) {
        if (other instanceof DelegatePath) {
            return raw.endsWith(((DelegatePath) other).raw);
        }
        return raw.endsWith(other);
    }

    /**
     * Tests if this path ends with the given path string.
     *
     * @param other The path string to test against.
     * @return {@code true} if this path ends with the given path string; {@code false} otherwise.
     */
    @Override
    public boolean endsWith(final String other) {
        return raw.endsWith(other);
    }

    /**
     * Returns a path that is this path with redundant name elements eliminated.
     *
     * @return A {@code DelegatePath} object representing the normalized path.
     */
    @Override
    public Path normalize() {
        return new DelegatePath(raw.normalize());
    }

    /**
     * Resolves the given path against this path. If the {@code other} path is absolute, then this method returns
     * {@code other}. If {@code other} is an empty path then this method returns this path. Otherwise this method
     * considers this path to be a directory and resolves the {@code other} path against this path.
     *
     * @param other The path to resolve against this path.
     * @return The resulting {@code DelegatePath} object.
     */
    @Override
    public Path resolve(final Path other) {
        if (other instanceof DelegatePath) {
            return new DelegatePath(raw.resolve(((DelegatePath) other).raw));
        }
        return new DelegatePath(raw.resolve(other));
    }

    /**
     * Resolves the given path string against this path. This method works in the same manner as {@link #resolve(Path)}
     * except that it converts the given path string to a path.
     *
     * @param other The path string to resolve against this path.
     * @return The resulting {@code DelegatePath} object.
     */
    @Override
    public Path resolve(final String other) {
        return new DelegatePath(raw.resolve(other));
    }

    /**
     * Resolves the given path against this path's parent path. This is useful where a file in a directory needs to be
     * replaced with another file.
     *
     * @param other The path to resolve against this path's parent.
     * @return The resulting {@code DelegatePath} object.
     */
    @Override
    public Path resolveSibling(final Path other) {
        if (other instanceof DelegatePath) {
            return new DelegatePath(raw.resolveSibling(((DelegatePath) other).raw));
        }
        return new DelegatePath(raw.resolveSibling(other));
    }

    /**
     * Resolves the given path string against this path's parent path. This method works in the same manner as
     * {@link #resolveSibling(Path)} except that it converts the given path string to a path.
     *
     * @param other The path string to resolve against this path's parent.
     * @return The resulting {@code DelegatePath} object.
     */
    @Override
    public Path resolveSibling(final String other) {
        return new DelegatePath(raw.resolveSibling(other));
    }

    /**
     * Constructs a relative path between this path and a given path.
     *
     * @param other The path to relativize against this path.
     * @return The resulting relative {@code DelegatePath} object.
     * @throws IllegalArgumentException If {@code other} is not an instance of {@code DelegatePath}.
     */
    @Override
    public Path relativize(final Path other) {
        if (other instanceof DelegatePath) {
            return new DelegatePath(raw.relativize(((DelegatePath) other).raw));
        }
        return new DelegatePath(raw.relativize(other));
    }

    /**
     * Returns a {@link URI} to represent this path.
     *
     * @return A {@link URI} that represents this path.
     * @throws IOError If an I/O error occurs when constructing the URI.
     */
    @Override
    public URI toUri() {
        return raw.toUri();
    }

    /**
     * Returns a {@code DelegatePath} object representing the absolute path of this path.
     *
     * @return A {@code DelegatePath} object representing the absolute path.
     * @throws IOError If an I/O error occurs when constructing the absolute path.
     */
    @Override
    public Path toAbsolutePath() {
        return new DelegatePath(raw.toAbsolutePath());
    }

    /**
     * Returns the real path of an existing file. This method resolves any symbolic links, and if the path is relative,
     * it is resolved against the current working directory.
     *
     * @param options Options indicating how symbolic links are handled.
     * @return A {@code DelegatePath} object representing the real path.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public Path toRealPath(final LinkOption... options) throws IOException {
        return new DelegatePath(raw.toRealPath(options));
    }

    /**
     * Returns a {@link File} object representing this path.
     *
     * @return A {@link File} object representing this path.
     */
    @Override
    public File toFile() {
        return raw.toFile();
    }

    /**
     * Registers the file denoted by this path with a watch service.
     *
     * @param watcher   The watch service to which this path is to be registered.
     * @param events    The events for which the path should be watched.
     * @param modifiers Modifiers to the watch event.
     * @return A {@link WatchKey} representing the registration of this path with the watch service.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public WatchKey register(
            final WatchService watcher,
            final WatchEvent.Kind<?>[] events,
            final WatchEvent.Modifier... modifiers) throws IOException {
        return raw.register(watcher, events, modifiers);
    }

    /**
     * Registers the file denoted by this path with a watch service.
     *
     * @param watcher The watch service to which this path is to be registered.
     * @param events  The events for which the path should be watched.
     * @return A {@link WatchKey} representing the registration of this path with the watch service.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public WatchKey register(final WatchService watcher, final WatchEvent.Kind<?>... events) throws IOException {
        return raw.register(watcher, events);
    }

    /**
     * Returns an iterator over the name elements of this path. The first element returned by the iterator is the
     * element closest to the root in the directory hierarchy, and the last element is the element farthest from the
     * root.
     *
     * @return An iterator over the name elements of this path.
     */
    @Override
    public Iterator<Path> iterator() {
        return new Iterator<>() {

            private final Iterator<Path> itr = raw.iterator();

            /**
             * Returns true if the iteration has more elements.
             *
             * @return true if the iteration has more elements
             */
            @Override
            public boolean hasNext() {
                return itr.hasNext();
            }

            /**
             * Returns the next element in the iteration.
             *
             * @return the next element
             */
            @Override
            public Path next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return new DelegatePath(itr.next());
            }
        };
    }

    /**
     * Compares this path to another path.
     *
     * @param other The path to compare against.
     * @return Zero if this path is equal to the other path; a value less than zero if this path is lexicographically
     *         less than the other path; and a value greater than zero if this path is lexicographically greater than
     *         the other path.
     */
    @Override
    public int compareTo(final Path other) {
        if (other instanceof DelegatePath) {
            return raw.compareTo(((DelegatePath) other).raw);
        }
        return raw.compareTo(other);
    }

    /**
     * Tests this path for equality with the given object.
     *
     * @param other The object to which this path is to be compared.
     * @return {@code true} if, and only if, the given object is a {@code DelegatePath} that represents the same path as
     *         this path.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof DelegatePath) {
            return raw.equals(((DelegatePath) other).raw);
        }
        if (other instanceof Path) {
            return raw.equals(other);
        }
        return false;
    }

    /**
     * Computes a hash code for this path.
     *
     * @return The hash code for this path.
     */
    @Override
    public int hashCode() {
        return raw.hashCode();
    }

    /**
     * Returns the string representation of this path.
     *
     * @return The string representation of this path.
     */
    @Override
    public String toString() {
        return raw.toString();
    }

    // Convenience methods for Files class functionalities

    /**
     * Tests whether the file denoted by this path exists.
     *
     * @param options Options to configure how the existence check is performed.
     * @return {@code true} if the file exists; {@code false} otherwise.
     * @see Files#exists(Path, LinkOption...)
     */
    public boolean exists(final LinkOption... options) {
        return Files.exists(raw, options);
    }

    /**
     * Checks if this path is a sub-path of the given parent path.
     *
     * @param parent The parent path to check against.
     * @return {@code true} if this path is a sub-path of the parent; {@code false} otherwise.
     */
    public boolean isSubOf(final Path parent) {
        return PathResolve.isSub(parent, this.raw);
    }

    /**
     * Tests whether the file denoted by this path does not exist.
     *
     * @param options Options to configure how the non-existence check is performed.
     * @return {@code true} if the file does not exist; {@code false} otherwise.
     * @see Files#notExists(Path, LinkOption...)
     */
    public boolean notExists(final LinkOption... options) {
        return Files.notExists(raw, options);
    }

    /**
     * Tests whether the file denoted by this path is a directory.
     *
     * @param options Options to configure how symbolic links are handled.
     * @return {@code true} if the file is a directory; {@code false} otherwise.
     * @see Files#isDirectory(Path, LinkOption...)
     */
    public boolean isDirectory(final LinkOption... options) {
        return Files.isDirectory(raw, options);
    }

    /**
     * Tests whether the file denoted by this path is a regular file.
     *
     * @param options Options to configure how symbolic links are handled.
     * @return {@code true} if the file is a regular file; {@code false} otherwise.
     * @see Files#isRegularFile(Path, LinkOption...)
     */
    public boolean isFile(final LinkOption... options) {
        return Files.isRegularFile(raw, options);
    }

    /**
     * Tests whether the file denoted by this path is a symbolic link.
     *
     * @return {@code true} if the file is a symbolic link; {@code false} otherwise.
     * @see Files#isSymbolicLink(Path)
     */
    public boolean isSymbolicLink() {
        return Files.isSymbolicLink(raw);
    }

    /**
     * Tests whether the file denoted by this path is of an \"other\" type. An \"other\" type is a file that is not a
     * regular file, a directory, or a symbolic link.
     *
     * @return {@code true} if the file is of an \"other\" type; {@code false} otherwise.
     * @throws InternalException if an I/O error occurs while reading file attributes.
     */
    public boolean isOther() {
        return PathResolve.isOther(this.raw);
    }

    /**
     * Tests whether the file denoted by this path is executable.
     *
     * @return {@code true} if the file is executable; {@code false} otherwise.
     * @see Files#isExecutable(Path)
     */
    public boolean isExecutable() {
        return Files.isExecutable(raw);
    }

    /**
     * Tests whether the file denoted by this path is readable.
     *
     * @return {@code true} if the file is readable; {@code false} otherwise.
     * @see Files#isReadable(Path)
     */
    public boolean isReadable() {
        return Files.isReadable(raw);
    }

    /**
     * Tests whether the file denoted by this path is writable.
     *
     * @return {@code true} if the file is writable; {@code false} otherwise.
     * @see Files#isWritable(Path)
     */
    public boolean isWritable() {
        return Files.isWritable(raw);
    }

    /**
     * Returns the size of the file denoted by this path in bytes.
     *
     * @return The file size in bytes.
     * @throws InternalException if an I/O error occurs.
     * @see Files#size(Path)
     */
    @Override
    public long size() {
        try {
            return Files.size(raw);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Returns the name of the file or directory denoted by this path.
     *
     * @return The file name as a {@link String}.
     */
    @Override
    public String getName() {
        return PathResolve.getName(this.raw);
    }

    /**
     * Deletes the file or directory denoted by this path. If this path is a directory, the directory must be empty.
     *
     * @throws InternalException if an I/O error occurs.
     * @see Files#delete(Path)
     */
    public void delete() {
        try {
            Files.delete(raw);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Deletes the file or directory denoted by this path if it exists.
     *
     * @return {@code true} if the file was deleted by this method; {@code false} if the file could not be deleted
     *         because it did not exist.
     * @throws InternalException if an I/O error occurs.
     * @see Files#deleteIfExists(Path)
     */
    public boolean deleteIfExists() {
        try {
            return Files.deleteIfExists(raw);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Creates a new directory denoted by this path.
     *
     * @param attrs An optional list of file attributes to set atomically when creating the directory.
     * @return A new {@code DelegatePath} object representing the created directory.
     * @throws InternalException if an I/O error occurs.
     * @see Files#createDirectory(Path, FileAttribute[])
     */
    public DelegatePath createDirectory(final FileAttribute<?>... attrs) {
        try {
            return new DelegatePath(Files.createDirectory(raw, attrs));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Creates a directory denoted by this path, along with any necessary but nonexistent parent directories.
     *
     * @param attrs An optional list of file attributes to set atomically when creating the directory.
     * @return A new {@code DelegatePath} object representing the created directory.
     * @throws InternalException if an I/O error occurs.
     * @see Files#createDirectories(Path, FileAttribute[])
     */
    public DelegatePath createDirectories(final FileAttribute<?>... attrs) {
        try {
            return new DelegatePath(Files.createDirectories(raw, attrs));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Creates a new empty file denoted by this path.
     *
     * @param attrs An optional list of file attributes to set atomically when creating the file.
     * @return A new {@code DelegatePath} object representing the created file.
     * @throws InternalException if an I/O error occurs.
     * @see Files#createFile(Path, FileAttribute[])
     */
    public DelegatePath createFile(final FileAttribute<?>... attrs) {
        try {
            return new DelegatePath(Files.createFile(raw, attrs));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Creates a new empty directory in the directory denoted by this path, using the given prefix to generate its name.
     *
     * @param prefix The prefix string to be used in generating the directory's name; may be {@code null}.
     * @param attrs  An optional list of file attributes to set atomically when creating the directory.
     * @return A new {@code DelegatePath} object representing the created temporary directory.
     * @throws InternalException if an I/O error occurs.
     * @see Files#createTempDirectory(Path, String, FileAttribute[])
     */
    public DelegatePath createTempDirectory(final String prefix, final FileAttribute<?>... attrs) {
        try {
            return new DelegatePath(Files.createTempDirectory(raw, prefix, attrs));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Creates a new empty file in the directory denoted by this path, using the given prefix and suffix to generate its
     * name.
     *
     * @param prefix The prefix string to be used in generating the file's name; may be {@code null}.
     * @param suffix The suffix string to be used in generating the file's name; may be {@code null}.
     * @param attrs  An optional list of file attributes to set atomically when creating the file.
     * @return A new {@code DelegatePath} object representing the created temporary file.
     * @throws InternalException if an I/O error occurs.
     * @see Files#createTempFile(Path, String, String, FileAttribute[])
     */
    public DelegatePath createTempFile(final String prefix, final String suffix, final FileAttribute<?>... attrs) {
        try {
            return new DelegatePath(Files.createTempFile(raw, prefix, suffix, attrs));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Copies the file denoted by this path to the target path.
     *
     * @param target  The target path to copy the file to.
     * @param options Options specifying how the copy should be performed.
     * @return A new {@code DelegatePath} object representing the target path.
     * @throws InternalException if an I/O error occurs.
     * @see Files#copy(Path, Path, CopyOption...)
     */
    public DelegatePath copyTo(final Path target, final CopyOption... options) {
        Path actualTarget = target;
        if (target instanceof DelegatePath) {
            actualTarget = ((DelegatePath) target).raw;
        }
        try {
            Files.copy(raw, actualTarget, options);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return new DelegatePath(actualTarget);
    }

    /**
     * Moves the file denoted by this path to the target path.
     *
     * @param target  The target path to move the file to.
     * @param options Options specifying how the move should be performed.
     * @return A new {@code DelegatePath} object representing the target path.
     * @throws InternalException if an I/O error occurs.
     * @see Files#move(Path, Path, CopyOption...)
     */
    public DelegatePath moveTo(final Path target, final CopyOption... options) {
        Path actualTarget = target;
        if (target instanceof DelegatePath) {
            actualTarget = ((DelegatePath) target).raw;
        }
        try {
            Files.move(raw, actualTarget, options);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return new DelegatePath(actualTarget);
    }

    /**
     * Checks if the directory denoted by this path is empty.
     *
     * @return {@code true} if the directory is empty; {@code false} otherwise.
     */
    public boolean isDirEmpty() {
        return PathResolve.isDirEmpty(this);
    }

    /**
     * Lists all files and directories directly within the directory denoted by this path (non-recursive).
     *
     * @param filter A {@link Predicate} to filter the files. If {@code null}, all files are returned.
     * @return An array of {@link Path} objects representing the files and directories that match the filter.
     */
    public Path[] listFiles(final Predicate<? super Path> filter) {
        return PathResolve.listFiles(this, filter);
    }

    /**
     * Traverses the file tree rooted at this path and applies a {@link FileVisitor} to each file and directory.
     *
     * @param options  A set of {@link FileVisitOption} to configure the traversal.
     * @param maxDepth The maximum depth of directories to visit. Use {@link Integer#MAX_VALUE} for unlimited depth.
     * @param visitor  The {@link FileVisitor} to apply during traversal.
     * @throws InternalException if an I/O error occurs during traversal.
     * @see Files#walkFileTree(Path, Set, int, FileVisitor)
     */
    public void walkFiles(
            final Set<FileVisitOption> options,
            final int maxDepth,
            final FileVisitor<? super Path> visitor) {
        try {
            Files.walkFileTree(this.raw, options, maxDepth, visitor);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Retrieves the basic file attributes for the file denoted by this path.
     *
     * @param options Options to configure how symbolic links are handled.
     * @return The {@link BasicFileAttributes} of the file.
     */
    public BasicFileAttributes getAttributes(final LinkOption... options) {
        return PathResolve.getAttributes(this.raw, options);
    }

    /**
     * Obtains a buffered input stream for the file denoted by this path.
     *
     * @param options Options specifying how the file is opened.
     * @return A {@link BufferedInputStream} for the file.
     */
    public BufferedInputStream getStream(final LinkOption... options) {
        return PathResolve.getInputStream(this, options);
    }

    /**
     * Obtains an input stream for the file denoted by this path.
     *
     * @return An {@link InputStream} for the file.
     */
    @Override
    public InputStream getStream() {
        return getStream(new LinkOption[0]);
    }

    /**
     * Returns a {@link URL} to represent the file denoted by this path.
     *
     * @return A {@link URL} that represents this path.
     * @throws InternalException if a {@link MalformedURLException} occurs.
     */
    @Override
    public URL getUrl() {
        try {
            return this.raw.toUri().toURL();
        } catch (final MalformedURLException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Obtains a {@link Reader} for the file denoted by this path with a specified character set and open options.
     *
     * @param charset The character set to use for reading.
     * @param options Options specifying how the file is opened.
     * @return A {@link Reader} for the file.
     */
    public Reader getReader(final Charset charset, final OpenOption... options) {
        return PathResolve.getReader(this, charset, options);
    }

    /**
     * Reads all bytes from the file denoted by this path.
     *
     * @return A byte array containing all the bytes from the file.
     * @throws InternalException if an I/O error occurs.
     */
    @Override
    public byte[] readBytes() {
        return PathResolve.readBytes(this);
    }

    /**
     * Obtains a buffered output stream for the file denoted by this path with specified open options.
     *
     * @param options Options specifying how the file is opened.
     * @return A {@link BufferedOutputStream} for the file.
     */
    public BufferedOutputStream getOutputStream(final OpenOption... options) {
        return PathResolve.getOutputStream(this, options);
    }

    /**
     * Retrieves the MIME type of the file denoted by this path.
     *
     * @return The MIME type string (e.g., "image/jpeg", "text/plain"), or {@code null} if the MIME type cannot be
     *         determined.
     */
    public String getMimeType() {
        return PathResolve.getMimeType(this);
    }

}
