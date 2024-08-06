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

import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.http.Headers;

/**
 * 缓存控制头，带有来自服务器或客户端的缓存指令。 这些指令设置了哪些响应可以存储，以及哪些请求可以由存储的响应来满足的策略
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CacheControl {

    /**
     * 需要对响应进行网络验证的缓存控制请求指令。请注意，缓存可以通过有条件的GET请求来辅助这些请求.
     */
    public static final CacheControl FORCE_NETWORK = new Builder().noCache().build();

    /**
     * 仅使用缓存的缓存控制请求指令，即使缓存的响应已过期。如果响应在缓存中不可用， 或者需要服务器验证，调用将失败
     */
    public static final CacheControl FORCE_CACHE = new Builder().onlyIfCached()
            .maxStale(Integer.MAX_VALUE, TimeUnit.SECONDS).build();

    /**
     * 在请求中，它意味着不使用缓存来满足请求
     */
    private final boolean noCache;
    /**
     * 如果为真，则不应缓存此响应
     */
    private final boolean noStore;
    /**
     * 响应服务日期之后的持续时间，可以在不进行验证的情况下提供该响应
     */
    private final int maxAgeSeconds;
    /**
     * "s-maxage"指令是共享缓存的最大年龄。不要和非共享缓存的"max-age"混淆， 就像在Firefox和Chrome中一样，这个指令在这个缓存中不受重视
     */
    private final int sMaxAgeSeconds;
    private final boolean isPrivate;
    private final boolean isPublic;
    private final boolean mustRevalidate;
    private final int maxStaleSeconds;
    private final int minFreshSeconds;
    /**
     * 这个字段的名称“only-if-cached”具有误导性。它的实际意思是“不要使用网络” 它是由客户端设置的，客户端只希望请求能够被缓存完全满足。缓存的响应将需要 验证(即。如果设置了此标头，则不允许使用条件获取
     */
    private final boolean onlyIfCached;
    private final boolean noTransform;
    private final boolean immutable;

    String headerValue;

    private CacheControl(boolean noCache, boolean noStore, int maxAgeSeconds, int sMaxAgeSeconds, boolean isPrivate,
            boolean isPublic, boolean mustRevalidate, int maxStaleSeconds, int minFreshSeconds, boolean onlyIfCached,
            boolean noTransform, boolean immutable, String headerValue) {
        this.noCache = noCache;
        this.noStore = noStore;
        this.maxAgeSeconds = maxAgeSeconds;
        this.sMaxAgeSeconds = sMaxAgeSeconds;
        this.isPrivate = isPrivate;
        this.isPublic = isPublic;
        this.mustRevalidate = mustRevalidate;
        this.maxStaleSeconds = maxStaleSeconds;
        this.minFreshSeconds = minFreshSeconds;
        this.onlyIfCached = onlyIfCached;
        this.noTransform = noTransform;
        this.immutable = immutable;
        this.headerValue = headerValue;
    }

    CacheControl(Builder builder) {
        this.noCache = builder.noCache;
        this.noStore = builder.noStore;
        this.maxAgeSeconds = builder.maxAgeSeconds;
        this.sMaxAgeSeconds = -1;
        this.isPrivate = false;
        this.isPublic = false;
        this.mustRevalidate = false;
        this.maxStaleSeconds = builder.maxStaleSeconds;
        this.minFreshSeconds = builder.minFreshSeconds;
        this.onlyIfCached = builder.onlyIfCached;
        this.noTransform = builder.noTransform;
        this.immutable = builder.immutable;
    }

    /**
     * 返回{@code headers}的缓存指令。 如果存在Cache-Control和Pragma头文件，则会同时显示它们
     *
     * @param headers headers
     * @return 缓存控制头
     */
    public static CacheControl parse(Headers headers) {
        boolean noCache = false;
        boolean noStore = false;
        int maxAgeSeconds = -1;
        int sMaxAgeSeconds = -1;
        boolean isPrivate = false;
        boolean isPublic = false;
        boolean mustRevalidate = false;
        int maxStaleSeconds = -1;
        int minFreshSeconds = -1;
        boolean onlyIfCached = false;
        boolean noTransform = false;
        boolean immutable = false;

        boolean canUseHeaderValue = true;
        String headerValue = null;

        for (int i = 0, size = headers.size(); i < size; i++) {
            String name = headers.name(i);
            String value = headers.value(i);

            if (name.equalsIgnoreCase(HTTP.CACHE_CONTROL)) {
                if (null != headerValue) {
                    // 多个cache-control头文件意味着不能使用原始值
                    canUseHeaderValue = false;
                } else {
                    headerValue = value;
                }
            } else if (name.equalsIgnoreCase("Pragma")) {
                // 可以指定额外的缓存控制参数。只是以防万一
                canUseHeaderValue = false;
            } else {
                continue;
            }

            int pos = 0;
            while (pos < value.length()) {
                int tokenStart = pos;
                pos = Headers.skipUntil(value, pos, "=,;");
                String directive = value.substring(tokenStart, pos).trim();
                String parameter;

                if (pos == value.length() || value.charAt(pos) == Symbol.C_COMMA
                        || value.charAt(pos) == Symbol.C_SEMICOLON) {
                    pos++; // consume ',' or ';' (if necessary)
                    parameter = null;
                } else {
                    pos++; // consume '='
                    pos = Headers.skipWhitespace(value, pos);

                    // quoted string
                    if (pos < value.length() && value.charAt(pos) == '\"') {
                        pos++; // consume '"' open quote
                        int parameterStart = pos;
                        pos = Headers.skipUntil(value, pos, "\"");
                        parameter = value.substring(parameterStart, pos);
                        pos++; // consume '"' close quote (if necessary)

                        // unquoted string
                    } else {
                        int parameterStart = pos;
                        pos = Headers.skipUntil(value, pos, ",;");
                        parameter = value.substring(parameterStart, pos).trim();
                    }
                }

                if ("no-cache".equalsIgnoreCase(directive)) {
                    noCache = true;
                } else if ("no-store".equalsIgnoreCase(directive)) {
                    noStore = true;
                } else if ("max-age".equalsIgnoreCase(directive)) {
                    maxAgeSeconds = Headers.parseSeconds(parameter, -1);
                } else if ("s-maxage".equalsIgnoreCase(directive)) {
                    sMaxAgeSeconds = Headers.parseSeconds(parameter, -1);
                } else if ("private".equalsIgnoreCase(directive)) {
                    isPrivate = true;
                } else if ("public".equalsIgnoreCase(directive)) {
                    isPublic = true;
                } else if ("must-revalidate".equalsIgnoreCase(directive)) {
                    mustRevalidate = true;
                } else if ("max-stale".equalsIgnoreCase(directive)) {
                    maxStaleSeconds = Headers.parseSeconds(parameter, Integer.MAX_VALUE);
                } else if ("min-fresh".equalsIgnoreCase(directive)) {
                    minFreshSeconds = Headers.parseSeconds(parameter, -1);
                } else if ("only-if-cached".equalsIgnoreCase(directive)) {
                    onlyIfCached = true;
                } else if ("no-transform".equalsIgnoreCase(directive)) {
                    noTransform = true;
                } else if ("immutable".equalsIgnoreCase(directive)) {
                    immutable = true;
                }
            }
        }

        if (!canUseHeaderValue) {
            headerValue = null;
        }
        return new CacheControl(noCache, noStore, maxAgeSeconds, sMaxAgeSeconds, isPrivate, isPublic, mustRevalidate,
                maxStaleSeconds, minFreshSeconds, onlyIfCached, noTransform, immutable, headerValue);
    }

    public boolean noCache() {
        return noCache;
    }

    public boolean noStore() {
        return noStore;
    }

    public int maxAgeSeconds() {
        return maxAgeSeconds;
    }

    public int sMaxAgeSeconds() {
        return sMaxAgeSeconds;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean mustRevalidate() {
        return mustRevalidate;
    }

    public int maxStaleSeconds() {
        return maxStaleSeconds;
    }

    public int minFreshSeconds() {
        return minFreshSeconds;
    }

    public boolean onlyIfCached() {
        return onlyIfCached;
    }

    public boolean noTransform() {
        return noTransform;
    }

    public boolean immutable() {
        return immutable;
    }

    @Override
    public String toString() {
        String result = headerValue;
        return null != result ? result : (headerValue = headerValue());
    }

    private String headerValue() {
        StringBuilder result = new StringBuilder();
        if (noCache)
            result.append("no-cache, ");
        if (noStore)
            result.append("no-store, ");
        if (maxAgeSeconds != -1)
            result.append("max-age=").append(maxAgeSeconds).append(", ");
        if (sMaxAgeSeconds != -1)
            result.append("s-maxage=").append(sMaxAgeSeconds).append(", ");
        if (isPrivate)
            result.append("private, ");
        if (isPublic)
            result.append("public, ");
        if (mustRevalidate)
            result.append("must-revalidate, ");
        if (maxStaleSeconds != -1)
            result.append("max-stale=").append(maxStaleSeconds).append(", ");
        if (minFreshSeconds != -1)
            result.append("min-fresh=").append(minFreshSeconds).append(", ");
        if (onlyIfCached)
            result.append("only-if-cached, ");
        if (noTransform)
            result.append("no-transform, ");
        if (immutable)
            result.append("immutable, ");
        if (result.length() == 0) {
            return Normal.EMPTY;
        }
        result.delete(result.length() - 2, result.length());
        return result.toString();
    }

    /**
     * 构建一个{@code Cache-Control}请求头
     */
    public static class Builder {

        boolean noCache;
        boolean noStore;
        int maxAgeSeconds = -1;
        int maxStaleSeconds = -1;
        int minFreshSeconds = -1;
        boolean onlyIfCached;
        boolean noTransform;
        boolean immutable;

        /**
         * @return 不要接受未经验证的缓存响应
         */
        public Builder noCache() {
            this.noCache = true;
            return this;
        }

        /**
         * @return 不要将服务器的响应存储在任何缓存中
         */
        public Builder noStore() {
            this.noStore = true;
            return this;
        }

        /**
         * 设置缓存响应的最大时间。如果缓存响应的时间超过{@code maxAge}，则将不使用它，并发出网络请求
         *
         * @param maxAge   一个非负整数。它以{@link TimeUnit#SECONDS}精度存储和传输;精度会降低
         * @param timeUnit 单位
         * @return the builder
         */
        public Builder maxAge(int maxAge, TimeUnit timeUnit) {
            if (maxAge < 0)
                throw new IllegalArgumentException("maxAge < 0: " + maxAge);
            long maxAgeSecondsLong = timeUnit.toSeconds(maxAge);
            this.maxAgeSeconds = maxAgeSecondsLong > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) maxAgeSecondsLong;
            return this;
        }

        /**
         * 接受超过新鲜度生存期的缓存响应，最多接受{@code maxStale}。如果未指定，则不使用陈旧的缓存响应
         *
         * @param maxStale 一个非负整数。它以{@link TimeUnit#SECONDS}精度存储和传输;精度会降低
         * @param timeUnit 单位
         * @return the builder
         */
        public Builder maxStale(int maxStale, TimeUnit timeUnit) {
            if (maxStale < 0)
                throw new IllegalArgumentException("maxStale < 0: " + maxStale);
            long maxStaleSecondsLong = timeUnit.toSeconds(maxStale);
            this.maxStaleSeconds = maxStaleSecondsLong > Integer.MAX_VALUE ? Integer.MAX_VALUE
                    : (int) maxStaleSecondsLong;
            return this;
        }

        /**
         * 设置一个响应持续刷新的最小秒数。如果响应在{@code minFresh}过期后失效，则将不使用缓存的响应，并发出网络请求
         *
         * @param minFresh 一个非负整数。它以{@link TimeUnit#SECONDS}精度存储和传输;精度会降低
         * @param timeUnit 单位
         * @return the builder
         */
        public Builder minFresh(int minFresh, TimeUnit timeUnit) {
            if (minFresh < 0)
                throw new IllegalArgumentException("minFresh < 0: " + minFresh);
            long minFreshSecondsLong = timeUnit.toSeconds(minFresh);
            this.minFreshSeconds = minFreshSecondsLong > Integer.MAX_VALUE ? Integer.MAX_VALUE
                    : (int) minFreshSecondsLong;
            return this;
        }

        /**
         * @return 只接受缓存中的响应
         */
        public Builder onlyIfCached() {
            this.onlyIfCached = true;
            return this;
        }

        /**
         * @return 不要接受改变的回应
         */
        public Builder noTransform() {
            this.noTransform = true;
            return this;
        }

        public Builder immutable() {
            this.immutable = true;
            return this;
        }

        public CacheControl build() {
            return new CacheControl(this);
        }
    }

}
