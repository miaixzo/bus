/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org sandao and other contributors.             ~
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
package org.miaixz.bus.socket.metric.message;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.socket.Message;
import org.miaixz.bus.socket.Session;
import org.miaixz.bus.socket.metric.decoder.FixedLengthFrameDecoder;

/**
 * 字符模式
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StringMessage implements Message<String> {

    private final Charset charset;

    private final Map<Session, FixedLengthFrameDecoder> decoderMap = new ConcurrentHashMap<>();
    private long lastClearTime = System.currentTimeMillis();

    public StringMessage(Charset charset) {
        this.charset = charset;
    }

    public StringMessage() {
        this(org.miaixz.bus.core.lang.Charset.UTF_8);
    }

    @Override
    public String decode(ByteBuffer readBuffer, Session session) {
        if (System.currentTimeMillis() - lastClearTime > 5000) {
            lastClearTime = System.currentTimeMillis();
            decoderMap.keySet().stream().filter(Session::isInvalid).forEach(decoderMap::remove);
        }
        FixedLengthFrameDecoder decoder = decoderMap.get(session);
        // 消息长度超过缓冲区容量
        if (decoder != null) {
            String content = bigContent(readBuffer, decoder);
            // 解码成功,释放解码器
            if (content != null) {
                decoderMap.remove(session);
            }
            return content;
        }

        int remaining = readBuffer.remaining();
        if (remaining < Integer.BYTES) {
            return null;
        }
        readBuffer.mark();
        int length = readBuffer.getInt();
        // 消息长度超过缓冲区容量引发的半包,启用定长消息解码器,本次解码失败
        if (length + Integer.BYTES > readBuffer.capacity()) {
            FixedLengthFrameDecoder fixedLengthFrameDecoder = new FixedLengthFrameDecoder(length);
            decoderMap.put(session, fixedLengthFrameDecoder);
            return null;
        }
        // 半包，解码失败
        if (length > readBuffer.remaining()) {
            readBuffer.reset();
            return null;
        }
        return convert(readBuffer, length);
    }

    /**
     * 大消息体解码
     */
    private String bigContent(ByteBuffer readBuffer, FixedLengthFrameDecoder decoder) {
        if (!decoder.decode(readBuffer)) {
            return null;
        }
        ByteBuffer byteBuffer = decoder.getBuffer();
        return convert(byteBuffer, byteBuffer.capacity());
    }

    /**
     * 消息解码
     */
    private String convert(ByteBuffer byteBuffer, int length) {
        byte[] b = new byte[length];
        byteBuffer.get(b);
        return new String(b, charset);
    }

}
