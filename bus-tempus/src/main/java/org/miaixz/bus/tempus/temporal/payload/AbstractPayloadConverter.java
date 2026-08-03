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
package org.miaixz.bus.tempus.temporal.payload;

import java.lang.reflect.Type;
import java.util.List;

import org.miaixz.bus.extra.json.JsonFactory;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.logger.Logger;

/**
 * Compatibility base class for Temporal payload converters.
 * <p>
 * JSON processing is delegated to {@code bus-extra}; this class no longer detects or invokes concrete JSON engines. New
 * Temporal integrations should use {@link JsonPayloadConverter} directly.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class AbstractPayloadConverter implements PayloadConverter {

    /**
     * Creates a payload converter template.
     */
    public AbstractPayloadConverter() {
        // No initialization required.
    }

    /**
     * Returns a preferred adapter supplied by a legacy subclass.
     *
     * @return preferred adapter, or {@code null}
     */
    protected PayloadAdapter preferredAdapter() {
        return null;
    }

    /**
     * Returns the application-selected provider as the sole default candidate.
     *
     * @return provider-backed adapter list
     */
    protected List<PayloadAdapter> candidateAdapters() {
        return List.of(providerAdapter(JsonFactory.get()));
    }

    /**
     * Resolves the adapter without performing framework-specific detection.
     *
     * @return resolved adapter
     */
    protected PayloadAdapter resolveAdapter() {
        PayloadAdapter preferred = preferredAdapter();
        if (preferred != null) {
            Logger.debug(
                    false,
                    "Tempus",
                    "Temporal payload adapter resolved: adapter={}, source=preferred",
                    preferred.name());
            return preferred;
        }
        return DefaultPayloadAdapterHolder.ADAPTER;
    }

    /**
     * Adapts the shared JSON provider contract to the legacy Tempus payload adapter contract.
     *
     * @param provider application JSON provider
     * @return payload adapter delegating all JSON work to the provider
     */
    private static PayloadAdapter providerAdapter(JsonProvider provider) {
        return new PayloadAdapter() {

            /**
             * Returns the underlying JSON provider name.
             *
             * @return canonical provider name
             */
            @Override
            public String name() {
                return provider.name();
            }

            /**
             * Serializes a payload value as UTF-8 JSON bytes.
             *
             * @param value payload value
             * @return serialized JSON bytes
             */
            @Override
            public byte[] toBytes(Object value) {
                return provider.write(value);
            }

            /**
             * Deserializes UTF-8 JSON bytes into the requested concrete or generic Java type.
             *
             * @param <T>        target value type
             * @param bytes      serialized JSON bytes
             * @param valueClass concrete target class
             * @param valueType  generic target type, or {@code null} to use {@code valueClass}
             * @return deserialized payload value
             */
            @Override
            public <T> T fromBytes(byte[] bytes, Class<T> valueClass, Type valueType) {
                return provider.read(bytes, valueType == null ? valueClass : valueType);
            }
        };
    }

    /**
     * Initialization-on-demand holder for the adapter backed by the application-wide provider.
     */
    private static final class DefaultPayloadAdapterHolder {

        /**
         * Lazily initialized default payload adapter.
         */
        private static final PayloadAdapter ADAPTER = providerAdapter(JsonFactory.get());

        /**
         * Prevents instantiation of this holder class.
         */
        private DefaultPayloadAdapterHolder() {
            // No initialization required.
        }

    }

}
