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

import org.aoju.bus.core.io.segment.Buffer;
import org.aoju.bus.core.io.segment.BufferSource;
import org.aoju.bus.core.io.segment.ByteString;
import org.aoju.bus.core.lang.MediaType;
import org.aoju.bus.http.Call;
import org.aoju.bus.http.Callback;
import org.aoju.bus.http.Internal;
import org.aoju.bus.http.Response;

import java.io.*;
import java.nio.charset.Charset;

/**
 * A first-shot stream from the origin server to the client application with the raw bytes of the
 * response body. Each response body is supported by an active connection to the webserver. This
 * imposes both obligations and limits on the client application.
 *
 * <h3>The response body must be closed.</h3>
 * <p>
 * Each response body is backed by a limited resource like a socket (live network responses) or
 * an open file (for cached responses). Failing to close the response body will leak resources and
 * may ultimately cause the application to slow down or crash.
 *
 * <p>Both this class and {@link Response} implement {@link Closeable}. Closing a response simply
 * closes its response body. If you invoke {@link Call#execute()} or implement {@link
 * Callback#onResponse} you must close this body by calling any of the following methods:
 *
 * <ul>
 * <li>Response.close()</li>
 * <li>Response.body().close()</li>
 * <li>Response.body().source().close()</li>
 * <li>Response.body().charStream().close()</li>
 * <li>Response.body().byteStream().close()</li>
 * <li>Response.body().bytes()</li>
 * <li>Response.body().string()</li>
 * </ul>
 *
 * <p>There is no benefit to invoking multiple {@code close()} methods for the same response body.
 *
 * <p>For synchronous calls, the easiest way to make sure a response body is closed is with a {@code
 * try} block. With this structure the compiler inserts an implicit {@code finally} clause that
 * calls {@code close()} for you.
 *
 * <pre>   {@code
 *
 *   Call call = client.newCall(request);
 *   try (Response response = call.execute()) {
 *     ... // Use the response.
 *   }
 * }</pre>
 * <p>
 * You can use a similar block for asynchronous calls: <pre>   {@code
 *
 *   Call call = client.newCall(request);
 *   call.enqueue(new Callback() {
 *     public void onResponse(Call call, Response response) throws IOException {
 *       try (ResponseBody responseBody = response.body()) {
 *         ... // Use the response.
 *       }
 *     }
 *
 *     public void onFailure(Call call, IOException e) {
 *       ... // Handle the failure.
 *     }
 *   });
 * }</pre>
 * <p>
 * These examples will not work if you're consuming the response body on another thread. In such
 * cases the consuming thread must call {@link #close} when it has finished reading the response
 * body.
 *
 * <h3>The response body can be consumed only once.</h3>
 *
 * <p>This class may be used to stream very large responses. For example, it is possible to use this
 * class to read a response that is larger than the entire memory allocated to the current process.
 * It can even stream a response larger than the total storage on the current device, which is a
 * core requirement for video streaming applications.
 *
 * <p>Because this class does not buffer the full response in memory, the application may not
 * re-read the bytes of the response. Use this first shot to read the entire response into memory with
 * {@link #bytes()} or {@link #string()}. Or stream the response with either {@link #source()},
 * {@link #byteStream()}, or {@link #charStream()}.
 *
 * @author Kimi Liu
 * @version 5.3.3
 * @since JDK 1.8+
 */
public abstract class ResponseBody implements Closeable {

    /**
     * Multiple calls to {@link #charStream()} must return the same instance.
     */
    private Reader reader;

    public static ResponseBody create(MediaType contentType, String content) {
        Charset charset = org.aoju.bus.core.lang.Charset.UTF_8;
        if (contentType != null) {
            charset = contentType.charset();
            if (charset == null) {
                charset = org.aoju.bus.core.lang.Charset.UTF_8;
                contentType = MediaType.valueOf(contentType + "; charset=utf-8");
            }
        }
        Buffer buffer = new Buffer().writeString(content, charset);
        return create(contentType, buffer.size(), buffer);
    }

    public static ResponseBody create(final MediaType contentType, byte[] content) {
        Buffer buffer = new Buffer().write(content);
        return create(contentType, content.length, buffer);
    }

    public static ResponseBody create(MediaType contentType, ByteString content) {
        Buffer buffer = new Buffer().write(content);
        return create(contentType, content.size(), buffer);
    }

    public static ResponseBody create(final MediaType contentType,
                                      final long contentLength, final BufferSource content) {
        if (content == null) throw new NullPointerException("source == null");
        return new ResponseBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return contentLength;
            }

            @Override
            public BufferSource source() {
                return content;
            }
        };
    }

    public abstract MediaType contentType();

    public abstract long contentLength();

    public final InputStream byteStream() {
        return source().inputStream();
    }

    public abstract BufferSource source();

    public final byte[] bytes() throws IOException {
        long contentLength = contentLength();
        if (contentLength > Integer.MAX_VALUE) {
            throw new IOException("Cannot buffer entire body for content length: " + contentLength);
        }

        BufferSource source = source();
        byte[] bytes;
        try {
            bytes = source.readByteArray();
        } finally {
            Internal.closeQuietly(source);
        }
        if (contentLength != -1 && contentLength != bytes.length) {
            throw new IOException("Content-Length ("
                    + contentLength
                    + ") and stream length ("
                    + bytes.length
                    + ") disagree");
        }
        return bytes;
    }

    public final Reader charStream() {
        Reader r = reader;
        return r != null ? r : (reader = new BomAwareReader(source(), charset()));
    }

    public final String string() throws IOException {
        BufferSource source = source();
        try {
            Charset charset = Internal.bomAwareCharset(source, charset());
            return source.readString(charset);
        } finally {
            Internal.closeQuietly(source);
        }
    }

    private Charset charset() {
        MediaType contentType = contentType();
        return contentType != null ? contentType.charset(org.aoju.bus.core.lang.Charset.UTF_8) : org.aoju.bus.core.lang.Charset.UTF_8;
    }

    @Override
    public void close() {
        Internal.closeQuietly(source());
    }

    static final class BomAwareReader extends Reader {
        private final BufferSource source;
        private final Charset charset;

        private boolean closed;
        private Reader delegate;

        BomAwareReader(BufferSource source, Charset charset) {
            this.source = source;
            this.charset = charset;
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            if (closed) throw new IOException("Stream closed");

            Reader delegate = this.delegate;
            if (delegate == null) {
                Charset charset = Internal.bomAwareCharset(source, this.charset);
                delegate = this.delegate = new InputStreamReader(source.inputStream(), charset);
            }
            return delegate.read(cbuf, off, len);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            if (delegate != null) {
                delegate.close();
            } else {
                source.close();
            }
        }
    }

}
