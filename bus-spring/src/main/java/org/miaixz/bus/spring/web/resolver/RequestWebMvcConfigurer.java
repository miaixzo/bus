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
package org.miaixz.bus.spring.web.resolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;

import org.miaixz.bus.core.basic.entity.Authorize;

/**
 * Registers request-object binding and preserves native MVC model-attribute binding with context identity.
 *
 * @author Kimi Liu
 */
public class RequestWebMvcConfigurer implements WebMvcConfigurer {

    /**
     * Request-binding options shared with the argument resolver.
     */
    private final RequestBindingOptions options;

    /**
     * Argument resolver registered with Spring MVC.
     */
    private final RequestObjectArgumentResolver resolver;

    /**
     * Creates the MVC configurer sharing one identity binder across automatic and native request-object binding.
     *
     * @param options  request-binding options shared with Spring MVC
     * @param resolver request-object argument resolver to register
     */
    public RequestWebMvcConfigurer(RequestBindingOptions options, RequestObjectArgumentResolver resolver) {
        this.options = Objects.requireNonNull(options, "options");
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    /**
     * Registers the request-object resolver when request binding is enabled.
     *
     * @param resolvers mutable MVC argument resolver list
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        Objects.requireNonNull(this.options, "options");
        if (resolvers.stream().noneMatch(RequestObjectArgumentResolver.class::isInstance)) {
            resolvers.add(this.resolver);
        }
    }

    /**
     * Installs native identity-bearing model-attribute binding without eagerly initializing the shared binder.
     *
     * @author Kimi Liu
     */
    public static final class ModelAttributeResolver implements HandlerMethodArgumentResolver, BeanPostProcessor {

        /**
         * Lazy shared binder lookup that preserves the normal bean post-processing and proxy lifecycle.
         */
        private final ObjectProvider<RequestObjectArgumentResolver> resolver;

        /**
         * Creates the narrow native model-attribute resolver.
         *
         * @param resolver lazy lookup of the shared request-object identity binder
         */
        public ModelAttributeResolver(ObjectProvider<RequestObjectArgumentResolver> resolver) {
            this.resolver = Objects.requireNonNull(resolver, "resolver");
        }

        /**
         * Adds identity-aware model-attribute binding before Spring's native model-attribute processor.
         * <p>
         * Native processors retain ownership of unrelated parameters. Installing the narrow resolver after adapter
         * initialization preserves the native resolver order and avoids bypassing explicit Spring binding annotations.
         *
         * @param bean     initialized application bean
         * @param beanName application bean name
         * @return the unchanged bean with model-attribute identity binding installed when applicable
         */
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            if (bean instanceof RequestMappingHandlerAdapter adapter && adapter.getArgumentResolvers() != null) {
                List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>(adapter.getArgumentResolvers());
                if (resolvers.stream().noneMatch(ModelAttributeResolver.class::isInstance)) {
                    for (int index = 0; index < resolvers.size(); index++) {
                        if (resolvers.get(index).getClass() == ServletModelAttributeMethodProcessor.class) {
                            resolvers.add(index, this);
                            adapter.setArgumentResolvers(resolvers);
                            break;
                        }
                    }
                }
            }
            return bean;
        }

        /**
         * Selects only explicitly annotated model attributes that inherit the identity contract.
         *
         * @param parameter controller method parameter
         * @return whether this resolver owns native identity-bearing model-attribute binding
         */
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(ModelAttribute.class)
                    && Authorize.class.isAssignableFrom(parameter.getParameterType());
        }

        /**
         * Preserves native construction, editors, multipart conversion, validation, and binding-result handling.
         *
         * @param parameter     controller method parameter
         * @param container     current model and view container
         * @param webRequest    current request
         * @param binderFactory native binder factory retaining existing controller initialization
         * @return the native model attribute with context identity applied exactly once
         * @throws Exception when native construction, binding, or validation fails
         */
        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer container,
                NativeWebRequest webRequest,
                WebDataBinderFactory binderFactory) throws Exception {
            RequestObjectArgumentResolver binder = this.resolver.getObject();
            ModelAttributeProcessor processor = new ModelAttributeProcessor(binder);
            Object target = processor.resolveArgument(parameter, container, webRequest, binderFactory);
            if (!processor.bound) {
                binder.bindAuthorize(target);
            }
            return target;
        }

    }

    /**
     * Performs one native model-attribute resolution with identity applied before annotated validation.
     *
     * @author Kimi Liu
     */
    private static final class ModelAttributeProcessor extends ServletModelAttributeMethodProcessor {

        /**
         * Shared context identity binder; no request identity is retained by this processor.
         */
        private final RequestObjectArgumentResolver resolver;

        /**
         * Whether the current attribute has already received its context identity during native binding.
         */
        private boolean bound;

        /**
         * Creates a processor confined to one native argument resolution.
         *
         * @param resolver shared request-object identity binder
         */
        private ModelAttributeProcessor(RequestObjectArgumentResolver resolver) {
            super(false);
            this.resolver = resolver;
        }

        /**
         * Applies identity once a constructed target is available, including binding-disabled attributes.
         *
         * @param binder native binder containing the current model attribute
         */
        private void bindAuthorize(WebDataBinder binder) {
            if (!this.bound && binder.getTarget() instanceof Authorize) {
                this.resolver.bindAuthorize(binder.getTarget());
                this.bound = true;
            }
        }

        /**
         * Preserves native parameter and multipart binding, then replaces identity even when binding reports errors.
         *
         * @param binder  native binder for the model attribute
         * @param request current request
         */
        @Override
        protected void bindRequestParameters(WebDataBinder binder, NativeWebRequest request) {
            try {
                super.bindRequestParameters(binder, request);
            } finally {
                bindAuthorize(binder);
            }
        }

        /**
         * Applies context identity before native validation while retaining validation groups and error handling.
         *
         * @param binder    native binder for the model attribute
         * @param parameter controller parameter carrying the existing validation annotations
         */
        @Override
        protected void validateIfApplicable(WebDataBinder binder, MethodParameter parameter) {
            bindAuthorize(binder);
            super.validateIfApplicable(binder, parameter);
        }

    }

}
