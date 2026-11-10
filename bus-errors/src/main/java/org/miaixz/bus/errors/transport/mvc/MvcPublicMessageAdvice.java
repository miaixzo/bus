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
package org.miaixz.bus.errors.transport.mvc;

import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.errors.transport.http.PublicHttpError;
import org.miaixz.bus.errors.transport.servlet.ServletErrorFilter;

/**
 * Applies the public error catalog to legacy MVC business responses, including HTTP 200 failures.
 *
 * @author Kimi Liu
 */
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public final class MvcPublicMessageAdvice implements ResponseBodyAdvice<Object> {

    /**
     * Creates the stateless legacy response adapter.
     */
    public MvcPublicMessageAdvice() {
    }

    /**
     * Allows inspection of the actual body even when declared as an asynchronous or generic result.
     *
     * @param returnType    declared handler return type
     * @param converterType selected Spring converter
     * @return whether the advice inspects the body
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /**
     * Preserves successful envelopes and rebuilds error envelopes from their public code and approved data.
     *
     * @param body          body selected by MVC
     * @param returnType    declared handler return type
     * @param contentType   negotiated media type
     * @param converterType selected Spring converter
     * @param request       HTTP request
     * @param response      HTTP response
     * @return original success or a public error envelope
     */
    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType contentType,
            Class<? extends HttpMessageConverter<?>> converterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        if (!(body instanceof Message<?> message) || successful(message.getCode())) {
            return body;
        }
        var error = PublicHttpError.resolve(message.getCode(), ErrorCode._FAILURE);
        if (response instanceof ServletServerHttpResponse servlet) {
            var output = servlet.getServletResponse();
            if (ServletErrorFilter.isApproved(output)) {
                return Message.builder().code(error.code()).desc(error.description()).data(message.getData()).build();
            }
            if (!output.isCommitted()) {
                int status = output.getStatus();
                output.reset();
                output.setStatus(status);
                response.getHeaders().setContentType(contentType);
            }
        }
        return MvcErrorAdvice.handleBusiness(
                "mvc",
                null,
                error,
                safe -> Message.builder().code(safe.code()).desc(safe.description()).build());
    }

    /**
     * Identifies the standard success code and HTTP success codes used by legacy MVC callers.
     *
     * @param code current message code
     * @return whether the message is a success envelope
     */
    private static boolean successful(String code) {
        return ErrorCode._SUCCESS.getKey().equals(code) || (code != null && code.matches("2[0-9]{2}"));
    }

}
