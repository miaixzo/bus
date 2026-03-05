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
