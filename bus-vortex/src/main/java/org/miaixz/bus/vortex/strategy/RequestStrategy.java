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
 * The foundational strategy responsible for request parsing, body caching, and context initialization.
 * <p>
 * As the first strategy in the chain (with the highest precedence), its primary roles are:
 * <ol>
 * <li>Performing initial security checks, such as path validation and path traversal detection.</li>
 * <li>Dispatching the request to a specific handler based on its HTTP method and {@code Content-Type}.</li>
 * <li><b>Caching the request body:</b> Since a reactive request body can only be consumed once, this strategy reads the
 * body into memory and wraps the request with a {@link ServerHttpRequestDecorator}. This crucial step allows downstream
 * strategies or controllers to re-read the body if necessary.</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestStrategy extends AbstractStrategy {

    /**
     * Applies the request parsing and validation logic.
     * <p>
     * This is the main entry point for the strategy. It performs initial path validation and then dispatches the
     * request to the appropriate handler (e.g., {@code handleGetRequest}, {@code handleJsonRequest}) based on the HTTP
     * method and {@code Content-Type}.
     *
     * @param exchange The current server exchange.
     * @param chain    The next strategy in the chain.
     * @return A {@code Mono<Void>} that signals the completion of this strategy.
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, Chain chain) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);

            // 1. Set default Content-Type if missing and record the request start time.
            ServerWebExchange mutate = setContentType(exchange);
            context.setTimestamp(DateKit.current());
            ServerHttpRequest request = mutate.getRequest();

            // 2. Dispatch to the appropriate handler based on method and Content-Type.
            if (Objects.equals(request.getMethod(), HttpMethod.GET)) {
                return handleGetRequest(mutate, chain, context);
            } else {
                MediaType contentType = mutate.getRequest().getHeaders().getContentType();
                if (contentType == null) {
                    return handleFormRequest(mutate, chain, context);
                } else if (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
                    return handleJsonRequest(mutate, chain, context);
                } else if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType)) {
                    long contentLength = request.getHeaders().getContentLength();
                    if (contentLength > MAX_MULTIPART_REQUEST_SIZE) {
                        throw new ValidateException(ErrorCode._100530);
                    }
                    return handleMultipartRequest(mutate, chain, context);
                } else if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType)) {
                    return handleFormRequest(mutate, chain, context);
                } else {
                    // Default to form processing for other unknown Content-Types.
                    return handleFormRequest(mutate, chain, context);
                }
            }
        });
    }

    /**
     * Handles GET requests by extracting parameters directly from the URL query.
     *
     * @param exchange The current server exchange.
     * @param chain    The next strategy in the chain.
     * @param context  The request context to be populated.
     * @return A {@code Mono<Void>} that signals the completion of processing.
     */
    private Mono<Void> handleGetRequest(ServerWebExchange exchange, Chain chain, Context context) {
        context.getParameters().putAll(exchange.getRequest().getQueryParams().toSingleValueMap());
        this.validateParameters(exchange, context);
        Logger.info(
                "==>     Filter: GET request processed - Path: {}, Params: {}",
                exchange.getRequest().getURI().getPath(),
                JsonKit.toJsonString(context.getParameters()));

        return chain.apply(exchange).doOnSuccess(
                v -> Logger.info(
                        "==>     Filter: Request processed - Path: {}, ExecutionTime: {}ms",
                        exchange.getRequest().getURI().getPath(),
                        (System.currentTimeMillis() - context.getTimestamp())));
    }

    /**
     * Handles {@code application/json} requests. It reads the request body and delegates to
     * {@link #processJsonData(ServerWebExchange, Chain, Context, List)}.
     * <p>
     * This process is wrapped in a retry mechanism to handle transient network or parsing errors.
     *
     * @param exchange The current server exchange.
     * @param chain    The next strategy in the chain.
     * @param context  The request context.
     * @return A {@code Mono<Void>} that signals the completion of processing.
     */
    private Mono<Void> handleJsonRequest(ServerWebExchange exchange, Chain chain, Context context) {
        return exchange.getRequest().getBody().collectList()
                .flatMap(dataBuffers -> processJsonData(exchange, chain, context, dataBuffers)).retryWhen(
                        Retry.backoff(MAX_RETRY_ATTEMPTS, java.time.Duration.ofMillis(RETRY_DELAY_MS))
                                .maxBackoff(java.time.Duration.ofMillis(500)).jitter(0.75)
                                .doBeforeRetry(
                                        retrySignal -> Logger.warn(
                                                "==>     Filter: Retrying JSON request processing, attempt: {}, error: {}",
                                                (retrySignal.totalRetries() + 1),
                                                retrySignal.failure().getMessage()))
                                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                    Logger.error(
                                            "==>     Filter: JSON request processing failed after {} attempts, error: {}",
                                            MAX_RETRY_ATTEMPTS,
                                            retrySignal.failure().getMessage());
                                    return new InternalException(ErrorCode._116000);
                                }));
    }

    /**
     * Performs the actual parsing of a JSON request body.
     * <p>
     * This method merges the data buffers, parses the resulting JSON string into a map, and populates the
     * {@link Context}. It then decorates the request to allow the body to be re-read downstream.
     *
     * @param exchange    The current server exchange.
     * @param chain       The next strategy in the chain.
     * @param context     The request context to be populated.
     * @param dataBuffers A list of data buffer fragments from the request body.
     * @return A {@code Mono<Void>} that signals the completion of processing.
     */
    private Mono<Void> processJsonData(
            ServerWebExchange exchange,
            Chain chain,
            Context context,
            List<DataBuffer> dataBuffers) {
        try {
            byte[] bytes = readBodyToBytes(dataBuffers);

            String jsonBody = new String(bytes, Charset.UTF_8);
            Map<String, Object> jsonMap = JsonKit.toMap(jsonBody);
            context.getParameters().putAll(jsonMap);

            ServerHttpRequest newRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {

                @Override
                public Flux<DataBuffer> getBody() {
                    return Flux.just(exchange.getResponse().bufferFactory().wrap(bytes));
                }
            };

            ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();

            this.validateParameters(newExchange, context);
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
            return Mono.error(e);
        }
    }

    /**
     * Handles {@code application/x-www-form-urlencoded} requests. It reads the request body and delegates to
     * {@link #processFormData(ServerWebExchange, Chain, Context, List)}.
     * <p>
     * This process is wrapped in a retry mechanism.
     *
     * @param exchange The current server exchange.
     * @param chain    The next strategy in the chain.
     * @param context  The request context.
     * @return A {@code Mono<Void>} that signals the completion of processing.
     */
    private Mono<Void> handleFormRequest(ServerWebExchange exchange, Chain chain, Context context) {
        return exchange.getRequest().getBody().collectList()
                .flatMap(dataBuffers -> processFormData(exchange, chain, context, dataBuffers)).retryWhen(
                        Retry.backoff(MAX_RETRY_ATTEMPTS, java.time.Duration.ofMillis(RETRY_DELAY_MS))
                                .maxBackoff(java.time.Duration.ofMillis(500)).jitter(0.75)
                                .doBeforeRetry(
                                        retrySignal -> Logger.warn(
                                                "==>     Filter: Retrying form request processing, attempt: {}, error: {}",
                                                (retrySignal.totalRetries() + 1),
                                                retrySignal.failure().getMessage()))
                                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                    Logger.error(
                                            "==>     Filter: Form request processing failed after {} attempts, error: {}",
                                            MAX_RETRY_ATTEMPTS,
                                            retrySignal.failure().getMessage());
                                    return new InternalException(ErrorCode._116000);
                                }));
    }

    /**
     * Performs the actual parsing of a form-data request body.
     * <p>
     * This method caches the request body, decorates the request to make it re-readable, and then uses the built-in
     * {@link ServerWebExchange#getFormData()} to parse the parameters into the {@link Context}.
     *
     * @param exchange    The current server exchange.
     * @param chain       The next strategy in the chain.
     * @param context     The request context to be populated.
     * @param dataBuffers A list of data buffer fragments from the request body.
     * @return A {@code Mono<Void>} that signals the completion of processing.
     */
    private Mono<Void> processFormData(
            ServerWebExchange exchange,
            Chain chain,
            Context context,
            List<DataBuffer> dataBuffers) {
        try {
            byte[] bytes = readBodyToBytes(dataBuffers);

            ServerHttpRequest newRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {

                @Override
                public Flux<DataBuffer> getBody() {
                    return Flux.just(exchange.getResponse().bufferFactory().wrap(bytes));
                }
            };

            ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();

            return newExchange.getFormData().flatMap(params -> {
                context.getParameters().putAll(params.toSingleValueMap());
                this.validateParameters(newExchange, context);
                Logger.info(
                        "==>     Filter: Form request processed - Path: {}, Params: {}",
                        newExchange.getRequest().getURI().getPath(),
                        JsonKit.toJsonString(context.getParameters()));
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
     * Handles {@code multipart/form-data} requests, typically used for file uploads.
     * <p>
     * This method delegates directly to {@link #processMultipartData(ServerWebExchange, Chain, Context, MultiValueMap)}
     * after using the built-in {@link ServerWebExchange#getMultipartData()} parser.
     *
     * @param exchange The current server exchange.
     * @param chain    The next strategy in the chain.
     * @param context  The request context.
     * @return A {@code Mono<Void>} that signals the completion of processing.
     */
    private Mono<Void> handleMultipartRequest(ServerWebExchange exchange, Chain chain, Context context) {
        return exchange.getMultipartData().flatMap(params -> processMultipartData(exchange, chain, context, params))
                .retryWhen(
                        Retry.backoff(MAX_RETRY_ATTEMPTS, java.time.Duration.ofMillis(RETRY_DELAY_MS))
                                .maxBackoff(java.time.Duration.ofMillis(500)).jitter(0.75)
                                .doBeforeRetry(
                                        retrySignal -> Logger.warn(
                                                "==>     Filter: Retrying multipart request processing, attempt: {}, error: {}",
                                                (retrySignal.totalRetries() + 1),
                                                retrySignal.failure().getMessage()))
                                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                    Logger.error(
                                            "==>     Filter: Multipart request processing failed after {} attempts, error: {}",
                                            MAX_RETRY_ATTEMPTS,
                                            retrySignal.failure().getMessage());

                                    if (retrySignal.failure().getMessage() != null && retrySignal.failure().getMessage()
                                            .contains("Could not find first boundary")) {
                                        return new InternalException(ErrorCode._100303);
                                    }
                                    return new InternalException(ErrorCode._116000);
                                }));
    }

    /**
     * Performs the actual processing of multipart form data.
     * <p>
     * This method iterates through the parsed parts, separating them into form fields (which are added to
     * {@link Context#getParameters()}) and file parts (which are added to {@link Context#getFileParts()}).
     *
     * @param exchange The current server exchange.
     * @param chain    The next strategy in the chain.
     * @param context  The request context to be populated.
     * @param params   A {@link MultiValueMap} containing all parsed parts of the multipart request.
     * @return A {@code Mono<Void>} that signals the completion of processing.
     */
    private Mono<Void> processMultipartData(
            ServerWebExchange exchange,
            Chain chain,
            Context context,
            MultiValueMap<String, Part> params) {
        try {
            Map<String, String> formMap = new LinkedHashMap<>();
            Map<String, Part> fileMap = new LinkedHashMap<>();

            params.toSingleValueMap().forEach((k, v) -> {
                if (v instanceof FormFieldPart) {
                    formMap.put(k, ((FormFieldPart) v).value());
                } else if (v instanceof FilePart) {
                    fileMap.put(k, v);
                }
            });

            context.getParameters().putAll(formMap);
            context.setFileParts(fileMap);

            this.validateParameters(exchange, context);

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
     * Reads a list of {@link DataBuffer}s into a single byte array, while enforcing a maximum size limit.
     *
     * @param dataBuffers The list of {@link DataBuffer}s to read.
     * @return A byte array containing the merged data from all buffers.
     * @throws ValidateException if the total size of the data buffers exceeds {@code MAX_REQUEST_SIZE}.
     */
    private byte[] readBodyToBytes(List<DataBuffer> dataBuffers) {
        int totalSize = dataBuffers.stream().mapToInt(DataBuffer::readableByteCount).sum();
        if (totalSize > MAX_REQUEST_SIZE) {
            throw new ValidateException(ErrorCode._100530);
        }

        byte[] bytes = new byte[totalSize];
        int pos = 0;
        for (DataBuffer buffer : dataBuffers) {
            int length = buffer.readableByteCount();
            buffer.read(bytes, pos, length);
            pos += length;
        }
        return bytes;
    }

}
