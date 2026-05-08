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
package org.miaixz.bus.vortex.strategy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Holder;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.core.Order;
import org.springframework.core.io.buffer.DataBuffer;
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
 * Basic request strategy for routes without protocol-specific request parsing.
 * <p>
 * This class initializes request metadata and provides protected parsing helpers for protocol request strategies.
 * Protocol-specific classes decide whether to parse and cache the body or pass the request through untouched.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@org.springframework.core.annotation.Order(Order.FIRST)
public class RequestStrategy extends AbstractStrategy {

    /**
     * Creates a request strategy.
     */
    public RequestStrategy() {

    }

    /**
     * Initializes common request metadata and continues without parsing the body.
     *
     * @param exchange The current server exchange.
     * @param chain    The next strategy in the chain.
     * @return A {@code Mono<Void>} that signals the completion of this strategy.
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, Chain chain) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            return chain.apply(prepare(exchange, context, false));
        });
    }

    /**
     * Initializes context request metadata and optionally supplies a default content type.
     *
     * @param exchange           current exchange
     * @param context            request context
     * @param defaultContentType whether missing content type should be defaulted for body parsing
     * @return prepared exchange
     */
    protected ServerWebExchange prepare(ServerWebExchange exchange, Context context, boolean defaultContentType) {
        context.setX_request_ipv4(this.getClientIp(exchange.getRequest()));
        context.setQuery(exchange.getRequest().getQueryParams().toSingleValueMap());

        ServerWebExchange mutate = defaultContentType ? setContentType(exchange) : exchange;
        ServerHttpRequest request = mutate.getRequest();
        Logger.debug(
                true,
                "Vortex",
                "Request headers captured: strategy=request, clientIp={}, path={}, headerCount={}, contentType={}",
                context.getX_request_ip(),
                request.getURI().getPath(),
                request.getHeaders().size(),
                request.getHeaders().getContentType());
        Logger.debug(
                true,
                "Vortex",
                "Request header snapshot: strategy=request, clientIp={}, path={}",
                context.getX_request_ip(),
                request.getURI().getPath());
        Logger.debug(
                true,
                "Vortex",
                "Request headers: strategy=request, clientIp={}, headers={}",
                context.getX_request_ip(),
                request.getHeaders().toSingleValueMap());
        return mutate;
    }

    /**
     * Parses a REST-like request into {@link Context#getParameters()}.
     *
     * @param exchange current exchange
     * @param chain    remaining chain
     * @param context  request context
     * @return parsing completion signal
     */
    protected Mono<Void> parse(ServerWebExchange exchange, Chain chain, Context context) {
        ServerHttpRequest request = exchange.getRequest();
        if (context.getHttpMethod() == HTTP.Method.GET) {
            return handleGetRequest(exchange, chain, context);
        }
        MediaType contentType = request.getHeaders().getContentType();
        if (contentType == null) {
            return handleFormRequest(exchange, chain, context);
        } else if (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
            return handleJsonRequest(exchange, chain, context);
        } else if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType)) {
            long contentLength = request.getHeaders().getContentLength();
            if (contentLength > Holder.getMaxMultipartRequestSize()) {
                throw new ValidateException(ErrorCode._100530);
            }
            return handleMultipartRequest(exchange, chain, context);
        } else if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType)) {
            return handleFormRequest(exchange, chain, context);
        }
        return handleFormRequest(exchange, chain, context);
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
        return Mono.fromRunnable(() -> {
            context.getParameters().putAll(context.getQuery());
            Logger.debug(
                    true,
                    "Vortex",
                    "GET request parameter snapshot: strategy=request, clientIp={}, path={}",
                    context.getX_request_ip(),
                    exchange.getRequest().getURI().getPath());
            Logger.debug(
                    true,
                    "Vortex",
                    "Request parameters: strategy=request, clientIp={}, parameters={}",
                    context.getX_request_ip(),
                    context.getParameters());
            Logger.info(
                    true,
                    "Vortex",
                    "GET request parameters processed: strategy=request, clientIp={}, path={}, parameterCount={}",
                    context.getX_request_ip(),
                    exchange.getRequest().getURI().getPath(),
                    context.getParameters().size());
        }).then(chain.apply(exchange)).doFinally(
                signalType -> Logger.info(
                        false,
                        "Vortex",
                        "Request processing completed: strategy=request, clientIp={}, path={}, executionTimeMs={}",
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
        long contentLength = exchange.getRequest().getHeaders().getContentLength();
        boolean shouldStream = contentLength > Holder.getStreamingRequestThreshold();

        if (shouldStream) {
            Logger.info(
                    true,
                    "Vortex",
                    "Large JSON request detected: strategy=request, clientIp={}, bytes={}, mode=streaming",
                    context.getX_request_ip(),
                    contentLength);
        }

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

        return Mono.fromCallable(() -> {
            byte[] bytes = readBodyToBytes(dataBuffers);

            if (bytes.length > Holder.getStreamingRequestThreshold()) {
                Logger.info(
                        true,
                        "Vortex",
                        "Large JSON content cached in memory: strategy=request, clientIp={}, bytes={}",
                        context.getX_request_ip(),
                        bytes.length);
            }

            String jsonBody = new String(bytes, Charset.UTF_8);
            Map<String, Object> jsonMap = JsonKit.toMap(jsonBody);
            context.getParameters().putAll(jsonMap);
            Logger.debug(
                    true,
                    "Vortex",
                    "JSON request parameter snapshot: strategy=request, clientIp={}, path={}, bytes={}",
                    context.getX_request_ip(),
                    exchange.getRequest().getURI().getPath(),
                    bytes.length);
            Logger.debug(
                    true,
                    "Vortex",
                    "Request parameters: strategy=request, clientIp={}, parameters={}",
                    context.getX_request_ip(),
                    context.getParameters());

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
                    "Vortex",
                    "JSON request processed: strategy=request, clientIp={}, path={}, parameterCount={}",
                    context.getX_request_ip(),
                    exchange.getRequest().getURI().getPath(),
                    context.getParameters().size());

            return exchange.mutate().request(newRequest).build();
        }).flatMap(chain::apply).doFinally(
                signalType -> Logger.info(
                        false,
                        "Vortex",
                        "Request processing completed: strategy=request, clientIp={}, path={}, executionTimeMs={}",
                        context.getX_request_ip(),
                        exchange.getRequest().getURI().getPath(),
                        (System.currentTimeMillis() - context.getTimestamp())))
                .onErrorResume(e -> {
                    Logger.error(
                            false,
                            "Vortex",
                            e,
                            "JSON request processing failed: strategy=request, clientIp={}, exception={}",
                            context.getX_request_ip(),
                            e.getClass().getSimpleName());
                    return Mono.error(e);
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

        return Mono.fromCallable(() -> {
            byte[] bytes = readBodyToBytes(dataBuffers);

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
            return exchange.mutate().request(newRequest).build();
        }).flatMap(newExchange -> newExchange.getFormData().flatMap(params -> {
            context.getParameters().putAll(params.toSingleValueMap());
            Logger.debug(
                    true,
                    "Vortex",
                    "Form request parameter snapshot: strategy=request, clientIp={}, path={}",
                    getClientIp(newExchange.getRequest()),
                    newExchange.getRequest().getURI().getPath());
            Logger.debug(
                    true,
                    "Vortex",
                    "Request parameters: strategy=request, clientIp={}, parameters={}",
                    context.getX_request_ip(),
                    context.getParameters());
            Logger.info(
                    true,
                    "Vortex",
                    "Form request processed: strategy=request, clientIp={}, path={}, parameterCount={}",
                    getClientIp(newExchange.getRequest()),
                    newExchange.getRequest().getURI().getPath(),
                    context.getParameters().size());
            return chain.apply(newExchange);
        })).doFinally(
                signalType -> Logger.info(
                        false,
                        "Vortex",
                        "Request processing completed: strategy=request, clientIp={}, path={}, executionTimeMs={}",
                        context.getX_request_ip(),
                        exchange.getRequest().getURI().getPath(),
                        (System.currentTimeMillis() - context.getTimestamp())))
                .onErrorResume(e -> {
                    Logger.error(
                            false,
                            "Vortex",
                            e,
                            "Form request processing failed: strategy=request, clientIp={}, exception={}",
                            context.getX_request_ip(),
                            e.getClass().getSimpleName());
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
                    "Vortex",
                    "Multipart request detected: strategy=request, clientIp={}, bytes={}, mode=streaming",
                    context.getX_request_ip(),
                    contentLength);
        }

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
            Logger.debug(
                    true,
                    "Vortex",
                    "Multipart request parameter snapshot: strategy=request, clientIp={}, path={}",
                    context.getX_request_ip(),
                    exchange.getRequest().getURI().getPath());
            Logger.debug(
                    true,
                    "Vortex",
                    "Request parameters: strategy=request, clientIp={}, parameters={}",
                    context.getX_request_ip(),
                    context.getParameters());
            Logger.debug(
                    true,
                    "Vortex",
                    "Request file fields: strategy=request, clientIp={}, fileFields={}",
                    context.getX_request_ip(),
                    fileMap.keySet());

            Logger.info(
                    true,
                    "Vortex",
                    "Multipart request processed: strategy=request, clientIp={}, path={}, parameterCount={}",
                    context.getX_request_ip(),
                    exchange.getRequest().getURI().getPath(),
                    context.getParameters().size());
        }).then(chain.apply(exchange)).doFinally(
                signalType -> Logger.info(
                        false,
                        "Vortex",
                        "Request processing completed: strategy=request, clientIp={}, path={}, executionTimeMs={}",
                        context.getX_request_ip(),
                        exchange.getRequest().getURI().getPath(),
                        (System.currentTimeMillis() - context.getTimestamp())))
                .onErrorResume(e -> {
                    Logger.error(
                            false,
                            "Vortex",
                            e,
                            "Multipart request processing failed: strategy=request, clientIp={}, exception={}",
                            context.getX_request_ip(),
                            e.getClass().getSimpleName());
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
        int totalSize = dataBuffers.stream().mapToInt(DataBuffer::readableByteCount).sum();

        if (totalSize > Holder.getMaxRequestSize()) {
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
