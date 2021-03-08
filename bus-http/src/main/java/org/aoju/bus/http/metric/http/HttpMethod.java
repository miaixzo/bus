/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.http.metric.http;

import org.aoju.bus.core.lang.Http;

/**
 * Http请求方法
 *
 * @author Kimi Liu
 * @version 6.2.1
 * @since JDK 1.8+
 */
public final class HttpMethod {

    private HttpMethod() {
    }

    public static boolean invalidatesCache(String method) {
        return Http.POST.equals(method)
                || Http.PUT.equals(method)
                || Http.PATCH.equals(method)
                || Http.DELETE.equals(method)
                || Http.MOVE.equals(method);
    }

    public static boolean requiresRequestBody(String method) {
        return Http.POST.equals(method)
                || Http.PUT.equals(method)
                || Http.PATCH.equals(method)
                || Http.PROPPATCH.equals(method)
                || Http.REPORT.equals(method);

    }

    public static boolean permitsRequestBody(String method) {
        return !(method.equals("GET") || method.equals("HEAD"));
    }

    public static boolean redirectsWithBody(String method) {
        // (WebDAV)重定向还应该维护请求体
        return Http.PROPFIND.equals(method);
    }

    public static boolean redirectsToGet(String method) {
        // 除了PROPFIND之外的所有请求都应该重定向到GET请求.
        return !Http.PROPFIND.equals(method);
    }

}
