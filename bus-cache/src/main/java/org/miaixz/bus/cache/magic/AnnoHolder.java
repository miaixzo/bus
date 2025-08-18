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
package org.miaixz.bus.cache.magic;

import java.lang.reflect.Method;
import java.util.Map;
import org.miaixz.bus.cache.magic.annotation.CacheKey;

/**
 * 注解持有者类
 * <p>
 * 用于存储和访问方法上的缓存相关注解信息，包括缓存名称、前缀、过期时间等。 提供了Builder模式来方便地创建AnnoHolder实例。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AnnoHolder {

    /**
     * 方法对象
     */
    private final Method method;

    /**
     * 缓存名称
     */
    private final String cache;

    /**
     * 缓存键前缀
     */
    private final String prefix;

    /**
     * 缓存过期时间（毫秒）
     */
    private final int expire;

    /**
     * 缓存键注解映射，键为参数索引，值为CacheKey注解
     */
    private final Map<Integer, CacheKey> cacheKeyMap;

    /**
     * 多键参数索引，-1表示不是多键缓存
     */
    private final int multiIndex;

    /**
     * 缓存标识符
     */
    private final String id;

    /**
     * 私有构造方法
     *
     * @param method      方法对象
     * @param cache       缓存名称
     * @param prefix      缓存键前缀
     * @param expire      缓存过期时间（毫秒）
     * @param cacheKeyMap 缓存键注解映射
     * @param multiIndex  多键参数索引
     * @param id          缓存标识符
     */
    private AnnoHolder(Method method, String cache, String prefix, int expire, Map<Integer, CacheKey> cacheKeyMap,
            int multiIndex, String id) {
        this.method = method;
        this.cache = cache;
        this.prefix = prefix;
        this.expire = expire;
        this.cacheKeyMap = cacheKeyMap;
        this.multiIndex = multiIndex;
        this.id = id;
    }

    /**
     * 获取方法对象
     *
     * @return 方法对象
     */
    public Method getMethod() {
        return method;
    }

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    public String getCache() {
        return cache;
    }

    /**
     * 获取缓存键前缀
     *
     * @return 缓存键前缀
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * 获取缓存过期时间（毫秒）
     *
     * @return 缓存过期时间（毫秒）
     */
    public int getExpire() {
        return expire;
    }

    /**
     * 获取缓存键注解映射
     *
     * @return 缓存键注解映射，键为参数索引，值为CacheKey注解
     */
    public Map<Integer, CacheKey> getCacheKeyMap() {
        return cacheKeyMap;
    }

    /**
     * 获取多键参数索引
     *
     * @return 多键参数索引，-1表示不是多键缓存
     */
    public int getMultiIndex() {
        return multiIndex;
    }

    /**
     * 判断是否为多键缓存
     *
     * @return 如果是多键缓存则返回true，否则返回false
     */
    public boolean isMulti() {
        return multiIndex != -1;
    }

    /**
     * 获取缓存标识符
     *
     * @return 缓存标识符
     */
    public String getId() {
        return id;
    }

    /**
     * AnnoHolder构建器类
     * <p>
     * 使用Builder模式来创建AnnoHolder实例，提供流畅的API设置各个属性。
     * </p>
     */
    public static class Builder {
        /**
         * 方法对象
         */
        private Method method;

        /**
         * 缓存名称
         */
        private String cache;

        /**
         * 缓存键前缀
         */
        private String prefix;

        /**
         * 缓存过期时间（毫秒）
         */
        private int expire;

        /**
         * 缓存键注解映射
         */
        private Map<Integer, CacheKey> cacheKeyMap;

        /**
         * 多键参数索引，默认为-1
         */
        private int multiIndex = -1;

        /**
         * 缓存标识符
         */
        private String id;

        /**
         * 私有构造方法
         *
         * @param method 方法对象
         */
        private Builder(Method method) {
            this.method = method;
        }

        /**
         * 创建新的Builder实例
         *
         * @param method 方法对象
         * @return Builder实例
         */
        public static Builder newBuilder(Method method) {
            return new Builder(method);
        }

        /**
         * 设置缓存名称
         *
         * @param cache 缓存名称
         * @return Builder实例
         */
        public Builder setCache(String cache) {
            this.cache = cache;
            return this;
        }

        /**
         * 设置缓存键前缀
         *
         * @param prefix 缓存键前缀
         * @return Builder实例
         */
        public Builder setPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * 设置缓存过期时间
         *
         * @param expire 缓存过期时间（毫秒）
         * @return Builder实例
         */
        public Builder setExpire(int expire) {
            this.expire = expire;
            return this;
        }

        /**
         * 设置多键参数索引
         *
         * @param multiIndex 多键参数索引
         * @return Builder实例
         */
        public Builder setMultiIndex(int multiIndex) {
            this.multiIndex = multiIndex;
            return this;
        }

        /**
         * 设置缓存标识符
         *
         * @param id 缓存标识符
         * @return Builder实例
         */
        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        /**
         * 设置缓存键注解映射
         *
         * @param cacheKeyMap 缓存键注解映射
         * @return Builder实例
         */
        public Builder setCacheKeyMap(Map<Integer, CacheKey> cacheKeyMap) {
            this.cacheKeyMap = cacheKeyMap;
            return this;
        }

        /**
         * 构建AnnoHolder实例
         *
         * @return AnnoHolder实例
         */
        public AnnoHolder build() {
            return new AnnoHolder(method, cache, prefix, expire, cacheKeyMap, multiIndex, id);
        }
    }

}