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
package org.miaixz.bus.http;

import java.io.IOException;

/**
 * HTTP 请求的异步回调接口
 * <p>
 * 定义了处理 HTTP 请求失败和成功的回调方法，支持异步请求的响应处理。 实现类需确保响应体的正确关闭，并处理可能的异常情况。
 * </p>
 *
 * @param <T> 回调数据的类型
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Callback<T> {

    /**
     * 请求失败时的回调
     * <p>
     * 当请求因取消、连接问题或超时等原因失败时调用。 注意：失败前远程服务器可能已接收到请求。
     * </p>
     *
     * @param call 请求调用者
     * @param ex   异常信息
     */
    default void onFailure(NewCall call, IOException ex) {
    }

    /**
     * 请求失败时的回调（带请求标识）
     * <p>
     * 当请求失败时调用，包含请求标识以区分多个请求。
     * </p>
     *
     * @param newCall   请求调用者
     * @param exception 异常信息
     * @param id        请求标识
     */
    default void onFailure(NewCall newCall, Exception exception, String id) {
    }

    /**
     * 请求成功时的回调
     * <p>
     * 当远程服务器成功返回 HTTP 响应时调用。 实现类需确保 {@link Response#body} 被正确关闭。 注意：传输层成功不代表应用层成功（如 404、500 状态码）。
     * </p>
     *
     * @param call     请求调用者
     * @param response 响应体
     * @throws IOException 如果处理响应失败
     */
    default void onResponse(NewCall call, Response response) throws IOException {
    }

    /**
     * 请求成功时的回调（带请求标识）
     * <p>
     * 当请求成功时调用，包含请求标识以区分多个请求。
     * </p>
     *
     * @param newCall  请求调用者
     * @param response 响应信息
     * @param id       请求标识
     */
    default void onResponse(NewCall newCall, Response response, String id) {
    }

    /**
     * 通用数据回调
     * <p>
     * 用于处理特定类型的回调数据。
     * </p>
     *
     * @param data 回调数据
     */
    default void on(T data) {
    }

}