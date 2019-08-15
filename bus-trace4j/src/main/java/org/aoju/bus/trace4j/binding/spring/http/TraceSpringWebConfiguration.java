package org.aoju.bus.trace4j.binding.spring.http;


import org.aoju.bus.trace4j.Backend;
import org.aoju.bus.trace4j.consts.TraceConsts;
import org.aoju.bus.trace4j.transport.HttpHeaderTransport;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TraceSpringWebConfiguration {

    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean
    TraceClientHttpRequestInterceptor TraceClientHttpRequestInterceptor(Backend Backend) {
        return new TraceClientHttpRequestInterceptor(Backend, new HttpHeaderTransport(), TraceConsts.DEFAULT);
    }

    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean
    BeanPostProcessor restTemplatePostProcessor(TraceClientHttpRequestInterceptor TraceClientHttpRequestInterceptor) {
        return new RestTemplatePostProcessor(TraceClientHttpRequestInterceptor);
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
