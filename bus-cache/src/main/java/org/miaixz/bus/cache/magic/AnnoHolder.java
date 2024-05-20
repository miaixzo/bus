/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.cache.magic;

import org.miaixz.bus.cache.magic.annotation.CacheKey;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class AnnoHolder {

    private Method method;

    //  cached 内容
    private String cache;
    private String prefix;
    private int expire;

    // @CacheKey
    private Map<Integer, CacheKey> cacheKeyMap;

    private int multiIndex = -1;

    private String id;

    private AnnoHolder(Method method,
                       String cache, String prefix, int expire,
                       Map<Integer, CacheKey> cacheKeyMap, int multiIndex, String id) {
        this.method = method;
        this.cache = cache;
        this.prefix = prefix;
        this.expire = expire;
        this.cacheKeyMap = cacheKeyMap;
        this.multiIndex = multiIndex;
        this.id = id;
    }

    public Method getMethod() {
        return method;
    }

    public String getCache() {
        return cache;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getExpire() {
        return expire;
    }

    public Map<Integer, CacheKey> getCacheKeyMap() {
        return cacheKeyMap;
    }

    public int getMultiIndex() {
        return multiIndex;
    }

    public boolean isMulti() {
        return multiIndex != -1;
    }

    public String getId() {
        return id;
    }

    public static class Builder {

        private Method method;

        private String cache;

        private String prefix;

        private int expire;

        private Map<Integer, CacheKey> cacheKeyMap;

        private int multiIndex = -1;

        private String id;

        private Builder(Method method) {
            this.method = method;
        }

        public static Builder newBuilder(Method method) {
            return new Builder(method);
        }

        public Builder setCache(String cache) {
            this.cache = cache;
            return this;
        }

        public Builder setPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Builder setExpire(int expire) {
            this.expire = expire;
            return this;
        }

        public Builder setMultiIndex(int multiIndex) {
            this.multiIndex = multiIndex;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setCacheKeyMap(Map<Integer, CacheKey> cacheKeyMap) {
            this.cacheKeyMap = cacheKeyMap;
            return this;
        }

        public AnnoHolder build() {
            return new AnnoHolder(method, cache, prefix, expire, cacheKeyMap, multiIndex, id);
        }
    }

}
