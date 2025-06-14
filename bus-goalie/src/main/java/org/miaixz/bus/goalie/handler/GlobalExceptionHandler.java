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
package org.miaixz.bus.goalie.handler;

import java.util.Map;

import org.miaixz.bus.core.basic.spring.Controller;
import org.miaixz.bus.core.lang.exception.BusinessException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.goalie.Config;
import org.miaixz.bus.goalie.Context;
import org.miaixz.bus.goalie.Provider;
import org.miaixz.bus.goalie.magic.ErrorCode;
import org.miaixz.bus.logger.Logger;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.server.ServerWebExchange;

import io.netty.handler.timeout.ReadTimeoutException;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

/**
 * 全局异常处理器，处理 Web 应用中的异常并返回标准化的 JSON 响应
 *
 * @author Justubborn
 * @since Java 17+
 */
public class GlobalExceptionHandler extends Controller implements ErrorWebExceptionHandler {

    /**
     * 处理异常，生成标准化的错误响应
     *
     * @param exchange 当前的 ServerWebExchange 对象，包含请求和响应
     * @param ex       捕获的异常对象
     * @return {@link Mono<Void>} 表示异步处理完成
     */
    @NonNull
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // 获取响应对象并设置状态码和内容类型
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 获取请求上下文和参数
        Context context = Context.get(exchange);
        Map<String, String> map = context.getRequestMap();
        String method = null;
        if (null != map) {
            method = map.get(Config.METHOD); // 获取请求方法名
        }

        // 记录错误日志，包含 traceId、请求方法和异常信息
        Logger.error("traceId:{},request: {},error:{}", exchange.getLogPrefix(), method, ex.getMessage());

        // 根据异常类型生成错误消息
        Object message;
        if (ex instanceof WebClientException) {
            if (ex.getCause() instanceof ReadTimeoutException) {
                message = Controller.write(ErrorCode.EM_80010003); // 读取超时错误
            } else {
                message = Controller.write(ErrorCode.EM_80010004); // 其他 WebClient 异常
            }
        } else if (ex instanceof BusinessException e) {
            if (StringKit.isNotBlank(e.getErrcode())) {
                message = Controller.write(e.getErrcode()); // 业务异常的特定错误码
            } else {
                message = Controller.write(ErrorCode._100513, e.getMessage()); // 通用业务异常
            }
        } else {
            message = Controller.write(ErrorCode._100513); // 默认未知错误
        }

        // 使用上下文指定的序列化提供者格式化响应
        Provider provider = context.getFormat().getProvider();
        String formatBody;
        if (null != provider) {
            formatBody = provider.serialize(message);
        } else {
            // 回退到 JSON 序列化
            formatBody = Context.Format.json.getProvider().serialize(message);
        }

        // 将格式化后的响应写入 DataBuffer
        DataBuffer db = response.bufferFactory().wrap(formatBody.getBytes());

        // 返回响应并记录执行耗时
        return response.writeWith(Mono.just(db)).doOnTerminate(() -> Logger.info("traceId:{},exec time :{}ms",
                exchange.getLogPrefix(), System.currentTimeMillis() - context.getStartTime()));
    }

}