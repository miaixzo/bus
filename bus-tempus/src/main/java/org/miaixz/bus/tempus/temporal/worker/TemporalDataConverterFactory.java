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
package org.miaixz.bus.tempus.temporal.worker;

import java.lang.reflect.Constructor;
import java.util.List;

import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.tempus.temporal.payload.JsonPayloadConverter;

import io.temporal.common.converter.ByteArrayPayloadConverter;
import io.temporal.common.converter.DataConverter;
import io.temporal.common.converter.NullPayloadConverter;
import io.temporal.common.converter.PayloadConverter;
import io.temporal.common.converter.ProtobufJsonPayloadConverter;
import io.temporal.common.converter.ProtobufPayloadConverter;

/**
 * Creates Temporal's package-private payload converter container without initializing its Jackson 2 default.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class TemporalDataConverterFactory {

    /**
     * Temporal SDK converter-container type instantiated reflectively to avoid initializing its Jackson default.
     */
    private static final String CONVERTER_TYPE = "io.temporal.common.converter.PayloadAndFailureDataConverter";

    /**
     * Prevents instantiation of this factory class.
     */
    private TemporalDataConverterFactory() {
        // No initialization required.
    }

    /**
     * Creates Temporal's standard payload converter chain with the final JSON converter replaced by the selected Bus
     * provider.
     *
     * @param provider application-wide JSON provider
     * @return Temporal data converter using the provider for ordinary JSON payloads
     * @throws IllegalStateException if the installed Temporal SDK no longer exposes the expected converter container
     */
    static DataConverter create(JsonProvider provider) {
        List<PayloadConverter> converters = List.of(
                new NullPayloadConverter(),
                new ByteArrayPayloadConverter(),
                new ProtobufJsonPayloadConverter(),
                new ProtobufPayloadConverter(),
                new JsonPayloadConverter(provider).converter());
        try {
            Class<?> converterType = Class.forName(CONVERTER_TYPE);
            Constructor<?> constructor = converterType.getDeclaredConstructor(List.class);
            constructor.setAccessible(true);
            return (DataConverter) constructor.newInstance(converters);
        } catch (ReflectiveOperationException | LinkageError e) {
            throw new IllegalStateException("Temporal SDK DataConverter API is incompatible", e);
        }
    }

}
