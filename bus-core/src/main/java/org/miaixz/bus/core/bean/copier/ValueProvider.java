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
package org.miaixz.bus.core.bean.copier;

import java.lang.reflect.Type;

/**
 * An abstract interface for value providers, used to supply corresponding values during Bean injection. Implement or
 * anonymously instantiate this interface. During the Bean injection process, the Bean obtains a field name, and an
 * external mechanism uses this field name to find the corresponding field value, which is then injected into the Bean.
 *
 * @param <K> The type of the key, typically {@link String}.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ValueProvider<K> {

    /**
     * Retrieves the value associated with the given key. The returned value generally needs to match the type being
     * injected. If it does not match, a default conversion will be attempted using
     * {@code Convert#convert(Type, Object)}.
     *
     * @param key       The parameter name in the Bean object.
     * @param valueType The type of the value being injected.
     * @return The value corresponding to the parameter name.
     */
    Object value(K key, Type valueType);

    /**
     * Checks if the value provider contains the specified key. If it does not contain the key, injection is ignored.
     * The significance of this method is that for some value providers (e.g., Map), a key might exist but its value is
     * {@code null}. If this {@code null} value needs to be injected, this method is used to determine if the key is
     * present.
     *
     * @param key The parameter name in the Bean object.
     * @return {@code true} if the specified key is contained, {@code false} otherwise.
     */
    boolean containsKey(K key);

}
