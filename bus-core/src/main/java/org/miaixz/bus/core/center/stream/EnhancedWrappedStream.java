/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
