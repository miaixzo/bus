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
package org.miaixz.bus.starter.context;

import java.util.EnumSet;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import org.miaixz.bus.spring.ContextBuilder;
import org.miaixz.bus.spring.web.ContextBindingFilter;
import org.miaixz.bus.spring.web.RequestContext;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Configures runtime context propagation for Servlet web applications.
 *
 * <p>
 * This configuration supplies the shared request accessor for every Servlet application. Context binding is activated
 * by default and can be disabled with {@code bus.context.web.enabled=false}; disabling binding does not create a second
 * request-access implementation for other Servlet integrations.
 *
 * @author Kimi Liu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ Servlet.class, Filter.class, FilterRegistrationBean.class })
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebConfiguration {

    /**
     * Initializes the configuration activated for Servlet runtime context propagation.
     */
    public WebConfiguration() {
        // No initialization required.
    }

    /**
     * Creates the request accessor used to resolve transport values at the Servlet boundary.
     *
     * @return stateless accessor whose derived values are stored in Servlet request attributes
     */
    @Bean
    @ConditionalOnMissingBean(RequestContext.class)
    RequestContext requestContext() {
        return new RequestContext();
    }

    /**
     * Creates the filter responsible for the runtime context lifecycle of a Servlet dispatch.
     *
     * <p>
     * The filter obtains or creates the dispatch state, installs it before invoking the filter chain, captures the
     * final state for a later dispatch, and restores the worker thread state when processing finishes.
     *
     * @param contextBuilder runtime context facade used to create, install, capture, and restore dispatch state
     * @param requestContext request accessor used to resolve credentials once per request
     * @return a filter that manages runtime context state around the Servlet filter chain
     */
    @Bean
    @ConditionalOnMissingBean(ContextBindingFilter.class)
    @ConditionalOnProperty(prefix = GeniusBuilder.CONTEXT, name = "web.enabled", havingValue = "true", matchIfMissing = true)
    ContextBindingFilter contextBindingFilter(ContextBuilder contextBuilder, RequestContext requestContext) {
        return new ContextBindingFilter(contextBuilder, requestContext);
    }

    /**
     * Registers the context binding filter with the Servlet container.
     *
     * <p>
     * The registration enables asynchronous processing, applies the filter to {@link DispatcherType#REQUEST},
     * {@link DispatcherType#ASYNC}, and {@link DispatcherType#ERROR}, and assigns the order
     * {@code Ordered.HIGHEST_PRECEDENCE + 10}.
     *
     * @param contextBindingFilter filter that manages context state for every supported dispatch
     * @return the fully configured Servlet filter registration
     */
    @Bean("registrationContextFilter")
    @ConditionalOnMissingBean(name = "registrationContextFilter")
    @ConditionalOnProperty(prefix = GeniusBuilder.CONTEXT, name = "web.enabled", havingValue = "true", matchIfMissing = true)
    FilterRegistrationBean<ContextBindingFilter> registrationContextFilter(ContextBindingFilter contextBindingFilter) {
        FilterRegistrationBean<ContextBindingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(contextBindingFilter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registration.setAsyncSupported(true);
        registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR));
        return registration;
    }

}
