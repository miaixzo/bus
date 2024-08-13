/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
 * 参数过滤/校验
 *
 * @author Justubborn
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PrimaryFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerWebExchange mutate = setDefaultContentTypeIfNecessary(exchange);
        Context context = Context.get(mutate);
        context.setStartTime(System.currentTimeMillis());
        ServerHttpRequest request = mutate.getRequest();
        if (Objects.equals(request.getMethod(), HttpMethod.GET)) {
            MultiValueMap<String, String> params = request.getQueryParams();
            context.setRequestMap(params.toSingleValueMap());
            doParams(mutate);
            return chain.filter(mutate).then(Mono.fromRunnable(() -> Logger.info("traceId:{},exec time :{} ms",
                    mutate.getLogPrefix(), System.currentTimeMillis() - context.getStartTime())));
        } else {
            // 文件上传处理
            if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mutate.getRequest().getHeaders().getContentType())) {
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
                    doParams(mutate);
                    return chain.filter(mutate).doOnTerminate(() -> Logger.info("traceId:{},exec time :{}ms",
                            mutate.getLogPrefix(), System.currentTimeMillis() - context.getStartTime()));
                });

            } else {
                return mutate.getFormData().flatMap(params -> {
                    context.setRequestMap(params.toSingleValueMap());
                    doParams(mutate);
                    return chain.filter(mutate).doOnTerminate(() -> Logger.info("traceId:{},exec time :{}ms",
                            mutate.getLogPrefix(), System.currentTimeMillis() - context.getStartTime()));
                });
            }

        }
    }

    /**
     * 参数校验
     *
     * @param exchange 消息
     */
    private void doParams(ServerWebExchange exchange) {
        Context context = Context.get(exchange);
        Map<String, String> params = context.getRequestMap();

        // 过滤无效参数及值- undefined
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (Normal.UNDEFINED.equals(entry.getKey().toLowerCase())
                    || Normal.UNDEFINED.equals(entry.getValue().toLowerCase())) {
                throw new BusinessException(ErrorCode.EM_100101);
            }
        }

        if (StringKit.isBlank(params.get(Config.METHOD))) {
            throw new BusinessException(ErrorCode.EM_100108);
        }
        if (StringKit.isBlank(params.get(Config.VERSION))) {
            throw new BusinessException(ErrorCode.EM_100107);
        }
        if (StringKit.isBlank(params.get(Config.FORMAT))) {
            throw new BusinessException(ErrorCode.EM_100111);
        }

        if (StringKit.isNotBlank(params.get(Config.SIGN))) {
            context.setNeedDecrypt(true);
        }
        Logger.info("traceId:{},method:{},req =>{}", exchange.getLogPrefix(), params.get(Config.METHOD),
                JsonKit.toJsonString(context.getRequestMap()));
    }

    /**
     * 设置默认值
     *
     * @param exchange 消息
     */
    private ServerWebExchange setDefaultContentTypeIfNecessary(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        MediaType mediaType = request.getHeaders().getContentType();
        if (null == mediaType) {
            mediaType = MediaType.APPLICATION_FORM_URLENCODED;
            HttpHeaders headers = new HttpHeaders();
            headers.putAll(exchange.getRequest().getHeaders());
            headers.setContentType(mediaType);
            // 变异
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
