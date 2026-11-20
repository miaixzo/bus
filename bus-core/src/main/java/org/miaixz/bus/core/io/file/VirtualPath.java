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
     * Virtual paths do not support file system operations, so this method throws an
     * {@link UnsupportedOperationException}.
     *
     * @return no value because this operation always raises UnsupportedOperationException
     */
    @Override
    public FileSystem getFileSystem() {
        throw new UnsupportedOperationException("VirtualPath does not support FileSystem operations");
    }

    /**
     * Virtual paths are always considered relative, so this method always returns {@code false}.
     *
     * @return false because virtual paths are relative
     */
    @Override
    public boolean isAbsolute() {
        return false;
    }

    /**
     * Virtual paths do not have a root component, so this method returns {@code null}.
     *
     * @return null because virtual paths have no root component
     */
    @Override
    public Path getRoot() {
        return null;
    }

    /**
     * Returns the final name element of this virtual path.
     *
     * @return last virtual-path name element
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
     * Returns the parent of this virtual path, or null when it has no parent.
     *
     * @return parent virtual path, or null if there is no parent
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
     * Returns the number of name elements in this virtual path.
     *
     * @return number of name elements
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
     * Returns the virtual path representing the selected name element.
     *
     * @param index The index of the element.
     * @return virtual path for the requested name element
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
     * Returns the selected contiguous range of virtual-path name elements.
     *
     * @param beginIndex The index of the first element, inclusive.
     * @param endIndex   The index of the last element, exclusive.
     * @return virtual path containing the requested name range
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
     * Tests whether this virtual path begins with the supplied path.
     *
     * @param other path or value to compare or resolve against this virtual path
     * @return whether this path begins with the supplied path
     */
    @Override
    public boolean startsWith(final Path other) {
        if (!(other instanceof final VirtualPath otherPath)) {
            return false;
        }
        return this.path.startsWith(otherPath.path);
    }

    /**
     * Tests whether this virtual path begins with the supplied path.
     *
     * @param other path or value to compare or resolve against this virtual path
     * @return whether this path begins with the supplied path
     */
    @Override
    public boolean startsWith(final String other) {
        return this.path.startsWith(other);
    }

    /**
     * Tests whether this virtual path ends with the supplied path.
     *
     * @param other path or value to compare or resolve against this virtual path
     * @return whether this path ends with the supplied path
     */
    @Override
    public boolean endsWith(final Path other) {
        if (!(other instanceof final VirtualPath otherPath)) {
            return false;
        }
        return this.path.endsWith(otherPath.path);
    }

    /**
     * Tests whether this virtual path ends with the supplied path.
     *
     * @param other path or value to compare or resolve against this virtual path
     * @return whether this path ends with the supplied path
     */
    @Override
    public boolean endsWith(final String other) {
        return this.path.endsWith(other);
    }

    /**
     * Returns this path with redundant name elements normalized.
     *
     * @return normalized virtual path
     */
    @Override
    public Path normalize() {
        return this;
    }

    /**
     * Resolves another path against this virtual path.
     *
     * @param other path or value to compare or resolve against this virtual path
     * @return resolved virtual path
     */
    @Override
    public Path resolve(final Path other) {
        if (other.isAbsolute()) {
            return other;
        }
        if (other.toString().isEmpty()) {
            return this;
        }
        final String newPath = this.path + Symbol.SLASH + other;
        return new VirtualPath(newPath, other instanceof VirtualPath ? ((VirtualPath) other).content : null);
    }

    /**
     * Resolves another path against this virtual path.
     *
     * @param other path or value to compare or resolve against this virtual path
     * @return resolved virtual path
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
     * Resolves another path against the parent of this virtual path.
     *
     * @param other path or value to compare or resolve against this virtual path
     * @return resolved sibling virtual path
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
     * Resolves another path against the parent of this virtual path.
     *
     * @param other path or value to compare or resolve against this virtual path
     * @return resolved sibling virtual path
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
     * Returns the relative path from this virtual path to another path.
     *
     * @param other path or value to compare or resolve against this virtual path
     * @return relative path to the supplied path
     */
    @Override
    public Path relativize(final Path other) {
        if (!(other instanceof final VirtualPath otherPath)) {
            throw new IllegalArgumentException("other must be a VirtualPath");
        }
        if (this.path.isEmpty()) {
            return otherPath;
        }
        if (otherPath.path.startsWith(this.path + Symbol.SLASH)) {
            return new VirtualPath(otherPath.path.substring(this.path.length() + 1), otherPath.content);
        }
        return otherPath;
    }

    /**
     * Virtual paths do not support URI conversion, so this method throws an {@link UnsupportedOperationException}.
     *
     * @return no value because virtual paths do not support URI conversion
     */
    @Override
    public URI toUri() {
        throw new UnsupportedOperationException("VirtualPath does not support URI conversion");
    }

    /**
     * For a virtual path, this returns the path itself.
     *
     * @return this virtual path
     */
    @Override
    public Path toAbsolutePath() {
        return this;
    }

    /**
     * For a virtual path, this returns the path itself.
     *
     * @param options Options indicating how symbolic links are handled.
     * @return this virtual path without accessing a filesystem
     */
    @Override
    public Path toRealPath(final LinkOption... options) {
        return this;
    }

    /**
     * Rejects conversion because this virtual path has no backing file.
     *
     * @return no value because virtual paths cannot be converted to files
     * @throws UnsupportedOperationException always, because a virtual path has no backing file
     */
    @Override
    public File toFile() {
        return new VirtualFile(path, content);
    }

    /**
     * Virtual paths do not support watch services, so this method throws an {@link UnsupportedOperationException}.
     *
     * @param watcher   The watch service to which this path is to be registered.
     * @param events    The events for which the path should be watched.
     * @param modifiers Modifiers to the watch event.
     * @return no value because virtual paths do not support watch registration
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public WatchKey register(
            final WatchService watcher,
            final WatchEvent.Kind<?>[] events,
            final WatchEvent.Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException("VirtualPath does not support watch service");
    }

    /**
     * Virtual paths do not support watch services, so this method throws an {@link UnsupportedOperationException}.
     *
     * @param watcher The watch service to which this path is to be registered.
     * @param events  The events for which the path should be watched.
     * @return no value because virtual paths do not support watch registration
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public WatchKey register(final WatchService watcher, final WatchEvent.Kind<?>... events) throws IOException {
        throw new UnsupportedOperationException("VirtualPath does not support watch service");
    }

    /**
     * Returns the virtual path name elements in traversal order.
     *
     * @return iterator over the virtual-path name elements
     */
    @Override
    public Iterator<Path> iterator() {
        return new Iterator<>() {

            /**
             * Individual name elements of the virtual path.
             */
            private final List<String> parts = CharsBacker.splitTrim(path, Symbol.SLASH);
            /**
             * Index of the next path element to return.
             */
            private int index = 0;

            /**
             * Returns true if the iteration has more elements.
             *
             * @return true if the iteration has more elements
             */
            @Override
            public boolean hasNext() {
                return index < parts.size();
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
                return new VirtualPath(parts.get(index++), index == parts.size() ? content : null);
            }

        };
    }

    /**
     * Compares the stored path text with another virtual path.
     *
     * @param other path or value to compare or resolve against this virtual path
     * @return lexicographic ordering of the stored path strings
     */
    @Override
    public int compareTo(final Path other) {
        if (!(other instanceof final VirtualPath otherPath)) {
            throw new ClassCastException("Cannot compare VirtualPath with " + other.getClass().getName());
        }
        return this.path.compareTo(otherPath.path);
    }

    /**
     * Tests whether another virtual path contains the same path text.
     *
     * @param other path or value to compare or resolve against this virtual path
     * @return whether both values are virtual paths containing the same path text
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
     * Returns the hash code of the stored path text.
     *
     * @return hash code of the stored path text
     */
    @Override
    public int hashCode() {
        return path.hashCode();
    }

    /**
     * Returns the stored path text.
     *
     * @return stored path text
     */
    @Override
    public String toString() {
        return path;
    }

}
