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
package org.miaixz.bus.spring.web.advice;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Objects;

import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import org.miaixz.bus.core.basic.entity.Authorize;
import org.miaixz.bus.spring.web.resolver.RequestObjectArgumentResolver;

/**
 * Applies the current context identity to native Spring request bodies before validation.
 * <p>
 * Message conversion, required-body handling, and business validation remain owned by Spring MVC.
 *
 * @author Kimi Liu
 */
@ControllerAdvice
public class AuthorizeRequestBodyAdvice extends RequestBodyAdviceAdapter {

    /**
     * Shared request binder that projects the same context snapshot onto supported request objects.
     */
    private final RequestObjectArgumentResolver resolver;

    /**
     * Creates request-body identity binding using the existing request-object resolver.
     *
     * @param resolver shared request-object resolver
     */
    public AuthorizeRequestBodyAdvice(RequestObjectArgumentResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    /**
     * Selects identity-bearing objects and typed arrays or collections of identity-bearing objects.
     *
     * @param parameter     controller parameter being converted
     * @param targetType    resolved request body type
     * @param converterType message converter selected by Spring MVC
     * @return whether the body contains supported identity-bearing request objects
     */
    @Override
    public boolean supports(
            MethodParameter parameter,
            Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        ResolvableType type = ResolvableType.forType(targetType);
        if (type.isArray()) {
            type = type.getComponentType();
        } else if (Collection.class.isAssignableFrom(type.resolve(Object.class))) {
            type = type.as(Collection.class).getGeneric(0);
        }
        return Authorize.class.isAssignableFrom(type.resolve(Object.class));
    }

    /**
     * Binds identity after message conversion and before Spring performs annotated validation.
     *
     * @param body          converted request object or its direct collection elements
     * @param inputMessage  original HTTP input message
     * @param parameter     controller parameter receiving the body
     * @param targetType    resolved request body type
     * @param converterType selected message converter type
     * @return the same request body with context identity applied
     */
    @Override
    public Object afterBodyRead(
            Object body,
            HttpInputMessage inputMessage,
            MethodParameter parameter,
            Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        this.resolver.bindAuthorize(body);
        return body;
    }

}
