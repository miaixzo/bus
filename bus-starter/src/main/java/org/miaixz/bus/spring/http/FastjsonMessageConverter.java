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
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.PropertyFilter;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.xyz.FieldKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.logger.Logger;

/**
 * Fastjson2 JSON converter configurer for Spring MVC.
 * <p>
 * The registered converter prefers the standard {@code application/json} media type for normal HTTP content
 * negotiation, while still keeping {@code application/json+fastjson} available for clients that explicitly opt in to a
 * Fastjson-specific media type.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Component
@ConditionalOnClass({ JSON.class })
public class FastjsonMessageConverter extends AbstractHttpMessageConverter {

    /**
     * Default media types supported by this converter.
     * <p>
     * Order matters: Spring uses this list during content negotiation, so {@code application/json} must remain first to
     * avoid wildcard browser requests selecting a library-specific media type by default.
     * </p>
     */
    private static final List<MediaType> DEFAULT_MEDIA_TYPES = List.of(
            MediaType.APPLICATION_JSON,
            new MediaType(MediaType.TEXT_PLAIN, Charset.UTF_8),
            MediaType.parseMediaType(org.miaixz.bus.core.net.MediaType.APPLICATION_JSON_FASTJSON));

    /**
     * Auto type allow-list expression used by Fastjson2 deserialization.
     */
    private String autoType;

    /**
     * Constructs a new FastjsonMessageConverter instance.
     */
    public FastjsonMessageConverter() {
        // No initialization required.
    }

    /**
     * Returns the converter configurer name.
     *
     * @return converter configurer name
     */
    @Override
    public String name() {
        return "Fastjson2";
    }

    /**
     * Returns the converter insertion order.
     *
     * @return converter insertion order
     */
    @Override
    public int order() {
        return 0;
    }

    /**
     * Adds the Fastjson2 HTTP message converter to the Spring converter list.
     *
     * @param converters the mutable Spring converter list
     */
    @Override
    public void configure(List<org.springframework.http.converter.HttpMessageConverter<?>> converters) {
        Logger.debug(false, "Starter", "Configuring FastJson2HttpMessageConverter for Fastjson2");
        converters.add(order(), new FastJson2HttpMessageConverter(this.autoType));
        Logger.debug(
                false,
                "Starter",
                "FastJson2HttpMessageConverter configured with media types: {}",
                DEFAULT_MEDIA_TYPES);
    }

    /**
     * Sets the auto type allow-list expression for Fastjson2 deserialization.
     *
     * @param autoType auto type allow-list expression
     */
    @Override
    public void autoType(String autoType) {
        this.autoType = autoType;
    }

    /**
     * A custom {@link org.springframework.http.converter.AbstractHttpMessageConverter} for Fastjson2. It configures
     * serialization using {@link JSONWriter.Feature} and deserialization using {@link JSONReader.Feature}, and enables
     * {@code autoType} based on the constructor parameter.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    static class FastJson2HttpMessageConverter
            extends org.springframework.http.converter.AbstractHttpMessageConverter<Object> {

        /**
         * Features to use for JSON serialization.
         */
        private static final JSONWriter.Feature[] WRITER_FEATURES = { JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.IgnoreNonFieldGetter };

        /**
         * Features to use for JSON deserialization.
         */
        private static final JSONReader.Feature[] READER_FEATURES = { JSONReader.Feature.FieldBased };

        /**
         * Auto type matcher compiled from the allow-list expression.
         */
        private final AutoBindingTypeMatcher autoTypeMatcher;

        /**
         * Constructs a new converter, parsing the autoType whitelist.
         *
         * @param autoType A comma-separated string of whitelisted classes for autoType.
         */
        public FastJson2HttpMessageConverter(String autoType) {
            super(Charset.UTF_8, DEFAULT_MEDIA_TYPES.toArray(new MediaType[0]));
            this.autoTypeMatcher = AutoBindingTypeMatcher.of(autoType);
            if (this.autoTypeMatcher == null) {
                Logger.info(
                        false,
                        "Starter",
                        "Fastjson2 autoType is not configured, @type deserialization is disabled");
            } else {
                Logger.info(
                        false,
                        "Starter",
                        "Fastjson2 autoType is enabled, whitelist patterns: {}",
                        autoTypeMatcher.description());
            }
        }

        /**
         * Supports all application return value types.
         *
         * @param clazz the return value class
         * @return always {@code true}
         */
        @Override
        protected boolean supports(Class<?> clazz) {
            return true;
        }

        /**
         * Reads the request body into the target class with Fastjson2.
         *
         * @param clazz        the target class
         * @param inputMessage the HTTP input message
         * @return deserialized object
         * @throws HttpMessageNotReadableException if JSON cannot be read
         */
        @Override
        protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
                throws HttpMessageNotReadableException {
            try (var inputStream = inputMessage.getBody()) {
                String jsonString = new String(IoKit.readBytes(inputStream), Charset.UTF_8);
                Logger.debug(false, "Starter", "Fastjson deserializing JSON for class {}", clazz.getName());

                return autoTypeMatcher == null ? JSON.parseObject(jsonString, clazz, READER_FEATURES)
                        : JSON.parseObject(
                                jsonString,
                                clazz,
                                new PatternAutoTypeBeforeHandler(autoTypeMatcher),
                                READER_FEATURES);
            } catch (IOException e) {
                Logger.error(
                        false,
                        "Starter",
                        "Fastjson IO error occurred during JSON deserialization: targetClass={}, exception={}",
                        clazz.getName(),
                        e.getClass().getSimpleName(),
                        e);
                throw new HttpMessageNotReadableException(
                        "IO error occurred during JSON deserialization: " + e.getMessage(), e, inputMessage);
            } catch (Exception e) {
                Logger.error(
                        false,
                        "Starter",
                        "Fastjson JSON deserialization failed: targetClass={}, exception={}",
                        clazz.getName(),
                        e.getClass().getSimpleName(),
                        e);
                throw new HttpMessageNotReadableException("JSON deserialization failed: " + e.getMessage(), e,
                        inputMessage);
            }
        }

        /**
         * Writes the response body with Fastjson2.
         *
         * @param object        the object to serialize
         * @param outputMessage the HTTP output message
         * @throws HttpMessageNotWritableException if JSON cannot be written
         */
        @Override
        protected void writeInternal(Object object, HttpOutputMessage outputMessage)
                throws HttpMessageNotWritableException {
            try {
                Logger.debug(false, "Starter", "Result {}", object != null ? object.getClass().getName() : "null");

                PropertyFilter filter = (source, name, value) -> {
                    try {
                        Field field = FieldKit.getField(source.getClass(), name);
                        // Fastjson expects true to include; shouldSkipField returns true to skip.
                        return !shouldSkipField(field, value);
                    } catch (Exception e) {
                        Logger.warn(
                                false,
                                "Starter",
                                "Fastjson failed to get field for annotation check: {}, exception={}",
                                name,
                                e.getClass().getSimpleName());
                        // Keep serialization permissive when reflection lookup fails.
                        return !shouldSkipField(null, null);
                    }
                };

                String jsonString = JSON.toJSONString(object, filter, WRITER_FEATURES);
                outputMessage.getBody().write(jsonString.getBytes(Charset.UTF_8));
                Logger.info(false, "Starter", "Fastjson {}", jsonString.length());
            } catch (IOException e) {
                Logger.error(
                        false,
                        "Starter",
                        "Fastjson IO error occurred during JSON serialization: exception={}",
                        e.getClass().getSimpleName(),
                        e);
                throw new HttpMessageNotWritableException(
                        "IO error occurred during JSON serialization: " + e.getMessage(), e);
            } catch (Exception e) {
                Logger.error(
                        false,
                        "Starter",
                        "Fastjson JSON serialization failed: exception={}",
                        e.getClass().getSimpleName(),
                        e);
                throw new HttpMessageNotWritableException("JSON serialization failed: " + e.getMessage(), e);
            }
        }

        /**
         * Fastjson2 auto type guard backed by {@link AutoBindingTypeMatcher}.
         *
         * @author Kimi Liu
         * @since Java 21+
         */
        private static class PatternAutoTypeBeforeHandler implements JSONReader.AutoTypeBeforeHandler {

            /**
             * Auto type matcher used to approve or reject incoming type names.
             */
            private final AutoBindingTypeMatcher autoTypeMatcher;

            /**
             * Constructs a new auto type guard.
             *
             * @param autoTypeMatcher auto type matcher
             */
            private PatternAutoTypeBeforeHandler(AutoBindingTypeMatcher autoTypeMatcher) {
                this.autoTypeMatcher = autoTypeMatcher;
            }

            /**
             * Resolves an allowed Fastjson2 auto type.
             *
             * @param typeName    incoming type name
             * @param expectClass expected class supplied by Fastjson2
             * @param features    enabled Fastjson2 reader features
             * @return resolved class, or {@code null} if the type is rejected or unavailable
             */
            @Override
            public Class<?> apply(String typeName, Class<?> expectClass, long features) {
                if (!autoTypeMatcher.matches(typeName)) {
                    Logger.error(
                            false,
                            "Starter",
                            "Fastjson2 rejected @type '{}' by auto-type patterns: {}",
                            typeName,
                            autoTypeMatcher.description());
                    return null;
                }
                try {
                    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                    return resolveType(typeName, contextClassLoader);
                } catch (ClassNotFoundException e) {
                    Logger.error(
                            false,
                            "Starter",
                            "Fastjson2 failed to resolve type: typeNamePresent={}, exception={}",
                            typeName != null,
                            e.getClass().getSimpleName(),
                            e);
                    return null;
                }
            }

            /**
             * Resolves standard class names and canonical array names such as {@code java.lang.String[]}.
             *
             * @param typeName    the fastjson type name
             * @param classLoader the class loader to use first
             * @return the resolved class
             * @throws ClassNotFoundException if the type cannot be resolved
             */
            private Class<?> resolveType(String typeName, ClassLoader classLoader) throws ClassNotFoundException {
                if (typeName.endsWith("[]")) {
                    Class<?> componentType = resolveType(typeName.substring(0, typeName.length() - 2), classLoader);
                    return componentType.arrayType();
                }
                if (classLoader != null) {
                    return Class.forName(typeName, false, classLoader);
                }
                return Class.forName(typeName);
            }

        }

    }

}
