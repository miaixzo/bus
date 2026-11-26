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
 * Reads and writes UTF-8 JSON bodies through the selected Bus JSON provider.
 *
 * @author Kimi Liu
 */
public class JsonMessageValueConverter extends AbstractGenericHttpMessageConverter<Object>
        implements MessageValueConverter<Object> {

    /**
     * JSON media types supported by the converter.
     */
    private static final MediaType[] MEDIA_TYPES = { MediaType.APPLICATION_JSON,
            new MediaType("application", "*+json") };

    /**
     * JSON provider shared with other application integrations.
     */
    private final JsonProvider provider;

    /**
     * Request deserialization options with the configured type filter.
     */
    private final JsonReadOptions readOptions;

    /**
     * Response serialization options with application property filtering.
     */
    private final JsonWriteOptions writeOptions = new JsonWriteOptions(null, false, JsonPropertyFilter.always());

    /**
     * Creates a JSON converter with the framework's default target type policy.
     *
     * @param provider application-wide JSON provider
     */
    public JsonMessageValueConverter(JsonProvider provider) {
        this(provider, JsonTypeMatcher.of(null)::matches);
    }

    /**
     * Creates a JSON converter with the selected provider and target type policy.
     *
     * @param provider   application-wide JSON provider
     * @param typeFilter filter applied to deserialization target classes
     */
    public JsonMessageValueConverter(JsonProvider provider, JsonTypeFilter typeFilter) {
        super(Charset.UTF_8, MEDIA_TYPES);
        this.provider = Objects.requireNonNull(provider, "provider");
        this.readOptions = new JsonReadOptions(Objects.requireNonNull(typeFilter, "typeFilter"));
    }

    /**
     * Creates a JSON converter with the supplied target class and package rules.
     *
     * @param provider     application-wide JSON provider
     * @param allowedTypes comma-separated target class and package rules
     * @return configured JSON converter
     */
    public static JsonMessageValueConverter withAllowedTypes(JsonProvider provider, String allowedTypes) {
        return new JsonMessageValueConverter(provider, JsonTypeMatcher.of(allowedTypes)::matches);
    }

    /**
     * Returns the diagnostic name including the selected JSON provider.
     *
     * @return converter name
     */
    @Override
    public String name() {
        return "BusJson[" + provider.type() + Symbol.BRACKET_RIGHT;
    }

    /**
     * Gives the Bus JSON converter priority over the default MVC converters.
     *
     * @return converter order
     */
    @Override
    public int getOrder() {
        return 0;
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
