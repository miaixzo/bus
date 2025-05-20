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
package org.miaixz.bus.http.bodys;

import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.lang.MediaType;

/**
 * HTTP 响应体
 * <p>
 * 表示 HTTP 响应的内容，仅能使用一次。提供对响应内容的媒体类型、长度和数据源的访问。 使用字符串存储媒体类型以避免解析错误。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RealResponseBody extends ResponseBody {

    /**
     * 媒体类型字符串
     */
    private final String mediaType;
    /**
     * 内容长度
     */
    private final long length;
    /**
     * 数据源
     */
    private final BufferSource source;

    /**
     * 构造函数，初始化 RealResponseBody 实例
     *
     * @param mediaType 媒体类型字符串（可能为 null）
     * @param length    内容长度
     * @param source    数据源
     */
    public RealResponseBody(String mediaType, long length, BufferSource source) {
        this.mediaType = mediaType;
        this.length = length;
        this.source = source;
    }

    /**
     * 获取媒体类型
     *
     * @return 媒体类型（不存在时为 null）
     */
    @Override
    public MediaType mediaType() {
        return null != mediaType ? MediaType.valueOf(mediaType) : null;
    }

    /**
     * 获取内容长度
     *
     * @return 内容长度
     */
    @Override
    public long length() {
        return length;
    }

    /**
     * 获取数据源
     *
     * @return 数据源
     */
    @Override
    public BufferSource source() {
        return source;
    }

}