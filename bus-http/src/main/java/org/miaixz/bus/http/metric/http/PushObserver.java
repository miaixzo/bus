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

import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.http.Headers;
import org.miaixz.bus.http.Protocol;

import java.io.IOException;
import java.util.List;

/**
 * 仅{@link Protocol#HTTP_2 HTTP/2}
 * 在客户端处理服务器发起的HTTP请求
 * 返回true以请求取消已推的流。
 * 注意，这并不保证将来的帧不会到达流ID
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface PushObserver {

    PushObserver CANCEL = new PushObserver() {

        @Override
        public boolean onRequest(int streamId, List<Headers.Header> requestHeaders) {
            return true;
        }

        @Override
        public boolean onHeaders(int streamId, List<Headers.Header> responseHeaders, boolean last) {
            return true;
        }

        @Override
        public boolean onData(int streamId, BufferSource source, int byteCount,
                              boolean last) throws IOException {
            source.skip(byteCount);
            return true;
        }

        @Override
        public void onReset(int streamId, ErrorCode errorCode) {
        }
    };

    /**
     * 描述服务器打算为其推送响应的请求
     *
     * @param streamId       务器发起的流ID:偶数
     * @param requestHeaders 最低限度包括 method、scheme、authority和path
     * @return the true/false
     */
    boolean onRequest(int streamId, List<Headers.Header> requestHeaders);

    /**
     * 推送请求对应的响应标头。当{@code last}为真时，则没有后续的数据帧
     *
     * @param streamId        服务器发起的流ID:偶数.
     * @param responseHeaders 最少包含status
     * @param last            如果为真，则没有响应数据
     * @return the true/false
     */
    boolean onHeaders(int streamId, List<Headers.Header> responseHeaders, boolean last);

    /**
     * 与推送请求对应的响应数据块。必须读取或跳过这些数据.
     *
     * @param streamId  服务器发起的流ID:偶数.
     * @param source    与此流ID对应的数据的位置.
     * @param byteCount 从源读取或跳过的字节数.
     * @param last      如果为真，则不需要遵循任何数据帧.
     * @return the true/false
     * @throws IOException 异常
     */
    boolean onData(int streamId, BufferSource source, int byteCount, boolean last)
            throws IOException;

    /**
     * 指示此流被取消的原因
     *
     * @param streamId  服务器发起的流ID:偶数.
     * @param errorCode 错误码信息
     */
    void onReset(int streamId, ErrorCode errorCode);

}
