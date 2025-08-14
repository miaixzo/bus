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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
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
        if (isBlockedPath(path)) {
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
            if (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
                return mutate.getRequest().getBody().collectList().flatMap(dataBuffers -> {
                    String jsonBody = dataBuffers.stream().map(dataBuffer -> dataBuffer.toString(Charset.UTF_8))
                            .collect(Collectors.joining());
                    try {
                        Map<String, String> jsonMap = JsonKit.toMap(jsonBody);
                        context.setRequestMap(jsonMap);
                        checkParams(mutate);
                        Format.info(mutate, "JSON_PARAMS_PROCESSED",
                                "Path: " + request.getURI().getPath() + ", Params: " + JsonKit.toJsonString(jsonMap));
                        return chain.filter(mutate)
                                .doOnTerminate(() -> Format.info(mutate, "REQUEST_PROCESSED",
                                        "Path: " + request.getURI().getPath() + ", ExecutionTime: "
                                                + (System.currentTimeMillis() - context.getStartTime()) + "ms"));
                    } catch (Exception e) {
                        Format.warn(mutate, "JSON_PARSING_ERROR", "Failed to parse JSON: " + e.getMessage());
                        throw new InternalException(ErrorCode._100302);
                    }
                });
            } else if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType)) {
                return mutate.getMultipartData().flatMap(params -> {
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
                    checkParams(mutate);
                    Format.info(mutate, "MULTIPART_PARAMS_PROCESSED",
                            "Path: " + request.getURI().getPath() + ", Params: " + JsonKit.toJsonString(formMap));
                    return chain.filter(mutate)
                            .doOnTerminate(() -> Format.info(mutate, "REQUEST_PROCESSED",
                                    "Path: " + request.getURI().getPath() + ", ExecutionTime: "
                                            + (System.currentTimeMillis() - context.getStartTime()) + "ms"));
                });
            } else {
                return mutate.getFormData().flatMap(params -> {
                    context.setRequestMap(params.toSingleValueMap());
                    checkParams(mutate);
                    Format.info(mutate, "FORM_PARAMS_PROCESSED", "Path: " + request.getURI().getPath() + ", Params: "
                            + JsonKit.toJsonString(context.getRequestMap()));
                    return chain.filter(mutate)
                            .doOnTerminate(() -> Format.info(mutate, "REQUEST_PROCESSED",
                                    "Path: " + request.getURI().getPath() + ", ExecutionTime: "
                                            + (System.currentTimeMillis() - context.getStartTime()) + "ms"));
                });
            }
        }
    }

    /**
     * 检查路径是否在拦截列表中
     */
    private boolean isBlockedPath(String path) {
        return BLOCKED_PATHS.contains(path);
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