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
import java.util.stream.Collectors;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Format;
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
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 参数过滤和校验过滤器，负责处理和验证请求参数，设置上下文
 *
 * @author Justubborn
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PrimaryFilter extends AbstractFilter {

    /**
     * 需要拦截的非法请求路径列表
     */
    private static final List<String> BLOCKED_PATHS = Arrays.asList("/favicon.ico", "/robots.txt", "/sitemap.xml",
            "/apple-touch-icon.png", "/apple-touch-icon-precomposed.png",
            "/.well-known/appspecific/com.chrome.devtools.json");

    /**
     * 内部过滤方法，处理请求参数并进行校验
     *
     * @param exchange 当前的 ServerWebExchange 对象
     * @param chain    过滤器链
     * @param context  请求上下文
     * @return {@link Mono<Void>} 表示异步处理完成
     */
    @Override
    protected Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain, Context context) {
        String path = exchange.getRequest().getPath().value();
        // 检查是否为需要拦截的路径
        if (BLOCKED_PATHS.contains(path)) {
            Format.warn(exchange, "BLOCKED_REQUEST", "Blocked request to path: " + path);
            // 抛出异常，避免继续处理
            throw new InternalException(ErrorCode._BLOCKED);
        }
        // 检查是否为浏览器地址遍历攻击
        if (isPathTraversalAttempt(path)) {
            Format.warn(exchange, "PATH_TRAVERSAL_ATTEMPT", "Path traversal attempt detected: " + path);
            // 抛出异常，避免继续处理
            throw new InternalException(ErrorCode._BLOCKED);
        }
        ServerWebExchange mutate = setContentType(exchange);
        context.setStartTime(System.currentTimeMillis());
        ServerHttpRequest request = mutate.getRequest();
        if (Objects.equals(request.getMethod(), HttpMethod.GET)) {
            MultiValueMap<String, String> params = request.getQueryParams();
            context.setRequestMap(params.toSingleValueMap());
            checkParams(mutate);
            Format.info(mutate, "GET_PARAMS_PROCESSED", "Path: " + request.getURI().getPath() + ", Params: "
                    + JsonKit.toJsonString(context.getRequestMap()));
            return chain.filter(mutate)
                    .doOnSuccess(v -> Format.info(mutate, "REQUEST_PROCESSED", "Path: " + request.getURI().getPath()
                            + ", ExecutionTime: " + (System.currentTimeMillis() - context.getStartTime()) + "ms"));
        } else {
            MediaType contentType = mutate.getRequest().getHeaders().getContentType();

            // 通用请求体处理：先读取并缓存原始请求体
            return mutate.getRequest().getBody().collectList().flatMap(dataBuffers -> {
                // 保存原始数据缓冲区
                List<DataBuffer> originalBuffers = new ArrayList<>(dataBuffers);

                // 创建新的请求装饰器，重写getBody()方法以返回原始数据
                ServerHttpRequest newRequest = new ServerHttpRequestDecorator(mutate.getRequest()) {
                    @Override
                    public Flux<DataBuffer> getBody() {
                        return Flux.fromIterable(originalBuffers);
                    }
                };

                // 创建新的交换机，使用新的请求
                ServerWebExchange newExchange = mutate.mutate().request(newRequest).build();

                // 根据内容类型处理请求
                if (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
                    return handleJsonRequest(newExchange, chain, context, originalBuffers);
                } else if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType)) {
                    return handleMultipartRequest(newExchange, chain, context);
                } else {
                    return handleFormRequest(newExchange, chain, context);
                }
            });
        }
    }

    /**
     * 处理JSON请求
     */
    private Mono<Void> handleJsonRequest(ServerWebExchange exchange, WebFilterChain chain, Context context,
            List<DataBuffer> originalBuffers) {
        String jsonBody = originalBuffers.stream().map(dataBuffer -> dataBuffer.toString(Charset.UTF_8))
                .collect(Collectors.joining());
        try {
            Map<String, String> jsonMap = JsonKit.toMap(jsonBody);
            context.setRequestMap(jsonMap);
            checkParams(exchange);
            Format.info(exchange, "JSON_PARAMS_PROCESSED",
                    "Path: " + exchange.getRequest().getURI().getPath() + ", Params: " + JsonKit.toJsonString(jsonMap));
            return chain.filter(exchange).doOnTerminate(
                    () -> Format.info(exchange, "REQUEST_PROCESSED", "Path: " + exchange.getRequest().getURI().getPath()
                            + ", ExecutionTime: " + (System.currentTimeMillis() - context.getStartTime()) + "ms"));
        } catch (Exception e) {
            Format.warn(exchange, "JSON_PARSING_ERROR", "Failed to parse JSON: " + e.getMessage());
            throw new InternalException(ErrorCode._100302);
        }
    }

    /**
     * 处理表单请求
     */
    private Mono<Void> handleFormRequest(ServerWebExchange exchange, WebFilterChain chain, Context context) {
        return exchange.getFormData().flatMap(params -> {
            context.setRequestMap(params.toSingleValueMap());
            checkParams(exchange);
            Format.info(exchange, "FORM_PARAMS_PROCESSED", "Path: " + exchange.getRequest().getURI().getPath()
                    + ", Params: " + JsonKit.toJsonString(context.getRequestMap()));
            return chain.filter(exchange).doOnTerminate(
                    () -> Format.info(exchange, "REQUEST_PROCESSED", "Path: " + exchange.getRequest().getURI().getPath()
                            + ", ExecutionTime: " + (System.currentTimeMillis() - context.getStartTime()) + "ms"));
        });
    }

    /**
     * 处理文件上传请求
     */
    private Mono<Void> handleMultipartRequest(ServerWebExchange exchange, WebFilterChain chain, Context context) {
        return exchange.getMultipartData().flatMap(params -> {
            Map<String, String> formMap = new LinkedHashMap<>();
            Map<String, Part> fileMap = new LinkedHashMap<>();
            Map<String, Part> map = params.toSingleValueMap();
            map.forEach((k, v) -> {
                if (v instanceof FormFieldPart) {
                    formMap.put(k, ((FormFieldPart) v).value());
                }
                if (v instanceof FilePart) {
                    fileMap.put(k, v);
                }
            });
            context.setRequestMap(formMap);
            context.setFilePartMap(fileMap);
            checkParams(exchange);
            Format.info(exchange, "MULTIPART_PARAMS_PROCESSED",
                    "Path: " + exchange.getRequest().getURI().getPath() + ", Params: " + JsonKit.toJsonString(formMap));
            return chain.filter(exchange).doOnTerminate(
                    () -> Format.info(exchange, "REQUEST_PROCESSED", "Path: " + exchange.getRequest().getURI().getPath()
                            + ", ExecutionTime: " + (System.currentTimeMillis() - context.getStartTime()) + "ms"));
        });
    }

    /**
     * 检查是否为路径遍历攻击尝试
     */
    private boolean isPathTraversalAttempt(String path) {
        // 检查路径遍历攻击特征
        return path.contains("../") || path.contains("..\\") || path.contains("%2e%2e%2f") || path.contains("%2e%2e\\")
                || path.contains("..%2f") || path.contains("..%5c");
    }

}