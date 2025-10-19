/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.spring.http;

import java.util.stream.Collectors;

import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.UrlKit;
import org.miaixz.bus.extra.json.JsonKit;
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
 * @since Java 17+
 */
public class CompositeArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * Determines whether this resolver supports the given method parameter.
     *
     * @param parameter Method parameter information.
     * @return {@code true} if the parameter is not a simple type (primitive, String, Number, etc.) or is annotated with
     *         {@link ModelAttribute}; {@code false} otherwise.
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(ModelAttribute.class) || !isSimpleType(parameter.getParameterType());
    }

    /**
     * Resolves the method argument by binding request parameters to the target object.
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
            if (body == null || body.length == 0) {
                // If the input stream is empty and there are parameters, use parameterMap
                if (MapKit.isNotEmpty(request.getParameterMap())) {
                    String paramString = request.getParameterMap().entrySet().stream()
                            .map(entry -> entry.getKey() + Symbol.EQUAL + String.join(Symbol.COMMA, entry.getValue()))
                            .collect(Collectors.joining(Symbol.AND));
                    body = paramString.getBytes(Charset.UTF_8);
                    contentType = MediaType.APPLICATION_FORM_URLENCODED;
                } else {
                    body = new byte[0];
                }
            }
        }

        // Create target object instance
        Class<?> parameterType = methodParameter.getParameterType();
        Object target = parameterType.getDeclaredConstructor().newInstance();
        WebDataBinder binder = binderFactory.createBinder(webRequest, target, methodParameter.getParameterName());

        // Add query parameters
        MutablePropertyValues mpvs = new MutablePropertyValues(request.getParameterMap());

        // Handle application/x-www-form-urlencoded requests
        if (contentType != null && contentType.startsWith(MediaType.APPLICATION_FORM_URLENCODED)) {
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

        if (mpvs.isEmpty() && !methodParameter.hasParameterAnnotation(ModelAttribute.class)
                && methodParameter.hasParameterAnnotation(Valid.class)) {
            throw new ValidateException(ErrorCode._100116);
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

}
