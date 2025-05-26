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
package org.miaixz.bus.goalie.magic;

import com.google.common.util.concurrent.RateLimiter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * 限流器类，基于令牌桶算法实现请求限流功能
 *
 * @author Justubborn
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@RequiredArgsConstructor
public class Limiter {

    /**
     * 请求来源的 IP 地址
     */
    private String ip;

    /**
     * 请求的方法名
     */
    private String method;

    /**
     * 请求的版本号
     */
    private String version;

    /**
     * 令牌桶的令牌生成速率（每秒生成令牌数）
     */
    private int tokenCount;

    /**
     * 令牌桶实例，使用 Google Guava 的 RateLimiter 实现限流
     */
    private volatile RateLimiter rateLimiter;

    /**
     * 初始化令牌桶，设置令牌生成速率 使用 synchronized 确保线程安全
     */
    public synchronized void initRateLimiter() {
        rateLimiter = RateLimiter.create(tokenCount);
    }

    /**
     * 获取令牌桶实例，采用双重检查锁确保线程安全
     *
     * @return 限流器实例（RateLimiter）
     */
    public RateLimiter fetchRateLimiter() {
        if (null == rateLimiter) {
            synchronized (this) {
                if (null == rateLimiter) {
                    rateLimiter = RateLimiter.create(tokenCount);
                }
            }
        }
        return rateLimiter;
    }

    /**
     * 尝试获取一个令牌，阻塞直到获取成功
     *
     * @return 获取令牌的等待时间（秒）
     */
    public double acquire() {
        return fetchRateLimiter().acquire();
    }

}