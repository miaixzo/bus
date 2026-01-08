/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
