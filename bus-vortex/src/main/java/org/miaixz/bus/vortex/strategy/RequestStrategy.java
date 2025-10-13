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
package org.miaixz.bus.vortex.strategy;

import java.util.*;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * The foundational filter strategy, responsible for request parsing and context initialization. This strategy must run
 * first to prepare the {@link Context} object, which is then attached to the exchange attributes for all subsequent
 * strategies and handlers to use.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestStrategy extends AbstractStrategy {

    private static final List<String> ALLOW_PATHS = Arrays.asList("/router/rest", "/router/mcp");
    /**
     * The maximum number of of automatic retry attempts allowed when processing the request body.
     */
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * The base delay time in milliseconds between automatic retry attempts.
     */
    private static final long RETRY_DELAY_MS = 1000;

    /**
     * The core execution method of the filter, responsible for initial security checks and request dispatching.
     * <p>
     * This method first performs path security checks, including blacklisted path filtering and path traversal attack
     * detection. After passing the checks, it dispatches the {@link ServerWebExchange} to the appropriate handling
     * method (e.g., {@code handleGetRequest}, {@code handleJsonRequest}) based on the request type (GET or other
     * methods and Content-Type).
     * </p>
     *
     * @param exchange The current {@link ServerWebExchange} object.
     * @param chain    The filter chain.
     * @param context  The context object used to pass data throughout the request lifecycle.
     * @return {@link Mono<Void>} indicating the asynchronous completion of the filtering operation.
     * @throws ValidateException If the request path is blocked or a path traversal attack is detected.
     */
    @Override
    protected Mono<Void> apply(ServerWebExchange exchange, StrategyChain chain, Context context) {
        String path = exchange.getRequest().getPath().value();
        // Check for blacklisted paths, such as favicon.ico, to avoid unnecessary processing.
        if (!ALLOW_PATHS.contains(path)) {
            Logger.warn("==>     Filter: Blocked request to path: {}", path);
            // Throw an exception to prevent further processing
            throw new ValidateException(ErrorCode._BLOCKED);
        }
        // Check for attempts at path traversal attacks to enhance system security.
        if (isPathTraversalAttempt(path)) {
            Logger.warn("==>     Filter: Path traversal attempt detected: {}", path);
            // Throw an exception to prevent further processing
            throw new ValidateException(ErrorCode._LIMITER);
        }

        ServerWebExchange mutate = setContentType(exchange);
        context.setTimestamp(DateKit.current());
        ServerHttpRequest request = mutate.getRequest();

        // Dispatch to different handlers based on HTTP method and Content-Type
        if (Objects.equals(request.getMethod(), HttpMethod.GET)) {
            return handleGetRequest(mutate, chain, context);
        } else {
            MediaType contentType = mutate.getRequest().getHeaders().getContentType();
            if (contentType == null) {
                // If no Content-Type, default to form processing
                return handleFormRequest(mutate, chain, context);
            } else if (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
                return handleJsonRequest(mutate, chain, context);
            } else if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType)) {
                // Multipart requests have a dedicated handling method and do not read the request body beforehand
                return handleMultipartRequest(mutate, chain, context);
            } else if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType)) {
                return handleFormRequest(mutate, chain, context);
            }
            // For other unknown Content-Types, attempt to process as a general form
            return handleFormRequest(mutate, chain, context);
        }
    }

    /**
     * Handles GET requests.
     * <p>
     * Extracts data directly from URL query parameters, stores it in the context, and then continues the filter chain.
     * </p>
     *
     * @param exchange The {@link ServerWebExchange} object.
     * @param chain    The filter chain.
     * @param context  The request context.
     * @return {@link Mono<Void>} indicating the completion of asynchronous processing.
     */
    private Mono<Void> handleGetRequest(ServerWebExchange exchange, StrategyChain chain, Context context) {
        MultiValueMap<String, String> params = exchange.getRequest().getQueryParams();
        context.setRequestMap(params.toSingleValueMap());
        this.validate(exchange);
        Logger.info(
                "==>     Filter: GET request processed - Path: {}, Params: {}",
                exchange.getRequest().getURI().getPath(),
                JsonKit.toJsonString(context.getRequestMap()));

        return chain.apply(exchange).doOnSuccess(
                v -> Logger.info(
                        "==>     Filter: Request processed - Path: {}, ExecutionTime: {}ms",
                        exchange.getRequest().getURI().getPath(),
                        (System.currentTimeMillis() - context.getTimestamp())));
    }

    /**
     * Handles requests with Content-Type as application/json.
     * <p>
     * This method asynchronously reads and parses the JSON data from the request body, storing the result in the
     * context. To handle potential transient network or parsing errors, this process is wrapped in a retry mechanism
     * with a backoff strategy.
     * </p>
     *
     * @param exchange The {@link ServerWebExchange} object.
     * @param chain    The filter chain.
     * @param context  The request context.
     * @return {@link Mono<Void>} indicating the completion of asynchronous processing.
     */
    private Mono<Void> handleJsonRequest(ServerWebExchange exchange, StrategyChain chain, Context context) {
        // Asynchronously collect all data fragments from the request body
        return exchange.getRequest().getBody().collectList()
                .flatMap(dataBuffers -> processJsonData(exchange, chain, context, dataBuffers)).retryWhen(
                        Retry.backoff(MAX_RETRY_ATTEMPTS, java.time.Duration.ofMillis(RETRY_DELAY_MS))
                                .maxBackoff(java.time.Duration.ofMillis(500)).jitter(0.75)
                                .doBeforeRetry(retrySignal -> {
                                    // Log each retry attempt
                                    Logger.warn(
                                            "==>     Filter: Retrying JSON request processing, attempt: {}, error: {}",
                                            (retrySignal.totalRetries() + 1),
                                            retrySignal.failure().getMessage());
                                }).onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                    // Log severe error and throw exception after retry exhaustion
                                    Logger.error(
                                            "==>     Filter: JSON request processing failed after {} attempts, error: {}",
                                            MAX_RETRY_ATTEMPTS,
                                            retrySignal.failure().getMessage());
                                    return new InternalException(ErrorCode._116000);
                                }));
    }

    /**
     * The core logic for actually processing and parsing the JSON request body.
     * <p>
     * This method is responsible for merging fragmented {@link DataBuffer}s into a complete byte array, and then
     * parsing the result into a JSON-formatted Map. <b>Key Operation:</b> Since the request body is a
     * single-consumption stream, after reading, this method creates a {@link ServerHttpRequestDecorator} that re-wraps
     * the read byte data into a new {@code Flux<DataBuffer>} and places it into a new request object. This ensures that
     * downstream filters or controllers can still access the original request body.
     * </p>
     *
     * @param exchange    The {@link ServerWebExchange} object.
     * @param chain       The filter chain.
     * @param context     The request context.
     * @param dataBuffers A list of data buffer fragments collected from the request body.
     * @return {@link Mono<Void>} indicating the completion of asynchronous processing.
     */
    private Mono<Void> processJsonData(
            ServerWebExchange exchange,
            StrategyChain chain,
            Context context,
            List<DataBuffer> dataBuffers) {
        try {
            // Merge all data buffers into a single byte array
            byte[] bytes = new byte[dataBuffers.stream().mapToInt(DataBuffer::readableByteCount).sum()];
            int pos = 0;
            for (DataBuffer buffer : dataBuffers) {
                int length = buffer.readableByteCount();
                buffer.read(bytes, pos, length);
                pos += length;
            }

            String jsonBody = new String(bytes, Charset.UTF_8);
            Map<String, String> jsonMap = JsonKit.toMap(jsonBody);
            context.setRequestMap(jsonMap);

            // Create a request decorator to override the getBody method, allowing downstream components to re-consume
            // the request body
            ServerHttpRequest newRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {

                @Override
                public Flux<DataBuffer> getBody() {
                    DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
                    return Flux.just(bufferFactory.wrap(bytes));
                }
            };

            // Build a new ServerWebExchange with the decorated request
            ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();

            this.validate(newExchange);
            Logger.info(
                    "==>     Filter: JSON request processed - Path: {}, Params: {}",
                    newExchange.getRequest().getURI().getPath(),
                    JsonKit.toJsonString(jsonMap));
            return chain.apply(newExchange).doOnTerminate(
                    () -> Logger.info(
                            "==>     Filter: Request processed - Path: {}, ExecutionTime: {}ms",
                            newExchange.getRequest().getURI().getPath(),
                            (System.currentTimeMillis() - context.getTimestamp())));
        } catch (Exception e) {
            Logger.error("==>     Filter: Failed to process JSON: {}", e.getMessage());
            return Mono.error(e); // Convert synchronous exception to asynchronous error
        }
    }

    /**
     * Handles form requests with Content-Type application/x-www-form-urlencoded or no Content-Type.
     * <p>
     * This method is similar to the JSON request handling logic, also including request body reading, reusability, and
     * a retry mechanism.
     * </p>
     *
     * @param exchange The {@link ServerWebExchange} object.
     * @param chain    The filter chain.
     * @param context  The request context.
     * @return {@link Mono<Void>} indicating the completion of asynchronous processing.
     */
    private Mono<Void> handleFormRequest(ServerWebExchange exchange, StrategyChain chain, Context context) {
        return exchange.getRequest().getBody().collectList()
                .flatMap(dataBuffers -> processFormData(exchange, chain, context, dataBuffers)).retryWhen(
                        Retry.backoff(MAX_RETRY_ATTEMPTS, java.time.Duration.ofMillis(RETRY_DELAY_MS))
                                .maxBackoff(java.time.Duration.ofMillis(500)).jitter(0.75)
                                .doBeforeRetry(retrySignal -> {
                                    Logger.warn(
                                            "==>     Filter: Retrying form request processing, attempt: {}, error: {}",
                                            (retrySignal.totalRetries() + 1),
                                            retrySignal.failure().getMessage());
                                }).onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                    Logger.error(
                                            "==>     Filter: Form request processing failed after {} attempts, error: {}",
                                            MAX_RETRY_ATTEMPTS,
                                            retrySignal.failure().getMessage());
                                    return new InternalException(ErrorCode._116000);
                                }));
    }

    /**
     * The core logic for actually processing and parsing form-data from the request body.
     * <p>
     * This method first caches the request body data and wraps it with a {@link ServerHttpRequestDecorator} to support
     * downstream consumption. Subsequently, it uses Spring Framework's {@code getFormData()} method to parse form
     * parameters from the re-readable request body.
     * </p>
     *
     * @param exchange    The {@link ServerWebExchange} object.
     * @param chain       The filter chain.
     * @param context     The request context.
     * @param dataBuffers A list of data buffer fragments collected from the request body.
     * @return {@link Mono<Void>} indicating the completion of asynchronous processing.
     */
    private Mono<Void> processFormData(
            ServerWebExchange exchange,
            StrategyChain chain,
            Context context,
            List<DataBuffer> dataBuffers) {
        try {
            byte[] bytes = new byte[dataBuffers.stream().mapToInt(DataBuffer::readableByteCount).sum()];
            int pos = 0;
            for (DataBuffer buffer : dataBuffers) {
                int length = buffer.readableByteCount();
                buffer.read(bytes, pos, length);
                pos += length;
            }

            // Similarly, create a request decorator to support repeated reading of the request body
            ServerHttpRequest newRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {

                @Override
                public Flux<DataBuffer> getBody() {
                    DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
                    return Flux.just(bufferFactory.wrap(bytes));
                }
            };

            ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();

            // Parse form data from the re-readable request
            return newExchange.getFormData().flatMap(params -> {
                context.setRequestMap(params.toSingleValueMap());
                this.validate(newExchange);
                Logger.info(
                        "==>     Filter: Form request processed - Path: {}, Params: {}",
                        newExchange.getRequest().getURI().getPath(),
                        JsonKit.toJsonString(context.getRequestMap()));
                return chain.apply(newExchange).doOnTerminate(
                        () -> Logger.info(
                                "==>     Filter: Request processed - Path: {}, ExecutionTime: {}ms",
                                newExchange.getRequest().getURI().getPath(),
                                (System.currentTimeMillis() - context.getTimestamp())));
            });
        } catch (Exception e) {
            Logger.error("==>     Filter: Failed to process form: {}", e.getMessage());
            return Mono.error(e);
        }
    }

    /**
     * Handles requests of type multipart/form-data, typically used for file uploads.
     * <p>
     * For such requests, Spring WebFlux provides a direct parsing method {@code getMultipartData()}, eliminating the
     * need to manually process the request body stream. The parsing process is also protected by a retry mechanism.
     * </p>
     *
     * @param exchange The {@link ServerWebExchange} object.
     * @param chain    The filter chain.
     * @param context  The request context.
     * @return {@link Mono<Void>} indicating the completion of asynchronous processing.
     */
    private Mono<Void> handleMultipartRequest(ServerWebExchange exchange, StrategyChain chain, Context context) {
        return exchange.getMultipartData().flatMap(params -> processMultipartData(exchange, chain, context, params))
                .retryWhen(
                        Retry.backoff(MAX_RETRY_ATTEMPTS, java.time.Duration.ofMillis(RETRY_DELAY_MS))
                                .maxBackoff(java.time.Duration.ofMillis(500)).jitter(0.75)
                                .doBeforeRetry(retrySignal -> {
                                    Logger.warn(
                                            "==>     Filter: Retrying multipart request processing, attempt: {}, error: {}",
                                            (retrySignal.totalRetries() + 1),
                                            retrySignal.failure().getMessage());
                                }).onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                    Logger.error(
                                            "==>     Filter: Multipart request processing failed after {} attempts, error: {}",
                                            MAX_RETRY_ATTEMPTS,
                                            retrySignal.failure().getMessage());

                                    // For specific boundary errors, return a more explicit error code
                                    if (retrySignal.failure().getMessage() != null && retrySignal.failure().getMessage()
                                            .contains("Could not find first boundary")) {
                                        return new InternalException(ErrorCode._100303);
                                    }
                                    return new InternalException(ErrorCode._116000);
                                }));
    }

    /**
     * The core logic for actually processing and parsing multipart/form-data.
     * <p>
     * This method iterates through the parsed parts, storing form fields and file parts separately into the context's
     * {@code requestMap} and {@code filePartMap} respectively, for use by subsequent business logic.
     * </p>
     *
     * @param exchange The {@link ServerWebExchange} object.
     * @param chain    The filter chain.
     * @param context  The request context.
     * @param params   A {@link MultiValueMap} containing form fields and file parts.
     * @return {@link Mono<Void>} indicating the completion of asynchronous processing.
     */
    private Mono<Void> processMultipartData(
            ServerWebExchange exchange,
            StrategyChain chain,
            Context context,
            MultiValueMap<String, Part> params) {
        try {
            Map<String, String> formMap = new LinkedHashMap<>();
            Map<String, Part> fileMap = new LinkedHashMap<>();

            // Iterate through all parts, distinguishing between form fields and files
            params.toSingleValueMap().forEach((k, v) -> {
                if (v instanceof FormFieldPart) {
                    formMap.put(k, ((FormFieldPart) v).value());
                }
                if (v instanceof FilePart) {
                    fileMap.put(k, v);
                }
            });

            context.setRequestMap(formMap);
            context.setFilePartMap(fileMap);
            this.validate(exchange);

            Logger.info(
                    "==>     Filter: Multipart request processed - Path: {}, Params: {}",
                    exchange.getRequest().getURI().getPath(),
                    JsonKit.toJsonString(formMap));

            return chain.apply(exchange).doOnTerminate(
                    () -> Logger.info(
                            "==>     Filter: Request processed - Path: {}, ExecutionTime: {}ms",
                            exchange.getRequest().getURI().getPath(),
                            (System.currentTimeMillis() - context.getTimestamp())));
        } catch (Exception e) {
            Logger.error("==>     Filter: Failed to process multipart: {}", e.getMessage());
            return Mono.error(e);
        }
    }

    /**
     * Checks if the given URL path contains characteristics of a path traversal (directory traversal) attack.
     * <p>
     * Path traversal is a common security vulnerability where attackers attempt to access restricted directories by
     * manipulating file paths with "..". This method checks for various common traversal sequences and their
     * URL-encoded forms.
     * </p>
     *
     * @param path The URL path string to check.
     * @return {@code true} if a traversal attempt is detected, {@code false} otherwise.
     */
    private boolean isPathTraversalAttempt(String path) {
        // Check for various characteristics of path traversal attacks, including plain text and URL-encoded forms
        return path.contains("../") || path.contains("..\\") || path.contains("%2e%2e%2f") || path.contains("%2e%2e\\")
                || path.contains("..%2f") || path.contains("..%5c");
    }

}
