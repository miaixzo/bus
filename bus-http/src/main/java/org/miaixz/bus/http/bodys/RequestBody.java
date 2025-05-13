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

import java.io.File;
import java.io.IOException;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.Builder;

/**
 * HTTP 请求体
 * <p>
 * 表示 HTTP 请求的内容，支持从字符串、字节数组、文件等创建请求体。 提供媒体类型、内容长度和写入功能，支持双工和一次性传输的特殊场景。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class RequestBody {

    /**
     * 从字符串创建请求体
     * <p>
     * 如果 <code>mediaType</code> 非空且缺少字符集，则使用 UTF-8。
     * </p>
     *
     * @param mediaType 媒体类型（可能为 null）
     * @param content   内容字符串
     * @return 请求体实例
     */
    public static RequestBody create(MediaType mediaType, String content) {
        java.nio.charset.Charset charset = Charset.UTF_8;
        if (mediaType != null) {
            charset = mediaType.charset();
            if (charset == null) {
                charset = Charset.UTF_8;
                mediaType = MediaType.valueOf(mediaType + "; charset=utf-8");
            }
        }
        byte[] bytes = content.getBytes(charset);
        return create(mediaType, bytes);
    }

    /**
     * 从 ByteString 创建请求体
     *
     * @param mediaType 媒体类型（可能为 null）
     * @param content   内容 ByteString
     * @return 请求体实例
     */
    public static RequestBody create(final MediaType mediaType, final ByteString content) {
        return new RequestBody() {
            @Override
            public MediaType mediaType() {
                return mediaType;
            }

            @Override
            public long length() {
                return content.size();
            }

            @Override
            public void writeTo(BufferSink sink) throws IOException {
                sink.write(content);
            }
        };
    }

    /**
     * 从字节数组创建请求体
     *
     * @param mediaType 媒体类型（可能为 null）
     * @param content   内容字节数组
     * @return 请求体实例
     * @throws NullPointerException 如果 content 为 null
     */
    public static RequestBody create(final MediaType mediaType, final byte[] content) {
        return create(mediaType, content, 0, content.length);
    }

    /**
     * 从字节数组部分创建请求体
     *
     * @param mediaType 媒体类型（可能为 null）
     * @param content   内容字节数组
     * @param offset    偏移量
     * @param byteCount 字节数
     * @return 请求体实例
     * @throws NullPointerException           如果 content 为 null
     * @throws ArrayIndexOutOfBoundsException 如果 offset 或 byteCount 无效
     */
    public static RequestBody create(final MediaType mediaType, final byte[] content, final int offset,
            final int byteCount) {
        if (null == content) {
            throw new NullPointerException("content == null");
        }
        Builder.checkOffsetAndCount(content.length, offset, byteCount);
        return new RequestBody() {
            @Override
            public MediaType mediaType() {
                return mediaType;
            }

            @Override
            public long length() {
                return byteCount;
            }

            @Override
            public void writeTo(BufferSink sink) throws IOException {
                sink.write(content, offset, byteCount);
            }
        };
    }

    /**
     * 从文件创建请求体
     *
     * @param mediaType 媒体类型（可能为 null）
     * @param file      文件
     * @return 请求体实例
     * @throws NullPointerException 如果 file 为 null
     */
    public static RequestBody create(final MediaType mediaType, final File file) {
        if (null == file) {
            throw new NullPointerException("file == null");
        }

        return new RequestBody() {
            @Override
            public MediaType mediaType() {
                return mediaType;
            }

            @Override
            public long length() {
                return file.length();
            }

            @Override
            public void writeTo(BufferSink sink) throws IOException {
                try (Source source = IoKit.source(file)) {
                    sink.writeAll(source);
                }
            }
        };
    }

    /**
     * 获取媒体类型
     *
     * @return 媒体类型（可能为 null）
     */
    public abstract MediaType mediaType();

    /**
     * 获取内容长度
     * <p>
     * 返回写入 <code>sink</code> 的字节数，如果未知则返回 -1。
     * </p>
     *
     * @return 内容长度
     * @throws IOException 如果无法确定长度
     */
    public long length() throws IOException {
        return -1;
    }

    /**
     * 将请求体内容写入输出流
     *
     * @param sink 输出流
     * @throws IOException 如果写入失败
     */
    public abstract void writeTo(BufferSink sink) throws IOException;

    /**
     * 检查是否为双工请求体
     * <p>
     * 双工请求体允许请求和响应数据交错传输，仅支持 HTTP/2。 默认返回 false，除非子类重写。
     * </p>
     *
     * @return true 如果是双工请求体
     */
    public boolean isDuplex() {
        return false;
    }

    /**
     * 检查是否为一次性请求体
     * <p>
     * 一次性请求体只能传输一次，通常用于破坏性写入场景。 默认返回 false，除非子类重写。
     * </p>
     *
     * @return true 如果是一次性请求体
     */
    public boolean isOneShot() {
        return false;
    }

}