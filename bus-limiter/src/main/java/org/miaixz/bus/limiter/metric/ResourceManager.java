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
package org.miaixz.bus.limiter.metric;

import org.miaixz.bus.limiter.magic.annotation.Limiting;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 资源管理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ResourceManager {

    /**
     * 用于判断是否添加过方法（打印日志需要...）
     */
    private static final Set<String> PROTECTED_METHODS = new HashSet<>();
    /**
     * 资源缓存
     */
    private final Map<String, Protection> map = new ConcurrentHashMap<>();

    /**
     * 判断资源是否是保护的
     *
     * @param resourceKey 资源标识
     * @return the true/false
     */
    public static boolean contain(String resourceKey) {
        return PROTECTED_METHODS.contains(resourceKey);
    }

    /**
     * 添加到保护资源
     *
     * @param resourceKey 资源标识
     */
    public static void add(String resourceKey) {
        PROTECTED_METHODS.add(resourceKey);
    }

    /**
     * 查看资源是否可以执行
     *
     * @param resourceKey 资源标识
     * @param limiting    限流注解
     * @return the true/false
     */
    public boolean entry(String resourceKey, Limiting limiting) throws IllegalStateException {
        Protection protection = map.get(resourceKey);

        // 缓存操作
        if (Objects.isNull(protection)) {
            protection = new Protection(limiting);
            map.put(resourceKey, protection);
        }

        // 判断是否过期
        if (protection.isExpire()) {
            protection.reset();
        }

        // 判断次数是否超出
        if (!protection.isAllow()) {
            return false;
        }

        protection.allowCount -= 1;
        return true;
    }

    /**
     * 清理
     *
     * @return the true/false
     */
    public boolean isClear() {
        map.keySet().forEach(key -> {
            Protection protection = map.get(key);
            if (Objects.nonNull(protection) && protection.isExpire()) {
                map.remove(key);
            }
        });
        return map.size() == 0;
    }

    /**
     * 资源保护
     */
    static class Protection {

        /**
         * 注解对象
         */
        Limiting limiting;
        /**
         * 保护到期的时间
         */
        LocalDateTime targetTime;
        /**
         * 剩余的请求次数
         */
        int allowCount;

        public Protection(Limiting limiting) {
            this.limiting = limiting;
            this.reset();
        }

        /**
         * 是否过期
         *
         * @return the true/false
         */
        public boolean isExpire() {
            return targetTime.compareTo(LocalDateTime.now()) < 0;
        }

        /**
         * 重置
         */
        public void reset() {
            this.allowCount = limiting.count();
            this.targetTime = LocalDateTime.now().plusSeconds(limiting.duration());
        }

        /**
         * 是否允许访问
         *
         * @return the true/false
         */
        public boolean isAllow() {
            return allowCount > 0;
        }
    }

}
