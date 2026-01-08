/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org sandao and other contributors.             ~
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

import org.miaixz.bus.socket.Message;
import org.miaixz.bus.socket.Session;
import org.miaixz.bus.socket.metric.decoder.FixedLengthFrameDecoder;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A message implementation for handling string messages.
 * <p>
 * This class implements the {@link Message} interface to decode byte buffers into strings. It supports messages
 * prefixed with their length and can handle fragmented messages using a {@link FixedLengthFrameDecoder} for larger
 * messages.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StringMessage implements Message<String> {

    /**
     * The character set used for encoding and decoding strings.
     */
    private final Charset charset;

    /**
     * A map to store {@link FixedLengthFrameDecoder} instances per session for handling large, fragmented messages.
     */
    private final Map<Session, FixedLengthFrameDecoder> decoderMap = new ConcurrentHashMap<>();
    /**
     * The last time the decoder map was cleared, used for periodic cleanup of invalid sessions.
     */
    private long lastClearTime = System.currentTimeMillis();

    /**
     * Constructs a {@code StringMessage} with the specified character set.
     *
     * @param charset the character set to use for string encoding/decoding
     */
    public StringMessage(Charset charset) {
        this.charset = charset;
    }

    /**
     * Constructs a {@code StringMessage} with the default character set (UTF-8).
     */
    public StringMessage() {
        this(org.miaixz.bus.core.lang.Charset.UTF_8);
    }

    @Override
    public String decode(ByteBuffer readBuffer, Session session) {
        // Periodically clean up decoders for invalid sessions
        if (System.currentTimeMillis() - lastClearTime > 5000) {
            lastClearTime = System.currentTimeMillis();
            decoderMap.keySet().stream().filter(Session::isInvalid).forEach(decoderMap::remove);
        }
        FixedLengthFrameDecoder decoder = decoderMap.get(session);
        // If a decoder exists, it means a large message is being fragmented
        if (decoder != null) {
            String content = bigContent(readBuffer, decoder);
            // If decoding is successful, remove the decoder
            if (content != null) {
                decoderMap.remove(session);
            }
            return content;
        }

        int remaining = readBuffer.remaining();
        // Not enough bytes to read the length prefix
        if (remaining < Integer.BYTES) {
            return null;
        }
        readBuffer.mark(); // Mark current position
        int length = readBuffer.getInt(); // Read message length
        // If message length exceeds buffer capacity, it's a fragmented message. Enable fixed-length decoder.
        if (length + Integer.BYTES > readBuffer.capacity()) {
            FixedLengthFrameDecoder fixedLengthFrameDecoder = new FixedLengthFrameDecoder(length);
            decoderMap.put(session, fixedLengthFrameDecoder);
            readBuffer.reset(); // Reset to before reading length
            return null;
        }
        // Partial package, decoding failed
        if (length > readBuffer.remaining()) {
            readBuffer.reset(); // Reset to before reading length
            return null;
        }
        return convert(readBuffer, length);
    }

    /**
     * Decodes a large message body using a {@link FixedLengthFrameDecoder}.
     *
     * @param readBuffer the buffer containing the incoming data
     * @param decoder    the fixed-length frame decoder for the session
     * @return the decoded string content, or {@code null} if not yet complete
     */
    private String bigContent(ByteBuffer readBuffer, FixedLengthFrameDecoder decoder) {
        if (!decoder.decode(readBuffer)) {
            return null;
        }
        ByteBuffer byteBuffer = decoder.getBuffer();
        return convert(byteBuffer, byteBuffer.capacity());
    }

    /**
     * Converts a {@link ByteBuffer} to a string using the configured character set.
     *
     * @param byteBuffer the buffer to convert
     * @param length     the length of the string in the buffer
     * @return the decoded string
     */
    private String convert(ByteBuffer byteBuffer, int length) {
        byte[] b = new byte[length];
        byteBuffer.get(b);
        return new String(b, charset);
    }

}
