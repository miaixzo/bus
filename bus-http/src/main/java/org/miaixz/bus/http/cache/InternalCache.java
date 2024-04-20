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
package org.miaixz.bus.http.cache;

import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;

import java.io.IOException;

/**
 * Http's 的内部缓存接口
 * 应用程序不应该实现这个:而是使用{@link Cache}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface InternalCache {

    Response get(Request request) throws IOException;

    CacheRequest put(Response response) throws IOException;

    /**
     * 删除提供的{@code request}的所有缓存项。当客户端使缓存无效时(如发出POST请求时)，将调用此方法
     *
     * @param request 请求
     * @throws IOException 异常
     */
    void remove(Request request) throws IOException;

    /**
     * 通过使用来自{@code network}的报头更新存储的缓存响应来处理条件请求
     * 如果存储的响应在返回{@code cached}后发生了变化，这将不起任何作用
     *
     * @param cached  缓存请求
     * @param network 网络请求
     */
    void update(Response cached, Response network);

    /**
     * 跟踪此缓存满足的条件GET
     */
    void trackConditionalCacheHit();

    /**
     * 跟踪一个满足{@code cacheStrategy}的HTTP响应
     *
     * @param cacheStrategy 请求和缓存的响应
     */
    void trackResponse(CacheStrategy cacheStrategy);

}
