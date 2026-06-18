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

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.*;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.UncheckedException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Egress;
import org.miaixz.bus.vortex.Formats;
import org.miaixz.bus.vortex.magic.ErrorCode;

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
 * @author Kimi Liu
 * @since Java 21+
 */
public class ErrorsHandler implements WebExceptionHandler {

    /**
     * Creates a global errors handler.
     */
    public ErrorsHandler() {
        // No initialization required.
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
        Context context = exchange.getAttribute(Context.$);
        if (context != null) {
            Logger.info(false, "Vortex", "Context format is: {}", context.getFormat());
        } else {
            Logger.info(false, "Vortex", "Context is null, defaulting to JSON format.");
        }

        String path = request.getPath().value();
        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";

        Formats formats = (context != null) ? context.getFormat() : Formats.JSON;
        response.setStatusCode(HttpStatus.OK);

        AtomicInteger responseBytes = new AtomicInteger();
        Mono<DataBuffer> dataBufferMono = Mono.fromCallable(() -> buildErrorMessage(ex, method, path))
                .flatMap(message -> formats.getProvider().serialize(message)).map(formatBody -> {
                    byte[] bytes;
                    if (formatBody instanceof byte[] value) {
                        bytes = value;
                    } else {
                        bytes = String.valueOf(formatBody).getBytes(Charset.UTF_8);
                    }
                    responseBytes.set(bytes.length);
                    response.getHeaders().setContentType(formats.getMediaType());
                    response.getHeaders().setContentLength(bytes.length);
                    return response.bufferFactory().wrap(bytes);
                });

        if (response.isCommitted()) {
            Logger.error(
                    false,
                    "Vortex",
                    "Error response skipped because response is already committed: method={}, path={}, event=ERROR_RESPONSE_COMMITTED, exception={}",
                    method,
                    path,
                    ex.getClass().getSimpleName());
            return Mono.empty();
        }

        return response.writeWith(dataBufferMono).doOnTerminate(() -> {
            String exceptionName = ex.getClass().getSimpleName();
            String ip = "N/A";
            if (context != null) {
                ip = context.getX_request_ip() != null ? context.getX_request_ip() : "N/A";
                long executionTime = System.currentTimeMillis() - context.getTimestamp();
                Logger.error(
                        false,
                        "Vortex",
                        "Error response handled: clientIp={}, method={}, path={}, event=ERROR_COMPLETION, executionTimeMs={}, responseBytes={}, exception={}",
                        ip,
                        method,
                        path,
                        executionTime,
                        responseBytes.get(),
                        exceptionName);
            } else {
                Logger.error(
                        false,
                        "Vortex",
                        "Error response handled: clientIp={}, method={}, path={}, event=ERROR_COMPLETION, responseBytes={}, exception={}",
                        ip,
                        method,
                        path,
                        responseBytes.get(),
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

        OutOfMemoryError outOfMemoryError = findCause(ex, OutOfMemoryError.class);
        if (outOfMemoryError != null) {
            Logger.error(
                    false,
                    "Vortex",
                    outOfMemoryError,
                    "Out of memory while processing request: clientIp=N/A, method={}, path={}, event=ERROR_OUT_OF_MEMORY, exception={}",
                    method,
                    path,
                    outOfMemoryError.getClass().getSimpleName());
            return Message.builder().errcode(ErrorCode._100807.getKey()).errmsg(ErrorCode._100807.getValue()).build();
        }

        ResponseStatusException rse = findCause(ex, ResponseStatusException.class);
        if (rse != null) {
            Logger.error(
                    false,
                    "Vortex",
                    "HTTP status error returned: clientIp=N/A, method={}, path={}, event=ERROR_STATUS, status={}, message={}",
                    method,
                    path,
                    rse.getStatusCode().value(),
                    rse.getReason());
            return Message.builder().errcode(String.valueOf(rse.getStatusCode().value())).errmsg(rse.getReason())
                    .build();
        }

        UncheckedException ue = findCause(ex, UncheckedException.class);
        if (ue != null) {
            String errcode = ue.getErrcode();
            String errmsg = ue.getErrmsg();
            if (StringKit.isNotBlank(errcode)) {
                Logger.error(
                        false,
                        "Vortex",
                        "Unchecked error returned: clientIp=N/A, method={}, path={}, event=ERROR_UNCHECKED, errorCode={}, message={}",
                        method,
                        path,
                        errcode,
                        errmsg);
                return Message.builder().errcode(errcode).errmsg(errmsg).build();
            }
        }

        TimeoutException timeoutException = findCause(ex, TimeoutException.class);
        SocketTimeoutException socketTimeoutException = findCause(ex, SocketTimeoutException.class);
        if (timeoutException != null || socketTimeoutException != null) {
            Throwable timeout = timeoutException != null ? timeoutException : socketTimeoutException;
            Logger.error(
                    false,
                    "Vortex",
                    timeout,
                    "Request timed out: clientIp=N/A, method={}, path={}, event=ERROR_TIMEOUT, exception={}",
                    method,
                    path,
                    timeout.getClass().getSimpleName());
            return Message.builder().errcode(ErrorCode._100811.getKey()).errmsg(ErrorCode._100811.getValue()).build();
        }

        WebClientResponseException responseException = findCause(ex, WebClientResponseException.class);
        if (responseException != null) {
            Throwable root = Egress.rootCause(responseException);
            Logger.error(
                    false,
                    "Vortex",
                    responseException,
                    "Downstream response error: clientIp=N/A, method={}, path={}, event=ERROR_WEBCLIENT_RESPONSE, status={}, exception={}, rootCauseClass={}, rootCauseMessage={}",
                    method,
                    path,
                    responseException.getStatusCode().value(),
                    responseException.getClass().getSimpleName(),
                    root == null ? null : root.getClass().getName(),
                    root == null ? null : root.getMessage());
            return Message.builder().errcode(ErrorCode._116000.getKey()).errmsg(ErrorCode._116000.getValue()).build();
        }

        UnknownHostException unknownHostException = findCause(ex, UnknownHostException.class);
        if (unknownHostException != null) {
            Throwable root = Egress.rootCause(unknownHostException);
            Logger.error(
                    false,
                    "Vortex",
                    unknownHostException,
                    "Unknown host: clientIp=N/A, method={}, path={}, event=ERROR_WEBCLIENT, exception={}, rootCauseClass={}, rootCauseMessage={}",
                    method,
                    path,
                    unknownHostException.getClass().getSimpleName(),
                    root == null ? null : root.getClass().getName(),
                    root == null ? null : root.getMessage());
            return Message.builder().errcode(ErrorCode._100811.getKey()).errmsg(ErrorCode._100811.getValue()).build();
        }

        WebClientRequestException requestException = findCause(ex, WebClientRequestException.class);
        WebClientException clientException = findCause(ex, WebClientException.class);
        if (requestException != null || clientException != null) {
            Throwable webClientError = requestException != null ? requestException : clientException;
            Throwable root = Egress.rootCause(webClientError);
            Logger.error(
                    false,
                    "Vortex",
                    webClientError,
                    "Web client failure: clientIp=N/A, method={}, path={}, event=ERROR_WEBCLIENT, exception={}, rootCauseClass={}, rootCauseMessage={}",
                    method,
                    path,
                    webClientError.getClass().getSimpleName(),
                    root == null ? null : root.getClass().getName(),
                    root == null ? null : root.getMessage());
            return Message.builder().errcode(ErrorCode._116000.getKey()).errmsg(ErrorCode._116000.getValue()).build();
        }

        Logger.error(
                false,
                "Vortex",
                ex,
                "Unknown exception type: clientIp=N/A, method={}, path={}, event=ERROR_UNKNOWN, exceptionType={}, exception={}",
                method,
                path,
                ex.getClass().getName(),
                ex.getClass().getSimpleName());
        return Message.builder().errcode(ErrorCode._100807.getKey()).errmsg(ErrorCode._100807.getValue()).build();
    }

    /**
     * Finds the first exception of the requested type in the cause chain.
     *
     * @param throwable the exception to inspect
     * @param type      the expected exception type
     * @param <T>       the expected exception type
     * @return the first matching exception, or {@code null} when no match exists
     */
    private <T extends Throwable> T findCause(Throwable throwable, Class<T> type) {
        Throwable current = throwable;
        int depth = 0;
        while (current != null && depth++ < 64) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getCause();
        }
        return null;
    }

    /**
     * Represents a standardized message structure for API responses, typically used for error messages.
     *
     * @author Kimi Liu
     * @since Java 21+
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
            // No initialization required.
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
