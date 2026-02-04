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
package org.miaixz.bus.cache.support.serialize;

import java.io.*;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * A serializer that uses standard Java serialization.
 * <p>
 * This implementation is based on Java's native serialization mechanism, using {@link ObjectOutputStream} and
 * {@link ObjectInputStream} to serialize and deserialize objects. It requires that the objects to be serialized
 * implement the {@link Serializable} interface.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdkSerializer extends AbstractSerializer {

    /**
     * Serializes a {@link Serializable} object to an {@link OutputStream}.
     *
     * @param object       The object to serialize. Must implement {@link Serializable}.
     * @param outputStream The stream to write to. Must not be null.
     * @throws InternalException if an I/O error occurs during serialization.
     */
    private static void serialize(Serializable object, OutputStream outputStream) {
        if (null == outputStream) {
            throw new IllegalArgumentException("The OutputStream must not be null");
        }
        try (ObjectOutputStream out = new ObjectOutputStream(outputStream)) {
            out.writeObject(object);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Deserializes an object from an {@link InputStream}.
     *
     * @param inputStream The stream to read from. Must not be null.
     * @return The deserialized object.
     * @throws InternalException if an I/O, class loading, or casting error occurs during deserialization.
     */
    private static Object deserialize(InputStream inputStream) {
        if (null == inputStream) {
            throw new IllegalArgumentException("The InputStream must not be null");
        }
        try (ObjectInputStream in = new ObjectInputStream(inputStream)) {
            return in.readObject();
        } catch (ClassCastException | IOException | ClassNotFoundException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Performs the serialization of the object using Java's native serialization.
     *
     * @param object The object to be serialized, which must implement {@link Serializable}.
     * @return The resulting byte array.
     * @throws InternalException if an error occurs during serialization.
     */
    @Override
    protected byte[] doSerialize(Object object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(Normal._512);
        serialize((Serializable) object, baos);
        return baos.toByteArray();
    }

    /**
     * Performs the deserialization of the byte array using Java's native deserialization.
     *
     * @param bytes The byte array to be deserialized.
     * @return The deserialized object.
     * @throws IllegalArgumentException if the byte array is null.
     * @throws InternalException        if an error occurs during deserialization.
     */
    @Override
    protected Object doDeserialize(byte[] bytes) {
        if (null == bytes) {
            throw new IllegalArgumentException("The byte[] must not be null");
        }
        return deserialize(new ByteArrayInputStream(bytes));
    }

}
