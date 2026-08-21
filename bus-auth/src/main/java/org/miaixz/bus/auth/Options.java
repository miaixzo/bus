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
package org.miaixz.bus.auth;

/**
 * Marks an immutable decoded configuration value accepted by one {@link Scheme}.
 * <p>
 * An options value contains deployment input and external credential references only. It does not describe management
 * presentation, select a protocol, decode persistence JSON, compile workers, or execute authentication operations.
 * </p>
 * <p>
 * Persistence and transport serialization belong to the integrating project. This runtime contract deliberately does
 * not require Java object serialization.
 * </p>
 *
 * @param <O> exact immutable implementation type
 * @author Kimi Liu
 */
public interface Options<O extends Options<O>> {

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    Class<O> type();

    /**
     * Returns an immutable detached value safe to retain in one compiled runtime container.
     * <p>
     * Immutable value implementations may return {@code this}. Mutable project implementations must return a detached
     * immutable copy. The framework does not serialize deployment options to manufacture a snapshot.
     * </p>
     *
     * @return non-null immutable options snapshot
     */
    O snapshot();

}
