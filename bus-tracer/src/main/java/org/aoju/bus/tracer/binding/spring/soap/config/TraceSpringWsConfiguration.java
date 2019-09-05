/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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
package org.aoju.bus.tracer.binding.spring.soap.config;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.binding.spring.soap.TraceClientInterceptor;
import org.aoju.bus.tracer.binding.spring.soap.TracendpointInterceptor;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 3.1.9
 * @since JDK 1.8
 */
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Configuration
public class TraceSpringWsConfiguration {

    @Autowired
    Backend backend;

    @Bean
    TracendpointInterceptor TraceEndpointInterceptor() {
        return new TracendpointInterceptor(backend, TraceConsts.DEFAULT);
    }

    @Bean
    TraceClientInterceptor TraceClientInterceptor() {
        return new TraceClientInterceptor(backend, TraceConsts.DEFAULT);
    }

    @Bean
    WsConfigurerAdapter TraceWsConfigurerAdapter(final TracendpointInterceptor TraceEndpointInterceptor) {
        return new WsConfigurerAdapter() {
            @Override
            public void addInterceptors(List<EndpointInterceptor> interceptors) {
                super.addInterceptors(Collections.<EndpointInterceptor>singletonList(TraceEndpointInterceptor));
            }
        };
    }

    @Bean(name = "org.aoju.bus.trace4j.binding.spring.http.RestTemplatePostProcessor")
    WebServiceTemplatePostProcessor restTemplatePostProcessor(TraceClientInterceptor TraceClientInterceptor) {
        return new WebServiceTemplatePostProcessor(TraceClientInterceptor);
    }

    static class WebServiceTemplatePostProcessor implements BeanPostProcessor {

        final TraceClientInterceptor interceptor;

        WebServiceTemplatePostProcessor(TraceClientInterceptor interceptor) {
            this.interceptor = interceptor;
        }

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof WebServiceTemplate) {
                final WebServiceTemplate webServiceTemplate = (WebServiceTemplate) bean;
                final ClientInterceptor[] interceptors = webServiceTemplate.getInterceptors();
                final ClientInterceptor[] newInterceptors;
                if (interceptors != null) {
                    newInterceptors = Arrays.copyOf(interceptors, interceptors.length + 1);
                } else {
                    newInterceptors = new ClientInterceptor[1];
                }
                newInterceptors[newInterceptors.length - 1] = interceptor;
                webServiceTemplate.setInterceptors(newInterceptors);
            }
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }
    }

}
