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

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.http.accord.Exchange;
import org.miaixz.bus.http.bodys.ResponseBody;
import org.miaixz.bus.http.cache.CacheControl;
import org.miaixz.bus.http.secure.Challenge;
import org.miaixz.bus.http.socket.Handshake;

/**
 * HTTP 响应类，封装 HTTP 响应的所有信息，包括请求、协议、状态码、头部、响应体等。
 * <p>
 * 注意：除响应体外，所有属性都是不可变的。响应体是一次性资源，必须在读取后关闭，且只能消费一次。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Response implements Closeable {

    /**
     * 发起此响应的原始请求
     */
    final Request request;
    /**
     * 使用的协议（如 HTTP/1.1、HTTP/2）
     */
    final Protocol protocol;
    /**
     * HTTP 状态码
     */
    final int code;
    /**
     * HTTP 状态消息
     */
    final String message;
    /**
     * TLS 握手信息（非 TLS 连接为 null）
     */
    final Handshake handshake;
    /**
     * 响应头部
     */
    final Headers headers;
    /**
     * 响应体（可为 null，如 HEAD 请求）
     */
    final ResponseBody body;
    /**
     * 网络响应（直接从网络获取，未缓存）
     */
    final Response networkResponse;
    /**
     * 缓存响应（从缓存获取）
     */
    final Response cacheResponse;
    /**
     * 前一个响应（如重定向或认证触发的响应）
     */
    final Response priorResponse;
    /**
     * 发送请求的时间戳（毫秒）
     */
    final long sentRequestAtMillis;
    /**
     * 接收响应头部的时间戳（毫秒）
     */
    final long receivedResponseAtMillis;
    /**
     * 交换对象，管理请求和响应的传输
     */
    final Exchange exchange;
    /**
     * 缓存控制（延迟初始化）
     */
    private volatile CacheControl cacheControl;

    /**
     * 构造函数，基于 Builder 初始化 Response 实例。
     *
     * @param builder Builder 实例，包含所有响应属性
     */
    Response(Builder builder) {
        this.request = builder.request;
        this.protocol = builder.protocol;
        this.code = builder.code;
        this.message = builder.message;
        this.handshake = builder.handshake;
        this.headers = builder.headers.build(); // 构建不可变头部
        this.body = builder.body;
        this.networkResponse = builder.networkResponse;
        this.cacheResponse = builder.cacheResponse;
        this.priorResponse = builder.priorResponse;
        this.sentRequestAtMillis = builder.sentRequestAtMillis;
        this.receivedResponseAtMillis = builder.receivedResponseAtMillis;
        this.exchange = builder.exchange;
    }

    /**
     * 获取发起此响应的原始请求。
     * <p>
     * 注意：此请求可能与应用程序发出的请求不同，可能被 HTTP 客户端修改（如添加 Content-Length 头部） 或因重定向/认证生成新请求（URL 可能不同）。
     * </p>
     *
     * @return 发起响应的 Request 对象
     */
    public Request request() {
        return request;
    }

    /**
     * 获取使用的 HTTP 协议。
     *
     * @return 协议，如 HTTP/1.1、HTTP/2
     */
    public Protocol protocol() {
        return protocol;
    }

    /**
     * 获取 HTTP 状态码。
     *
     * @return 状态码（如 200、404）
     */
    public int code() {
        return code;
    }

    /**
     * 检查响应是否成功（状态码在 [200..300) 范围内）。
     *
     * @return true 如果请求被成功接收、理解和接受
     */
    public boolean isSuccessful() {
        return code >= 200 && code < 300;
    }

    /**
     * 获取 HTTP 状态消息。
     *
     * @return 状态消息（如 "OK"、"Not Found"）
     */
    public String message() {
        return message;
    }

    /**
     * 获取 TLS 握手信息。
     *
     * @return Handshake 对象（非 TLS 连接为 null）
     */
    public Handshake handshake() {
        return handshake;
    }

    /**
     * 获取指定名称的头部值列表。
     *
     * @param name 头部名称
     * @return 头部值列表（可能为空）
     */
    public List<String> headers(String name) {
        return headers.values(name);
    }

    /**
     * 获取指定名称的第一个头部值。
     *
     * @param name 头部名称
     * @return 头部值（不存在时为 null）
     */
    public String header(String name) {
        return header(name, null);
    }

    /**
     * 获取指定名称的第一个头部值，带默认值。
     *
     * @param name         头部名称
     * @param defaultValue 默认值（不存在时返回）
     * @return 头部值或默认值
     */
    public String header(String name, String defaultValue) {
        String result = headers.get(name);
        return null != result ? result : defaultValue;
    }

    /**
     * 获取所有响应头部。
     *
     * @return Headers 对象
     */
    public Headers headers() {
        return headers;
    }

    /**
     * 获取响应尾部（trailer headers）。
     * <p>
     * 注意：必须在响应体完全消费后调用，否则抛出 IllegalStateException。
     * </p>
     *
     * @return 尾部 Headers 对象（可能为空）
     * @throws IOException           如果无法获取尾部
     * @throws IllegalStateException 如果响应体未完全消费
     */
    public Headers trailers() throws IOException {
        if (exchange == null)
            throw new IllegalStateException("trailers not available");
        return exchange.trailers(); // 从交换对象获取尾部
    }

    /**
     * 预览响应体内容，最多读取指定字节数。
     * <p>
     * 返回新的 ResponseBody，包含最多 byteCount 字节的响应体内容。如果响应体不足 byteCount 字节， 返回全部内容；如果超过，则截断。
     * </p>
     * <p>
     * 注意：此方法会将请求的字节加载到内存，建议设置合理的 byteCount（如 1MB）。 在响应体消费后调用会抛出错误。
     * </p>
     *
     * @param byteCount 最大预览字节数
     * @return 新的 ResponseBody 对象
     * @throws IOException 如果读取响应体失败
     */
    public ResponseBody peekBody(long byteCount) throws IOException {
        BufferSource peeked = body.source().peek();
        Buffer buffer = new Buffer();
        peeked.request(byteCount);
        buffer.write(peeked, Math.min(byteCount, peeked.getBuffer().size())); // 复制最多 byteCount 字节
        return ResponseBody.create(body.mediaType(), buffer.size(), buffer);
    }

    /**
     * 获取响应体。
     * <p>
     * 注意：响应体是一次性资源，必须关闭且只能消费一次。对于 {@link #cacheResponse}、{@link #networkResponse}、 {@link #priorResponse} 返回的响应，此方法返回
     * null。
     * </p>
     *
     * @return ResponseBody 对象（可能为 null）
     */
    public ResponseBody body() {
        return body;
    }

    /**
     * 创建新的 Builder 实例，基于当前 Response。
     *
     * @return Builder 实例
     */
    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * 检查响应是否为重定向。
     *
     * @return true 如果状态码表示重定向（300、301、302、303、307、308）
     */
    public boolean isRedirect() {
        switch (code) {
        case HTTP.HTTP_PERM_REDIRECT: // 308
        case HTTP.HTTP_TEMP_REDIRECT: // 307
        case HTTP.HTTP_MULT_CHOICE: // 300
        case HTTP.HTTP_MOVED_PERM: // 301
        case HTTP.HTTP_MOVED_TEMP: // 302
        case HTTP.HTTP_SEE_OTHER: // 303
            return true;
        default:
            return false;
        }
    }

    /**
     * 获取网络响应。
     * <p>
     * 如果响应直接从网络获取（未使用缓存），返回原始响应；否则返回 null。 返回的响应体不应被读取。
     * </p>
     *
     * @return 网络 Response 对象（可能为 null）
     */
    public Response networkResponse() {
        return networkResponse;
    }

    /**
     * 获取缓存响应。
     * <p>
     * 如果响应从缓存获取，返回缓存响应；否则返回 null。对于条件 GET 请求， 可能同时存在缓存和网络响应。返回的响应体不应被读取。
     * </p>
     *
     * @return 缓存 Response 对象（可能为 null）
     */
    public Response cacheResponse() {
        return cacheResponse;
    }

    /**
     * 获取前一个响应。
     * <p>
     * 如果响应由重定向或认证挑战触发，返回前一个响应；否则返回 null。 返回的响应体不应被读取（已消费）。
     * </p>
     *
     * @return 前一个 Response 对象（可能为 null）
     */
    public Response priorResponse() {
        return priorResponse;
    }

    /**
     * 获取 RFC 7235 认证挑战列表。
     * <p>
     * 对于状态码 401（未授权），返回 "WWW-Authenticate" 挑战； 对于状态码 407（代理未授权），返回 "Proxy-Authenticate" 挑战； 其他状态码返回空列表。
     * </p>
     *
     * @return 认证挑战列表（可能为空）
     */
    public List<Challenge> challenges() {
        String responseField;
        if (code == HTTP.HTTP_UNAUTHORIZED) {
            responseField = HTTP.WWW_AUTHENTICATE; // 401 使用 WWW-Authenticate
        } else if (code == HTTP.HTTP_PROXY_AUTH) {
            responseField = HTTP.PROXY_AUTHENTICATE; // 407 使用 Proxy-Authenticate
        } else {
            return Collections.emptyList(); // 其他状态码无挑战
        }
        return Headers.parseChallenges(headers(), responseField); // 解析挑战头部
    }

    /**
     * 获取缓存控制指令。
     * <p>
     * 即使响应不包含 "Cache-Control" 头部，也返回非 null 的 CacheControl 对象。 使用延迟初始化以提高性能。
     * </p>
     *
     * @return CacheControl 对象
     */
    public CacheControl cacheControl() {
        CacheControl result = cacheControl;
        return result != null ? result : (cacheControl = CacheControl.parse(headers)); // 延迟解析缓存控制
    }

    /**
     * 获取发送请求的时间戳。
     *
     * @return 发送请求的毫秒时间戳（System.currentTimeMillis）
     */
    public long sentRequestAtMillis() {
        return sentRequestAtMillis;
    }

    /**
     * 获取接收响应头部的时间戳。
     *
     * @return 接收响应的毫秒时间戳（System.currentTimeMillis）
     */
    public long receivedResponseAtMillis() {
        return receivedResponseAtMillis;
    }

    /**
     * 关闭响应体。
     * <p>
     * 等价于调用 {@code body().close()}。对于无响应体的响应（如 {@link #cacheResponse}、
     * {@link #networkResponse}、{@link #priorResponse}），调用会抛出异常。
     * </p>
     *
     * @throws IllegalStateException 如果响应无响应体
     */
    @Override
    public void close() {
        if (null == body) {
            throw new IllegalStateException("response is not eligible for a body and must not be closed");
        }
        body.close(); // 关闭响应体
    }

    /**
     * 返回响应的字符串表示。
     *
     * @return 包含协议、状态码、消息和 URL 的字符串
     */
    @Override
    public String toString() {
        return "Response{protocol=" + protocol + ", code=" + code + ", message=" + message + ", url=" + request.url()
                + Symbol.C_BRACE_RIGHT;
    }

    /**
     * Response 构建器，用于创建和修改 Response 实例。
     */
    public static class Builder {

        /**
         * 发起响应的请求
         */
        Request request;

        /**
         *
         */
        Protocol protocol;
        /**
         * 使用的协议
         */
        int code = -1;
        /**
         * HTTP 状态消息
         */
        String message;
        /**
         * TLS 握手信息
         */
        Handshake handshake;
        /**
         * 响应头部构建器
         */
        Headers.Builder headers;
        /**
         * 响应体
         */
        ResponseBody body;
        /**
         * 网络响应
         */
        Response networkResponse;
        /**
         * 缓存响应
         */
        Response cacheResponse;
        /**
         * 前一个响应
         */
        Response priorResponse;
        /**
         * 发送请求时间戳
         */
        long sentRequestAtMillis;
        /**
         * 接收响应时间戳
         */
        long receivedResponseAtMillis;
        /**
         * 交换对象
         */
        Exchange exchange;

        /**
         * 默认构造函数，初始化空 Builder。
         */
        public Builder() {
            headers = new Headers.Builder();
        }

        /**
         * 构造函数，基于现有 Response 初始化 Builder。
         *
         * @param response Response 实例
         */
        Builder(Response response) {
            this.request = response.request;
            this.protocol = response.protocol;
            this.code = response.code;
            this.message = response.message;
            this.handshake = response.handshake;
            this.headers = response.headers.newBuilder();
            this.body = response.body;
            this.networkResponse = response.networkResponse;
            this.cacheResponse = response.cacheResponse;
            this.priorResponse = response.priorResponse;
            this.sentRequestAtMillis = response.sentRequestAtMillis;
            this.receivedResponseAtMillis = response.receivedResponseAtMillis;
            this.exchange = response.exchange;
        }

        /**
         * 设置发起响应的请求。
         *
         * @param request Request 对象
         * @return 当前 Builder 实例
         */
        public Builder request(Request request) {
            this.request = request;
            return this;
        }

        /**
         * 设置使用的协议。
         *
         * @param protocol 协议（如 HTTP/1.1、HTTP/2）
         * @return 当前 Builder 实例
         */
        public Builder protocol(Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        /**
         * 设置 HTTP 状态码。
         *
         * @param code 状态码
         * @return 当前 Builder 实例
         */
        public Builder code(int code) {
            this.code = code;
            return this;
        }

        /**
         * 设置 HTTP 状态消息。
         *
         * @param message 状态消息
         * @return 当前 Builder 实例
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * 设置 TLS 握手信息。
         *
         * @param handshake Handshake 对象（可为 null）
         * @return 当前 Builder 实例
         */
        public Builder handshake(Handshake handshake) {
            this.handshake = handshake;
            return this;
        }

        /**
         * 设置指定名称的头部值，替换现有同名头部。
         *
         * @param name  头部名称
         * @param value 头部值
         * @return 当前 Builder 实例
         */
        public Builder header(String name, String value) {
            headers.set(name, value);
            return this;
        }

        /**
         * 添加指定名称和值的头部，保留现有同名头部。
         *
         * @param name  头部名称
         * @param value 头部值
         * @return 当前 Builder 实例
         */
        public Builder addHeader(String name, String value) {
            headers.add(name, value);
            return this;
        }

        /**
         * 移除指定名称的所有头部。
         *
         * @param name 头部名称
         * @return 当前 Builder 实例
         */
        public Builder removeHeader(String name) {
            headers.removeAll(name);
            return this;
        }

        /**
         * 设置所有头部，替换现有头部。
         *
         * @param headers Headers 对象
         * @return 当前 Builder 实例
         */
        public Builder headers(Headers headers) {
            this.headers = headers.newBuilder();
            return this;
        }

        /**
         * 设置响应体。
         *
         * @param body ResponseBody 对象（可为 null）
         * @return 当前 Builder 实例
         */
        public Builder body(ResponseBody body) {
            this.body = body;
            return this;
        }

        /**
         * 设置网络响应。
         *
         * @param networkResponse 网络 Response 对象（可为 null）
         * @return 当前 Builder 实例
         * @throws IllegalArgumentException 如果 networkResponse 包含无效属性
         */
        public Builder networkResponse(Response networkResponse) {
            if (networkResponse != null)
                checkSupportResponse("networkResponse", networkResponse);
            this.networkResponse = networkResponse;
            return this;
        }

        /**
         * 设置缓存响应。
         *
         * @param cacheResponse 缓存 Response 对象（可为 null）
         * @return 当前 Builder 实例
         * @throws IllegalArgumentException 如果 cacheResponse 包含无效属性
         */
        public Builder cacheResponse(Response cacheResponse) {
            if (cacheResponse != null)
                checkSupportResponse("cacheResponse", cacheResponse);
            this.cacheResponse = cacheResponse;
            return this;
        }

        /**
         * 验证支持响应（networkResponse 或 cacheResponse）的有效性。
         *
         * @param name     响应名称（用于错误信息）
         * @param response Response 对象
         * @throws IllegalArgumentException 如果响应包含 body、networkResponse、cacheResponse 或 priorResponse
         */
        private void checkSupportResponse(String name, Response response) {
            if (null != response.body) {
                throw new IllegalArgumentException(name + ".body != null");
            } else if (null != response.networkResponse) {
                throw new IllegalArgumentException(name + ".networkResponse != null");
            } else if (null != response.cacheResponse) {
                throw new IllegalArgumentException(name + ".cacheResponse != null");
            } else if (null != response.priorResponse) {
                throw new IllegalArgumentException(name + ".priorResponse != null");
            }
        }

        /**
         * 设置前一个响应。
         *
         * @param priorResponse 前一个 Response 对象（可为 null）
         * @return 当前 Builder 实例
         * @throws IllegalArgumentException 如果 priorResponse 包含 body
         */
        public Builder priorResponse(Response priorResponse) {
            if (null != priorResponse) {
                checkPriorResponse(priorResponse);
            }
            this.priorResponse = priorResponse;
            return this;
        }

        /**
         * 验证前一个响应的有效性。
         *
         * @param response Response 对象
         * @throws IllegalArgumentException 如果响应包含 body
         */
        private void checkPriorResponse(Response response) {
            if (null != response.body) {
                throw new IllegalArgumentException("priorResponse.body != null");
            }
        }

        /**
         * 设置发送请求的时间戳。
         *
         * @param sentRequestAtMillis 时间戳（毫秒）
         * @return 当前 Builder 实例
         */
        public Builder sentRequestAtMillis(long sentRequestAtMillis) {
            this.sentRequestAtMillis = sentRequestAtMillis;
            return this;
        }

        /**
         * 设置接收响应头部的时间戳。
         *
         * @param receivedResponseAtMillis 时间戳（毫秒）
         * @return 当前 Builder 实例
         */
        public Builder receivedResponseAtMillis(long receivedResponseAtMillis) {
            this.receivedResponseAtMillis = receivedResponseAtMillis;
            return this;
        }

        /**
         * 初始化交换对象。
         *
         * @param deferredTrailers Exchange 对象
         */
        void initExchange(Exchange deferredTrailers) {
            this.exchange = deferredTrailers;
        }

        /**
         * 构建 Response 实例。
         *
         * @return Response 对象
         * @throws IllegalStateException 如果 request、protocol、message 为空或 code 小于 0
         */
        public Response build() {
            if (null == request) {
                throw new IllegalStateException("request == null");
            }
            if (null == protocol) {
                throw new IllegalStateException("protocol == null");
            }
            if (code < 0) {
                throw new IllegalStateException("code < 0: " + code);
            }
            if (null == message) {
                throw new IllegalStateException("message == null");
            }
            return new Response(this);
        }
    }

}