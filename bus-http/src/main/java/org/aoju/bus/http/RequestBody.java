package org.aoju.bus.http;

import org.aoju.bus.core.consts.MediaType;
import org.aoju.bus.core.io.BufferedSink;
import org.aoju.bus.core.io.ByteString;
import org.aoju.bus.core.io.Source;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.http.internal.Internal;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class RequestBody {
    /**
     * Returns a new request body that transmits {@code content}. If {@code contentType} is non-null
     * and lacks a charset, this will use UTF-8.
     */
    public static RequestBody create(MediaType contentType, String content) {
        Charset charset = org.aoju.bus.core.consts.Charset.UTF_8;
        if (contentType != null) {
            charset = contentType.charset();
            if (charset == null) {
                charset = org.aoju.bus.core.consts.Charset.UTF_8;
                contentType = MediaType.get(contentType + "; charset=utf-8");
            }
        }
        byte[] bytes = content.getBytes(charset);
        return create(contentType, bytes);
    }

    /**
     * Returns a new request body that transmits {@code content}.
     */
    public static RequestBody create(
            final MediaType contentType, final ByteString content) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() throws IOException {
                return content.size();
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                sink.write(content);
            }
        };
    }

    /**
     * Returns a new request body that transmits {@code content}.
     */
    public static RequestBody create(final MediaType contentType, final byte[] content) {
        return create(contentType, content, 0, content.length);
    }

    /**
     * Returns a new request body that transmits {@code content}.
     */
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
            public void writeTo(BufferedSink sink) throws IOException {
                sink.write(content, offset, byteCount);
            }
        };
    }

    /**
     * Returns a new request body that transmits the content of {@code file}.
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
            public void writeTo(BufferedSink sink) throws IOException {
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

    /**
     * Returns the Content-Type header for this body.
     */
    public abstract MediaType contentType();

    /**
     * Returns the number of bytes that will be written to {@code sink} in a call to {@link #writeTo},
     * or -1 if that count is unknown.
     */
    public long contentLength() throws IOException {
        return -1;
    }

    /**
     * Writes the content of this request to {@code sink}.
     */
    public abstract void writeTo(BufferedSink sink) throws IOException;

}
