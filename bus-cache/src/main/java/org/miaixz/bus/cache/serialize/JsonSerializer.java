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

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * A serializer that uses JSON for object serialization and deserialization.
 * <p>
 * Delegates to {@link JsonKit}, which is a provider-agnostic JSON facade that supports Jackson, Gson, Fastjson 2, and
 * other backends. Objects are serialized to a UTF-8 JSON string and deserialized back using the target type supplied at
 * construction time.
 * </p>
 * <p>
 * This approach offers good readability and cross-language compatibility. Note that the target {@code type} must be
 * known at construction time, as it is required for deserialization.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JsonSerializer extends AbstractSerializer {

    /**
     * The target class type used for deserialization.
     */
    private final Class<?> type;

    /**
     * Constructs a new {@code JsonSerializer}.
     *
     * @param type the target class type; required when deserializing the JSON bytes back into an object
     */
    public JsonSerializer(Class<?> type) {
        this.type = type;
    }

    /**
     * Serializes the object to a UTF-8 JSON byte array.
     *
     * @param object the object to serialize
     * @return the UTF-8 encoded JSON byte array
     * @throws Throwable if an error occurs during serialization
     */
    @Override
    protected byte[] doSerialize(Object object) throws Throwable {
        String json = JsonKit.toJsonString(object);
        return json.getBytes(Charset.DEFAULT_UTF_8);
    }

    /**
     * Deserializes a UTF-8 JSON byte array into an object of the configured target type.
     *
     * @param bytes the UTF-8 encoded JSON byte array
     * @return the deserialized object
     * @throws Throwable if an error occurs during deserialization
     */
    @Override
    protected Object doDeserialize(byte[] bytes) throws Throwable {
        String json = new String(bytes, Charset.DEFAULT_UTF_8);
        return JsonKit.toPojo(json, type);
    }

}
