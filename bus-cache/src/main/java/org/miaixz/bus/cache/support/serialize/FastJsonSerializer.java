/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
