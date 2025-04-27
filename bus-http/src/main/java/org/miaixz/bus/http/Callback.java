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

import org.miaixz.bus.http.bodys.ResponseBody;

/**
 * 异步回调信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Callback<T> {

    /**
     * 当请求由于取消、连接问题或超时而无法执行时调用 因为网络可能在交换期间失败，所以远程服务器可能在失败之前接受了请求
     *
     * @param call 调用者信息
     * @param ex   异常信息
     */
    default void onFailure(NewCall call, IOException ex) {

    }

    /**
     * 失败回调信息
     *
     * @param newCall   回调信息
     * @param exception 异常
     * @param id        当前请求标识
     */
    default void onFailure(NewCall newCall, Exception exception, String id) {

    }

    /**
     * 当远程服务器成功返回HTTP响应时调用。回调可以继续使用{@link Response#body}读取响应体响应仍然是 活动的直到它的响应体是{@linkplain ResponseBody closed}
     * 回调的接收者可以使用另一个线程上的响应体 注意，传输层的成功(接收HTTP响应代码、报头和正文)不一定表示应用程序层的 成功:{@code response}可能仍然表示不满意的HTTP响应代码，如404或500
     *
     * @param call     调用者信息
     * @param response 响应体
     * @throws IOException 异常信息
     */
    default void onResponse(NewCall call, Response response) throws IOException {

    }

    /**
     * 响应回调信息
     *
     * @param newCall  回调信息
     * @param response 响应信息
     * @param id       当前请求标识
     */
    default void onResponse(NewCall newCall, Response response, String id) {

    }

    default void on(T data) {

    }

}
