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
package org.miaixz.bus.spring.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.logger.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Jackson JSON 框架的配置器，与 Spring MVC 集成。 支持自定义日期格式和 Java 8/11 时间 API（LocalDateTime）。
 */
public record JacksonConverterConfigurer() implements JsonConverterConfigurer {
    private static final String JACKSON_CLASS = "com.fasterxml.jackson.databind.ObjectMapper";

    @Override
    public String converterName() {
        return "Jackson";
    }

    @Override
    public boolean isAvailable() {
        boolean available = ClassKit.isPresent(JACKSON_CLASS, JacksonConverterConfigurer.class.getClassLoader());
        if (!available) {
            Logger.warn("Jackson dependency {} is not present in the classpath.", JACKSON_CLASS);
        } else {
            Logger.debug("Jackson dependency {} is present.", JACKSON_CLASS);
        }
        return available;
    }

    @Override
    public int order() {
        return 0; // 最高优先级
    }

    @Override
    public void configure(List<HttpMessageConverter<?>> converters) {
        Logger.debug("Configuring MappingJackson2HttpMessageConverter");
        // 配置 ObjectMapper，启用非空序列化和自定义日期处理
        ObjectMapper jacksonMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 添加对 Java 时间 API（LocalDateTime）的支持，使用自定义格式
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Fields.NORM_DATETIME);
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
        jacksonMapper.registerModule(javaTimeModule);

        // 创建并配置 Jackson 转换器
        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
        jacksonConverter.setObjectMapper(jacksonMapper);
        jacksonConverter.setSupportedMediaTypes(
                List.of(MediaType.APPLICATION_JSON, new MediaType("application", "json+jackson")));
        converters.add(order(), jacksonConverter);
        Logger.debug("Jackson converter configured with media types: {}", jacksonConverter.getSupportedMediaTypes());
    }
}