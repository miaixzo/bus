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
package org.miaixz.bus.vortex;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 异步拦截器接口，定义请求处理的三个阶段
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Handler {

    /**
     * 获取处理器顺序
     *
     * @return 顺序值，越小越先执行
     */
    default int getOrder() {
        return 0;
    }

    /**
     * 异步预处理方法，在请求处理前执行
     *
     * @param exchange 当前 ServerWebExchange 对象
     * @param service  服务实例（通常为策略对象）
     * @param args     方法参数，可为 null
     * @return {@code Mono<Boolean>} 返回 true 表示验证通过，false 表示验证失败
     */
    default Mono<Boolean> preHandle(ServerWebExchange exchange, Object service, Object args) {
        return Mono.just(true);
    }

    /**
     * 异步后处理方法，在请求处理后执行
     *
     * @param exchange 当前 ServerWebExchange 对象
     * @param service  服务实例
     * @param args     方法参数，可为 null
     * @param result   接口方法返回的结果
     * @return {@code Mono<Void>} 表示异步处理完成
     */
    default Mono<Void> postHandle(ServerWebExchange exchange, Object service, Object args, Object result) {
        return Mono.empty();
    }

    /**
     * 异步完成处理方法，在请求完成后执行（无论成功或失败）
     *
     * @param exchange  当前 ServerWebExchange 对象
     * @param service   服务实例
     * @param args      方法参数，可为 null
     * @param result    最终响应结果，可为 null
     * @param exception 异常对象（若有），可为 null
     * @return {@code Mono<Void>} 表示异步处理完成
     */
    default Mono<Void> afterCompletion(ServerWebExchange exchange, Object service, Object args, Object result,
            Throwable exception) {
        return Mono.empty();
    }

}