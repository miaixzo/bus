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
package org.miaixz.bus.core.xyz;

import java.io.ByteArrayInputStream;
import java.io.Serializable;

import org.miaixz.bus.core.io.stream.FastByteArrayOutputStream;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Serialization utility class. Note: This utility relies on Java's default serialization mechanism, which may have
 * security vulnerabilities in some JDK versions.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SerializeKit {

    /**
     * Clones an object by serializing and then deserializing it. The object must implement the {@link Serializable}
     * interface.
     *
     * @param <T>    The type of the object.
     * @param object The object to be cloned.
     * @return The cloned object.
     * @throws InternalException wrapping IOExceptions and ClassNotFoundExceptions.
     */
    public static <T> T clone(final T object) {
        if (!(object instanceof Serializable)) {
            return null;
        }
        return deserialize(serialize(object));
    }

    /**
     * Serializes an object into a byte array. The object must implement {@link Serializable}.
     *
     * @param <T>    The type of the object.
     * @param object The object to be serialized.
     * @return The serialized byte array.
     */
    public static <T> byte[] serialize(final T object) {
        if (!(object instanceof Serializable)) {
            return null;
        }
        final FastByteArrayOutputStream byteOut = new FastByteArrayOutputStream();
        IoKit.write(byteOut, false, object);
        return byteOut.toByteArray();
    }

    /**
     * Deserializes a byte array into an object.
     * <p>
     * WARNING: This method does not perform any security checks and may be vulnerable to deserialization attacks.
     *
     * @param <T>           The type of the object.
     * @param bytes         The byte array to deserialize.
     * @param acceptClasses A whitelist of classes that are allowed to be deserialized.
     * @return The deserialized object.
     */
    public static <T> T deserialize(final byte[] bytes, final Class<?>... acceptClasses) {
        return IoKit.readObject(new ByteArrayInputStream(bytes), acceptClasses);
    }

}
