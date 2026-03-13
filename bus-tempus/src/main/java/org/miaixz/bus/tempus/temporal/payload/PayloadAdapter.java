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
package org.miaixz.bus.tempus.temporal.payload;

import java.lang.reflect.Type;

/**
 * Abstraction for JSON framework adapters used in payload conversion.
 * <p>
 * This interface provides a unified API for serializing and deserializing objects using different JSON libraries
 * (fastjson2, Jackson, Gson) without direct dependencies.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface PayloadAdapter {

    /**
     * Returns the adapter name.
     *
     * @return the adapter name
     */
    String name();

    /**
     * Serializes an object to a byte array.
     *
     * @param value the object to serialize
     * @return the serialized byte array
     * @throws Exception if serialization fails
     */
    byte[] toBytes(Object value) throws Exception;

    /**
     * Deserializes a byte array to a target object.
     *
     * @param bytes      the raw byte array
     * @param valueClass the target class type
     * @param valueType  the target generic type
     * @param <T>        the target type parameter
     * @return the deserialized result
     * @throws Exception if deserialization fails
     */
    <T> T fromBytes(byte[] bytes, Class<T> valueClass, Type valueType) throws Exception;

}
