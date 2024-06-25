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
package org.miaixz.bus.goalie;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

/**
 * 拦截器，原理同spring拦截器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Handler {

    /**
     * 预处理回调方法，在方法调用前执行。返回false不继续向下执行，此时可使用response返回错误信息
     *
     * @param request  网络请求
     * @param response 响应信息
     * @param service  service类
     * @param args     方法参数
     * @return 返回false不继续向下执行，此时可使用response返回错误信息
     */
    default boolean preHandle(ServerHttpRequest request, ServerHttpResponse response, Object service, Object args) {
        return true;
    }

    /**
     * 接口方法执行完后调用此方法。
     *
     * @param request  网络请求
     * @param response 响应信息
     * @param service  service类
     * @param args     参数
     * @param result   返回结果
     */
    default void postHandle(ServerHttpRequest request, ServerHttpResponse response, Object service, Object args,
                            Object result) {

    }

    /**
     * 结果包装完成后执行
     *
     * @param request   网络请求
     * @param response  响应信息
     * @param service   service类
     * @param args      参数
     * @param result    最终结果，被包装过
     * @param exception 业务异常
     */
    default void afterCompletion(ServerHttpRequest request, ServerHttpResponse response, Object service, Object args,
                                 Object result, Exception exception) {

    }

}
