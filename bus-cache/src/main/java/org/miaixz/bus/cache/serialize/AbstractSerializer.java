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
package org.miaixz.bus.cache.serialize;

import org.miaixz.bus.cache.Serializer;
import org.miaixz.bus.logger.Logger;

/**
 * An abstract base class for serializers.
 * <p>
 * This class provides a template for serialization and deserialization, including common logic for handling nulls and
 * logging exceptions. Subclasses must implement the actual serialization and deserialization logic in the `doSerialize`
 * and `doDeserialize` methods.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class AbstractSerializer implements Serializer {

    /**
     * Performs the actual serialization of the object.
     *
     * @param object The object to be serialized.
     * @return The resulting byte array.
     * @throws Throwable if an error occurs during serialization.
     */
    protected abstract byte[] doSerialize(Object object) throws Throwable;

    /**
     * Performs the actual deserialization of the byte array.
     *
     * @param bytes The byte array to be deserialized.
     * @return The deserialized object.
     * @throws Throwable if an error occurs during deserialization.
     */
    protected abstract Object doDeserialize(byte[] bytes) throws Throwable;

    /**
     * Serializes an object into a byte array.
     * <p>
     * This method handles null input and wraps the serialization logic with a try-catch block to log any errors that
     * occur.
     * </p>
     *
     * @param <T>    The type of the object.
     * @param object The object to be serialized.
     * @return The resulting byte array, or {@code null} if the input is null or serialization fails.
     */
    @Override
    public <T> byte[] serialize(T object) {
        if (null == object) {
            return null;
        }
        try {
            return doSerialize(object);
        } catch (Throwable t) {
            Logger.error("{} serialize error.", this.getClass().getName(), t);
            return null;
        }
    }

    /**
     * Deserializes a byte array back into an object.
     * <p>
     * This method handles null input and wraps the deserialization logic with a try-catch block to log any errors that
     * occur.
     * </p>
     *
     * @param <T>   The expected type of the deserialized object.
     * @param bytes The byte array to be deserialized.
     * @return The deserialized object, or {@code null} if the input is null or deserialization fails.
     */
    @Override
    public <T> T deserialize(byte[] bytes) {
        if (null == bytes) {
            return null;
        }
        try {
            return (T) doDeserialize(bytes);
        } catch (Throwable t) {
            Logger.error("{} deserialize error.", this.getClass().getName(), t);
            return null;
        }
    }

}
