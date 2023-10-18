/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.starter.tracer;

import jakarta.annotation.Resource;
import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.Tracer;
import org.aoju.bus.tracer.binding.spring.context.PostTpicAsyncBeanPostProcessor;
import org.aoju.bus.tracer.binding.spring.context.PreTpicAsyncBeanPostProcessor;
import org.aoju.bus.tracer.binding.spring.http.TraceClientHttpRequestInterceptor;
import org.aoju.bus.tracer.binding.spring.web.TraceInterceptor;
import org.aoju.bus.tracer.binding.spring.web.TraceResponseBodyAdvice;
import org.aoju.bus.tracer.config.TraceFilterConfig;
import org.aoju.bus.tracer.transport.HttpHeaderTransport;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
@ConditionalOnWebApplication
@ConditionalOnClass({Tracer.class, RestTemplate.class})
@ConditionalOnBean({AsyncTaskExecutor.class, RestTemplate.class})
@EnableConfigurationProperties(TracerProperties.class)
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class TracerConfiguration {

    @Resource
    TracerProperties properties;

    @Resource
    Backend backend;

    @Bean
    WebMvcConfigurer traceSpringMvcWebMvcConfigurerAdapter() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new TraceInterceptor(backend));
            }
        };
    }

    @Bean
    public PreTpicAsyncBeanPostProcessor preTpicAsyncBeanPostProcessor(AsyncTaskExecutor executor, Backend backend) {
        return new PreTpicAsyncBeanPostProcessor(executor, backend);
    }

    @Bean
    public PostTpicAsyncBeanPostProcessor postTpicAsyncBeanPostProcessor(AsyncTaskExecutor executor, Backend backend) {
        return new PostTpicAsyncBeanPostProcessor(executor, backend);
    }

    @Bean
    TraceResponseBodyAdvice TraceSpringMvcResponseBodyAdvice() {
        return new TraceResponseBodyAdvice();
    }

    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean
    TraceClientHttpRequestInterceptor TraceClientHttpRequestInterceptor(Backend Backend) {
        return new TraceClientHttpRequestInterceptor(Backend, new HttpHeaderTransport(), Builder.DEFAULT);
    }

    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean
    BeanPostProcessor restTemplatePostProcessor(TraceClientHttpRequestInterceptor TraceClientHttpRequestInterceptor) {
        return new RestTemplatePostProcessor(TraceClientHttpRequestInterceptor);
    }

    @ConditionalOnMissingBean(TraceFilterConfig.class)
    public static class TracePropertiesConfig {

        @Bean
        @Role(BeanDefinition.ROLE_SUPPORT)
        public TraceFilterConfig filterConfiguration(TracerProperties properties) {
            return properties.getAsFilterConfiguration();
        }

    }

    private static class RestTemplatePostProcessor implements BeanPostProcessor {

        private final TraceClientHttpRequestInterceptor interceptor;

        private RestTemplatePostProcessor(TraceClientHttpRequestInterceptor interceptor) {
            this.interceptor = interceptor;
        }

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof RestTemplate) {
                ((RestTemplate) bean).getInterceptors().add(interceptor);
            }
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }
    }

}
