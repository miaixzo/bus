/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * @since Java 17+
 */
@Component
@ConditionalOnClass({ Gson.class })
public class GsonMessageConverter extends AbstractHttpMessageConverter {

    private String autoType;

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
        Logger.debug("Configuring GsonHttpMessageConverter with autoType: {}", autoType);

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

        if (autoType != null && !autoType.isEmpty()) {
            gsonBuilder.registerTypeAdapterFactory(new AutoTypeAdapterFactory(autoType));
            Logger.debug("Gson autoType enabled for package prefix: {}", autoType);
        }

        Gson gson = gsonBuilder.create();
        GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
        converter.setGson(gson);
        converter
                .setSupportedMediaTypes(List.of(MediaType.APPLICATION_JSON, new MediaType("application", "json+gson")));
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
        this.autoType = autoType;
    }

    /**
     * Custom {@link com.google.gson.TypeAdapterFactory} that restricts deserialization to types within a specified
     * package prefix (autoTypePrefix).
     */
    private static class AutoTypeAdapterFactory implements com.google.gson.TypeAdapterFactory {

        private final String autoTypePrefix;

        /**
         * Constructs an {@code AutoTypeAdapterFactory} with the given package prefix.
         *
         * @param autoTypePrefix The package prefix to allow for deserialization.
         */
        public AutoTypeAdapterFactory(String autoTypePrefix) {
            this.autoTypePrefix = autoTypePrefix;
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
            if (autoTypePrefix != null && !rawType.getName().startsWith(autoTypePrefix)) {
                throw new JsonParseException("Type not allowed: " + rawType.getName()
                        + ", must start with package prefix: " + autoTypePrefix);
            }
            return null; // Delegate to default adapter
        }
    }

}
