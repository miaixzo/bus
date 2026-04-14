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

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.xyz.FieldKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.logger.Logger;
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

/**
 * A JSON converter configurer for Fastjson2. This class configures the
 * {@link org.springframework.http.converter.HttpMessageConverter} for Fastjson2, with support for {@code autoType}
 * functionality.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Component
@ConditionalOnClass({ JSON.class })
public class FastjsonMessageConverter extends AbstractHttpMessageConverter {

    /**
     * The default media types supported by this converter.
     */
    private static final List<MediaType> DEFAULT_MEDIA_TYPES = List
            .of(MediaType.APPLICATION_JSON, new MediaType(MediaType.TEXT_PLAIN, Charset.UTF_8));

    /**
     * The comma-separated string of whitelisted classes for autoType deserialization.
     */
    private String autoType;

    @Override
    public String name() {
        return "Fastjson2";
    }

    @Override
    public int order() {
        return 0;
    }

    @Override
    public void configure(List<org.springframework.http.converter.HttpMessageConverter<?>> converters) {
        Logger.debug("Configuring FastJson2HttpMessageConverter for Fastjson2");
        converters.add(order(), new FastJson2HttpMessageConverter(this.autoType));
        Logger.debug("FastJson2HttpMessageConverter configured with media types: {}", DEFAULT_MEDIA_TYPES);
    }

    @Override
    public void autoType(String autoType) {
        this.autoType = autoType;
    }

    /**
     * A custom {@link org.springframework.http.converter.AbstractHttpMessageConverter} for Fastjson2. It configures
     * serialization using {@link JSONWriter.Feature} and deserialization using {@link JSONReader.Feature}, and enables
     * {@code autoType} based on the constructor parameter.
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
         * An array of whitelisted class names for autoType deserialization.
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
                Logger.info("Fastjson2 autoType is not configured, @type deserialization is disabled");
            } else {
                Logger.info("Fastjson2 autoType is enabled, whitelist patterns: {}", autoTypeMatcher.description());
            }
        }

        @Override
        protected boolean supports(Class<?> clazz) {
            return true;
        }

        @Override
        protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
                throws HttpMessageNotReadableException {
            try (var inputStream = inputMessage.getBody()) {
                String jsonString = new String(IoKit.readBytes(inputStream), Charset.UTF_8);
                Logger.debug("Deserializing JSON for class {}", clazz.getName());

                return autoTypeMatcher == null ? JSON.parseObject(jsonString, clazz, READER_FEATURES)
                        : JSON.parseObject(
                                jsonString,
                                clazz,
                                new PatternAutoTypeBeforeHandler(autoTypeMatcher),
                                READER_FEATURES);
            } catch (IOException e) {
                Logger.error(
                        "IO error occurred during JSON deserialization for class {}: {}",
                        clazz.getName(),
                        e.getMessage(),
                        e);
                throw new HttpMessageNotReadableException(
                        "IO error occurred during JSON deserialization: " + e.getMessage(), e, inputMessage);
            } catch (Exception e) {
                Logger.error("JSON deserialization failed for class {}: {}", clazz.getName(), e.getMessage(), e);
                throw new HttpMessageNotReadableException("JSON deserialization failed: " + e.getMessage(), e,
                        inputMessage);
            }
        }

        @Override
        protected void writeInternal(Object object, HttpOutputMessage outputMessage)
                throws HttpMessageNotWritableException {
            try {
                Logger.debug(false, "Result", "{}", object != null ? object.getClass().getName() : "null");

                // The PropertyFilter now delegates all logic to the shouldSkipField method.
                PropertyFilter filter = (source, name, value) -> {
                    try {
                        Field field = FieldKit.getField(source.getClass(), name);
                        // CORRECTED: Invert the result from shouldSkipField.
                        // shouldSkipField returns true to SKIP, but PropertyFilter expects true to INCLUDE.
                        return !shouldSkipField(field, value);
                    } catch (Exception e) {
                        Logger.warn("Failed to get field for annotation check: {}, error: {}", name, e.getMessage());
                        // If an error occurs, default to including the field to be safe.
                        // shouldSkipField(null) returns false (don't skip), so !false is true (include).
                        return !shouldSkipField(null, null);
                    }
                };

                String jsonString = JSON.toJSONString(object, filter, WRITER_FEATURES);
                outputMessage.getBody().write(jsonString.getBytes(Charset.UTF_8));
                Logger.info(false, "Fastjson", "{}", jsonString.length());
            } catch (IOException e) {
                Logger.error(false, "Fastjson", "IO error occurred during JSON serialization: {}", e.getMessage());
                throw new HttpMessageNotWritableException(
                        "IO error occurred during JSON serialization: " + e.getMessage(), e);
            } catch (Exception e) {
                Logger.error(false, "Fastjson", "JSON serialization failed: {}", e.getMessage());
                throw new HttpMessageNotWritableException("JSON serialization failed: " + e.getMessage(), e);
            }
        }

        private static class PatternAutoTypeBeforeHandler implements JSONReader.AutoTypeBeforeHandler {

            private final AutoBindingTypeMatcher autoTypeMatcher;

            private PatternAutoTypeBeforeHandler(AutoBindingTypeMatcher autoTypeMatcher) {
                this.autoTypeMatcher = autoTypeMatcher;
            }

            @Override
            public Class<?> apply(String typeName, Class<?> expectClass, long features) {
                if (!autoTypeMatcher.matches(typeName)) {
                    Logger.error(
                            "Fastjson2 rejected @type '{}' by auto-type patterns: {}",
                            typeName,
                            autoTypeMatcher.description());
                    return null;
                }
                try {
                    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                    return resolveType(typeName, contextClassLoader);
                } catch (ClassNotFoundException e) {
                    Logger.error("Fastjson2 failed to resolve @type '{}': {}", typeName, e.getMessage());
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
