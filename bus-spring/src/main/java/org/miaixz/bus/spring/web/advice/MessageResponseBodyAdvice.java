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

import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import org.miaixz.bus.core.basic.entity.Message;

/**
 * Wraps ordinary response bodies in a {@link Message} while leaving transport-specific responses untouched.
 *
 * @author Kimi Liu
 */
public class MessageResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    /**
     * Initializes stateless response advice that wraps eligible controller results in a Bus message envelope.
     */
    public MessageResponseBodyAdvice() {
        // No initialization required.
    }

    /**
     * Excludes response types whose transport contract must remain untouched.
     *
     * @param returnType    the controller return type
     * @param converterType the selected converter type
     * @return {@code true} when the declared response type can be wrapped
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return !isExcludedType(returnType.getParameterType());
    }

    /**
     * Wraps a regular response body unless it is already wrapped or represents a streaming/download response.
     *
     * @param body                  the response body
     * @param returnType            the controller return type
     * @param selectedContentType   the selected content type
     * @param selectedConverterType the selected converter type
     * @param request               the current request
     * @param response              the current response
     * @return the original excluded body or a newly wrapped success message
     */
    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        if (body instanceof Message<?> || isExcludedType(body == null ? null : body.getClass())
                || isStreamingMediaType(selectedContentType) || isDownload(response)) {
            return body;
        }
        return Message.success(body);
    }

    /**
     * Checks whether a type represents an already wrapped, downloadable, or streaming response.
     *
     * @param type response type
     * @return {@code true} when response wrapping must be skipped
     */
    private boolean isExcludedType(Class<?> type) {
        return type != null && (Message.class.isAssignableFrom(type) || Resource.class.isAssignableFrom(type)
                || ResourceRegion.class.isAssignableFrom(type) || ResponseBodyEmitter.class.isAssignableFrom(type)
                || StreamingResponseBody.class.isAssignableFrom(type));
    }

    /**
     * Checks whether the negotiated media type is streaming or binary.
     *
     * @param mediaType negotiated media type
     * @return {@code true} when the body must remain untouched
     */
    private boolean isStreamingMediaType(MediaType mediaType) {
        return mediaType != null && (MediaType.TEXT_EVENT_STREAM.includes(mediaType)
                || MediaType.APPLICATION_OCTET_STREAM.includes(mediaType));
    }

    /**
     * Checks the response disposition without reading or logging its body.
     *
     * @param response current response
     * @return {@code true} for attachment responses
     */
    private boolean isDownload(ServerHttpResponse response) {
        String disposition = response.getHeaders().getFirst("Content-Disposition");
        return disposition != null && disposition.regionMatches(true, 0, "attachment", 0, "attachment".length());
    }

}
