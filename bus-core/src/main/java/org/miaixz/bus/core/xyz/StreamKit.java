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
package org.miaixz.bus.core.xyz;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.miaixz.bus.core.center.stream.spliterators.DropWhileSpliterator;
import org.miaixz.bus.core.center.stream.spliterators.IterateSpliterator;
import org.miaixz.bus.core.center.stream.spliterators.TakeWhileSpliterator;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.tree.HierarchyIterator;

/**
 * Utility class for {@link Stream}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StreamKit {

    /**
     * Creates a {@link Stream} from an array.
     *
     * @param array The array.
     * @param <T>   The element type.
     * @return A {@link Stream}, or {@link Stream#empty()} if the array is `null`.
     */
    @SafeVarargs
    public static <T> Stream<T> of(final T... array) {
        return null == array ? Stream.empty() : Stream.of(array);
    }

    /**
     * Converts an {@link Iterable} to a sequential {@link Stream}.
     *
     * @param iterable The iterable.
     * @param <T>      The element type.
     * @return A {@link Stream}, or {@link Stream#empty()} if the iterable is `null`.
     */
    public static <T> Stream<T> of(final Iterable<T> iterable) {
        return of(iterable, false);
    }

    /**
     * Converts an {@link Iterable} to a {@link Stream}.
     *
     * @param iterable The iterable.
     * @param parallel If `true`, creates a parallel stream.
     * @param <T>      The element type.
     * @return A {@link Stream}, or {@link Stream#empty()} if the iterable is `null`.
     */
    public static <T> Stream<T> of(final Iterable<T> iterable, final boolean parallel) {
        if (null == iterable) {
            return Stream.empty();
        }
        return iterable instanceof Collection
                ? parallel ? ((Collection<T>) iterable).parallelStream() : ((Collection<T>) iterable).stream()
                : StreamSupport.stream(iterable.spliterator(), parallel);
    }

    /**
     * Converts an {@link Iterator} to a sequential {@link Stream}.
     *
     * @param iterator The iterator.
     * @param <T>      The element type.
     * @return A {@link Stream}, or {@link Stream#empty()} if the iterator is `null`.
     */
    public static <T> Stream<T> ofIter(final Iterator<T> iterator) {
        return ofIter(iterator, false);
    }

    /**
     * Converts an {@link Iterator} to a {@link Stream}.
     *
     * @param iterator The iterator.
     * @param parallel If `true`, creates a parallel stream.
     * @param <T>      The element type.
     * @return A {@link Stream}, or {@link Stream#empty()} if the iterator is `null`.
     */
    public static <T> Stream<T> ofIter(final Iterator<T> iterator, final boolean parallel) {
        if (null == iterator) {
            return Stream.empty();
        }
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), parallel);
    }

    /**
     * Reads a file line by line into a {@link Stream}.
     *
     * @param file The file.
     * @return A {@link Stream} of lines, or {@link Stream#empty()} if the file is `null`.
     */
    public static Stream<String> of(final File file) {
        return of(file, Charset.UTF_8);
    }

    /**
     * Reads a file line by line into a {@link Stream}.
     *
     * @param path The file path.
     * @return A {@link Stream} of lines, or {@link Stream#empty()} if the path is `null`.
     */
    public static Stream<String> of(final Path path) {
        return of(path, Charset.UTF_8);
    }

    /**
     * Reads a file line by line into a {@link Stream} with a specified charset.
     *
     * @param file    The file.
     * @param charset The character set.
     * @return A {@link Stream} of lines.
     */
    public static Stream<String> of(final File file, final java.nio.charset.Charset charset) {
        if (null == file) {
            return Stream.empty();
        }
        return of(file.toPath(), charset);
    }

    /**
     * Reads a file line by line into a {@link Stream} with a specified charset.
     *
     * @param path    The file path.
     * @param charset The character set.
     * @return A {@link Stream} of lines.
     */
    public static Stream<String> of(final Path path, final java.nio.charset.Charset charset) {
        if (null == path) {
            return Stream.empty();
        }
        try {
            return Files.lines(path, charset);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Creates a {@link Stream} using an initial value and a generation function.
     *
     * @param seed           The initial value.
     * @param elementCreator The function to generate the next element.
     * @param limit          The maximum number of elements.
     * @param <T>            The element type.
     * @return A {@link Stream}.
     */
    public static <T> Stream<T> of(final T seed, final UnaryOperator<T> elementCreator, final int limit) {
        return Stream.iterate(seed, elementCreator).limit(limit);
    }

    /**
     * Joins all elements in a stream into a single string with a specified separator.
     *
     * @param stream    The {@link Stream}.
     * @param delimiter The separator.
     * @param <T>       The element type.
     * @return The joined string, or `null` if the stream is `null`.
     */
    public static <T> String join(final Stream<T> stream, final CharSequence delimiter) {
        if (null == stream) {
            return null;
        }
        return stream.collect(CollectorKit.joining(delimiter));
    }

    /**
     * Joins all elements in a stream into a single string with a specified separator and a custom toString function.
     *
     * @param stream       The {@link Stream}.
     * @param delimiter    The separator.
     * @param toStringFunc The function to convert elements to strings.
     * @param <T>          The element type.
     * @return The joined string, or `null` if the stream is `null`.
     */
    public static <T> String join(
            final Stream<T> stream,
            final CharSequence delimiter,
            final Function<T, ? extends CharSequence> toStringFunc) {
        if (null == stream) {
            return null;
        }
        return stream.collect(CollectorKit.joining(delimiter, toStringFunc));
    }

    /**
     * Returns an infinite ordered stream produced by iterative application of a function `next` to an initial element
     * `seed`.
     *
     * @param <T>     The element type.
     * @param seed    The initial value.
     * @param hasNext A predicate to apply to elements to determine if the stream should continue.
     * @param next    A function to be applied to the previous element to produce a new element.
     * @return An infinite ordered stream.
     */
    public static <T> Stream<T> iterate(final T seed, final Predicate<? super T> hasNext, final UnaryOperator<T> next) {
        Objects.requireNonNull(next);
        Objects.requireNonNull(hasNext);
        return StreamSupport.stream(IterateSpliterator.of(seed, hasNext, next), false);
    }

    /**
     * Given a root node of a hierarchical structure (e.g., a tree or graph), returns a stream of all nodes in the
     * hierarchy, including the root, in breadth-first order.
     *
     * @param root       The root node.
     * @param discoverer A function to get the children of a node.
     * @param filter     A filter for nodes; non-matching nodes and their subtrees will be ignored.
     * @param <T>        The element type.
     * @return A stream of all nodes in the hierarchy.
     * @see HierarchyIterator
     */
    public static <T> Stream<T> iterateHierarchies(
            final T root,
            final Function<T, Collection<T>> discoverer,
            final Predicate<T> filter) {
        return ofIter(HierarchyIterator.breadthFirst(root, discoverer, filter));
    }

    /**
     * Given a root node of a hierarchical structure, returns a stream of all nodes in the hierarchy in breadth-first
     * order.
     *
     * @param root       The root node.
     * @param discoverer A function to get the children of a node.
     * @param <T>        The element type.
     * @return A stream of all nodes in the hierarchy.
     * @see HierarchyIterator
     */
    public static <T> Stream<T> iterateHierarchies(final T root, final Function<T, Collection<T>> discoverer) {
        return ofIter(HierarchyIterator.breadthFirst(root, discoverer));
    }

    /**
     * Returns a stream consisting of the longest prefix of elements taken from this stream that match the given
     * predicate.
     *
     * @param source    The source stream.
     * @param <T>       The element type.
     * @param predicate The predicate.
     * @return The new stream.
     */
    public static <T> Stream<T> takeWhile(final Stream<T> source, final Predicate<? super T> predicate) {
        if (null == source) {
            return Stream.empty();
        }
        Objects.requireNonNull(predicate);
        return createStatefulNewStream(source, TakeWhileSpliterator.of(source.spliterator(), predicate));
    }

    /**
     * Returns a stream consisting of the remaining elements of this stream after dropping the longest prefix of
     * elements that match the given predicate.
     *
     * @param source    The source stream.
     * @param <T>       The element type.
     * @param predicate The predicate.
     * @return The new stream.
     */
    public static <T> Stream<T> dropWhile(final Stream<T> source, final Predicate<? super T> predicate) {
        if (null == source) {
            return Stream.empty();
        }
        Objects.requireNonNull(predicate);
        return createStatefulNewStream(source, DropWhileSpliterator.of(source.spliterator(), predicate));
    }

    /**
     * Reads exactly `len` bytes from an input stream into a byte array.
     *
     * @param in  The input stream.
     * @param b   The buffer to read into.
     * @param off The offset in the buffer.
     * @param len The number of bytes to read.
     * @throws IOException if an I/O error occurs or the end of the stream is reached.
     */
    public static void readFully(InputStream in, byte[] b, int off, int len) throws IOException {
        if (readAvailable(in, b, off, len) < len)
            throw new EOFException();
    }

    /**
     * Skips exactly `n` bytes of input from an input stream.
     *
     * @param in The input stream.
     * @param n  The number of bytes to skip.
     * @throws IOException if an I/O error occurs or the end of the stream is reached.
     */
    public static void skipFully(InputStream in, long n) throws IOException {
        while (n > 0) {
            long count = in.skip(n);
            if (count == 0) {
                if (in.read() == -1) {
                    throw new EOFException();
                }
                count = 1;
            }
            n -= count;
        }
    }

    /**
     * Reads up to `len` bytes from an input stream into a byte array.
     *
     * @param in  The input stream.
     * @param b   The buffer.
     * @param off The offset.
     * @param len The maximum number of bytes to read.
     * @return The total number of bytes read.
     * @throws IOException if an I/O error occurs.
     */
    public static int readAvailable(InputStream in, byte[] b, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > b.length)
            throw new IndexOutOfBoundsException();
        int wpos = off;
        while (len > 0) {
            int count = in.read(b, wpos, len);
            if (count < 0)
                break;
            wpos += count;
            len -= count;
        }
        return wpos - off;
    }

    /**
     * Creates a new stateful stream from a source stream and a new Spliterator.
     *
     * @param source         The source stream.
     * @param newSpliterator The new Spliterator.
     * @param <T>            The element type of the old stream.
     * @param <R>            The element type of the new stream.
     * @return The new stream.
     */
    private static <T, R> Stream<R> createStatefulNewStream(
            final Stream<T> source,
            final Spliterator<R> newSpliterator) {
        Stream<R> newStream = StreamSupport.stream(newSpliterator, source.isParallel());
        if (source.isParallel()) {
            newStream = newStream.limit(Long.MAX_VALUE);
        }
        return newStream.onClose(source::close);
    }

}
