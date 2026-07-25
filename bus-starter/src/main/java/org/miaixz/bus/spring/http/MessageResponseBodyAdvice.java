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

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.basic.normal.ErrorCode;

/**
 * Normalizes {@link Message} response bodies before serialization.
 * <p>
 * Failed messages are converted to a structure without {@code data}, even when callers manually populated that field.
 * This keeps the rule framework-level without adding serialization annotations or JSON dependencies to bus-core.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ControllerAdvice
@RestControllerAdvice
@ConditionalOnWebApplication
public class MessageResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    /**
     * Applies this advice to all response bodies and only rewrites {@link Message} instances.
     *
     * @param returnType    the controller return type
     * @param converterType the selected converter type
     * @return always {@code true}
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /**
     * Removes {@code data} from non-success {@link Message} responses.
     *
     * @param body                  the response body
     * @param returnType            the controller return type
     * @param selectedContentType   the selected content type
     * @param selectedConverterType the selected converter type
     * @param request               the current request
     * @param response              the current response
     * @return the original body for success and non-Message responses, otherwise a failure response map
     */
    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        if (!(body instanceof Message<?> message) || isSuccess(message)) {
            return body;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(Consts.ERRCODE, message.getErrcode());
        result.put(Consts.ERRMSG, message.getErrmsg());
        return result;
    }

    /**
     * Checks whether the message is successful.
     *
     * @param message the response message
     * @return {@code true} for successful messages
     */
    private boolean isSuccess(Message<?> message) {
        return message != null && ErrorCode._SUCCESS.getKey().equals(message.getErrcode());
    }

}
