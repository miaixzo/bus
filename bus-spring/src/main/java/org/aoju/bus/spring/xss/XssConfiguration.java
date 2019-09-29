/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.spring.xss;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.aoju.bus.core.utils.CollUtils;
import org.aoju.bus.core.utils.EscapeUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author Kimi Liu
 * @version 3.6.1
 * @since JDK 1.8
 */
@EnableConfigurationProperties(value = {XssProperties.class})
public class XssConfiguration {

    @Autowired(required = false)
    private XssProperties xssProperties;

    @Bean
    public FilterRegistrationBean registrationXssFilter(XssProperties properties) {
        FilterRegistrationBean<XssFilter> registrationBean = new FilterRegistrationBean<>();
        // 设置过滤路径
        registrationBean.setEnabled(true);
        // 设置顺序
        registrationBean.setOrder(properties.getOrder());
        // 设置 BodyCacheFilter
        registrationBean.setFilter(new XssFilter());
        final String name = properties.getName();
        if (!StringUtils.isEmpty(name)) {
            registrationBean.setName(properties.getName());
        }
        final Map<String, String> initParameters = properties.getInitParameters();
        if (initParameters != null && initParameters.size() > 0) {
            registrationBean.setInitParameters(properties.getInitParameters());
        }
        final Set<ServletRegistrationBean<?>> registrationBeans = properties.getServletRegistrationBeans();
        if (registrationBeans != null && registrationBeans.size() > 0) {
            registrationBean.setServletRegistrationBeans(properties.getServletRegistrationBeans());
        }
        final Set<String> servletNames = properties.getServletNames();
        if (!CollUtils.isEmpty(servletNames)) {
            registrationBean.setServletNames(properties.getServletNames());
        }
        return registrationBean;
    }

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        //解析器
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        //注册xss解析器
        SimpleModule simpleModule = new SimpleModule("commonJsonSerializer");
        simpleModule.addSerializer(new CommonJsonSerializer(xssProperties));
        objectMapper.registerModule(simpleModule);
        return objectMapper;
    }

    static class CommonJsonSerializer extends JsonSerializer<String> {

        private XssProperties xssProperties;

        CommonJsonSerializer(XssProperties xssProperties) {
            this.xssProperties = xssProperties;
        }

        @Override
        public Class<String> handledType() {
            return String.class;
        }

        @Override
        public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (value != null && xssProperties != null) {
                value = EscapeUtils.escapeHtml4(value);
                jsonGenerator.writeString(value);
            }
        }
    }

}
