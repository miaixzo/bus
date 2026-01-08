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
import java.lang.reflect.Member;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.logger.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

/**
 * Jackson JSON framework configurer, integrated with Spring MVC.
 * <p>
 * This component configures {@link MappingJackson2HttpMessageConverter} to handle JSON serialization and
 * deserialization using Jackson. It supports custom date formats, Java 8/11 Time API ({@link LocalDateTime}), and an
 * {@code autoType} configuration to restrict deserialization to classes within a specified package prefix, enhancing
 * security. It also applies a unified field exclusion policy based on {@code @Include} and {@code @Transient}
 * annotations via a custom {@link PropertyFilter}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Component
@ConditionalOnClass({ ObjectMapper.class })
public class JacksonMessageConverter extends AbstractHttpMessageConverter {

    private String autoType;

    /**
     * A unique ID for the custom property filter used to handle @Include and @Transient.
     */
    private static final String FILTER_ID = "includeTransientFilter";

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
     * @return The order value, typically 0 for Jackson (highest precedence).
     */
    @Override
    public int order() {
        return 1; // Highest precedence among default JSON converters
    }

    /**
     * Configures the {@link MappingJackson2HttpMessageConverter} and adds it to the list of converters.
     * <p>
     * It sets up {@link ObjectMapper} with non-null serialization, disables writing dates as timestamps, configures
     * {@code autoType} restrictions, and registers {@link JavaTimeModule} for {@link LocalDateTime} handling.
     * </p>
     *
     * @param converters The list of {@link HttpMessageConverter}s to which the Jackson converter will be added.
     */
    @Override
    public void configure(List<org.springframework.http.converter.HttpMessageConverter<?>> converters) {
        Logger.debug("Configuring MappingJackson2HttpMessageConverter with autoType: {}", autoType);
        ObjectMapper jacksonMapper = new ObjectMapper();

        // 1. Configure the PropertyFilter to handle @Include/@Transient and null/empty values.
        SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        filterProvider.addFilter(FILTER_ID, new IncludeTransientPropertyFilter());
        jacksonMapper.setFilterProvider(filterProvider);

        // 2. Add a mix-in to apply the filter to all Object classes without modifying them.
        jacksonMapper.addMixIn(Object.class, FilterMixIn.class);

        // 3. Configure other mapper settings
        jacksonMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS); // Let the filter handle inclusion
        jacksonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 4. Configure autoType restrictions
        if (autoType != null && !autoType.isEmpty()) {
            Logger.debug("Jackson autoType is DISABLED to ensure clean JSON output.");
        }

        // 5. Add support for Java Time API (LocalDateTime)
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Fields.NORM_DATETIME);
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
        jacksonMapper.registerModule(javaTimeModule);

        // 6. Create and configure Jackson converter
        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
        jacksonConverter.setObjectMapper(jacksonMapper);
        jacksonConverter.setSupportedMediaTypes(
                List.of(MediaType.APPLICATION_JSON, new MediaType("application", "json+jackson")));
        converters.add(order(), jacksonConverter);
        Logger.debug("Jackson converter configured with media types: {}", jacksonConverter.getSupportedMediaTypes());
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
     * A simple mix-in interface that applies the {@code @JsonFilter} annotation to all classes.
     */
    @com.fasterxml.jackson.annotation.JsonFilter(FILTER_ID)
    interface FilterMixIn {
        // This interface is intentionally empty. It's only used to carry the annotation.
    }

    /**
     * A custom {@link PropertyFilter} that implements the unified filtering logic. It respects @Include, @Transient,
     * and excludes null/empty values.
     */
    static class IncludeTransientPropertyFilter extends SimpleBeanPropertyFilter {

        @Override
        public void serializeAsField(
                Object pojo,
                com.fasterxml.jackson.core.JsonGenerator jgen,
                com.fasterxml.jackson.databind.SerializerProvider provider,
                BeanPropertyWriter writer) throws Exception {
            // Robustly find the underlying Field object for annotation checks.
            Field field = null;
            AnnotatedMember annotatedMember = writer.getMember();
            Member member = annotatedMember.getMember();

            if (member instanceof Field) {
                field = (Field) member;
            } else {
                // If the property is backed by a method (e.g., a getter), try to find the corresponding field.
                try {
                    field = member.getDeclaringClass().getDeclaredField(writer.getName());
                } catch (NoSuchFieldException e) {
                    // This can happen for virtual properties without a backing field.
                    // We'll proceed with 'field' as null, which shouldSkipField can handle.
                    Logger.debug(
                            "Could not find backing field for property '{}' on class '{}'. Assuming inclusion.",
                            writer.getName(),
                            pojo.getClass().getName());
                }
            }

            // Use writer.get(pojo) to get the actual value of the property.
            Object value = writer.get(pojo);

            // CORRECTED: Invert the condition.
            // The logic should be: "If the field should NOT be skipped, then serialize it."
            if (!shouldSkipField(field, value)) {
                writer.serializeAsField(pojo, jgen, provider);
            } else {
                // If the field should be skipped, we can choose to write nothing or write a null.
                // Writing nothing is cleaner.
                if (!jgen.canOmitFields()) {
                    jgen.writeFieldName(writer.getName());
                    jgen.writeNull();
                }
            }
        }

    }

}
