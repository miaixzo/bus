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
import java.lang.reflect.Member;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;

import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.logger.Logger;

import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.PropertyFilter;
import tools.jackson.databind.ser.PropertyWriter;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;
import tools.jackson.databind.ser.std.SimpleFilterProvider;

/**
 * Jackson JSON converter configurer for Spring MVC.
 * <p>
 * This component configures {@link JacksonJsonHttpMessageConverter} to handle JSON serialization and deserialization
 * using Jackson 3. It supports custom date formats, Java Time API ({@link LocalDateTime}), and an {@code autoType}
 * configuration to restrict deserialization to classes within a specified package prefix, enhancing security. It also
 * applies a unified field exclusion policy based on {@code @Include} and {@code @Transient} annotations via a custom
 * {@link PropertyFilter}.
 * </p>
 * <p>
 * The registered converter prefers {@code application/json} for ordinary HTTP content negotiation and keeps
 * {@code application/json+jackson} for clients that explicitly opt in to Jackson-specific responses.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Component
@ConditionalOnClass({ JsonMapper.class })
public class JacksonMessageConverter extends AbstractHttpMessageConverter {

    /**
     * A unique ID for the custom property filter used to handle @Include and @Transient.
     */
    private static final String FILTER_ID = "includeTransientFilter";

    /**
     * Default media types supported by this converter.
     * <p>
     * Order matters: Spring uses this list during content negotiation, so {@code application/json} must remain first to
     * avoid wildcard browser requests selecting {@code application/json+jackson} by default.
     * </p>
     */
    private static final List<MediaType> DEFAULT_MEDIA_TYPES = List.of(
            MediaType.APPLICATION_JSON,
            MediaType.parseMediaType(org.miaixz.bus.core.lang.MediaType.APPLICATION_JSON_JACKSON));

    /**
     * Auto type matcher compiled from the configured allow-list expression.
     */
    private AutoBindingTypeMatcher autoTypeMatcher;

    /**
     * Constructs a new JacksonMessageConverter instance.
     */
    public JacksonMessageConverter() {
        // No initialization required.
    }

    /**
     * Returns the name of this JSON converter configurer.
     *
     * @return The string "Jackson".
     */
    @Override
    public String name() {
        return "Jackson";
    }

    /**
     * Returns the order of this JSON converter configurer. A lower value indicates higher precedence.
     *
     * @return The order value, typically 1 for Jackson.
     */
    @Override
    public int order() {
        return 1;
    }

    /**
     * Configures the {@link JacksonJsonHttpMessageConverter} and adds it to the list of converters.
     * <p>
     * It sets up {@link JsonMapper} with a unified filter, configures {@code autoType} restrictions, and registers
     * custom {@link LocalDateTime} handling.
     * </p>
     *
     * @param converters The list of {@link org.springframework.http.converter.HttpMessageConverter}s to which the
     *                   Jackson converter will be added.
     */
    @Override
    public void configure(List<org.springframework.http.converter.HttpMessageConverter<?>> converters) {
        Logger.debug(
                false,
                "Starter",
                "Configuring JacksonJsonHttpMessageConverter with autoType: {}",
                autoTypeMatcher == null ? null : autoTypeMatcher.description());

        SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        filterProvider.addFilter(FILTER_ID, new IncludeTransientPropertyFilter());

        if (autoTypeMatcher != null) {
            Logger.debug(
                    false,
                    "Starter",
                    "Jackson autoType patterns are prepared but default polymorphic typing remains disabled: {}",
                    autoTypeMatcher.description());
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Fields.NORM_DATETIME);
        SimpleModule javaTimeModule = new SimpleModule("BusJavaTimeModule");
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));

        JsonMapper jacksonMapper = JsonMapper.builder().filterProvider(filterProvider)
                .addMixIn(Object.class, FilterMixIn.class)
                .changeDefaultPropertyInclusion(
                        value -> JsonInclude.Value.construct(JsonInclude.Include.ALWAYS, JsonInclude.Include.ALWAYS))
                .addModule(javaTimeModule).build();

        JacksonJsonHttpMessageConverter jacksonConverter = new JacksonJsonHttpMessageConverter(jacksonMapper);
        jacksonConverter.setSupportedMediaTypes(DEFAULT_MEDIA_TYPES);
        converters.add(order(), jacksonConverter);
        Logger.debug(
                false,
                "Starter",
                "Jackson converter configured with media types: {}",
                jacksonConverter.getSupportedMediaTypes());
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
     * A simple mix-in interface that applies the {@code @JsonFilter} annotation to all classes.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @JsonFilter(FILTER_ID)
    interface FilterMixIn {
        // Carries the JsonFilter annotation for the global Object mix-in.

    }

    /**
     * A custom {@link PropertyFilter} that implements the unified filtering logic. It respects @Include, @Transient,
     * and excludes null/empty values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    static class IncludeTransientPropertyFilter extends SimpleBeanPropertyFilter {

        @Override
        public void serializeAsProperty(
                Object pojo,
                tools.jackson.core.JsonGenerator jgen,
                SerializationContext provider,
                PropertyWriter writer) throws Exception {
            // Robustly find the underlying Field object for annotation checks.
            Field field = null;
            Object value = null;

            if (writer instanceof BeanPropertyWriter beanWriter) {
                AnnotatedMember annotatedMember = beanWriter.getMember();
                if (annotatedMember != null) {
                    Member member = annotatedMember.getMember();
                    if (member instanceof Field memberField) {
                        field = memberField;
                    } else if (member != null) {
                        try {
                            field = member.getDeclaringClass().getDeclaredField(beanWriter.getName());
                        } catch (NoSuchFieldException e) {
                            Logger.debug(
                                    false,
                                    "Starter",
                                    "Jackson could not find backing field for property '{}' on class '{}'. Assuming inclusion.",
                                    beanWriter.getName(),
                                    pojo.getClass().getName());
                        }
                    }
                }
                value = beanWriter.get(pojo);
            }

            if (!shouldSkipField(field, value)) {
                writer.serializeAsProperty(pojo, jgen, provider);
            } else {
                if (!jgen.canOmitProperties()) {
                    jgen.writeName(writer.getName());
                    jgen.writeNull();
                }
            }
        }

    }

}
