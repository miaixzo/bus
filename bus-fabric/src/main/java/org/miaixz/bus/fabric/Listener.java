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
package org.miaixz.bus.fabric;

/**
 * Lifecycle listener contract for network and protocol resources.
 *
 * @param <T> lifecycle source type
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Listener<T> {

    /**
     * Handles an open event.
     *
     * @param source lifecycle source
     */
    default void open(final T source) {
        // Default open listener intentionally performs no action.
    }

    /**
     * Handles a close event.
     *
     * @param source lifecycle source
     */
    default void close(final T source) {
        // Default close listener intentionally performs no action.
    }

    /**
     * Handles a failure event.
     *
     * @param source lifecycle source
     * @param cause  failure cause
     */
    default void failure(final T source, final Throwable cause) {
        // Default failure listener intentionally performs no action.
    }

}
