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
package org.miaixz.bus.cache;

/**
 * Defines the basic operations for serialization and deserialization in the cache system.
 * <p>
 * Implementations of this interface provide the specific logic for converting objects to and from byte arrays for
 * storage and retrieval from a cache.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Serializer {

    /**
     * Serializes an object into a byte array.
     *
     * @param <T>    The type of the object.
     * @param object The object to be serialized.
     * @return The resulting byte array.
     */
    <T> byte[] serialize(T object);

    /**
     * Deserializes a byte array back into an object.
     *
     * @param <T>   The expected type of the deserialized object.
     * @param bytes The byte array to be deserialized.
     * @return The deserialized object.
     */
    <T> T deserialize(byte[] bytes);

}
