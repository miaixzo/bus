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

import java.lang.reflect.Field;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

import org.miaixz.bus.logger.Logger;

/**
 * Gson JSON converter configurer for Spring MVC.
 * <p>
 * This component configures {@link GsonHttpMessageConverter} to handle JSON serialization and deserialization. It
 * supports an {@code autoType} feature to restrict deserialization to classes within a specified package prefix,
 * enhancing security by preventing arbitrary type deserialization. It also applies a unified field exclusion policy
 * based on {@code @Include} and {@code @Transient} annotations by delegating to the
 * {@link AbstractHttpMessageConverter} base class.
 * </p>
 * <p>
 * The registered converter prefers {@code application/json} for ordinary HTTP content negotiation and keeps
 * {@code application/json+gson} for clients that explicitly opt in to Gson-specific responses.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Component
@ConditionalOnClass({ Gson.class })
public class GsonMessageConverter extends AbstractHttpMessageConverter {

    /**
     * Default media types supported by this converter.
     * <p>
     * Order matters: Spring uses this list during content negotiation, so {@code application/json} must remain first to
     * avoid wildcard browser requests selecting {@code application/json+gson} by default.
     * </p>
     */
    private static final List<MediaType> DEFAULT_MEDIA_TYPES = List.of(
            MediaType.APPLICATION_JSON,
            MediaType.parseMediaType(org.miaixz.bus.core.net.MediaType.APPLICATION_JSON_GSON));

    /**
     * Auto type matcher compiled from the configured allow-list expression.
     */
    private AutoBindingTypeMatcher autoTypeMatcher;

    /**
     * Constructs a new GsonMessageConverter instance.
     */
    public GsonMessageConverter() {
        // No initialization required.
    }

    /**
     * Returns the name of this JSON converter configurer.
     *
     * @return The string "Gson".
     */
    @Override
    public String name() {
        return "Gson";
    }

    /**
     * Returns the order of this JSON converter configurer. A higher value indicates lower precedence.
     *
     * @return The order value, typically 2 for Gson.
     */
    @Override
    public int order() {
        return 2;
    }

    /**
     * Configures the {@link GsonHttpMessageConverter} and adds it to the list of converters.
     * <p>
     * It applies the common field exclusion policy and optional {@code autoType} restrictions before registering the
     * converter.
     * </p>
     *
     * @param converters The list of {@link org.springframework.http.converter.HttpMessageConverter}s to which the Gson
     *                   converter will be added.
     */
    @Override
    public void configure(List<org.springframework.http.converter.HttpMessageConverter<?>> converters) {
        Logger.debug(
                false,
                "Starter",
                "Configuring GsonHttpMessageConverter with autoType: {}",
                autoTypeMatcher == null ? null : autoTypeMatcher.description());

        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.setExclusionStrategies(new com.google.gson.ExclusionStrategy() {

            @Override
            public boolean shouldSkipField(com.google.gson.FieldAttributes f) {
                try {
                    Class<?> declaringClass = f.getDeclaringClass();
                    String fieldName = f.getName();
                    Field field = declaringClass.getDeclaredField(fieldName);

                    // Gson and isFieldIgnored both use true to mean "skip this field".
                    return isFieldIgnored(field);
                } catch (NoSuchFieldException | SecurityException e) {
                    Logger.warn(
                            false,
                            "Starter",
                            "Gson could not access field '{}' for annotation check. Defaulting to include: {}",
                            f.getName(),
                            e.getClass().getSimpleName());
                    // Keep serialization permissive when reflection lookup fails.
                    return false;
                }
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return isClassIgnored(clazz);
            }
        });

        if (autoTypeMatcher != null) {
            gsonBuilder.registerTypeAdapterFactory(new AutoTypeAdapterFactory(autoTypeMatcher));
            Logger.debug(
                    false,
                    "Starter",
                    "Gson autoType enabled for package patterns: {}",
                    autoTypeMatcher.description());
        }

        Gson gson = gsonBuilder.create();
        GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
        converter.setGson(gson);
        converter.setSupportedMediaTypes(DEFAULT_MEDIA_TYPES);
        converters.add(order(), converter);
        Logger.debug(
                false,
                "Starter",
                "Gson converter configured with media types: {}",
                converter.getSupportedMediaTypes());
    }

    /**
     * Sets the autoType package prefix for deserialization safety.
     *
     * @param autoType The package prefix string.
     */
    @Override
    public void autoType(String autoType) {
        this.autoTypeMatcher = AutoBindingTypeMatcher.of(autoType);
    }

    /**
     * Custom {@link com.google.gson.TypeAdapterFactory} that restricts deserialization to types within a specified
     * package prefix (autoTypePrefix).
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static class AutoTypeAdapterFactory implements com.google.gson.TypeAdapterFactory {

        /**
         * Auto type matcher used to approve or reject adapter target types.
         */
        private final AutoBindingTypeMatcher autoTypeMatcher;

        /**
         * Constructs an {@code AutoTypeAdapterFactory} with the given package prefix.
         *
         * @param autoTypeMatcher The package matcher used to allow deserialization.
         */
        public AutoTypeAdapterFactory(AutoBindingTypeMatcher autoTypeMatcher) {
            this.autoTypeMatcher = autoTypeMatcher;
        }

        /**
         * Creates a {@link TypeAdapter} for the given type.
         * <p>
         * If the raw type's name does not start with the {@code autoTypePrefix}, a {@link JsonParseException} is thrown
         * to prevent deserialization of unauthorized types.
         * </p>
         *
         * @param gson The {@link Gson} instance.
         * @param type The {@link TypeToken} of the type to adapt.
         * @param <T>  The type.
         * @return A {@link TypeAdapter} for the type, or {@code null} to delegate to default adapters.
         * @throws JsonParseException if the type is not allowed by the autoTypePrefix.
         */
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            Class<?> rawType = type.getRawType();
            if (autoTypeMatcher != null && !autoTypeMatcher.matches(rawType)) {
                throw new JsonParseException("Type not allowed: " + rawType.getName()
                        + ", must match auto-type patterns: " + autoTypeMatcher.description());
            }
            return null;
        }

    }

}
