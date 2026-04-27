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
package org.miaixz.bus.vortex.handler;

import java.net.UnknownHostException;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.UncheckedException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Formats;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;

import lombok.*;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

/**
 * Global exception handler that processes exceptions in Web applications and returns standardized JSON responses.
 * <p>
 * This handler implements {@link WebExceptionHandler} to catch various exceptions that occur during request processing.
 * It sets the HTTP status to OK (200) and the content type to JSON, then constructs a {@link Message} object based on
 * the exception type. The message is then serialized to JSON and written to the response body.
 * <p>
 * This version is enhanced to correctly identify wrapped exceptions by checking the root cause before determining the
 * error code.
 *
 * @author Justubborn
 * @since Java 21+
 */
public class ErrorsHandler implements WebExceptionHandler {

    /**
     * Creates a global errors handler.
     */
    public ErrorsHandler() {
    }

    /**
     * Handles exceptions, generating a standardized error response.
     * <p>
     * This method is invoked when an exception occurs during request processing. It sets the response status to
     * {@code HttpStatus.OK} and content type to {@code MediaType.APPLICATION_JSON}. It then builds an error message
     * based on the exception type and writes the serialized JSON message to the response body.
     * </p>
     *
     * @param exchange The current {@link ServerWebExchange} object, containing the request and response.
     * @param ex       The caught {@link Throwable} object.
     * @return {@link Mono<Void>} indicating the asynchronous completion of processing.
     */
    @NonNull
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        ServerHttpRequest request = exchange.getRequest();
        response.setStatusCode(HttpStatus.OK);

        Context context = exchange.getAttribute(Context.$);
        if (context != null) {
            Logger.info(false, "Errors", "Context format is: {}", context.getFormat());
        } else {
            Logger.info(false, "Errors", "Context is null, defaulting to JSON format.");
        }

        String path = request.getPath().value();
        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";

        Formats formats = (context != null) ? context.getFormat() : Formats.JSON;
        response.getHeaders().setContentType(formats.getMediaType());

        Mono<Message> messageMono = Mono.fromCallable(() -> buildErrorMessage(ex, method, path));

        Mono<DataBuffer> dataBufferMono = messageMono.flatMap(message -> formats.getProvider().serialize(message))
                .map(formatBody -> {
                    if (formatBody instanceof byte[]) {
                        return response.bufferFactory().wrap((byte[]) formatBody);
                    } else {
                        return response.bufferFactory().wrap(((String) formatBody).getBytes(Charset.UTF_8));
                    }
                });

        return response.writeWith(dataBufferMono).doOnTerminate(() -> {
            String exceptionName = ex.getClass().getSimpleName();
            String ip = "N/A";
            if (context != null) {
                ip = context.getX_request_ip() != null ? context.getX_request_ip() : "N/A";
                long executionTime = System.currentTimeMillis() - context.getTimestamp();
                Logger.error(
                        false,
                        "Errors",
                        "[{}] [{}] [{}] [ERROR_COMPLETION] - Error handled, execution time: {}ms, exception: {}",
                        ip,
                        method,
                        path,
                        executionTime,
                        exceptionName);
            } else {
                Logger.error(
                        false,
                        "Errors",
                        "[{}] [{}] [{}] [ERROR_COMPLETION] - Error handled, exception: {}",
                        ip,
                        method,
                        path,
                        exceptionName);
            }
        });
    }

    /**
     * Builds the error Message object based on the exception type and logs the specific error.
     *
     * @param ex     The caught exception
     * @param method The HTTP method
     * @param path   The request path
     * @return A populated Message object
     */
    private Message buildErrorMessage(Throwable ex, String method, String path) {

        if (ex instanceof UncheckedException) {
            UncheckedException ue = (UncheckedException) ex;
            String errcode = ue.getErrcode();
            String errmsg = ue.getErrmsg();
            if (StringKit.isNotBlank(errcode)) {
                Logger.error(
                        false,
                        "Errors",
                        "[N/A] [{}] [{}] [ERROR_UNCHECKED] - ErrorCode: {}, Message: {}",
                        method,
                        path,
                        errcode,
                        errmsg);
                return Message.builder().errcode(errcode).errmsg(errmsg).build();
            }
        } else if (ex instanceof WebClientException || ex instanceof WebClientRequestException) {
            if (ex.getCause() instanceof UnknownHostException) {
                Logger.error(
                        false,
                        "Errors",
                        "[N/A] [{}] [{}] [ERROR_WEBCLIENT] - UnknownHostException: {}",
                        method,
                        path,
                        ex.getCause().getMessage());
                return Message.builder().errcode(ErrorCode._100811.getKey()).errmsg(ErrorCode._100811.getValue())
                        .build();
            } else {
                Logger.error(
                        false,
                        "Errors",
                        "[N/A] [{}] [{}] [ERROR_WEBCLIENT] - WebClientException: {}",
                        method,
                        path,
                        ex.getMessage());
                return Message.builder().errcode(ErrorCode._116000.getKey()).errmsg(ErrorCode._116000.getValue())
                        .build();
            }
        }
        Logger.error(
                false,
                "Errors",
                "[N/A] [{}] [{}] [ERROR_UNKNOWN] - Unknown exception type: {}, Message: {}",
                method,
                path,
                ex.getClass().getName(),
                ex.getMessage());
        return Message.builder().errcode(ErrorCode._100807.getKey()).errmsg(ErrorCode._100807.getValue()).build();
    }

    /**
     * Represents a standardized message structure for API responses, typically used for error messages.
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class Message {

        /**
         * Creates an empty response message.
         */
        public Message() {
        }

        /**
         * The response code, indicating the specific error or status.
         */
        private String errcode;

        /**
         * The descriptive error message or status message.
         */
        private String errmsg;

    }

}
