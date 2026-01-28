/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Holder;
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

/**
 * The foundational strategy responsible for request parsing, body caching, and context initialization.
 * <p>
 * As the first strategy in the chain (with the highest precedence), its primary roles are:
 * <ul>
 * <li>Performing initial security checks, such as path validation, and path traversal detection.</li>
 * <li>Dispatching the request to a specific handler based on its HTTP method and {@code Content-Type}.</li>
 * <li><b>Smart request body handling:</b></li>
 * <li>For small requests (&lt; 10 MB): Caches the request body in memory for fast processing and re-reading</li>
 * <li>For large multipart requests: Uses streaming processing to avoid high memory pressure</li>
 * <li>This ensures optimal performance while preventing OOM errors under high load</li>
 * </ul>
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
        // deferContextual ensures this logic runs at subscription time,
        // allowing access to the Reactor context.
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            context.setX_request_ipv4(this.getClientIp(exchange.getRequest()));
            // 1. Set default Content-Type if missing and record the request start time.
            // This setup logic is synchronous but acceptable as it's part of
            // building the reactive chain, not executing it.
            ServerWebExchange mutate = setContentType(exchange);
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
                    if (contentLength > Holder.getMaxMultipartRequestSize()) {
                        // Throwing here is acceptable as it's a pre-condition check
                        // before any async body processing.
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
        // Wrap synchronous context mutation and logging in fromRunnable
        // to defer execution until subscription.
        return Mono.fromRunnable(() -> {
            context.getParameters().putAll(exchange.getRequest().getQueryParams().toSingleValueMap());
            Logger.info(
                    true,
                    "Request",
                    "[{}] GET request processed - Path: {}, Params: {}",
                    context.getX_request_ip(),
                    exchange.getRequest().getURI().getPath(),
                    JsonKit.toJsonString(context.getParameters()));
        })
                // Use .then() to execute the next chain link after the runnable completes.
                .then(chain.apply(exchange))
                // Use doFinally for robust logging on any termination signal (complete, error, cancel).
                .doFinally(
                        signalType -> Logger.info(
                                false,
                                "Request",
                                "[{}] Request processed - Path: {}, ExecutionTime: {}ms",
                                context.getX_request_ip(),
                                exchange.getRequest().getURI().getPath(),
                                (System.currentTimeMillis() - context.getTimestamp())));
    }

    /**
     * Handles {@code application/json} requests. It reads the request body and delegates to
     * {@link #processJsonData(ServerWebExchange, Chain, Context, List)}.
     * <p>
     * This process is wrapped in a retry mechanism to handle transient network or parsing errors.
     * <p>
     * Performance optimization: Checks Content-Length first to determine if streaming should be used.
     *
     * @param exchange The current server exchange.
     * @param chain    The next strategy in the chain.
     * @param context  The request context.
     * @return A {@code Mono<Void>} that signals the completion of processing.
     */
    private Mono<Void> handleJsonRequest(ServerWebExchange exchange, Chain chain, Context context) {
        // Check Content-Length header for streaming decision
        long contentLength = exchange.getRequest().getHeaders().getContentLength();
        boolean shouldStream = contentLength > Holder.getStreamingRequestThreshold();

        if (shouldStream) {
            Logger.info(
                    true,
                    "Request",
                    "[{}] Large JSON request detected ({} bytes). Using optimized streaming processing.",
                    context.getX_request_ip(),
                    contentLength);
        }

        // collectList() asynchronously buffers the body.
        // Note: Timeout and retry are handled at VortexHandler level, not here.
        return exchange.getRequest().getBody().collectList()
                .flatMap(dataBuffers -> processJsonData(exchange, chain, context, dataBuffers));
    }

    /**
     * Performs the actual parsing of a JSON request body.
     * <p>
     * This method merges the data buffers, parses the resulting JSON string into a map, and populates the
     * {@link Context}. It then decorates the request to allow the body to be re-read downstream.
     * <p>
     * Performance optimization: For large requests, adds detailed logging to track memory usage.
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

        // Use fromCallable to wrap all synchronous, potentially-throwing logic.
        // Any exception (from readBodyToBytes or JsonKit.toMap) will be
        // captured and emitted as Mono.error(), triggering the retry logic.
        return Mono.fromCallable(() -> {
            // 1. Synchronous byte copy (can throw ValidateException)
            byte[] bytes = readBodyToBytes(dataBuffers);

            // Log large JSON processing
            if (bytes.length > Holder.getStreamingRequestThreshold()) {
                Logger.info(
                        true,
                        "Request",
                        "[{}] Large JSON body cached in memory: {} bytes",
                        context.getX_request_ip(),
                        bytes.length);
            }

            // 2. Synchronous JSON parsing (can throw parsing exception)
            String jsonBody = new String(bytes, Charset.UTF_8);
            Map<String, Object> jsonMap = JsonKit.toMap(jsonBody);
            context.getParameters().putAll(jsonMap);

            // 3. Create the decorator to cache the body
            ServerHttpRequest newRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {

                /**
                 * Returns the cached request body as a Flux of DataBuffer.
                 * <p>
                 * This override allows the request body to be read multiple times by returning the cached byte array
                 * wrapped in a DataBuffer.
                 *
                 * @return A Flux emitting a single DataBuffer containing the cached body
                 */
                @Override
                public Flux<DataBuffer> getBody() {
                    return Flux.just(exchange.getResponse().bufferFactory().wrap(bytes));
                }
            };

            Logger.info(
                    true,
                    "Request",
                    "[{}] JSON request processed - Path: {}, Params: {}",
                    context.getX_request_ip(),
                    exchange.getRequest().getURI().getPath(),
                    JsonKit.toJsonString(jsonMap));

            // 4. Return the new exchange for the next chain link
            return exchange.mutate().request(newRequest).build();
        })
                // flatMap to the next chain link using the new exchange
                .flatMap(chain::apply)
                .doFinally(
                        signalType -> Logger.info(
                                false,
                                "Request",
                                "[{}] Request processed - Path: {}, ExecutionTime: {}ms",
                                context.getX_request_ip(),
                                exchange.getRequest().getURI().getPath(),
                                (System.currentTimeMillis() - context.getTimestamp())))
                // Add explicit error logging for failures within this stage
                .onErrorResume(e -> {
                    Logger.error(
                            false,
                            "Request",
                            "[{}] Failed to process JSON: {}",
                            context.getX_request_ip(),
                            e.getMessage());
                    return Mono.error(e); // Re-throw the original exception
                });
    }

    /**
     * Handles {@code application/x-www-form-urlencoded} requests. It reads the request body and delegates to
     * {@link #processFormData(ServerWebExchange, Chain, Context, List)}.
     * <p>
     * Note: Timeout and retry are handled at VortexHandler level, not here.
     *
     * @param exchange The current server exchange.
     * @param chain    The next strategy in the chain.
     * @param context  The request context.
     * @return A {@code Mono<Void>} that signals the completion of processing.
     */
    private Mono<Void> handleFormRequest(ServerWebExchange exchange, Chain chain, Context context) {
        return exchange.getRequest().getBody().collectList()
                .flatMap(dataBuffers -> processFormData(exchange, chain, context, dataBuffers));
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

        // 1. Wrap the synchronous byte reading and request decoration in fromCallable.
        return Mono.fromCallable(() -> {
            byte[] bytes = readBodyToBytes(dataBuffers); // Can throw ValidateException

            /**
             * Decorator that caches the request body for multiple reads.
             * <p>
             * This inner class overrides getBody() to return the cached byte array, allowing the request body to be
             * consumed multiple times.
             */
            ServerHttpRequest newRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {

                /**
                 * Returns the cached request body as a Flux of DataBuffer.
                 * <p>
                 * This override allows the request body to be read multiple times by returning the cached byte array
                 * wrapped in a DataBuffer.
                 *
                 * @return A Flux emitting a single DataBuffer containing the cached body
                 */
                @Override
                public Flux<DataBuffer> getBody() {
                    return Flux.just(exchange.getResponse().bufferFactory().wrap(bytes));
                }
            };
            // Return the new exchange, which now has the cached body
            return exchange.mutate().request(newRequest).build();
        })
                // 2. flatMap to the *asynchronous* getFormData() call.
                // This call will consume the cached body from our newExchange.
                .flatMap(
                        newExchange -> newExchange.getFormData()
                                // 3. Once form data is parsed, flatMap again to:
                                // a) update the context (sync)
                                // b) call the next link in the chain (async)
                                .flatMap(params -> {
                                    context.getParameters().putAll(params.toSingleValueMap());
                                    Logger.info(
                                            true,
                                            "Request",
                                            "[{}] Form request processed - Path: {}, Params: {}",
                                            getClientIp(newExchange.getRequest()),
                                            newExchange.getRequest().getURI().getPath(),
                                            JsonKit.toJsonString(context.getParameters()));
                                    // Apply the chain using the newExchange
                                    return chain.apply(newExchange);
                                }))
                .doFinally(
                        signalType -> Logger.info(
                                false,
                                "Request",
                                "[{}] Request processed - Path: {}, ExecutionTime: {}ms",
                                context.getX_request_ip(),
                                exchange.getRequest().getURI().getPath(),
                                (System.currentTimeMillis() - context.getTimestamp())))
                .onErrorResume(e -> {
                    Logger.error(
                            false,
                            "Request",
                            "[{}] Failed to process form: {}",
                            context.getX_request_ip(),
                            e.getMessage());
                    return Mono.error(e);
                });
    }

    /**
     * Handles {@code multipart/form-data} requests, typically used for file uploads.
     * <p>
     * This method delegates directly to {@link #processMultipartData(ServerWebExchange, Chain, Context, MultiValueMap)}
     * after using the built-in {@link ServerWebExchange#getMultipartData()} parser.
     * <p>
     * <b>Performance Optimization:</b> Spring WebFlux's getMultipartData() already uses streaming processing for file
     * uploads. File parts are not loaded entirely into memory but are processed as streams. This prevents OOM errors
     * when handling large file uploads.
     * <p>
     * Note: Timeout and retry are handled at VortexHandler level, not here.
     *
     * @param exchange The current server exchange.
     * @param chain    The next strategy in the chain.
     * @param context  The request context.
     * @return A {@code Mono<Void>} that signals the completion of processing.
     */
    private Mono<Void> handleMultipartRequest(ServerWebExchange exchange, Chain chain, Context context) {
        long contentLength = exchange.getRequest().getHeaders().getContentLength();

        if (contentLength > 0) {
            Logger.info(
                    true,
                    "Request",
                    "[{}] Multipart request ({} bytes) - using streaming processing for file uploads",
                    context.getX_request_ip(),
                    contentLength);
        }

        // getMultipartData() is already fully asynchronous and reactive.
        // It uses streaming processing for file parts, avoiding loading entire files into memory.
        return exchange.getMultipartData().flatMap(params -> processMultipartData(exchange, chain, context, params));
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

        // The logic here is synchronous (map iteration and population).
        // Wrap it in fromRunnable to defer execution and catch potential errors.
        return Mono.fromRunnable(() -> {
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

            Logger.info(
                    true,
                    "Request",
                    "[{}] Multipart request processed - Path: {}, Params: {}",
                    context.getX_request_ip(),
                    exchange.getRequest().getURI().getPath(),
                    JsonKit.toJsonString(formMap));
        })
                // After the sync work, apply the rest of the chain.
                .then(chain.apply(exchange))
                .doFinally(
                        signalType -> Logger.info(
                                false,
                                "Request",
                                "[{}] Request processed - Path: {}, ExecutionTime: {}ms",
                                context.getX_request_ip(),
                                exchange.getRequest().getURI().getPath(),
                                (System.currentTimeMillis() - context.getTimestamp())))
                .onErrorResume(e -> {
                    Logger.error(
                            false,
                            "Request",
                            "[{}] Failed to process multipart: {}",
                            context.getX_request_ip(),
                            e.getMessage());
                    return Mono.error(e);
                });
    }

    /**
     * Reads a list of {@link DataBuffer}s into a single byte array, while enforcing a maximum size limit.
     * <p>
     * This is a synchronous, in-memory operation. It is intended to be called from within a {@code Mono.fromCallable}
     * to prevent blocking and to ensure exceptions are captured by the reactive stream.
     * <p>
     * <b>Performance Optimization:</b>
     * <ul>
     * <li>Pre-calculates total size to validate before allocation</li>
     * <li>Efficient single-pass copy algorithm</li>
     * <li>Detailed logging for large bodies to track memory usage</li>
     * </ul>
     * <p>
     * <b>Note:</b> DataBuffer lifecycle is managed by Spring WebFlux framework. When obtained via
     * {@code request.getBody().collectList()}, the framework automatically handles release. No manual release is
     * needed.
     *
     * @param dataBuffers The list of {@link DataBuffer}s to read.
     * @return A byte array containing the merged data from all buffers.
     * @throws ValidateException if the total size of the data buffers exceeds {@link Holder#getMaxRequestSize()}.
     */
    private byte[] readBodyToBytes(List<DataBuffer> dataBuffers) {
        // Calculate total size first to validate before allocating memory
        int totalSize = dataBuffers.stream().mapToInt(DataBuffer::readableByteCount).sum();

        if (totalSize > Holder.getMaxRequestSize()) {
            // DataBuffer will be automatically released by Spring WebFlux framework
            throw new ValidateException(ErrorCode._100530);
        }

        // Single allocation - more efficient than growing arrays
        byte[] bytes = new byte[totalSize];
        int pos = 0;

        // Copy data from each buffer
        for (DataBuffer buffer : dataBuffers) {
            int length = buffer.readableByteCount();
            buffer.read(bytes, pos, length);
            pos += length;
        }

        // DataBuffer lifecycle is managed by Spring WebFlux framework
        // No manual release needed - framework will handle cleanup automatically
        return bytes;
    }

}
