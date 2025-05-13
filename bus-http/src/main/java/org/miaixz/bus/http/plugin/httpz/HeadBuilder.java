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
package org.miaixz.bus.http.plugin.httpz;

import java.util.Map;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.http.Httpd;

/**
 * HEAD 请求参数构造器，提供链式调用接口来构建 HEAD 请求。 支持设置 URL、查询参数、请求头、标签和请求 ID。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HeadBuilder extends RequestBuilder<HeadBuilder> {

    /**
     * 构造函数，初始化 HeadBuilder。
     *
     * @param httpd Httpd 客户端
     */
    public HeadBuilder(Httpd httpd) {
        super(httpd);
    }

    /**
     * 构建 HEAD 请求的 RequestCall 对象。 如果存在查询参数，将其拼接至 URL。
     *
     * @return RequestCall 对象，用于执行请求
     */
    @Override
    public RequestCall build() {
        if (null != params) {
            url = append(url, params); // 拼接查询参数至 URL
        }
        return new HeadRequest(url, tag, params, headers, id).build(httpd);
    }

    /**
     * 将查询参数拼接至 URL。
     *
     * @param url    原始 URL
     * @param params 查询参数
     * @return 拼接后的 URL
     */
    protected String append(String url, Map<String, String> params) {
        if (null == url || null == params || params.isEmpty()) {
            return url; // 无参数或 URL 为空，直接返回
        }
        StringBuilder builder = new StringBuilder();
        params.forEach((k, v) -> {
            if (builder.length() == 0) {
                builder.append(Symbol.QUESTION_MARK); // 第一个参数前加 ?
            } else if (builder.length() > 0) {
                builder.append(Symbol.AND); // 后续参数加 &
            }
            builder.append(k);
            builder.append(Symbol.EQUAL).append(v);
        });
        return url + builder.toString(); // 返回拼接后的 URL
    }

}