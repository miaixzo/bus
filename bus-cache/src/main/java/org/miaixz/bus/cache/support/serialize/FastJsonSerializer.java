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
package org.miaixz.bus.cache.support.serialize;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * A serializer that uses the FastJson library.
 * <p>
 * This implementation serializes objects to a JSON string, which is then converted to a byte array. During
 * deserialization, the process is reversed. This approach offers good readability and cross-language compatibility but
 * may have lower performance compared to binary serialization formats.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FastJsonSerializer extends AbstractSerializer {

    /**
     * The target class type for deserialization.
     */
    private final Class<?> type;

    /**
     * Constructs a new {@code FastJsonSerializer}.
     *
     * @param type The target class type, which is required for deserializing the JSON string back into an object.
     */
    public FastJsonSerializer(Class<?> type) {
        this.type = type;
    }

    /**
     * Performs serialization by converting the object to a JSON string and then to a UTF-8 byte array.
     *
     * @param object The object to be serialized.
     * @return The serialized byte array.
     * @throws Throwable if an error occurs during serialization.
     */
    @Override
    protected byte[] doSerialize(Object object) throws Throwable {
        String json = JsonKit.toJsonString(object);
        return json.getBytes(Charset.DEFAULT_UTF_8);
    }

    /**
     * Performs deserialization by converting the byte array to a JSON string and then to an object of the target type.
     *
     * @param bytes The byte array to be deserialized.
     * @return The deserialized object.
     * @throws Throwable if an error occurs during deserialization.
     */
    @Override
    protected Object doDeserialize(byte[] bytes) throws Throwable {
        String json = new String(bytes, 0, bytes.length, Charset.DEFAULT_UTF_8);
        return JsonKit.toPojo(json, type);
    }

}
