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
package org.miaixz.bus.goalie;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

/**
 * 拦截器接口，类似于 Spring 的拦截器机制，用于在请求处理的不同阶段执行自定义逻辑
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Handler {

    /**
     * 预处理回调方法，在请求处理前执行 可用于权限验证、参数校验等，若返回 false，则终止请求处理，可通过 response 返回错误信息
     *
     * @param request  当前的 HTTP 请求对象
     * @param response 当前的 HTTP 响应对象
     * @param service  服务类实例（通常为控制器或处理类）
     * @param args     方法参数
     * @return 返回 true 继续处理请求，返回 false 终止处理
     */
    default boolean preHandle(ServerHttpRequest request, ServerHttpResponse response, Object service, Object args) {
        return true;
    }

    /**
     * 后处理回调方法，在接口方法执行完成后但响应返回前执行 可用于修改响应数据或记录日志
     *
     * @param request  当前的 HTTP 请求对象
     * @param response 当前的 HTTP 响应对象
     * @param service  服务类实例
     * @param args     方法参数
     * @param result   接口方法返回的结果
     */
    default void postHandle(ServerHttpRequest request, ServerHttpResponse response, Object service, Object args,
            Object result) {
        // 默认空实现
    }

    /**
     * 完成回调方法，在响应结果包装并返回客户端后执行 可用于清理资源、记录最终日志或处理异常
     *
     * @param request   当前的 HTTP 请求对象
     * @param response  当前的 HTTP 响应对象
     * @param service   服务类实例
     * @param args      方法参数
     * @param result    最终包装后的响应结果
     * @param exception 业务异常（若有）
     */
    default void afterCompletion(ServerHttpRequest request, ServerHttpResponse response, Object service, Object args,
            Object result, Exception exception) {
        // 默认空实现
    }

}