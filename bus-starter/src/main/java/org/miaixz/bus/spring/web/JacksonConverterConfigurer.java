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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.logger.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

/**
 * Jackson JSON 框架的配置器，与 Spring MVC 集成。支持自定义日期格式和 Java 8/11 时间 API（LocalDateTime）。 支持 autoType 配置，限制反序列化到指定包前缀的类。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Component
@ConditionalOnClass({ ObjectMapper.class })
public class JacksonConverterConfigurer implements JsonConverterConfigurer {

    private String autoType;

    @Override
    public String name() {
        return "Jackson";
    }

    @Override
    public int order() {
        return 0; // 最高优先级
    }

    @Override
    public void configure(List<HttpMessageConverter<?>> converters) {
        Logger.debug("Configuring MappingJackson2HttpMessageConverter with autoType: {}", autoType);
        // 配置 ObjectMapper，启用非空序列化和自定义日期处理
        ObjectMapper jacksonMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 配置 autoType 限制
        if (autoType != null && !autoType.isEmpty()) {
            PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder().allowIfBaseType(autoType).build();
            jacksonMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
            Logger.debug("Jackson autoType enabled for package prefix: {}", autoType);
        }

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

    @Override
    public void autoType(String autoType) {
        this.autoType = autoType;
    }

}