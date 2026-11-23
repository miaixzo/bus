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
package org.miaixz.bus.spring.web.converter;

import java.io.IOException;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import org.miaixz.bus.core.basic.entity.Message;

/**
 * Writes explicit HTTP messages before converter selection and delegates ordinary responses to Spring MVC.
 *
 * @author Kimi Liu
 */
public final class MessageReturnValueHandler implements HandlerMethodReturnValueHandler {

    /**
     * Original response-body handler of the MVC adapter.
     */
    private final HandlerMethodReturnValueHandler delegate;

    /**
     * Creates a handler that preserves the adapter's existing response-body behavior.
     *
     * @param delegate original MVC response-body handler
     */
    public MessageReturnValueHandler(HandlerMethodReturnValueHandler delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    /**
     * Preserves the original handler's supported return types.
     *
     * @param returnType controller method return type
     * @return whether the original handler supports this return type
     */
    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return delegate.supportsReturnType(returnType);
    }

    /**
     * Writes explicit HTTP messages before converter selection and delegates other values to Spring MVC.
     *
     * @param value      controller return value
     * @param returnType controller method return type
     * @param container  current model and view container
     * @param request    current MVC request and response
     * @throws Exception if writing the response fails
     */
    @Override
    public void handleReturnValue(
            Object value,
            MethodParameter returnType,
            ModelAndViewContainer container,
            NativeWebRequest request) throws Exception {
        if (value instanceof Message<?> message && message.status() != null) {
            container.setRequestHandled(true);
            writeHttp(message, request);
        } else {
            delegate.handleReturnValue(value, returnType, container, request);
        }
    }

    /**
     * Writes status, headers, and body bytes to the current Servlet response.
     *
     * @param message message with explicit HTTP metadata
     * @param request current MVC request and response
     * @throws IOException if the connection cannot write the body
     */
    private static void writeHttp(Message<?> message, NativeWebRequest request) throws IOException {
        int status = message.status();
        Object data = message.getData();
        if (data != null && !(data instanceof byte[])) {
            throw new IllegalArgumentException("HTTP message data must contain bytes");
        }
        byte[] bytes = (byte[]) data;
        var response = Objects.requireNonNull(request.getNativeResponse(HttpServletResponse.class));
        var servletRequest = Objects.requireNonNull(request.getNativeRequest(HttpServletRequest.class));
        response.setStatus(status);
        message.headers().forEach((name, values) -> {
            if (!values.isEmpty() && !HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(name)) {
                response.setHeader(name, values.getFirst());
                values.stream().skip(1).forEach(value -> response.addHeader(name, value));
            }
        });
        if (status == 204 || status == 304) {
            return;
        }
        if (status == 205) {
            response.setContentLength(0);
            return;
        }
        response.setContentLength(bytes == null ? 0 : bytes.length);
        if (bytes != null && bytes.length > 0 && !"HEAD".equals(servletRequest.getMethod())) {
            response.getOutputStream().write(bytes);
        }
    }

}
