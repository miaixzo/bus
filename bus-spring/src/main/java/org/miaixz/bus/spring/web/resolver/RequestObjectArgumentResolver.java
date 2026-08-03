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

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.MethodParameter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.spring.web.RequestContext;

/**
 * Resolves only explicit RequestObject form, query and multipart parameters.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RequestObjectArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * Accessor for parameters and body data of the active request.
     */
    private final RequestContext requestContext;
    /**
     * Matcher that determines which parameters support automatic binding.
     */
    private final AutoBindingTypeMatcher matcher;
    /**
     * Options controlling request-object binding behavior.
     */
    private final RequestBindingOptions options;

    /**
     * Creates the explicit request-object resolver.
     *
     * @param requestContext accessor for active request data
     * @param matcher        matcher for automatically bindable parameters
     * @param options        request-object binding options
     */
    public RequestObjectArgumentResolver(RequestContext requestContext, AutoBindingTypeMatcher matcher,
            RequestBindingOptions options) {
        this.requestContext = Objects.requireNonNull(requestContext, "requestContext");
        this.matcher = Objects.requireNonNull(matcher, "matcher");
        this.options = Objects.requireNonNull(options, "options");
    }

    /**
     * Tests whether a controller parameter is eligible for request-object binding.
     *
     * @param parameter controller method parameter
     * @return {@code true} when this resolver owns the parameter
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return this.matcher.matches(parameter);
    }

    /**
     * Creates and binds the supported controller argument from trusted request sources.
     *
     * @param parameter     controller method parameter
     * @param container     current model and view container
     * @param webRequest    current web request
     * @param binderFactory binder factory used for validation and conversion
     * @return resolved controller argument
     * @throws Exception when construction, binding, or validation fails
     */
    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer container,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) throws Exception {
        Class<?> type = parameter.getParameterType();
        Object target = BeanKit.createBean(type);
        String name = parameter.getParameterName() == null ? type.getSimpleName() : parameter.getParameterName();
        WebDataBinder binder = binderFactory.createBinder(webRequest, target, name);
        binder.setAutoGrowCollectionLimit(this.options.getAutoGrowCollectionLimit());
        binder.setIgnoreUnknownFields(this.options.isIgnoreUnknownFields());
        binder.setBindEmptyMultipartFiles(this.options.isBindEmptyMultipartFiles());

        MutablePropertyValues values = new MutablePropertyValues();
        this.requestContext.getParameters().forEach(values::add);
        addMultipartValues(values, webRequest.getNativeRequest(MultipartHttpServletRequest.class));
        try {
            binder.bind(values);
        } catch (RuntimeException failure) {
            binder.getBindingResult().reject("request.binding", "Invalid request object");
            throw new MethodArgumentNotValidException(parameter, binder.getBindingResult());
        }
        if (requiresValidation(parameter)) {
            binder.validate();
        }
        if (binder.getBindingResult().hasErrors()) {
            throw new MethodArgumentNotValidException(parameter, binder.getBindingResult());
        }
        return target;
    }

    /**
     * Adds multipart fields and files to the binding value map.
     *
     * @param values           destination binding value map
     * @param multipartRequest source multipart request
     */
    private static void addMultipartValues(MutablePropertyValues values, MultipartHttpServletRequest multipartRequest) {
        if (multipartRequest == null) {
            return;
        }
        for (Map.Entry<String, List<MultipartFile>> entry : multipartRequest.getMultiFileMap().entrySet()) {
            List<MultipartFile> files = entry.getValue();
            if (files.size() == 1) {
                values.add(entry.getKey(), files.get(0));
            } else if (!files.isEmpty()) {
                values.add(entry.getKey(), List.copyOf(files));
            }
        }
    }

    /**
     * Determines whether the resolved argument requires Bean Validation.
     *
     * @param parameter controller method parameter to inspect
     * @return {@code true} when validation annotations are present
     */
    private static boolean requiresValidation(MethodParameter parameter) {
        for (Annotation annotation : parameter.getParameterAnnotations()) {
            Class<? extends Annotation> type = annotation.annotationType();
            if (type == Validated.class || "Valid".equals(type.getSimpleName())) {
                return true;
            }
        }
        return false;
    }

}
