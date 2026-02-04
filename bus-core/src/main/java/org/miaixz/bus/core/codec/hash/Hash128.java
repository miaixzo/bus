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
package org.miaixz.bus.core.codec.hash;

import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.codec.No128;

/**
 * Interface for 128-bit hash calculation. This functional interface defines a contract for classes that compute a
 * 128-bit hash value for a given object.
 *
 * @param <T> The type of the object for which the hash is to be computed.
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface Hash128<T> extends Encoder<T, Number> {

    /**
     * Computes the 128-bit hash value for the given object.
     *
     * @param t The object for which to compute the hash.
     * @return The 128-bit hash value as a {@link No128} object.
     */
    No128 hash128(T t);

    /**
     * Encodes the given object by computing its 128-bit hash value. This is a default method that delegates to
     * {@link #hash128(Object)}.
     *
     * @param t The object to encode (hash).
     * @return The 128-bit hash value as a {@link No128} object.
     */
    @Override
    default Number encode(final T t) {
        return hash128(t);
    }

}
