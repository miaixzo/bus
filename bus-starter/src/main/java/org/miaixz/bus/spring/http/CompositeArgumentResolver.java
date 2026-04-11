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
package org.miaixz.bus.spring.http;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.UrlKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.spring.options.WrapperRuntimeOptions;
import org.miaixz.bus.validate.magic.annotation.Valid;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;

/**
 * An automatic parameter resolver that handles parameter binding for various request formats.
 * <p>
 * This resolver unifies the handling of parameters from:
 * <ul>
 * <li>JSON requests ({@code application/json})</li>
 * <li>Form data ({@code application/x-www-form-urlencoded})</li>
 * <li>File uploads ({@code multipart/form-data})</li>
 * </ul>
 * It automatically binds request parameters to controller method parameter objects using reflection.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CompositeArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * Runtime wrapper compatibility snapshot used to decide resolver behavior.
     * <p>
     * This snapshot controls whether legacy non-simple argument resolution remains enabled and whether synthesized form
     * body content should continue to participate in parameter binding.
     */
    private final WrapperRuntimeOptions options;

    /**
     * Creates a resolver using the current shared {@link WrapperRuntimeOptions} snapshot.
     * <p>
     * This constructor is suitable for the default framework wiring path where all wrapper-related components share the
     * same runtime compatibility snapshot.
     */
    public CompositeArgumentResolver() {
        this(WrapperRuntimeOptions.of());
    }

    /**
     * Creates a resolver with an explicit runtime compatibility snapshot.
     *
     * @param options The runtime compatibility options. If {@code null}, the current shared snapshot is used.
     */
    public CompositeArgumentResolver(WrapperRuntimeOptions options) {
        this.options = options == null ? WrapperRuntimeOptions.of() : options;
    }

    /**
     * Determines whether this resolver supports the given method parameter.
     * <p>
     * The decision follows two paths:
     * <ul>
     * <li>Parameters explicitly annotated with {@link ModelAttribute} are always supported.</li>
     * <li>Otherwise, support depends on the runtime option {@code resolveNonSimpleArguments} and whether the parameter
     * type is considered a simple type by {@link #isSimpleType(Class)}.</li>
     * </ul>
     * This preserves the legacy "auto bind non-simple arguments" behavior while still allowing stricter operation
     * through runtime configuration.
     *
     * @param parameter Method parameter information.
     * @return {@code true} if the parameter is not a simple type (primitive, String, Number, etc.) or is annotated with
     *         {@link ModelAttribute}; {@code false} otherwise.
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (parameter.hasParameterAnnotation(ModelAttribute.class)) {
            return true;
        }
        return this.options.isResolveNonSimpleArguments() && !isSimpleType(parameter.getParameterType());
    }

    /**
     * Resolves the method argument by binding request parameters to the target object.
     * <p>
     * Resolution behavior depends on request content type and runtime compatibility settings:
     * <ul>
     * <li>For wrapped requests, cached body content is preferred.</li>
     * <li>For JSON requests, the body is deserialized directly into the target type.</li>
     * <li>For form requests, binding is driven by {@code parameterMap}, optionally supplemented by synthesized form
     * body content when the runtime option {@code synthesizeFormBody} is enabled.</li>
     * <li>For multipart requests, uploaded files are merged into binding values.</li>
     * </ul>
     * The custom {@code @Valid} semantics used by the framework remain unchanged and are applied after binding values
     * have been collected.
     *
     * @param methodParameter Method parameter information.
     * @param mavContainer    Model and view container.
     * @param webRequest      Native Web request.
     * @param binderFactory   Data binder factory.
     * @return The target object with bound parameters.
     * @throws Exception If an exception occurs during resolution.
     */
    @Override
    public Object resolveArgument(
            MethodParameter methodParameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            throw new IllegalStateException("No HttpServletRequest available");
        }

        byte[] body;
        String contentType = request.getContentType();
        // If it's a MutableRequestWrapper, prioritize its cached body
        if (request instanceof MutableRequestWrapper wrapper) {
            request = wrapper.getRequest();
            body = wrapper.getBody();
            contentType = wrapper.getContentType();
        } else {
            // Read input stream
            body = IoKit.readBytes(request.getInputStream());
            if ((body == null || body.length == 0) && this.options.isSynthesizeFormBody()) {
                if (MapKit.isNotEmpty(request.getParameterMap())) {
                    String paramString = request.getParameterMap().entrySet().stream()
                            .map(entry -> entry.getKey() + Symbol.EQUAL + String.join(Symbol.COMMA, entry.getValue()))
                            .collect(Collectors.joining(Symbol.AND));
                    body = paramString.getBytes(Charset.UTF_8);
                    contentType = MediaType.APPLICATION_FORM_URLENCODED;
                } else {
                    body = new byte[0];
                }
            } else if (body == null) {
                body = new byte[0];
            }
        }

        // Create target object instance
        Class<?> parameterType = methodParameter.getParameterType();
        Object target = parameterType.getDeclaredConstructor().newInstance();
        WebDataBinder binder = binderFactory.createBinder(webRequest, target, methodParameter.getParameterName());

        // 先合并所有来源的参数，再统一清理 null-like 值，避免后续追加的表单参数把 "null" 再带回来。
        MutablePropertyValues mpvs = new MutablePropertyValues(request.getParameterMap());

        // Handle application/x-www-form-urlencoded requests
        if (this.options.isSynthesizeFormBody() && contentType != null
                && contentType.startsWith(MediaType.APPLICATION_FORM_URLENCODED)) {
            String bodyString = new String(body, Charset.UTF_8);
            if (!StringKit.isEmpty(bodyString)) {
                mpvs.addPropertyValues(UrlKit.decodeQuery(bodyString, Charset.UTF_8));
            }
        }

        // Handle JSON requests
        if (contentType != null && contentType.startsWith(MediaType.APPLICATION_JSON)) {
            String bodyString = new String(body, Charset.UTF_8);
            if (!bodyString.isEmpty() && JsonKit.isJson(bodyString)) {
                return JsonKit.toPojo(bodyString, parameterType);
            }
        }

        // Handle multipart/form-data requests (file uploads)
        if (request instanceof MultipartHttpServletRequest multipartRequest) {
            multipartRequest.getMultiFileMap().forEach((key, files) -> {
                if (files.size() == 1) {
                    mpvs.add(key, files.get(0)); // Single file bound to MultipartFile
                } else {
                    mpvs.add(key, files); // Multiple files bound to List<MultipartFile> or MultipartFile[]
                }
            });
        }

        sanitizePropertyValues(mpvs);

        if (mpvs.isEmpty() && !methodParameter.hasParameterAnnotation(ModelAttribute.class)
                && methodParameter.hasParameterAnnotation(Valid.class)) {
            throw new ValidateException(ErrorCode._100100);
        }

        binder.bind(mpvs);
        if (binder.getBindingResult().hasErrors()) {
            throw new IllegalArgumentException("Binding errors: " + binder.getBindingResult().getAllErrors());
        }
        return target;
    }

    /**
     * Determines if a given type is a simple type.
     * <p>
     * This helper is used as the boundary for the legacy "resolve all non-simple arguments" behavior. When the runtime
     * option {@code resolveNonSimpleArguments} is enabled, types outside this set are considered eligible for automatic
     * binding by this resolver.
     * <p>
     * Simple types include:
     * <ul>
     * <li>Primitive types</li>
     * <li>{@code String}</li>
     * <li>{@code Number} and its subclasses</li>
     * <li>{@code Boolean}</li>
     * <li>{@code Character}</li>
     * <li>{@code MultipartFile}</li>
     * </ul>
     *
     * @param type The type to check.
     * @return {@code true} if it is a simple type, {@code false} otherwise.
     */
    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() || type == String.class || Number.class.isAssignableFrom(type)
                || Boolean.class.isAssignableFrom(type) || Character.class.isAssignableFrom(type)
                || MultipartFile.class.isAssignableFrom(type);
    }

    /**
     * Removes null-like values from binding inputs before invoking the data binder.
     * <p>
     * This keeps compatibility with requests that may submit placeholder strings such as {@code null} or
     * {@code undefined}. For scalar string values, the whole property is removed. For string arrays, only valid items
     * are retained; if none remain, the property is removed entirely.
     *
     * @param mpvs Mutable binding values collected from query parameters, synthesized form body content, and multipart
     *             inputs.
     */
    private void sanitizePropertyValues(MutablePropertyValues mpvs) {
        List<String> removeKeys = new ArrayList<>();
        mpvs.getPropertyValueList().forEach(propertyValue -> {
            Object value = propertyValue.getValue();
            if (value instanceof String stringValue) {
                if (isNullLike(stringValue)) {
                    removeKeys.add(propertyValue.getName());
                }
                return;
            }
            if (value instanceof String[] values) {
                List<String> validValues = new ArrayList<>();
                for (String item : values) {
                    if (!isNullLike(item)) {
                        validValues.add(item);
                    }
                }
                if (validValues.isEmpty()) {
                    removeKeys.add(propertyValue.getName());
                } else if (validValues.size() != values.length) {
                    mpvs.add(propertyValue.getName(), validValues.toArray(String[]::new));
                }
            }
        });
        removeKeys.forEach(mpvs::removePropertyValue);
    }

    /**
     * Determines whether a string should be treated as an empty binding value.
     * <p>
     * The resolver treats blank strings and common frontend placeholders such as {@code null} and {@code undefined} as
     * absent values so they do not participate in object binding.
     *
     * @param value The raw string value submitted by the request.
     * @return {@code true} if the value should be treated as absent, {@code false} otherwise.
     */
    private boolean isNullLike(String value) {
        return StringKit.isBlank(value) || "null".equalsIgnoreCase(value) || "undefined".equalsIgnoreCase(value);
    }

}
