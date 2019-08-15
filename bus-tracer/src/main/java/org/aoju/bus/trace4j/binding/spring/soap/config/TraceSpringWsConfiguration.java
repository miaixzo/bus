package org.aoju.bus.trace4j.binding.spring.soap.config;

import org.aoju.bus.trace4j.Backend;
import org.aoju.bus.trace4j.consts.TraceConsts;
import org.aoju.bus.trace4j.binding.spring.soap.TraceClientInterceptor;
import org.aoju.bus.trace4j.binding.spring.soap.TracendpointInterceptor;
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
