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
package org.miaixz.bus.spring.web.converter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.extra.json.JsonPropertyFilter;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.extra.json.JsonReadOptions;
import org.miaixz.bus.extra.json.JsonTypeFilter;
import org.miaixz.bus.extra.json.JsonWriteOptions;

/**
 * Spring HTTP converter backed by the application-wide Bus JSON provider.
 *
 * @author Kimi Liu
 */
public class JsonMessageConverter implements MessageConverterRegistrar {

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
    private JsonTypeFilter typeFilter = JsonTypeMatcher.of(null)::matches;

    /**
     * Creates the Spring HTTP converter registration backed by the selected provider.
     *
     * @param provider application-wide JSON provider
     */
    public JsonMessageConverter(JsonProvider provider) {
        this.provider = Objects.requireNonNull(provider, "provider");
    }

    /**
     * Returns the diagnostic name reported by the Bus converter registry.
     *
     * @return converter name containing the selected provider name
     */
    @Override
    public String name() {
        return "BusJson[" + provider.type() + Symbol.BRACKET_RIGHT;
    }

    /**
     * Returns the insertion position used in Spring's HTTP converter list.
     *
     * @return zero so the unified converter is consulted first
     */
    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * Registers a provider-backed generic JSON converter at the configured order.
     *
     * @param converters mutable Spring HTTP converter list
     */
    @Override
    public void register(List<org.springframework.http.converter.HttpMessageConverter<?>> converters) {
        JsonReadOptions readOptions = new JsonReadOptions(typeFilter);
        JsonWriteOptions writeOptions = new JsonWriteOptions(null, false, JsonPropertyFilter.always());
        converters.add(new ProviderHttpMessageConverter(provider, readOptions, writeOptions));
    }

    /**
     * Updates the safe deserialization target allow-list used by the provider-backed converter.
     *
     * @param autoType comma-separated package rules
     */
    @Override
    public void autoType(String autoType) {
        JsonTypeMatcher matcher = JsonTypeMatcher.of(autoType);
        this.typeFilter = matcher::matches;
    }

    /**
     * Replaces the JSON deserialization target filter.
     * <p>
     * This method must be called before the converter is registered with Spring MVC.
     *
     * @param typeFilter filter applied to every concrete target class
     */
    public void typeFilter(JsonTypeFilter typeFilter) {
        this.typeFilter = Objects.requireNonNull(typeFilter, "typeFilter");
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
            super(Charset.UTF_8, MEDIA_TYPES);
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
         * @param value         response body value to serialize
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
