/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
package org.aoju.bus.http.socket;

import org.aoju.bus.core.io.ByteString;
import org.aoju.bus.http.bodys.AbstractBody;
import org.aoju.bus.http.metric.TaskExecutor;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Kimi Liu
 * @version 6.0.0
 * @since JDK 1.8+
 */
public class WebSocketMessage extends AbstractBody implements WebSocketCover.Sockets.Message {

    private String text;
    private ByteString bytes;

    public WebSocketMessage(String text, TaskExecutor taskExecutor, Charset charset) {
        super(taskExecutor, charset);
        this.text = text;
    }

    public WebSocketMessage(ByteString bytes, TaskExecutor taskExecutor, Charset charset) {
        super(taskExecutor, charset);
        this.bytes = bytes;
    }

    @Override
    public boolean isText() {
        return text != null;
    }

    @Override
    public byte[] toBytes() {
        if (text != null) {
            return text.getBytes(StandardCharsets.UTF_8);
        }
        if (bytes != null) {
            return bytes.toByteArray();
        }
        return null;
    }

    @Override
    public String toString() {
        if (text != null) {
            return text;
        }
        if (bytes != null) {
            return bytes.utf8();
        }
        return null;
    }

    @Override
    public ByteString toByteString() {
        if (text != null) {
            return ByteString.encodeUtf8(text);
        }
        return bytes;
    }

    @Override
    public Reader toCharStream() {
        return new InputStreamReader(toByteStream());
    }

    @Override
    public InputStream toByteStream() {
        if (text != null) {
            return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        }
        if (bytes != null) {
            ByteBuffer buffer = bytes.asByteBuffer();
            return new InputStream() {

                @Override
                public int read() throws IOException {
                    if (buffer.hasRemaining()) {
                        return buffer.get();
                    }
                    return -1;
                }
            };
        }
        return null;
    }

}
