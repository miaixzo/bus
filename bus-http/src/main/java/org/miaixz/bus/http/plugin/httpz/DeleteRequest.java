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

import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.bodys.RequestBody;

/**
 * DELETE 请求处理类，封装 DELETE 请求的参数和配置。 DELETE 请求用于删除指定资源，不包含请求体。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DeleteRequest extends HttpRequest {

    /**
     * 构造函数，初始化 DELETE 请求的参数。
     *
     * @param url     请求的 URL
     * @param tag     请求的标签，用于取消请求
     * @param params  查询参数
     * @param headers 请求头
     * @param id      请求的唯一标识
     */
    public DeleteRequest(String url, Object tag, Map<String, String> params, Map<String, String> headers, String id) {
        super(url, tag, params, headers, null, null, null, id);
    }

    /**
     * 构建 DELETE 请求的请求体。 DELETE 请求无请求体，返回 null。
     *
     * @return 始终返回 null
     */
    @Override
    protected RequestBody buildRequestBody() {
        return null;
    }

    /**
     * 构建 DELETE 请求对象。
     *
     * @param requestBody 请求体（DELETE 请求为 null）
     * @return 构建完成的 Request 对象
     */
    @Override
    protected Request buildRequest(RequestBody requestBody) {
        return builder.delete().build(); // 使用 DELETE 方法构建请求
    }

}