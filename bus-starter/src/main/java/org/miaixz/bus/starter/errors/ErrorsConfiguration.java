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
package org.miaixz.bus.starter.errors;

import java.util.EnumSet;
import java.util.List;

import jakarta.servlet.DispatcherType;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import org.miaixz.bus.core.lang.annotation.Nullable;
import org.miaixz.bus.errors.ErrorBoundary;
import org.miaixz.bus.errors.ErrorObserver;
import org.miaixz.bus.errors.transport.http.HttpErrorPolicy;
import org.miaixz.bus.errors.transport.mvc.MvcErrorAdvice;
import org.miaixz.bus.errors.transport.mvc.MvcPublicMessageAdvice;
import org.miaixz.bus.errors.transport.servlet.ServletErrorFilter;
import org.miaixz.bus.spring.web.routing.RouteErrorPolicy;
import org.miaixz.bus.spring.web.routing.RouteErrorResolver;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Registers Bus error properties, the standard log subscription and the Servlet MVC error adapters.
 *
 * @author Kimi Liu
 */
@AutoConfiguration
@EnableConfigurationProperties(ErrorsProperties.class)
public class ErrorsConfiguration {

    /**
     * Creates the shared Bus error auto-configuration.
     */
    public ErrorsConfiguration() {
    }

    /**
     * Installs standard Bus error logging and releases this context's subscription on shutdown.
     *
     * @return subscription owned by the application context
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(prefix = GeniusBuilder.ERRORS_LOGGING, name = "enabled", matchIfMissing = true)
    @ConditionalOnMissingBean(name = "busErrorLogging")
    ErrorBoundary.Subscription busErrorLogging() {
        return ErrorObserver.install();
    }

    /**
     * Compiles the shared native HTTP rules once for a selected listener.
     *
     * @param properties bound Bus error settings
     * @param target     listener requesting its policy
     * @return compiled policy, or null when this listener uses its protocol or legacy error format
     */
    @Nullable
    public static HttpErrorPolicy policy(ErrorsProperties properties, ErrorsProperties.Target target) {
        var routing = properties.routing();
        return routing.enabled() && routing.targets().contains(target)
                ? HttpErrorPolicy.forStatuses(routing.pathPrefixes(), routing.statuses(), routing.cacheControl())
                : null;
    }

    /**
     * Isolates the Servlet resolver from applications without Spring MVC.
     *
     * @author Kimi Liu
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
    static class Mvc implements WebMvcConfigurer {

        /**
         * Creates the Servlet-only configuration.
         */
        Mvc() {
        }

        /**
         * Installs the Servlet guard before application filters and across container error dispatch.
         *
         * @return dispatch-aware filter registration
         */
        @Bean
        FilterRegistrationBean<ServletErrorFilter> busServletErrors() {
            var registration = new FilterRegistrationBean<>(new ServletErrorFilter());
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
            registration.setAsyncSupported(true);
            registration
                    .setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR));
            return registration;
        }

        /**
         * Installs public validation for the existing MVC business envelope.
         *
         * @return advice for legacy Message errors
         */
        @Bean
        MvcPublicMessageAdvice busPublicMessageAdvice() {
            return new MvcPublicMessageAdvice();
        }

        /**
         * Installs the safe fallback after specific controller and advice handlers and before Spring default writers.
         *
         * @param resolvers MVC's ordered exception resolvers
         */
        @Override
        public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
            for (int index = 0; index < resolvers.size(); index++) {
                if (resolvers.get(index) instanceof ExceptionHandlerExceptionResolver) {
                    resolvers.add(index + 1, MvcErrorAdvice::resolveFallback);
                    return;
                }
            }
            resolvers.add(MvcErrorAdvice::resolveFallback);
        }

        /**
         * Registers the highest-priority Bus route resolver unless the application supplies its own. The Servlet guard
         * and public legacy response advice also run when this listener has no native routing policy.
         *
         * @param properties bound Bus error settings
         * @return MVC resolver with the policy selected at startup
         */
        @Bean
        @ConditionalOnMissingBean(RouteErrorResolver.class)
        RouteErrorResolver routeErrorResolver(ErrorsProperties properties) {
            var policy = ErrorsConfiguration.policy(properties, ErrorsProperties.Target.MVC);
            return new RouteErrorResolver(policy == null ? null : RouteErrorPolicy.fromCompiled(policy));
        }
    }
}
