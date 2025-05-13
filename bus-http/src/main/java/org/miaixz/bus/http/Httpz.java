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

import javax.net.ssl.X509TrustManager;

import org.miaixz.bus.core.net.tls.SSLContextBuilder;
import org.miaixz.bus.http.plugin.httpz.*;

/**
 * 发送 HTTP 请求的辅助类，提供便捷的链式调用接口来构建和执行 HTTP 请求。 支持 GET、POST、PUT、HEAD、DELETE 等请求方法。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Httpz {

    // 静态客户端实例，用于管理 HTTP 请求
    private static Client client = new Client();

    /**
     * 创建一个新的 HttpBuilder 实例，使用默认客户端。
     *
     * @return HttpBuilder 实例
     */
    public static HttpBuilder newBuilder() {
        return new HttpBuilder(client.getHttpd());
    }

    /**
     * 创建一个新的 HttpBuilder 实例，使用指定的客户端。
     *
     * @param client Httpd 客户端
     * @return HttpBuilder 实例
     */
    public static HttpBuilder newBuilder(Httpd client) {
        return new HttpBuilder(client);
    }

    /**
     * 创建一个新的 GetBuilder 实例，用于构建 GET 请求。
     *
     * @return GetBuilder 实例
     */
    public static GetBuilder get() {
        return client.get();
    }

    /**
     * 创建一个新的 PostBuilder 实例，用于构建 POST 请求。
     *
     * @return PostBuilder 实例
     */
    public static PostBuilder post() {
        return client.post();
    }

    /**
     * 创建一个新的 PutBuilder 实例，用于构建 PUT 请求。
     *
     * @return PutBuilder 实例
     */
    public static PutBuilder put() {
        return client.put();
    }

    /**
     * 创建一个新的 HeadBuilder 实例，用于构建 HEAD 请求。
     *
     * @return HeadBuilder 实例
     */
    public static HeadBuilder head() {
        return client.head();
    }

    /**
     * 创建一个新的 DeleteBuilder 实例，用于构建 DELETE 请求。
     *
     * @return DeleteBuilder 实例
     */
    public static DeleteBuilder delete() {
        return client.delete();
    }

    /**
     * 获取当前使用的客户端实例。
     *
     * @return Client 实例
     */
    public static Client getClient() {
        return client;
    }

    /**
     * 设置自定义客户端实例。
     *
     * @param httpClient 自定义 Client 实例
     */
    public static void setClient(Client httpClient) {
        Httpz.client = httpClient;
    }

    /**
     * 内部客户端类，管理 HTTP 请求的执行和取消。
     */
    public static class Client {

        // HTTP 请求核心客户端
        private Httpd httpd;

        /**
         * 默认构造函数，初始化 Httpd 客户端并配置 SSL。
         */
        public Client() {
            final X509TrustManager trustManager = SSLContextBuilder.newTrustManager();
            this.httpd = new Httpd().newBuilder()
                    .sslSocketFactory(SSLContextBuilder.newSslSocketFactory(trustManager), trustManager)
                    .hostnameVerifier((hostname, session) -> true) // 信任所有主机名
                    .build();
        }

        /**
         * 使用指定的 Httpd 客户端进行初始化。
         *
         * @param httpd Httpd 客户端
         */
        public Client(Httpd httpd) {
            this.httpd = httpd;
        }

        /**
         * 取消所有正在排队或运行的请求，使用默认客户端。
         */
        public static void cancelAll() {
            cancelAll(client.getHttpd());
        }

        /**
         * 取消指定客户端的所有正在排队或运行的请求。
         *
         * @param httpd Httpd 客户端
         */
        public static void cancelAll(final Httpd httpd) {
            if (httpd != null) {
                // 取消排队中的请求
                for (NewCall call : httpd.dispatcher().queuedCalls()) {
                    call.cancel();
                }
                // 取消运行中的请求
                for (NewCall call : httpd.dispatcher().runningCalls()) {
                    call.cancel();
                }
            }
        }

        /**
         * 取消带有指定标签的请求，使用默认客户端。
         *
         * @param tag 请求的标签
         */
        public static void cancel(final Object tag) {
            cancel(client.getHttpd(), tag);
        }

        /**
         * 取消指定客户端中带有特定标签的请求。
         *
         * @param httpd Httpd 客户端
         * @param tag   请求的标签
         */
        public static void cancel(final Httpd httpd, final Object tag) {
            if (httpd != null && tag != null) {
                // 取消排队中匹配标签的请求
                for (NewCall call : httpd.dispatcher().queuedCalls()) {
                    if (tag.equals(call.request().tag())) {
                        call.cancel();
                    }
                }
                // 取消运行中匹配标签的请求
                for (NewCall call : httpd.dispatcher().runningCalls()) {
                    if (tag.equals(call.request().tag())) {
                        call.cancel();
                    }
                }
            }
        }

        /**
         * 创建 GetBuilder 实例，用于构建 GET 请求。
         *
         * @return GetBuilder 实例
         */
        public GetBuilder get() {
            return new GetBuilder(httpd);
        }

        /**
         * 创建 PostBuilder 实例，用于构建 POST 请求。
         *
         * @return PostBuilder 实例
         */
        public PostBuilder post() {
            return new PostBuilder(httpd);
        }

        /**
         * 创建 PutBuilder 实例，用于构建 PUT 请求。
         *
         * @return PutBuilder 实例
         */
        public PutBuilder put() {
            return new PutBuilder(httpd);
        }

        /**
         * 创建 HeadBuilder 实例，用于构建 HEAD 请求。
         *
         * @return HeadBuilder 实例
         */
        public HeadBuilder head() {
            return new HeadBuilder(httpd);
        }

        /**
         * 创建 DeleteBuilder 实例，用于构建 DELETE 请求。
         *
         * @return DeleteBuilder 实例
         */
        public DeleteBuilder delete() {
            return new DeleteBuilder(httpd);
        }

        /**
         * 获取当前 Httpd 客户端。
         *
         * @return Httpd 实例
         */
        public Httpd getHttpd() {
            return httpd;
        }

        /**
         * 设置 Httpd 客户端。
         *
         * @param httpd Httpd 实例
         */
        public void setHttpd(Httpd httpd) {
            this.httpd = httpd;
        }
    }

}