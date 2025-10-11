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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A virtual path class that implements {@link Path} to simulate a file path in memory.
 * <p>
 * This class provides a way to represent a file system path in memory without needing to access the physical file
 * system. It can be used for testing, simulating file system operations, or handling file paths in memory.
 * 
 * <p>
 * A virtual path can be associated with a {@link Resource} object, which represents the content at that path.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class VirtualPath implements Path {

    /**
     * The path string.
     */
    private final String path;

    /**
     * The resource representing the content at this path.
     */
    private final Resource content;

    /**
     * Constructs a new virtual path.
     *
     * @param path    The path string, cannot be null.
     * @param content The resource for the path's content, which can be null.
     * @throws IllegalArgumentException if the path is null.
     */
    public VirtualPath(final String path, final Resource content) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        this.path = path;
        this.content = content;
    }

    /**
     * Gets the resource representing the path's content.
     *
     * @return The content resource, which may be null.
     */
    public Resource getContent() {
        return this.content;
    }

    /**
     * Gets the file content as a byte array.
     *
     * @return A byte array of the content, or null if there is no content.
     */
    public byte[] getBytes() {
        return null != this.content ? this.content.readBytes() : null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Virtual paths do not support file system operations, so this method throws an
     * {@link UnsupportedOperationException}.
     */
    @Override
    public FileSystem getFileSystem() {
        throw new UnsupportedOperationException("VirtualPath does not support FileSystem operations");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Virtual paths are always considered relative, so this method always returns {@code false}.
     */
    @Override
    public boolean isAbsolute() {
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Virtual paths do not have a root component, so this method returns {@code null}.
     */
    @Override
    public Path getRoot() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getFileName() {
        final int index = path.lastIndexOf(Symbol.C_SLASH);
        if (index == -1) {
            return new VirtualPath(path, content);
        }
        return new VirtualPath(path.substring(index + 1), content);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getParent() {
        final int index = path.lastIndexOf(Symbol.C_SLASH);
        if (index == -1) {
            return null;
        }
        return new VirtualPath(path.substring(0, index), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNameCount() {
        if (StringKit.isEmpty(path)) {
            return 1;
        }
        if (StringKit.equals(path, Symbol.SLASH)) {
            return 0;
        }
        return StringKit.count(path, Symbol.SLASH) + 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getName(final int index) {
        if (index < 0) {
            throw new IllegalArgumentException("Index must be >= 0");
        }
        final List<String> parts = CharsBacker.splitTrim(path, Symbol.SLASH);
        if (index >= parts.size()) {
            throw new IllegalArgumentException("Index exceeds name count");
        }
        return new VirtualPath(parts.get(index), index == parts.size() - 1 ? content : null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path subpath(final int beginIndex, final int endIndex) {
        if (beginIndex < 0 || endIndex <= beginIndex) {
            throw new IllegalArgumentException("beginIndex or endIndex is invalid");
        }
        final List<String> parts = CharsBacker.splitTrim(path, Symbol.SLASH);
        if (endIndex > parts.size()) {
            throw new IllegalArgumentException("endIndex exceeds name count");
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = beginIndex; i < endIndex; i++) {
            if (!sb.isEmpty()) {
                sb.append(Symbol.C_SLASH);
            }
            sb.append(parts.get(i));
        }
        return new VirtualPath(sb.toString(), endIndex == parts.size() ? content : null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean startsWith(final Path other) {
        if (!(other instanceof final VirtualPath otherPath)) {
            return false;
        }
        return this.path.startsWith(otherPath.path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean startsWith(final String other) {
        return this.path.startsWith(other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean endsWith(final Path other) {
        if (!(other instanceof final VirtualPath otherPath)) {
            return false;
        }
        return this.path.endsWith(otherPath.path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean endsWith(final String other) {
        return this.path.endsWith(other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path normalize() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path resolve(final Path other) {
        if (other.isAbsolute()) {
            return other;
        }
        if (other.toString().isEmpty()) {
            return this;
        }
        final String newPath = this.path + "/" + other;
        return new VirtualPath(newPath, other instanceof VirtualPath ? ((VirtualPath) other).content : null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path resolve(final String other) {
        if (other.isEmpty()) {
            return this;
        }
        final String newPath = this.path + Symbol.SLASH + other;
        return new VirtualPath(newPath, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path resolveSibling(final Path other) {
        if (other == null) {
            throw new NullPointerException("other cannot be null");
        }
        final Path parent = getParent();
        return (parent == null) ? other : parent.resolve(other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path resolveSibling(final String other) {
        if (other == null) {
            throw new NullPointerException("other cannot be null");
        }
        final Path parent = getParent();
        return (parent == null) ? new VirtualPath(other, null) : parent.resolve(other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path relativize(final Path other) {
        if (!(other instanceof final VirtualPath otherPath)) {
            throw new IllegalArgumentException("other must be a VirtualPath");
        }
        if (this.path.isEmpty()) {
            return otherPath;
        }
        if (otherPath.path.startsWith(this.path + "/")) {
            return new VirtualPath(otherPath.path.substring(this.path.length() + 1), otherPath.content);
        }
        return otherPath;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Virtual paths do not support URI conversion, so this method throws an {@link UnsupportedOperationException}.
     */
    @Override
    public URI toUri() {
        throw new UnsupportedOperationException("VirtualPath does not support URI conversion");
    }

    /**
     * {@inheritDoc}
     * <p>
     * For a virtual path, this returns the path itself.
     */
    @Override
    public Path toAbsolutePath() {
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * For a virtual path, this returns the path itself.
     */
    @Override
    public Path toRealPath(final LinkOption... options) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File toFile() {
        return new VirtualFile(path, content);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Virtual paths do not support watch services, so this method throws an {@link UnsupportedOperationException}.
     */
    @Override
    public WatchKey register(
            final WatchService watcher,
            final WatchEvent.Kind<?>[] events,
            final WatchEvent.Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException("VirtualPath does not support watch service");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Virtual paths do not support watch services, so this method throws an {@link UnsupportedOperationException}.
     */
    @Override
    public WatchKey register(final WatchService watcher, final WatchEvent.Kind<?>... events) throws IOException {
        throw new UnsupportedOperationException("VirtualPath does not support watch service");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Path> iterator() {
        return new Iterator<>() {

            private int index = 0;
            private final List<String> parts = CharsBacker.splitTrim(path, Symbol.SLASH);

            @Override
            public boolean hasNext() {
                return index < parts.size();
            }

            @Override
            public Path next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return new VirtualPath(parts.get(index++), index == parts.size() ? content : null);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Path other) {
        if (!(other instanceof final VirtualPath otherPath)) {
            throw new ClassCastException("Cannot compare VirtualPath with " + other.getClass().getName());
        }
        return this.path.compareTo(otherPath.path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof final VirtualPath otherPath)) {
            return false;
        }
        return this.path.equals(otherPath.path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return path.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return path;
    }

}
