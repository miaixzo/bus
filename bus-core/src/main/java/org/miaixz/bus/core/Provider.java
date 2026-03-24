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
package org.miaixz.bus.core;

import java.io.Serializable;

/**
 * A generic interface representing the Strategy Pattern, where each implementation provides a specific strategy or
 * capability. This is often used to select an appropriate implementation at runtime based on a given type or context.
 *
 * @param <T> The type of object this provider produces or handles.
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Provider<T> extends Serializable {

    /**
     * Returns an identifier for the type or strategy that this provider supports. This identifier is used to look up
     * and select the correct provider from a collection of available providers.
     *
     * @return An object that uniquely identifies the supported type or strategy (e.g., a {@code String}, {@code Enum},
     *         or {@code Class}).
     */
    Object type();

}
