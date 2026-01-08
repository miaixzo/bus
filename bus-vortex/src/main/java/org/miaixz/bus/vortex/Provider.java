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
package org.miaixz.bus.vortex;

import reactor.core.publisher.Mono;

/**
 * Data serialization interface, defining a common method for serializing objects into strings or other formats.
 * <p>
 * This interface uses generics to provide type safety for different serialization implementations:
 * <ul>
 * <li>{@code I} - The input type to be serialized (e.g., Object, specific entity types)</li>
 * <li>{@code O} - The output type produced by serialization (e.g., String, byte[], XML, JSON)</li>
 * </ul>
 * <p>
 * Implementations of this interface handle different serialization formats such as JSON, XML, and binary formats.
 *
 * @param <I> The input type to be serialized
 * @param <O> The output type produced by the serialization
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Provider<I, O> {

    /**
     * Asynchronously serializes an object of type {@code I} into the output type {@code O}.
     * <p>
     * This method performs the serialization operation asynchronously, returning a {@link Mono} that emits the
     * serialized result. Common output types include String (for JSON/XML), byte[] (for binary), etc.
     *
     * @param input The object of type {@code I} to be serialized.
     * @return A {@code Mono<O>} emitting the serialized result.
     */
    Mono<O> serialize(I input);

}
