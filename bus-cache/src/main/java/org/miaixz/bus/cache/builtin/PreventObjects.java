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
package org.miaixz.bus.cache.builtin;

import java.io.Serial;
import java.io.Serializable;

/**
 * A utility class for handling cache penetration prevention.
 * <p>
 * Cache penetration occurs when a non-existent key is frequently requested, causing each request to bypass the cache
 * and hit the underlying data source (e.g., a database). This class provides a special singleton object that can be
 * cached to represent a "null" or non-existent value, thereby preventing subsequent requests for the same key from
 * hitting the data source.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PreventObjects {

    /**
     * Returns the singleton marker object used for cache penetration prevention.
     * <p>
     * This object serves as a placeholder in the cache for keys that correspond to non-existent data.
     * </p>
     *
     * @return The singleton {@link NullMarker} instance.
     */
    public static Object getPreventObject() {
        return NullMarker.INSTANCE;
    }

    /**
     * Checks if a given object is the special penetration prevention marker.
     *
     * @param object The object to check.
     * @return {@code true} if the object is the {@link NullMarker} instance, otherwise {@code false}.
     */
    public static boolean isPrevent(Object object) {
        return object == NullMarker.INSTANCE || object instanceof NullMarker;
    }

    /**
     * A serializable singleton marker used to represent a cached null value.
     * <p>
     * Stored in the cache in place of a missing entry to prevent repeated cache-miss lookups against the underlying
     * data source. Implements {@link Serializable} so it survives round-trips through distributed caches;
     * {@link #readResolve()} guarantees that deserialization always returns the single canonical instance.
     * </p>
     */
    private static final class NullMarker implements Serializable {

        /**
         * The serialization version UID.
         */
        @Serial
        private static final long serialVersionUID = 2852290208329L;

        /**
         * The singleton instance.
         */
        private static final NullMarker INSTANCE = new NullMarker();

        private NullMarker() {
        }

        @Serial
        private Object readResolve() {
            return INSTANCE;
        }
    }

}
