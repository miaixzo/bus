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

import java.util.Map;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.BusinessException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.vortex.*;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 抽象过滤器基类，提供公共方法和模板方法模式 所有具体过滤器继承此类，实现 doFilterInternal 方法
 *
 * @author Justubborn
 * @since Java 17+
 */
public abstract class AbstractFilter implements Filter {

    /**
     * 过滤器主逻辑，获取上下文并调用子类的内部过滤方法
     *
     * @param exchange 当前的 ServerWebExchange 对象，包含请求和响应
     * @param chain    过滤器链，用于继续处理请求
     * @return {@link Mono<Void>} 表示异步处理完成
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Format.info(exchange, "FILTER_ENTER", "Entering filter: " + this.getClass().getSimpleName());
        try {
            return doFilter(exchange, chain, getContext(exchange)).doOnTerminate(
                    () -> Format.debug(exchange, "FILTER_EXIT", "Exiting filter: " + this.getClass().getSimpleName()))
                    .doOnError(e -> Format.error(exchange, "FILTER_ERROR",
                            "Error in " + this.getClass().getSimpleName() + ": " + e.getMessage()));
        } catch (Exception e) {
            Format.error(exchange, "FILTER_FAILED",
                    "Failed in " + this.getClass().getSimpleName() + ": " + e.getMessage());
            return Mono.error(e instanceof BusinessException ? e : new BusinessException("Filter execution failed", e));
        }
    }

    /**
     * 内部过滤方法，由子类实现具体逻辑
     *
     * @param exchange 当前的 ServerWebExchange 对象
     * @param chain    过滤器链
     * @param context  请求上下文
     * @return {@link Mono<Void>} 表示异步处理完成
     */
    protected abstract Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain, Context context);

    /**
     * 获取请求上下文
     *
     * @param exchange ServerWebExchange 对象
     * @return 请求上下文
     */
    protected Context getContext(ServerWebExchange exchange) {
        Context context = Context.get(exchange);
        if (context == null) {
            throw new BusinessException(ErrorCode._100805);
        }
        return context;
    }

    /**
     * 获取资产信息
     *
     * @param context 请求上下文
     * @return 资产信息
     */
    protected Assets getAssets(Context context) {
        if (context == null) {
            throw new BusinessException(ErrorCode._100805);
        }
        return context.getAssets();
    }

    /**
     * 获取请求参数映射
     *
     * @param context 请求上下文
     * @return 请求参数映射
     */
    protected Map<String, String> getRequestMap(Context context) {
        if (context == null) {
            throw new BusinessException(ErrorCode._100805);
        }
        return context.getRequestMap();
    }

    /**
     * 设置默认 Content-Type（如果请求头缺失）
     *
     * @param exchange ServerWebExchange 对象
     * @return 更新后的 ServerWebExchange
     */
    protected ServerWebExchange setContentType(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        MediaType mediaType = request.getHeaders().getContentType();
        if (null == mediaType) {
            mediaType = MediaType.APPLICATION_FORM_URLENCODED;
            HttpHeaders headers = new HttpHeaders();
            headers.putAll(exchange.getRequest().getHeaders());
            headers.setContentType(mediaType);
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

    /**
     * 校验请求参数，确保必要参数存在且有效
     *
     * @param exchange ServerWebExchange 对象
     * @throws BusinessException 如果参数无效或缺失，抛出异常
     */
    protected void checkParams(ServerWebExchange exchange) {
        Context context = getContext(exchange);
        Map<String, String> params = getRequestMap(context);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (Normal.UNDEFINED.equals(entry.getKey().toLowerCase())
                    || Normal.UNDEFINED.equals(entry.getValue().toLowerCase())) {
                throw new BusinessException(ErrorCode._100101);
            }
        }
        if (StringKit.isBlank(params.get(Config.METHOD))) {
            throw new BusinessException(ErrorCode._100108);
        }
        if (StringKit.isBlank(params.get(Config.VERSION))) {
            throw new BusinessException(ErrorCode._100107);
        }
        if (StringKit.isBlank(params.get(Config.FORMAT))) {
            throw new BusinessException(ErrorCode._100111);
        }
        if (StringKit.isNotBlank(params.get(Config.SIGN))) {
            context.setNeedDecrypt(true);
        }
    }

}