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
package org.miaixz.bus.spring.http;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import org.miaixz.bus.core.xyz.FieldKit;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.extra.json.JsonReadOptions;
import org.miaixz.bus.extra.json.JsonWriteOptions;
import org.miaixz.bus.logger.Logger;

/**
 * Spring HTTP converter backed by the application-wide Bus JSON provider.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class JsonMessageConverter extends AbstractHttpMessageConverter {

    /**
     * Media types handled by the unified JSON converter.
     */
    private static final MediaType[] MEDIA_TYPES = { MediaType.APPLICATION_JSON,
            new MediaType("application", "*+json") };

    /**
     * Application-selected JSON provider shared with the other Bus integrations.
     */
    private final JsonProvider provider;

    /**
     * Optional allow-list matcher used when the application configures safe deserialization package rules.
     */
    private AutoBindingTypeMatcher typeMatcher = AutoBindingTypeMatcher.of(null);

    /**
     * Creates the Spring HTTP converter registration backed by the selected provider.
     *
     * @param provider application-wide JSON provider
     */
    public JsonMessageConverter(JsonProvider provider) {
        this.provider = provider;
    }

    /**
     * Returns the diagnostic name reported by the Bus converter registry.
     *
     * @return converter name containing the selected provider name
     */
    @Override
    public String name() {
        return "BusJson(" + provider.name() + ")";
    }

    /**
     * Returns the insertion position used in Spring's HTTP converter list.
     *
     * @return zero so the unified converter is consulted first
     */
    @Override
    public int order() {
        return 0;
    }

    /**
     * Registers a provider-backed generic JSON converter at the configured order.
     *
     * @param converters mutable Spring HTTP converter list
     */
    @Override
    public void register(List<org.springframework.http.converter.HttpMessageConverter<?>> converters) {
        JsonReadOptions readOptions = new JsonReadOptions(typeMatcher::matches);
        JsonWriteOptions writeOptions = new JsonWriteOptions(null, false, this::includeProperty);
        int insertionIndex = Math.max(0, Math.min(order(), converters.size()));
        converters.add(insertionIndex, new ProviderHttpMessageConverter(provider, readOptions, writeOptions));
    }

    /**
     * Updates the safe deserialization target allow-list used by the provider-backed converter.
     *
     * @param autoType comma-separated package rules
     */
    @Override
    public void autoType(String autoType) {
        this.typeMatcher = AutoBindingTypeMatcher.of(autoType);
    }

    /**
     * Applies the existing Bus HTTP annotation and empty-value policy through the framework-independent property filter
     * contract.
     *
     * @param source owning object
     * @param name   serialized property name
     * @param value  current property value
     * @return {@code true} when the property should be serialized
     */
    private boolean includeProperty(Object source, String name, Object value) {
        if (source == null || source instanceof Map<?, ?>) {
            return true;
        }
        if (isClassIgnored(source.getClass())) {
            return false;
        }
        try {
            Field field = FieldKit.getField(source.getClass(), name);
            return !shouldSkipField(field, value);
        } catch (RuntimeException e) {
            Logger.debug(
                    false,
                    "Starter",
                    "JSON property metadata unavailable; property retained: sourceType={}, property={}, exception={}",
                    source.getClass().getName(),
                    name,
                    e.getClass().getSimpleName());
            return true;
        }
    }

    /**
     * Spring generic HTTP converter that delegates JSON byte encoding and decoding exclusively to a
     * {@link JsonProvider}.
     */
    static final class ProviderHttpMessageConverter extends AbstractGenericHttpMessageConverter<Object> {

        /**
         * Provider used for both request deserialization and response serialization.
         */
        private final JsonProvider provider;

        /**
         * Framework-independent deserialization options.
         */
        private final JsonReadOptions readOptions;

        /**
         * Framework-independent serialization options.
         */
        private final JsonWriteOptions writeOptions;

        /**
         * Creates a UTF-8 {@code application/json} converter.
         *
         * @param provider     selected application JSON provider
         * @param readOptions  deserialization options
         * @param writeOptions serialization options
         */
        ProviderHttpMessageConverter(JsonProvider provider, JsonReadOptions readOptions,
                JsonWriteOptions writeOptions) {
            super(StandardCharsets.UTF_8, MEDIA_TYPES);
            this.provider = provider;
            this.readOptions = readOptions;
            this.writeOptions = writeOptions;
        }

        /**
         * Reads an HTTP request body into the requested generic Java type.
         *
         * @param type         target Java type
         * @param contextClass containing class used for generic type resolution, when supplied by Spring
         * @param inputMessage source HTTP message
         * @return deserialized request value
         * @throws IOException                     if the request body cannot be read
         * @throws HttpMessageNotReadableException if JSON deserialization fails
         */
        @Override
        public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException {
            try {
                return provider.read(inputMessage.getBody().readAllBytes(), type, readOptions);
            } catch (RuntimeException e) {
                throw new HttpMessageNotReadableException("JSON deserialization failed", e, inputMessage);
            }
        }

        /**
         * Reads an HTTP request body into a concrete Java class.
         *
         * @param clazz        target value class
         * @param inputMessage source HTTP message
         * @return deserialized request value
         * @throws IOException                     if the request body cannot be read
         * @throws HttpMessageNotReadableException if JSON deserialization fails
         */
        @Override
        protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage)
                throws IOException, HttpMessageNotReadableException {
            return read(clazz, null, inputMessage);
        }

        /**
         * Serializes a response value as UTF-8 JSON and writes it to the HTTP response body.
         *
         * @param value         response value
         * @param type          declared response type, when supplied by Spring
         * @param outputMessage target HTTP message
         * @throws IOException                     if the response body cannot be written
         * @throws HttpMessageNotWritableException if JSON serialization fails
         */
        @Override
        protected void writeInternal(Object value, Type type, HttpOutputMessage outputMessage)
                throws IOException, HttpMessageNotWritableException {
            try {
                outputMessage.getBody().write(provider.write(value, writeOptions));
            } catch (RuntimeException e) {
                throw new HttpMessageNotWritableException("JSON serialization failed", e);
            }
        }
    }

}
