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
package org.miaixz.bus.http.metric.http;

import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.http.Headers;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.accord.RealConnection;

import java.io.IOException;

/**
 * Encode HTTP请求和decode HTTP响应
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface HttpCodec {

    /**
     * 丢弃输入数据流时要使用的超时。由于这是用于连接重用， 因此此超时时间应该大大少于建立新连接所需的时间.
     */
    int DISCARD_STREAM_TIMEOUT_MILLIS = 100;

    /**
     * 返回携带此编解码器的连接
     */
    RealConnection connection();

    /**
     * 返回一个可以对请求体进行流处理的输出流.
     *
     * @param request       网络请求
     * @param contentLength 内容长度
     * @return 缓冲信息
     */
    Sink createRequestBody(Request request, long contentLength) throws IOException;

    /**
     * 这应该更新HTTP引擎的sentRequestMillis字段.
     *
     * @param request 网络请求
     * @throws IOException 异常
     */
    void writeRequestHeaders(Request request) throws IOException;

    /**
     * 将请求刷新到基础套接字
     *
     * @throws IOException 异常
     */
    void flushRequest() throws IOException;

    /**
     * 将请求刷新到基础套接字，就不会传输更多的字节.
     *
     * @throws IOException 异常
     */
    void finishRequest() throws IOException;

    /**
     * 从HTTP传输解析响应头的字节
     *
     * @param expectContinue 如果这是一个带有“100”响应代码的中间响应， 则返回null。否则，此方法永远不会返回null.
     * @return 响应构建器
     * @throws IOException 异常
     */
    Response.Builder readResponseHeaders(boolean expectContinue) throws IOException;

    long reportedContentLength(Response response) throws IOException;

    Source openResponseBodySource(Response response) throws IOException;

    /**
     * 在HTTP响应之后返回
     *
     * @return the object
     * @throws IOException 异常
     */
    Headers trailers() throws IOException;

    /**
     * 取消这个流。这个流所持有的资源将被清理，尽管不是同步的。这可能会在连接池线程之后发生
     */
    void cancel();

}
