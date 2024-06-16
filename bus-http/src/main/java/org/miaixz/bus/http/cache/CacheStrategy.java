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
package org.miaixz.bus.http.cache;

import org.miaixz.bus.core.lang.Header;
import org.miaixz.bus.core.lang.Http;
import org.miaixz.bus.http.Builder;
import org.miaixz.bus.http.Headers;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.metric.Internal;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 给定一个请求和缓存的响应，这将确定是使用网络、缓存还是两者都使用
 * 选择缓存策略可能会向请求添加条件(比如条件get的“if - modified - since”报头)
 * 或向缓存的响应添加警告(如果缓存的数据可能过时)
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CacheStrategy {

    /**
     * 请求在网络上发送，如果调用不使用网络则为空
     */
    public final Request networkRequest;

    /**
     * 缓存响应以返回或验证;如果这个调用不使用缓存，则为null
     */
    public final Response cacheResponse;

    CacheStrategy(Request networkRequest, Response cacheResponse) {
        this.networkRequest = networkRequest;
        this.cacheResponse = cacheResponse;
    }

    /**
     * 如果{@code response}可以存储为以后服务另一个请求，则返回true
     *
     * @param response 相应信息
     * @param request  请求信息
     * @return the  true/false
     */
    public static boolean isCacheable(Response response, Request request) {
        // 总是去网络获取非缓存的响应代码(RFC 7231 section 6.1)，这个实现不支持缓存部分内容
        switch (response.code()) {
            case Http.HTTP_OK:
            case Http.HTTP_NOT_AUTHORITATIVE:
            case Http.HTTP_NO_CONTENT:
            case Http.HTTP_MULT_CHOICE:
            case Http.HTTP_MOVED_PERM:
            case Http.HTTP_NOT_FOUND:
            case Http.HTTP_BAD_METHOD:
            case Http.HTTP_GONE:
            case Http.HTTP_REQ_TOO_LONG:
            case Http.HTTP_NOT_IMPLEMENTED:
            case Http.HTTP_PERM_REDIRECT:
                // 这些代码可以被缓存，除非标头禁止
                break;
            case Http.HTTP_MOVED_TEMP:
            case Http.HTTP_TEMP_REDIRECT:
                if (null != response.header(Header.EXPIRES)
                        || response.cacheControl().maxAgeSeconds() != -1
                        || response.cacheControl().isPublic()
                        || response.cacheControl().isPrivate()) {
                    break;
                }
            default:
                // 不能缓存所有其他代码
                return false;
        }

        // 针对请求或响应的'no-store'指令会阻止缓存响应。
        return !response.cacheControl().noStore() && !request.cacheControl().noStore();
    }

    public static class Factory {

        final long nowMillis;
        final Request request;
        final Response cacheResponse;

        /**
         * 服务器提供缓存的响应的时间
         */
        private Date servedDate;
        private String servedDateString;

        /**
         * 缓存响应的最后修改日期
         */
        private Date lastModified;
        private String lastModifiedString;

        /**
         * 缓存的响应的过期日期。如果设置了该字段和最大值，则首选最大值
         */
        private Date expires;

        /**
         * 扩展头由Httpd设置，指定缓存的HTTP请求首次启动时的时间戳
         */
        private long sentRequestMillis;

        /**
         * 由Httpd设置的扩展头，指定首次接收缓存的HTTP响应时的时间戳
         */
        private long receivedResponseMillis;

        /**
         * 缓存响应的Etag
         */
        private String etag;

        /**
         * 缓存响应的时间
         */
        private int ageSeconds = -1;

        public Factory(long nowMillis, Request request, Response cacheResponse) {
            this.nowMillis = nowMillis;
            this.request = request;
            this.cacheResponse = cacheResponse;

            if (null != cacheResponse) {
                this.sentRequestMillis = cacheResponse.sentRequestAtMillis();
                this.receivedResponseMillis = cacheResponse.receivedResponseAtMillis();
                Headers headers = cacheResponse.headers();
                for (int i = 0, size = headers.size(); i < size; i++) {
                    String fieldName = headers.name(i);
                    String value = headers.value(i);
                    if ("Date".equalsIgnoreCase(fieldName)) {
                        servedDate = Builder.parse(value);
                        servedDateString = value;
                    } else if ("Expires".equalsIgnoreCase(fieldName)) {
                        expires = Builder.parse(value);
                    } else if ("Last-Modified".equalsIgnoreCase(fieldName)) {
                        lastModified = Builder.parse(value);
                        lastModifiedString = value;
                    } else if ("ETag".equalsIgnoreCase(fieldName)) {
                        etag = value;
                    } else if ("Age".equalsIgnoreCase(fieldName)) {
                        ageSeconds = Headers.parseSeconds(value, -1);
                    }
                }
            }
        }

        /**
         * 如果请求包含保存服务器不发送客户机本地响应的条件，则返回true
         * 当请求按照自己的条件加入队列时，将不使用内置的响应缓存。
         *
         * @param request 氢气信息
         * @return the true/false
         */
        private static boolean hasConditions(Request request) {
            return null != request.header(Header.IF_MODIFIED_SINCE) || null != request.header(Header.IF_NONE_MATCH);
        }

        /**
         * @return 使用缓存的响应{@code response}返回满足{@code request}的策略
         */
        public CacheStrategy get() {
            CacheStrategy candidate = getCandidate();

            if (null != candidate.networkRequest && request.cacheControl().onlyIfCached()) {
                // 被禁止使用网络和缓存是不够的
                return new CacheStrategy(null, null);
            }

            return candidate;
        }

        /**
         * @return 如果请求可以使用网络，则返回要使用的策略
         */
        private CacheStrategy getCandidate() {
            //没有缓存的响应.
            if (null == cacheResponse) {
                return new CacheStrategy(request, null);
            }

            // 如果缺少必要的握手，则删除缓存的响应。
            if (request.isHttps() && null == cacheResponse.handshake()) {
                return new CacheStrategy(request, null);
            }
            // 如果不应该存储此响应，则不应该将其用作响应源。
            // 只要持久性存储表现良好且规则不变，此检查就应该是多余的
            if (!isCacheable(cacheResponse, request)) {
                return new CacheStrategy(request, null);
            }

            CacheControl requestCaching = request.cacheControl();
            if (requestCaching.noCache() || hasConditions(request)) {
                return new CacheStrategy(request, null);
            }

            CacheControl responseCaching = cacheResponse.cacheControl();

            long ageMillis = cacheResponseAge();
            long freshMillis = computeFreshnessLifetime();

            if (requestCaching.maxAgeSeconds() != -1) {
                freshMillis = Math.min(freshMillis, TimeUnit.SECONDS.toMillis(requestCaching.maxAgeSeconds()));
            }

            long minFreshMillis = 0;
            if (requestCaching.minFreshSeconds() != -1) {
                minFreshMillis = TimeUnit.SECONDS.toMillis(requestCaching.minFreshSeconds());
            }

            long maxStaleMillis = 0;
            if (!responseCaching.mustRevalidate() && requestCaching.maxStaleSeconds() != -1) {
                maxStaleMillis = TimeUnit.SECONDS.toMillis(requestCaching.maxStaleSeconds());
            }

            if (!responseCaching.noCache() && ageMillis + minFreshMillis < freshMillis + maxStaleMillis) {
                Response.Builder builder = cacheResponse.newBuilder();
                if (ageMillis + minFreshMillis >= freshMillis) {
                    builder.addHeader("Warning", "110 HttpURLConnection \"Response is stale\"");
                }
                long oneDayMillis = 24 * 60 * 60 * 1000L;
                if (ageMillis > oneDayMillis && isFreshnessLifetimeHeuristic()) {
                    builder.addHeader("Warning", "113 HttpURLConnection \"Heuristic expiration\"");
                }
                return new CacheStrategy(null, builder.build());
            }

            // 查找要添加到请求的条件。如果条件满足，则不会传输响应体。
            String conditionName;
            String conditionValue;
            if (null != etag) {
                conditionName = "If-None-Match";
                conditionValue = etag;
            } else if (null != lastModified) {
                conditionName = "If-Modified-Since";
                conditionValue = lastModifiedString;
            } else if (null != servedDate) {
                conditionName = "If-Modified-Since";
                conditionValue = servedDateString;
            } else {
                return new CacheStrategy(request, null);
            }

            Headers.Builder conditionalRequestHeaders = request.headers().newBuilder();
            Internal.instance.addLenient(conditionalRequestHeaders, conditionName, conditionValue);

            Request conditionalRequest = request.newBuilder()
                    .headers(conditionalRequestHeaders.build())
                    .build();
            return new CacheStrategy(conditionalRequest, cacheResponse);
        }

        /**
         * @return 响应刷新的毫秒数，从服务日期开始
         */
        private long computeFreshnessLifetime() {
            CacheControl responseCaching = cacheResponse.cacheControl();
            if (responseCaching.maxAgeSeconds() != -1) {
                return TimeUnit.SECONDS.toMillis(responseCaching.maxAgeSeconds());
            } else if (null != expires) {
                long servedMillis = null != servedDate
                        ? servedDate.getTime()
                        : receivedResponseMillis;
                long delta = expires.getTime() - servedMillis;
                return delta > 0 ? delta : 0;
            } else if (null != lastModified
                    && null == cacheResponse.request().url().query()) {

                // 根据HTTP RFC的建议并在Firefox中实现，
                // 文档的最大值应该默认为其被提供时文档值的10%。
                // 默认过期日期不用于包含查询的uri
                long servedMillis = null != servedDate
                        ? servedDate.getTime()
                        : sentRequestMillis;
                long delta = servedMillis - lastModified.getTime();
                return delta > 0 ? (delta / 10) : 0;
            }
            return 0;
        }

        /**
         * @return 返回响应的当前值(以毫秒为单位)。计算按RFC 7234规定，4.2.3计算值
         */
        private long cacheResponseAge() {
            long apparentReceivedAge = null != servedDate
                    ? Math.max(0, receivedResponseMillis - servedDate.getTime())
                    : 0;
            long receivedAge = ageSeconds != -1
                    ? Math.max(apparentReceivedAge, TimeUnit.SECONDS.toMillis(ageSeconds))
                    : apparentReceivedAge;
            long responseDuration = receivedResponseMillis - sentRequestMillis;
            long residentDuration = nowMillis - receivedResponseMillis;
            return receivedAge + responseDuration + residentDuration;
        }

        /**
         * @return 如果computeFreshnessLifetime使用了启发式，则返回true
         * 如果我们使用启发式来服务大于24小时的缓存响应，则需要附加一个警告
         */
        private boolean isFreshnessLifetimeHeuristic() {
            return cacheResponse.cacheControl().maxAgeSeconds() == -1 && null == expires;
        }
    }

}
