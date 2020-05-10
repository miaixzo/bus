/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.http.bodys;

import org.aoju.bus.core.io.BufferSink;
import org.aoju.bus.core.io.ByteString;
import org.aoju.bus.core.io.Source;
import org.aoju.bus.core.lang.MediaType;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.http.Builder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 内容对象
 *
 * @author Kimi Liu
 * @version 5.9.0
 * @since JDK 1.8+
 */
public abstract class RequestBody {

    /**
     * 返回传输{@code content}的新请求体。
     * 如果{@code contentType}是非空且缺少字符集，则使用UTF-8
     *
     * @param contentType 请求类型
     * @param content     内容
     * @return 传输请求体
     */
    public static RequestBody create(MediaType contentType, String content) {
        Charset charset = org.aoju.bus.core.lang.Charset.UTF_8;
        if (contentType != null) {
            charset = contentType.charset();
            if (charset == null) {
                charset = org.aoju.bus.core.lang.Charset.UTF_8;
                contentType = MediaType.valueOf(contentType + "; charset=utf-8");
            }
        }
        byte[] bytes = content.getBytes(charset);
        return create(contentType, bytes);
    }

    /**
     * 返回发送{@code content}的新请求体
     *
     * @param contentType 请求类型
     * @param content     内容
     * @return 传输请求体
     */
    public static RequestBody create(
            final MediaType contentType,
            final ByteString content) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return content.size();
            }

            @Override
            public void writeTo(BufferSink sink) throws IOException {
                sink.write(content);
            }
        };
    }

    /**
     * 发送{@code content}的新请求体
     *
     * @param contentType 请求类型
     * @param content     内容
     * @return 传输请求体
     */
    public static RequestBody create(final MediaType contentType, final byte[] content) {
        return create(contentType, content, 0, content.length);
    }

    /**
     * 发送{@code content}的新请求体
     *
     * @param contentType 请求类型
     * @param content     内容
     * @param offset      偏移量
     * @param byteCount   当前大小
     * @return 传输请求体
     */
    public static RequestBody create(final MediaType contentType, final byte[] content,
                                     final int offset, final int byteCount) {
        if (content == null) throw new NullPointerException("content == null");
        Builder.checkOffsetAndCount(content.length, offset, byteCount);
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return byteCount;
            }

            @Override
            public void writeTo(BufferSink sink) throws IOException {
                sink.write(content, offset, byteCount);
            }
        };
    }

    /**
     * 新的请求体，该请求体传输{@code file}的内容
     *
     * @param contentType 请求类型
     * @param file        文件
     * @return 传输请求体
     */
    public static RequestBody create(final MediaType contentType, final File file) {
        if (file == null) throw new NullPointerException("file == null");

        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return file.length();
            }

            @Override
            public void writeTo(BufferSink sink) throws IOException {
                Source source = null;
                try {
                    source = IoUtils.source(file);
                    sink.writeAll(source);
                } finally {
                    IoUtils.close(source);
                }
            }
        };
    }

    /**
     * @return 返回此主体的媒体类型
     */
    public abstract MediaType contentType();

    /**
     * 返回调用{@link #writeTo}时写入{@code sink}的字节数，如果该计数未知，则返回-1
     *
     * @return 计数信息
     * @throws IOException 异常
     */
    public long contentLength() throws IOException {
        return -1;
    }

    /**
     * 将此请求的内容写入{@code sink}
     *
     * @param sink 缓存区
     * @throws IOException 异常信息
     */
    public abstract void writeTo(BufferSink sink) throws IOException;

}
