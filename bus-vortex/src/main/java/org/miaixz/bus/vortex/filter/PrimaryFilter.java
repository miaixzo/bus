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
package org.miaixz.bus.vortex.filter;

import java.util.*;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Format;
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
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * 核心前置过滤器，作为请求处理链的入口，负责初步的安全校验、参数解析和上下文初始化。
 * <p>
 * 此过滤器具有最高的执行优先级 ({@code Ordered.HIGHEST_PRECEDENCE})，确保在其他过滤器之前运行。 主要职责包括：
 * <ul>
 * <li><b>安全拦截:</b> 拦截针对如 {@code /favicon.ico} 等常见无效路径的请求，并防御路径遍历攻击。</li>
 * <li><b>请求分发:</b> 根据请求的 HTTP 方法 (GET) 和 Content-Type (JSON, form-data, urlencoded) 将请求分发给相应的处理器。</li>
 * <li><b>参数解析:</b> 从 URL 查询字符串或请求体中异步解析参数，并统一存入 {@link Context} 对象中，供后续过滤器和业务逻辑使用。</li>
 * <li><b>请求体复用:</b> 对于POST等包含请求体的请求，在读取后会通过 {@link ServerHttpRequestDecorator} 将请求体重新包装，以确保下游组件可以再次读取。</li>
 * <li><b>自动重试:</b> 内置了对请求体解析的重试机制，以增强系统在处理网络波动时的健壮性。</li>
 * </ul>
 *
 * @author Justubborn
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PrimaryFilter extends AbstractFilter {

    /**
     * 定义需要直接拦截并阻止访问的非法或无效请求路径列表。
     */
    private static final List<String> BLOCKED_PATHS = Arrays.asList(
            "/favicon.ico",
            "/robots.txt",
            "/sitemap.xml",
            "/apple-touch-icon.png",
            "/apple-touch-icon-precomposed.png",
            "/.well-known/appspecific/com.chrome.devtools.json");

    /**
     * 在处理请求体时，允许的最大自动重试次数。
     */
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * 自动重试之间的基础延迟时间（单位：毫秒）。
     */
    private static final long RETRY_DELAY_MS = 1000;

    /**
     * 过滤器的核心执行方法，负责对请求进行初步安全检查和分发处理。
     * <p>
     * 该方法首先执行路径安全检查，包括黑名单路径过滤和路径遍历攻击检测。 检查通过后，根据请求类型（GET 或其他方法及 Content-Type）将 {@link ServerWebExchange} 传递给相应的处理方法
     * ({@code handleGetRequest}, {@code handleJsonRequest} 等)。
     * </p>
     *
     * @param exchange 当前的 ServerWebExchange 对象
     * @param chain    过滤器链
     * @param context  用于在整个请求生命周期内传递数据的上下文对象
     * @return {@link Mono<Void>} 表示异步过滤操作的完成
     * @throws ValidateException 如果请求路径被阻止或检测到路径遍历攻击
     */
    @Override
    protected Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain, Context context) {
        String path = exchange.getRequest().getPath().value();
        // 检查是否为黑名单路径，如 favicon.ico 等，避免无效处理。
        if (BLOCKED_PATHS.contains(path)) {
            Format.warn(exchange, "BLOCKED_REQUEST", "Blocked request to path: " + path);
            // 抛出异常，避免继续处理
            throw new ValidateException(ErrorCode._BLOCKED);
        }
        // 检查是否存在路径遍历攻击的尝试，增强系统安全性。
        if (isPathTraversalAttempt(path)) {
            Format.warn(exchange, "PATH_TRAVERSAL_ATTEMPT", "Path traversal attempt detected: " + path);
            // 抛出异常，避免继续处理
            throw new ValidateException(ErrorCode._LIMITER);
        }

        ServerWebExchange mutate = setContentType(exchange);
        context.setTimestamp(DateKit.current());
        ServerHttpRequest request = mutate.getRequest();

        // 根据HTTP方法和Content-Type分发到不同的处理器
        if (Objects.equals(request.getMethod(), HttpMethod.GET)) {
            return handleGetRequest(mutate, chain, context);
        } else {
            MediaType contentType = mutate.getRequest().getHeaders().getContentType();
            if (contentType == null) {
                // 如果没有Content-Type，默认按表单处理
                return handleFormRequest(mutate, chain, context);
            } else if (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
                return handleJsonRequest(mutate, chain, context);
            } else if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType)) {
                // multipart 请求有专门的处理方式，不预先读取请求体
                return handleMultipartRequest(mutate, chain, context);
            } else if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType)) {
                return handleFormRequest(mutate, chain, context);
            }
            // 对于其他未知的Content-Type，尝试作为通用表单处理
            return handleFormRequest(mutate, chain, context);
        }
    }

    /**
     * 处理 GET 请求。
     * <p>
     * 直接从 URL 查询参数中提取数据，存入上下文，然后继续执行过滤器链。
     * </p>
     *
     * @param exchange ServerWebExchange 对象
     * @param chain    过滤器链
     * @param context  请求上下文
     * @return {@link Mono<Void>} 表示异步处理的完成
     */
    private Mono<Void> handleGetRequest(ServerWebExchange exchange, WebFilterChain chain, Context context) {
        MultiValueMap<String, String> params = exchange.getRequest().getQueryParams();
        context.setRequestMap(params.toSingleValueMap());
        this.validate(exchange);
        Format.info(
                exchange,
                "GET_PARAMS_PROCESSED",
                "Path: " + exchange.getRequest().getURI().getPath() + ", Params: "
                        + JsonKit.toJsonString(context.getRequestMap()));

        return chain.filter(exchange).doOnSuccess(
                v -> Format.info(
                        exchange,
                        "REQUEST_PROCESSED",
                        "Path: " + exchange.getRequest().getURI().getPath() + ", ExecutionTime: "
                                + (System.currentTimeMillis() - context.getTimestamp()) + "ms"));
    }

    /**
     * 处理 Content-Type 为 application/json 的请求。
     * <p>
     * 此方法会异步地将请求体中的 JSON 数据完全读取、解析，并将结果存入上下文。 为了应对可能的瞬时网络或解析错误，该过程被包裹在一个带退避策略的重试机制中。
     * </p>
     *
     * @param exchange ServerWebExchange 对象
     * @param chain    过滤器链
     * @param context  请求上下文
     * @return {@link Mono<Void>} 表示异步处理的完成
     */
    private Mono<Void> handleJsonRequest(ServerWebExchange exchange, WebFilterChain chain, Context context) {
        // 异步收集请求体中的所有数据片段
        return exchange.getRequest().getBody().collectList()
                .flatMap(dataBuffers -> processJsonData(exchange, chain, context, dataBuffers)).retryWhen(
                        Retry.backoff(MAX_RETRY_ATTEMPTS, java.time.Duration.ofMillis(RETRY_DELAY_MS))
                                .maxBackoff(java.time.Duration.ofMillis(500)).jitter(0.75)
                                .doBeforeRetry(retrySignal -> {
                                    // 记录每次重试
                                    Format.warn(
                                            exchange,
                                            "JSON_RETRY",
                                            "Retrying JSON request processing, attempt: "
                                                    + (retrySignal.totalRetries() + 1) + ", error: "
                                                    + retrySignal.failure().getMessage());
                                }).onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                    // 重试耗尽后记录严重错误并抛出异常
                                    Format.error(
                                            exchange,
                                            "JSON_RETRY_EXHAUSTED",
                                            "JSON request processing failed after " + MAX_RETRY_ATTEMPTS
                                                    + " attempts, error: " + retrySignal.failure().getMessage());
                                    return new InternalException(ErrorCode._116000);
                                }));
    }

    /**
     * 实际处理和解析 JSON 请求体的核心逻辑。
     * <p>
     * 此方法负责将分片的 {@link DataBuffer} 合并成完整的字节数组，然后解析为 JSON 格式的 Map。 <b>关键操作:</b> 由于请求体是一个只能消费一次的流，在读取后，该方法会创建一个
     * {@link ServerHttpRequestDecorator}，将已读取的字节数据重新包装成一个新的 {@code Flux<DataBuffer>}
     * 并放入新的请求对象中。这确保了下游的过滤器或控制器依然可以访问原始请求体。
     * </p>
     *
     * @param exchange    ServerWebExchange 对象
     * @param chain       过滤器链
     * @param context     请求上下文
     * @param dataBuffers 从请求体中收集到的数据缓冲区分片列表
     * @return {@link Mono<Void>} 表示异步处理的完成
     */
    private Mono<Void> processJsonData(
            ServerWebExchange exchange,
            WebFilterChain chain,
            Context context,
            List<DataBuffer> dataBuffers) {
        try {
            // 将所有数据缓冲区合并成一个字节数组
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

            // 创建请求装饰器，重写getBody方法，以便下游可以重复消费请求体
            ServerHttpRequest newRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {

                @Override
                public Flux<DataBuffer> getBody() {
                    DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
                    return Flux.just(bufferFactory.wrap(bytes));
                }
            };

            // 使用装饰后的新请求构建新的ServerWebExchange
            ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();

            this.validate(newExchange);
            Format.info(
                    newExchange,
                    "JSON_PARAMS_PROCESSED",
                    "Path: " + newExchange.getRequest().getURI().getPath() + ", Params: "
                            + JsonKit.toJsonString(jsonMap));
            return chain.filter(newExchange).doOnTerminate(
                    () -> Format.info(
                            newExchange,
                            "REQUEST_PROCESSED",
                            "Path: " + newExchange.getRequest().getURI().getPath() + ", ExecutionTime: "
                                    + (System.currentTimeMillis() - context.getTimestamp()) + "ms"));
        } catch (Exception e) {
            Format.error(exchange, "JSON_PROCESSING_ERROR", "Failed to process JSON: " + e.getMessage());
            return Mono.error(e); // 将同步异常转换为异步错误
        }
    }

    /**
     * 处理 application/x-www-form-urlencoded 或无 Content-Type 的表单请求。
     * <p>
     * 此方法与处理JSON请求的逻辑类似，同样包含了请求体读取、复用和重试机制。
     * </p>
     *
     * @param exchange ServerWebExchange 对象
     * @param chain    过滤器链
     * @param context  请求上下文
     * @return {@link Mono<Void>} 表示异步处理的完成
     */
    private Mono<Void> handleFormRequest(ServerWebExchange exchange, WebFilterChain chain, Context context) {
        return exchange.getRequest().getBody().collectList()
                .flatMap(dataBuffers -> processFormData(exchange, chain, context, dataBuffers)).retryWhen(
                        Retry.backoff(MAX_RETRY_ATTEMPTS, java.time.Duration.ofMillis(RETRY_DELAY_MS))
                                .maxBackoff(java.time.Duration.ofMillis(500)).jitter(0.75)
                                .doBeforeRetry(retrySignal -> {
                                    Format.warn(
                                            exchange,
                                            "FORM_RETRY",
                                            "Retrying form request processing, attempt: "
                                                    + (retrySignal.totalRetries() + 1) + ", error: "
                                                    + retrySignal.failure().getMessage());
                                }).onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                    Format.error(
                                            exchange,
                                            "FORM_RETRY_EXHAUSTED",
                                            "Form request processing failed after " + MAX_RETRY_ATTEMPTS
                                                    + " attempts, error: " + retrySignal.failure().getMessage());
                                    return new InternalException(ErrorCode._116000);
                                }));
    }

    /**
     * 实际处理和解析表单请求体的核心逻辑。
     * <p>
     * 此方法同样先将请求体数据缓存，并通过 {@link ServerHttpRequestDecorator} 包装以支持下游消费。 随后，它利用 Spring 框架的 {@code getFormData()}
     * 方法从可重复读的请求体中解析出表单参数。
     * </p>
     *
     * @param exchange    ServerWebExchange 对象
     * @param chain       过滤器链
     * @param context     请求上下文
     * @param dataBuffers 从请求体中收集到的数据缓冲区分片列表
     * @return {@link Mono<Void>} 表示异步处理的完成
     */
    private Mono<Void> processFormData(
            ServerWebExchange exchange,
            WebFilterChain chain,
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

            // 同样，创建请求装饰器以支持请求体的重复读取
            ServerHttpRequest newRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {

                @Override
                public Flux<DataBuffer> getBody() {
                    DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
                    return Flux.just(bufferFactory.wrap(bytes));
                }
            };

            ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();

            // 从可重复读的请求中解析表单数据
            return newExchange.getFormData().flatMap(params -> {
                context.setRequestMap(params.toSingleValueMap());
                this.validate(newExchange);
                Format.info(
                        newExchange,
                        "FORM_PARAMS_PROCESSED",
                        "Path: " + newExchange.getRequest().getURI().getPath() + ", Params: "
                                + JsonKit.toJsonString(context.getRequestMap()));
                return chain.filter(newExchange).doOnTerminate(
                        () -> Format.info(
                                newExchange,
                                "REQUEST_PROCESSED",
                                "Path: " + newExchange.getRequest().getURI().getPath() + ", ExecutionTime: "
                                        + (System.currentTimeMillis() - context.getTimestamp()) + "ms"));
            });
        } catch (Exception e) {
            Format.error(exchange, "FORM_PROCESSING_ERROR", "Failed to process form: " + e.getMessage());
            return Mono.error(e);
        }
    }

    /**
     * 处理 multipart/form-data 类型的请求，通常用于文件上传。
     * <p>
     * 对于此类请求，Spring WebFlux 提供了直接解析的方法 {@code getMultipartData()}，无需手动处理请求体流。 解析过程同样受重试机制保护。
     * </p>
     *
     * @param exchange ServerWebExchange 对象
     * @param chain    过滤器链
     * @param context  请求上下文
     * @return {@link Mono<Void>} 表示异步处理的完成
     */
    private Mono<Void> handleMultipartRequest(ServerWebExchange exchange, WebFilterChain chain, Context context) {
        return exchange.getMultipartData().flatMap(params -> processMultipartData(exchange, chain, context, params))
                .retryWhen(
                        Retry.backoff(MAX_RETRY_ATTEMPTS, java.time.Duration.ofMillis(RETRY_DELAY_MS))
                                .maxBackoff(java.time.Duration.ofMillis(500)).jitter(0.75)
                                .doBeforeRetry(retrySignal -> {
                                    Format.warn(
                                            exchange,
                                            "MULTIPART_RETRY",
                                            "Retrying multipart request processing, attempt: "
                                                    + (retrySignal.totalRetries() + 1) + ", error: "
                                                    + retrySignal.failure().getMessage());
                                }).onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                    Format.error(
                                            exchange,
                                            "MULTIPART_RETRY_EXHAUSTED",
                                            "Multipart request processing failed after " + MAX_RETRY_ATTEMPTS
                                                    + " attempts, error: " + retrySignal.failure().getMessage());

                                    // 针对特定的边界错误，返回更明确的错误码
                                    if (retrySignal.failure().getMessage() != null && retrySignal.failure().getMessage()
                                            .contains("Could not find first boundary")) {
                                        return new InternalException(ErrorCode._100303);
                                    }
                                    return new InternalException(ErrorCode._116000);
                                }));
    }

    /**
     * 实际处理和解析 multipart/form-data 数据的核心逻辑。
     * <p>
     * 此方法遍历解析后的各个部分（Part），将表单字段和文件部分分别存入上下文的 {@code requestMap} 和 {@code filePartMap} 中，以供后续业务逻辑使用。
     * </p>
     *
     * @param exchange ServerWebExchange 对象
     * @param chain    过滤器链
     * @param context  请求上下文
     * @param params   包含表单字段和文件部分的 MultiValueMap
     * @return {@link Mono<Void>} 表示异步处理的完成
     */
    private Mono<Void> processMultipartData(
            ServerWebExchange exchange,
            WebFilterChain chain,
            Context context,
            MultiValueMap<String, Part> params) {
        try {
            Map<String, String> formMap = new LinkedHashMap<>();
            Map<String, Part> fileMap = new LinkedHashMap<>();

            // 遍历所有part，区分表单字段和文件
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

            Format.info(
                    exchange,
                    "MULTIPART_PARAMS_PROCESSED",
                    "Path: " + exchange.getRequest().getURI().getPath() + ", Params: " + JsonKit.toJsonString(formMap));

            return chain.filter(exchange).doOnTerminate(
                    () -> Format.info(
                            exchange,
                            "REQUEST_PROCESSED",
                            "Path: " + exchange.getRequest().getURI().getPath() + ", ExecutionTime: "
                                    + (System.currentTimeMillis() - context.getTimestamp()) + "ms"));
        } catch (Exception e) {
            Format.error(exchange, "MULTIPART_PROCESSING_ERROR", "Failed to process multipart: " + e.getMessage());
            return Mono.error(e);
        }
    }

    /**
     * 检查给定的URL路径是否包含路径遍历攻击（目录遍历）的特征。
     * <p>
     * 路径遍历攻击是一种常见的安全漏洞，攻击者试图通过操纵带有 ".." 的文件路径来访问受限制的目录。 此方法会检查多种常见的遍历序列及其URL编码形式。
     * </p>
     *
     * @param path 要检查的URL路径字符串
     * @return 如果检测到遍历尝试，返回 {@code true}，否则返回 {@code false}
     */
    private boolean isPathTraversalAttempt(String path) {
        // 检查多种路径遍历攻击的特征，包括明文和URL编码形式
        return path.contains("../") || path.contains("..\\") || path.contains("%2e%2e%2f") || path.contains("%2e%2e\\")
                || path.contains("..%2f") || path.contains("..%5c");
    }

}
