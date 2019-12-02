/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http.bodys;

import org.aoju.bus.core.consts.MediaType;
import org.aoju.bus.core.io.segment.BufferSink;
import org.aoju.bus.core.io.segment.ByteString;
import org.aoju.bus.core.io.segment.Source;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.http.Internal;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Kimi Liu
 * @version 5.3.0
 * @since JDK 1.8+
 */
public abstract class RequestBody {

    public static RequestBody create(MediaType contentType, String content) {
        Charset charset = org.aoju.bus.core.consts.Charset.UTF_8;
        if (contentType != null) {
            charset = contentType.charset();
            if (charset == null) {
                charset = org.aoju.bus.core.consts.Charset.UTF_8;
                contentType = MediaType.valueOf(contentType + "; charset=utf-8");
            }
        }
        byte[] bytes = content.getBytes(charset);
        return create(contentType, bytes);
    }

    public static RequestBody create(
            final MediaType contentType, final ByteString content) {
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

    public static RequestBody create(final MediaType contentType, final byte[] content) {
        return create(contentType, content, 0, content.length);
    }

    public static RequestBody create(final MediaType contentType, final byte[] content,
                                     final int offset, final int byteCount) {
        if (content == null) throw new NullPointerException("content == null");
        Internal.checkOffsetAndCount(content.length, offset, byteCount);
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
                    Internal.closeQuietly(source);
                }
            }
        };
    }

    public abstract MediaType contentType();

    public long contentLength() throws IOException {
        return -1;
    }

    public abstract void writeTo(BufferSink sink) throws IOException;

}
