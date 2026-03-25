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

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * A serializer that uses the Hessian 2 binary web service protocol.
 * <p>
 * This implementation provides efficient, cross-language binary serialization. Hessian is a lightweight RPC protocol
 * that is well-suited for performance-critical applications.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Hessian2Serializer extends AbstractSerializer {

    /**
     * Performs serialization using Hessian 2.
     *
     * @param object The object to be serialized.
     * @return The serialized byte array.
     * @throws Throwable if an I/O or serialization error occurs.
     */
    @Override
    protected byte[] doSerialize(Object object) throws Throwable {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Hessian2Output out = new Hessian2Output(os);
            out.writeObject(object);
            out.flush(); // Ensure all data is written to the underlying stream
            return os.toByteArray();
        }
    }

    /**
     * Performs deserialization using Hessian 2.
     *
     * @param bytes The byte array to be deserialized.
     * @return The deserialized object.
     * @throws Throwable if an I/O or deserialization error occurs.
     */
    @Override
    protected Object doDeserialize(byte[] bytes) throws Throwable {
        try (ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
            Hessian2Input in = new Hessian2Input(is);
            return in.readObject();
        }
    }

}
