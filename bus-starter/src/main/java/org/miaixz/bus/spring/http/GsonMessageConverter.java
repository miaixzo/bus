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

import org.miaixz.bus.logger.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

/**
 * Gson JSON converter configurer, integrated with Spring MVC.
 * <p>
 * This component configures {@link GsonHttpMessageConverter} to handle JSON serialization and deserialization. It
 * supports an {@code autoType} feature to restrict deserialization to classes within a specified package prefix,
 * enhancing security by preventing arbitrary type deserialization. It also applies a unified field exclusion policy
 * based on {@code @Include} and {@code @Transient} annotations by delegating to the
 * {@link AbstractHttpMessageConverter} base class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Component
@ConditionalOnClass({ Gson.class })
public class GsonMessageConverter extends AbstractHttpMessageConverter {

    private AutoBindingTypeMatcher autoTypeMatcher;

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
     * It applies {@code autoType} restrictions if configured, creating a custom {@link Gson} instance.
     * </p>
     *
     * @param converters The list of {@link HttpMessageConverter}s to which the Gson converter will be added.
     */
    @Override
    public void configure(List<org.springframework.http.converter.HttpMessageConverter<?>> converters) {
        Logger.debug(
                "Configuring GsonHttpMessageConverter with autoType: {}",
                autoTypeMatcher == null ? null : autoTypeMatcher.description());

        // Configure Gson, adding autoType restriction
        GsonBuilder gsonBuilder = new GsonBuilder();

        // Apply the unified exclusion strategy for @Include and @Transient
        gsonBuilder.setExclusionStrategies(new com.google.gson.ExclusionStrategy() {

            @Override
            public boolean shouldSkipField(com.google.gson.FieldAttributes f) {
                try {
                    // Use reflection to get the Field object from its class and name.
                    Class<?> declaringClass = f.getDeclaringClass();
                    String fieldName = f.getName();
                    Field field = declaringClass.getDeclaredField(fieldName);

                    // CORRECTED: Removed the inversion. Both GSON's strategy and our method
                    // return true to SKIP, so the result can be used directly.
                    return isFieldIgnored(field);
                } catch (NoSuchFieldException | SecurityException e) {
                    Logger.warn(
                            "Could not access field '{}' for annotation check. Defaulting to include.",
                            f.getName(),
                            e.getMessage());
                    // If we can't inspect the field, don't skip it (default to including it).
                    return false;
                }
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                // CORRECTED: Delegate to the base class for consistent class-level skipping logic.
                return isClassIgnored(clazz);
            }
        });

        if (autoTypeMatcher != null) {
            gsonBuilder.registerTypeAdapterFactory(new AutoTypeAdapterFactory(autoTypeMatcher));
            Logger.debug("Gson autoType enabled for package patterns: {}", autoTypeMatcher.description());
        }

        Gson gson = gsonBuilder.create();
        GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
        converter.setGson(gson);
        // Keep Gson available for explicit opt-in only; regular application/json should prefer
        // Fastjson2/Jackson to avoid duplicate inherited-field binding issues in Gson.
        converter.setSupportedMediaTypes(List.of(new MediaType("application", "json+gson")));
        converters.add(order(), converter);
        Logger.debug("Gson converter configured with media types: {}", converter.getSupportedMediaTypes());
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
     */
    private static class AutoTypeAdapterFactory implements com.google.gson.TypeAdapterFactory {

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
            return null; // Delegate to default adapter
        }
    }

}
