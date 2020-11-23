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
package org.aoju.bus.goalie.handler;

import org.aoju.bus.base.consts.ErrorCode;
import org.aoju.bus.base.spring.Controller;
import org.aoju.bus.core.lang.exception.BusinessException;
import org.aoju.bus.core.toolkit.RuntimeKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.extra.json.JsonKit;
import org.aoju.bus.goalie.Consts;
import org.aoju.bus.goalie.Context;
import org.aoju.bus.logger.Logger;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.net.ConnectException;
import java.util.Map;

/**
 * 异常处理
 *
 * @author Justubborn
 * @since 2020/10/27
 */
public class GlobalExceptionHandler extends Controller implements ErrorWebExceptionHandler {

    @NonNull
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> map = Context.get(exchange).getRequestMap();
        String method = null;
        if (null != map) {
            method = map.get(Consts.METHOD);
        }
        Logger.error("request: {},error:{}", method, RuntimeKit.getMessage(ex));
        Object message;
        if (ex instanceof ConnectException) {
            message = Controller.write(ErrorCode.EM_FAILURE);
        } else if (ex instanceof BusinessException) {
            BusinessException e = (BusinessException) ex;
            if (StringKit.isNotBlank(e.getErrcode())) {
                message = Controller.write(e.getErrcode());
            } else {
                message = Controller.write(ErrorCode.EM_100513, e.getMessage());
            }
        } else {
            message = Controller.write(ErrorCode.EM_100513);
        }
        DataBuffer db = response.bufferFactory().wrap(JsonKit.toJsonString(message).getBytes());
        return response.writeWith(Mono.just(db));
    }

}
