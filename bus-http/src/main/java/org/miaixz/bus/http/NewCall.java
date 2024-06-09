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
package org.miaixz.bus.http;

import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.http.bodys.ResponseBody;

import java.io.IOException;

/**
 * 调用是准备执行的请求。电话可以取消。
 * 由于此对象表示单个请求/响应对(流)，因此不能执行两次.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface NewCall extends Cloneable {

    /**
     * @return 返回发起此调用的原始请求
     */
    Request request();

    /**
     * 立即调用请求，并阻塞，直到可以处理响应或出现错误.
     * 调用者可以使用响应的{@link Response#body}方法读取响应体。为了避免资源泄漏，
     * 调用者必须{@linkplain ResponseBody 关闭响应体}或响应
     * 注意，传输层的成功(接收HTTP响应代码、报头和正文)不一定表示应用程序层的成功:
     * {@code response}可能仍然表示不满意的HTTP响应代码，如404或500
     *
     * @return 响应体
     * @throws IOException           如果请求由于取消、连接问题或超时而无法执行
     *                               因为网络可能在交换期间失败，
     *                               所以远程服务器可能在失败之前接受了请求
     * @throws IllegalStateException 当调用已经执行
     */
    Response execute() throws IOException;

    /**
     * 调度将在将来某个时候执行的请求
     * {@link Httpd#dispatcher dispatcher}定义请求将在何时运行:
     * 通常是立即运行， 除非当前正在执行其他几个请求
     * 该客户端稍后将使用HTTP响应或失败异常回调{@code responseCallback}
     *
     * @param callback 异步回调
     * @throws IllegalStateException 当调用已经执行
     */
    void enqueue(Callback callback);

    /**
     * 如果可能，取消请求。已经完成的请求不能被取消
     */
    void cancel();

    /**
     * @return the true/false
     */
    boolean isExecuted();

    /**
     * 是否已经取消会停止
     *
     * @return the true/false
     */
    boolean isCanceled();

    /**
     * 返回跨越整个调用的超时:解析DNS、连接、写入请求体、服务器处理和读取响应体
     * 如果调用需要重定向或重试，所有操作都必须在一个超时周期内完成
     * 使用{@link Httpd.Builder#callTimeout}配置客户端的默认超时
     *
     * @return 超时时间
     */
    Timeout timeout();

    /**
     * @return 创建与此调用相同的新调用，即使该调用已经被加入队列或执行
     */
    NewCall clone();

    interface Factory {

        /**
         * 创建新的调用
         *
         * @param request 网络请求信息
         * @return 调用者信息
         */
        NewCall newCall(Request request);

    }

}
