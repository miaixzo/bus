/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.logger.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Jackson JSON framework configurer, integrated with Spring MVC.
 * <p>
 * This component configures {@link MappingJackson2HttpMessageConverter} to handle JSON serialization and
 * deserialization using Jackson. It supports custom date formats, Java 8/11 Time API ({@link LocalDateTime}), and an
 * {@code autoType} configuration to restrict deserialization to classes within a specified package prefix, enhancing
 * security.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Component
@ConditionalOnClass({ ObjectMapper.class })
public class JacksonConverterConfigurer implements JsonConverterConfigurer {

    private String autoType;

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
        return 0; // Highest precedence among default JSON converters
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
    public void configure(List<HttpMessageConverter<?>> converters) {
        Logger.debug("Configuring MappingJackson2HttpMessageConverter with autoType: {}", autoType);
        // Configure ObjectMapper, enable non-null serialization and custom date handling
        ObjectMapper jacksonMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Configure autoType restrictions
        if (autoType != null && !autoType.isEmpty()) {
            PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder().allowIfBaseType(autoType).build();
            jacksonMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
            Logger.debug("Jackson autoType enabled for package prefix: {}", autoType);
        }

        // Add support for Java Time API (LocalDateTime), using a custom format
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Fields.NORM_DATETIME);
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
        jacksonMapper.registerModule(javaTimeModule);

        // Create and configure Jackson converter
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

}
