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
package org.miaixz.bus.goalie.filter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.BusinessException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.goalie.Config;
import org.miaixz.bus.goalie.Context;
import org.miaixz.bus.logger.Logger;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

/**
 * 参数过滤和校验过滤器，负责处理和验证请求参数，设置上下文
 *
 * @author Justubborn
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PrimaryFilter implements WebFilter {

    /**
     * 过滤器主逻辑，处理请求参数并进行校验
     *
     * @param exchange 当前的 ServerWebExchange 对象，包含请求和响应
     * @param chain    过滤器链，用于继续处理请求
     * @return {@link Mono<Void>} 表示异步处理完成
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 设置默认 Content-Type（如果缺失）
        ServerWebExchange mutate = setDefaultContentTypeIfNecessary(exchange);
        Context context = Context.get(mutate);
        // 记录请求开始时间
        context.setStartTime(System.currentTimeMillis());
        ServerHttpRequest request = mutate.getRequest();

        // 处理 GET 请求
        if (Objects.equals(request.getMethod(), HttpMethod.GET)) {
            MultiValueMap<String, String> params = request.getQueryParams();
            context.setRequestMap(params.toSingleValueMap());
            doParams(mutate); // 校验参数
            return chain.filter(mutate).then(Mono.fromRunnable(() -> Logger.info("traceId:{},exec time :{} ms",
                    mutate.getLogPrefix(), System.currentTimeMillis() - context.getStartTime())));
        } else {
            // 处理文件上传（multipart/form-data）
            if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mutate.getRequest().getHeaders().getContentType())) {
                return mutate.getMultipartData().flatMap(params -> {
                    Map<String, String> formMap = new LinkedHashMap<>();
                    Map<String, Part> fileMap = new LinkedHashMap<>();

                    // 分离表单参数和文件参数
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
                    doParams(mutate); // 校验参数
                    return chain.filter(mutate).doOnTerminate(() -> Logger.info("traceId:{},exec time :{}ms",
                            mutate.getLogPrefix(), System.currentTimeMillis() - context.getStartTime()));
                });
            } else {
                // 处理普通表单数据
                return mutate.getFormData().flatMap(params -> {
                    context.setRequestMap(params.toSingleValueMap());
                    doParams(mutate); // 校验参数
                    return chain.filter(mutate).doOnTerminate(() -> Logger.info("traceId:{},exec time :{}ms",
                            mutate.getLogPrefix(), System.currentTimeMillis() - context.getStartTime()));
                });
            }
        }
    }

    /**
     * 校验请求参数，确保必要参数存在且有效
     *
     * @param exchange ServerWebExchange 对象
     * @throws BusinessException 如果参数无效或缺失，抛出异常
     */
    private void doParams(ServerWebExchange exchange) {
        Context context = Context.get(exchange);
        Map<String, String> params = context.getRequestMap();

        // 过滤无效参数（键或值为 "undefined"）
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (Normal.UNDEFINED.equals(entry.getKey().toLowerCase())
                    || Normal.UNDEFINED.equals(entry.getValue().toLowerCase())) {
                throw new BusinessException(ErrorCode._100101);
            }
        }

        // 校验必要参数
        if (StringKit.isBlank(params.get(Config.METHOD))) {
            throw new BusinessException(ErrorCode._100108);
        }
        if (StringKit.isBlank(params.get(Config.VERSION))) {
            throw new BusinessException(ErrorCode._100107);
        }
        if (StringKit.isBlank(params.get(Config.FORMAT))) {
            throw new BusinessException(ErrorCode._100111);
        }

        // 如果存在签名参数，标记需要解密
        if (StringKit.isNotBlank(params.get(Config.SIGN))) {
            context.setNeedDecrypt(true);
        }

        // 记录请求参数日志
        Logger.info("traceId:{},method:{},req =>{}", exchange.getLogPrefix(), params.get(Config.METHOD),
                JsonKit.toJsonString(context.getRequestMap()));
    }

    /**
     * 设置默认 Content-Type（如果请求头缺失）
     *
     * @param exchange ServerWebExchange 对象
     * @return 更新后的 ServerWebExchange
     */
    private ServerWebExchange setDefaultContentTypeIfNecessary(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        MediaType mediaType = request.getHeaders().getContentType();
        if (null == mediaType) {
            // 默认设置为 application/x-www-form-urlencoded
            mediaType = MediaType.APPLICATION_FORM_URLENCODED;
            HttpHeaders headers = new HttpHeaders();
            headers.putAll(exchange.getRequest().getHeaders());
            headers.setContentType(mediaType);
            // 创建装饰器以更新请求头
            ServerHttpRequest requestDecorator = new ServerHttpRequestDecorator(request) {
                @Override
                public HttpHeaders getHeaders() {
                    return headers;
                }
            };
            return exchange.mutate().request(requestDecorator).build();
        }
        return exchange;
    }

}