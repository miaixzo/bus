/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.starter.goalie.filter;

import org.aoju.bus.base.consts.ErrorCode;
import org.aoju.bus.core.lang.exception.BusinessException;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.goalie.Consts;
import org.aoju.bus.goalie.Context;
import org.aoju.bus.starter.goalie.GoalieConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 参数过滤/校验
 *
 * @author Justubborn
 * @version 6.1.5
 * @since JDK 1.8+
 */
@Component
@ConditionalOnBean(GoalieConfiguration.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PrimaryFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (Objects.equals(request.getMethod(), HttpMethod.GET)) {
            MultiValueMap<String, String> params = request.getQueryParams();
            Context.get(exchange).setRequestMap(params.toSingleValueMap());
            doParams(exchange);
            return chain.filter(exchange);
        } else {
            MediaType mediaType = request.getHeaders().getContentType();
            if (null == mediaType) {
                mediaType = MediaType.APPLICATION_FORM_URLENCODED;
            }
            String contentType = mediaType.toString().toLowerCase();
            //文件
            if (contentType.contains(MediaType.MULTIPART_FORM_DATA_VALUE)) {
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
                    Context.get(exchange).setRequestMap(formMap);
                    Context.get(exchange).setFilePartMap(fileMap);
                    doParams(exchange);
                    return chain.filter(exchange);
                });

            } else {
                return exchange.getFormData().flatMap(params -> {
                    Context.get(exchange).setRequestMap(params.toSingleValueMap());
                    doParams(exchange);
                    return chain.filter(exchange);
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

        if (StringKit.isBlank(params.get(Consts.METHOD))) {
            throw new BusinessException(ErrorCode.EM_100108);
        }
        if (StringKit.isBlank(params.get(Consts.VERSION))) {
            throw new BusinessException(ErrorCode.EM_100107);
        }
        String format = params.get(Consts.FORMAT);
        if (StringKit.isBlank(format)) {
            throw new BusinessException(ErrorCode.EM_100111);
        }
        context.setFormat(Context.Format.valueOf(format));
    }

}
