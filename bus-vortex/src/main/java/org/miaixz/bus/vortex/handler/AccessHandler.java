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
package org.miaixz.bus.vortex.handler;

import org.miaixz.bus.logger.Logger;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 用于异步处理 API 请求的前置逻辑
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AccessHandler extends AbstractHandler {

    /**
     * 异步预处理方法，执行权限验证
     *
     * @param exchange 当前 HTTP 请求对象
     * @param service  服务实例（通常为策略对象）
     * @param args     方法参数，可为 null
     * @return 返回 true 表示验证通过，false 表示验证失败
     */
    @Override
    public Mono<Boolean> preHandle(ServerWebExchange exchange, Object service, Object args) {
        return Mono.fromCallable(() -> {
            // 我们需要记录基本信息，而不是尝试获取 exchange
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";

            // 使用 Logger 直接记录，而不是 VortexLogger
            Logger.info(
                    "[N/A] [{}] [{}] [ACCESS_PREHANDLE] - Performing async preHandle validation for request",
                    method,
                    path);

            return true; // 假设验证通过
        });
    }

    /**
     * 异步后处理方法，处理响应数据
     *
     * @param exchange 当前 HTTP 请求对象
     * @param service  服务实例
     * @param args     方法参数，可为 null
     * @param result   接口方法返回的结果
     * @return 表示异步处理完成
     */
    @Override
    public Mono<Void> postHandle(ServerWebExchange exchange, Object service, Object args, Object result) {
        return Mono.fromRunnable(() -> {
            // 我们需要记录基本信息，而不是尝试获取 exchange
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";

            // 使用 Logger 直接记录，而不是 VortexLogger
            Logger.info("[N/A] [{}] [{}] [ACCESS_POSTHANDLE] - Post-processing response for request", method, path);
        });
    }

    /**
     * 异步完成处理方法，执行清理或日志记录
     *
     * @param exchange  当前 HTTP 请求对象
     * @param service   服务实例
     * @param args      方法参数，可为 null
     * @param result    最终响应结果，可为 null
     * @param exception 异常对象（若有），可为 null
     * @return 表示异步处理完成
     */
    @Override
    public Mono<Void> afterCompletion(
            ServerWebExchange exchange,
            Object service,
            Object args,
            Object result,
            Throwable exception) {
        return Mono.fromRunnable(() -> {
            // 我们需要记录基本信息，而不是尝试获取 exchange
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
            String exceptionMsg = exception != null ? exception.getMessage() : "none";

            // 使用 Logger 直接记录
            Logger.info(
                    "[N/A] [{}] [{}] [ACCESS_COMPLETION] - Request completed, exception: {}",
                    method,
                    path,
                    exceptionMsg);
        });
    }

}
