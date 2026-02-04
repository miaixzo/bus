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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A serializer that uses standard Java serialization followed by GZIP compression.
 * <p>
 * This implementation first serializes an object to a byte array using Java's native serialization, and then compresses
 * the resulting byte array using GZIP. This can significantly reduce the size of the serialized data, making it
 * suitable for caching large objects or in scenarios where storage space is a concern.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdkGzipSerializer extends AbstractSerializer {

    /**
     * Performs serialization by first using Java serialization and then GZIP compressing the result.
     *
     * @param object The object to be serialized.
     * @return The serialized and compressed byte array.
     * @throws Throwable if an I/O or serialization error occurs.
     */
    @Override
    protected byte[] doSerialize(Object object) throws Throwable {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                GZIPOutputStream gzout = new GZIPOutputStream(bos);
                ObjectOutputStream out = new ObjectOutputStream(gzout)) {
            out.writeObject(object);
            return bos.toByteArray();
        }
    }

    /**
     * Performs deserialization by first GZIP decompressing the byte array and then using Java deserialization.
     *
     * @param bytes The byte array to be deserialized.
     * @return The deserialized object.
     * @throws Throwable if an I/O, class loading, or deserialization error occurs.
     */
    @Override
    protected Object doDeserialize(byte[] bytes) throws Throwable {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                GZIPInputStream gzin = new GZIPInputStream(bis);
                ObjectInputStream ois = new ObjectInputStream(gzin)) {
            return ois.readObject();
        }
    }

}
