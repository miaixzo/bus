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
package org.miaixz.bus.vortex.handler;

import java.net.UnknownHostException;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.BusinessException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.UncheckedException;
import org.miaixz.bus.core.lang.exception.ValidateException;
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
 *
 * @author Justubborn
 * @since Java 17+
 */
public class ErrorsHandler implements WebExceptionHandler {

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
        // Get the response object and set the status code
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);

        // Get the request context from the exchange attributes
        Context context = exchange.getAttribute(Context.$);

        // Log the format from the context for debugging
        if (context != null) {
            Logger.info("==>    Handler: Context format is: {}", context.getFormat());
        } else {
            Logger.info("==>    Handler: Context is null, defaulting to JSON format.");
        }

        // Get request information for logging
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";

        // Direct log output without creating methods
        if (ex instanceof WebClientException) {
            if (ex.getCause() instanceof UnknownHostException) {
                Logger.info(
                        "==>    Handler: [N/A] [{}] [{}] [ERROR_WEBCLIENT] - UnknownHostException: {}",
                        method,
                        path,
                        ex.getCause().getMessage());
            } else {
                Logger.info(
                        "==>    Handler: [N/A] [{}] [{}] [ERROR_WEBCLIENT] - WebClientException: {}",
                        method,
                        path,
                        ex.getMessage());
            }
        } else if (ex instanceof UncheckedException) {
            if (StringKit.isNotBlank(((UncheckedException) ex).getErrcode())) {
                Logger.info(
                        "==>    Handler: [N/A] [{}] [{}] [ERROR_UNCHECKED] - ErrorCode: {}, Message: {}",
                        method,
                        path,
                        ((UncheckedException) ex).getErrcode(),
                        ((UncheckedException) ex).getErrmsg());
            } else {
                Logger.info(
                        "==>    Handler: [N/A] [{}] [{}] [ERROR_UNCHECKED] - Generic InternalException: {}",
                        method,
                        path,
                        ex.getMessage());
            }
        } else {
            Logger.info(
                    "==>    Handler: [N/A] [{}] [{}] [ERROR_UNKNOWN] - Unknown exception type: {}, Message: {}",
                    method,
                    path,
                    ex.getClass().getName(),
                    ex.getMessage());
        }

        // Determine the format and content type based on the context, defaulting to JSON
        Formats responseFormat = (context != null) ? context.getFormat() : Formats.JSON;
        response.getHeaders().setContentType(responseFormat.getMediaType());

        // Generate an error message based on the exception type
        Message message = buildErrorMessage(ex);
        String formatBody = responseFormat.getProvider().serialize(message);

        // Wrap the formatted response into a DataBuffer
        DataBuffer db = response.bufferFactory().wrap(formatBody.getBytes(Charset.UTF_8));

        // Return the response and log the execution time
        return response.writeWith(Mono.just(db)).doOnTerminate(() -> {
            // Direct log output without creating methods
            if (context != null) {
                long executionTime = System.currentTimeMillis() - context.getTimestamp();
                Logger.info(
                        "==>    Handler: [N/A] [{}] [{}] [ERROR_COMPLETION] - Error handled, execution time: {}ms, exception: {}",
                        method,
                        path,
                        executionTime,
                        ex.getClass().getSimpleName());
            } else {
                Logger.info(
                        "==>    Handler: [N/A] [{}] [{}] [ERROR_COMPLETION] - Error handled, exception: {}",
                        method,
                        path,
                        ex.getClass().getSimpleName());
            }
        });
    }

    /**
     * Builds an error message based on the provided exception.
     * <p>
     * This method categorizes exceptions and delegates to specific handlers for {@link WebClientException},
     * {@link UncheckedException} (and its subclasses like {@link InternalException}, {@link ValidateException},
     * {@link BusinessException}), and other unknown exceptions.
     * </p>
     *
     * @param ex The exception object.
     * @return An error {@link Message} object.
     */
    protected Message buildErrorMessage(Throwable ex) {
        // 1. First, handle specific exceptions that do not belong to the UncheckedException inheritance hierarchy
        if (ex instanceof WebClientException) {
            if (ex.getCause() instanceof UnknownHostException) {
                return Message.builder().errcode(ErrorCode._100811.getKey()).errmsg(ErrorCode._100811.getValue())
                        .build();
            } else {
                return Message.builder().errcode(ErrorCode._116000.getKey()).errmsg(ErrorCode._116000.getValue())
                        .build();
            }
        }

        // 2. Then, handle all UncheckedException and its subclasses with an if block
        // InternalException, ValidateException, BusinessException will all be caught here
        if (ex instanceof UncheckedException) {
            UncheckedException uncheckedEx = (UncheckedException) ex;
            if (StringKit.isNotBlank(uncheckedEx.getErrcode())) {
                return Message.builder().errcode(uncheckedEx.getErrcode()).errmsg(uncheckedEx.getErrmsg()).build();
            } else {
                return Message.builder().errcode(ErrorCode._100807.getKey()).errmsg(ErrorCode._100807.getValue())
                        .build();
            }
        }

        // 3. Finally, handle all other unknown exceptions
        return Message.builder().errcode(ErrorCode._100807.getKey()).errmsg(ErrorCode._100807.getValue()).build();
    }

    /**
     * Represents a standardized message structure for API responses, typically used for error messages.
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {

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
