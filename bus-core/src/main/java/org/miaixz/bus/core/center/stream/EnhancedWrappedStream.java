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
package org.miaixz.bus.core.center.stream;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * A common implementation of the {@link WrappedStream} interface, used to wrap and enhance an existing stream instance.
 *
 * @param <T> the type of the elements in the stream
 * @param <S> the type of the {@link EnhancedWrappedStream} implementation itself
 * @author Kimi Liu
 * @see EasyStream
 * @see EntryStream
 * @since Java 17+
 */
public abstract class EnhancedWrappedStream<T, S extends EnhancedWrappedStream<T, S>>
        implements TerminableWrappedStream<T, S>, TransformableWrappedStream<T, S> {

    /**
     * The original stream instance.
     */
    protected Stream<T> stream;

    /**
     * Constructs a stream wrapper.
     *
     * @param stream the stream object to be wrapped
     * @throws NullPointerException if {@code stream} is {@code null}
     */
    protected EnhancedWrappedStream(final Stream<T> stream) {
        this.stream = Objects.requireNonNull(stream, "unwrap must not null");
    }

    /**
     * Retrieves the underlying stream instance wrapped by this object.
     *
     * @return the stream instance wrapped by this object
     */
    @Override
    public Stream<T> unwrap() {
        return stream;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return stream.hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param object the reference object with which to compare
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object object) {
        return object instanceof Stream && stream.equals(object);
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return stream.toString();
    }

    /**
     * Triggers the execution of the stream. This is a terminal operation.
     */
    public void exec() {
        stream.forEach(t -> {
        });
    }

}
