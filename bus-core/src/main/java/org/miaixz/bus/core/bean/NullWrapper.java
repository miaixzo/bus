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
package org.miaixz.bus.core.bean;

/**
 * A wrapper class designed to address scenarios where a {@code null} parameter needs to be passed during reflection,
 * but the parameter's type information would otherwise be lost. This wrapper explicitly retains the type of the
 * {@code null} value.
 *
 * @param <T> The type corresponding to the {@code null} value.
 * @author Kimi Liu
 * @since Java 21+
 */
public class NullWrapper<T> {

    /**
     * The class type of the null value.
     */
    private final Class<T> clazz;

    /**
     * Constructs a {@code NullWrapper} with the specified class type for the {@code null} value.
     *
     * @param clazz The class type that the {@code null} value represents.
     */
    public NullWrapper(final Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * Retrieves the class type that this {@code NullWrapper} represents.
     *
     * @return The class type of the wrapped {@code null} value.
     */
    public Class<T> getWrappedClass() {
        return clazz;
    }

}
