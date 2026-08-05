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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.spring.web.wrapper.CachedBodyRequestWrapper;

/**
 * Resolves unclaimed application request objects from one deterministic request source.
 * <p>
 * Native Spring binding annotations always take precedence. Unannotated complex application types are resolved from a
 * JSON body when the request content type is JSON, otherwise from query, form, and multipart values.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RequestObjectArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * Request attribute used to share one bounded body across multiple controller parameters.
     */
    private static final String BODY_ATTRIBUTE = RequestObjectArgumentResolver.class.getName() + ".BODY";
    /**
     * Matcher that determines which parameters support automatic binding.
     */
    private final AutoBindingTypeMatcher matcher;
    /**
     * Options controlling request-object binding behavior.
     */
    private final RequestBindingOptions options;

    /**
     * Creates the unified request-object resolver.
     *
     * @param matcher matcher for automatically bindable parameters
     * @param options request-object binding options
     */
    public RequestObjectArgumentResolver(AutoBindingTypeMatcher matcher, RequestBindingOptions options) {
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
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            throw new IllegalStateException("No HttpServletRequest available");
        }
        if (isJsonBodyRequest(request)) {
            Object target = resolveJson(parameter, request);
            validate(parameter, target, webRequest, binderFactory);
            return target;
        }

        Object target = BeanKit.createBean(type);
        String name = parameter.getParameterName() == null ? type.getSimpleName() : parameter.getParameterName();
        WebDataBinder binder = binderFactory.createBinder(webRequest, target, name);
        binder.setAutoGrowCollectionLimit(this.options.getAutoGrowCollectionLimit());
        binder.setIgnoreUnknownFields(this.options.isIgnoreUnknownFields());
        binder.setBindEmptyMultipartFiles(this.options.isBindEmptyMultipartFiles());

        MutablePropertyValues values = new MutablePropertyValues(request.getParameterMap());
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
     * Resolves one JSON request object without mixing query or form values into it.
     *
     * @param parameter target controller parameter
     * @param request   current HTTP request
     * @return deserialized request object
     * @throws IOException when the bounded request body cannot be read
     */
    private Object resolveJson(MethodParameter parameter, HttpServletRequest request) throws IOException {
        byte[] body = readBody(request);
        if (body.length == 0) {
            return BeanKit.createBean(parameter.getParameterType());
        }
        try {
            return JsonKit.toPojo(new String(body, StandardCharsets.UTF_8), parameter.getParameterType());
        } catch (RuntimeException failure) {
            throw new HttpMessageNotReadableException("Invalid JSON request body", failure,
                    new ServletServerHttpRequest(request));
        }
    }

    /**
     * Reads a bounded body from an existing cache wrapper or directly from the owned request stream.
     *
     * @param request current HTTP request
     * @return request body bytes
     * @throws IOException when reading fails or the configured boundary is exceeded
     */
    private byte[] readBody(HttpServletRequest request) throws IOException {
        Object cached = request.getAttribute(BODY_ATTRIBUTE);
        if (cached instanceof byte[] body) {
            return body;
        }
        int maximum = this.options.getMaxRequestBodySize();
        byte[] body = request instanceof CachedBodyRequestWrapper wrapper ? wrapper.getBody()
                : request.getInputStream().readNBytes(maximum + 1);
        if (body != null && body.length > maximum) {
            throw new IOException("JSON request body exceeds the maximum size of " + maximum + " bytes");
        }
        byte[] resolved = body == null ? Normal.EMPTY_BYTE_ARRAY : body;
        request.setAttribute(BODY_ATTRIBUTE, resolved);
        return resolved;
    }

    /**
     * Returns whether this request carries the single JSON source owned by this resolver.
     *
     * @param request current HTTP request
     * @return {@code true} for JSON POST, PUT, or PATCH requests
     */
    private static boolean isJsonBodyRequest(HttpServletRequest request) {
        if (!org.miaixz.bus.core.net.MediaType.isJson(request.getContentType())) {
            return false;
        }
        String method = request.getMethod();
        return Http.Method.POST.value().equals(method) || Http.Method.PUT.value().equals(method)
                || Http.Method.PATCH.value().equals(method);
    }

    /**
     * Applies Bean Validation to an object created directly from a JSON body.
     */
    private static void validate(
            MethodParameter parameter,
            Object target,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) throws Exception {
        if (!requiresValidation(parameter)) {
            return;
        }
        String name = parameter.getParameterName() == null ? parameter.getParameterType().getSimpleName()
                : parameter.getParameterName();
        WebDataBinder binder = binderFactory.createBinder(webRequest, target, name);
        binder.validate();
        if (binder.getBindingResult().hasErrors()) {
            throw new MethodArgumentNotValidException(parameter, binder.getBindingResult());
        }
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
