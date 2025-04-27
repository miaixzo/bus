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
package org.miaixz.bus.http.socket;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;

/**
 * web socket协议信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WebSocketProtocol {

    /**
     * 须附加到响应标头中的键的魔法值
     */
    static final String ACCEPT_MAGIC = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    /**
     * 标记这是否是消息中的最后一个片段
     */
    static final int B0_FLAG_FIN = 0b10000000;
    /**
     * 保留标志1。必须是0
     */
    static final int B0_FLAG_RSV1 = 0b01000000;
    /**
     * 保留标志2。必须是0
     */
    static final int B0_FLAG_RSV2 = 0b00100000;
    /**
     * 保留标志3。必须是0
     */
    static final int B0_FLAG_RSV3 = 0b00010000;
    /**
     * 帧操作码的字节0掩码
     */
    static final int B0_MASK_OPCODE = 0b00001111;
    /**
     * 在操作码中标记，指示控制帧
     */
    static final int OPCODE_FLAG_CONTROL = 0b00001000;

    /**
     * 字节1标记是否屏蔽了有效负载数据 如果设置了这个标志，接下来的四个字节表示掩码键 这些字节出现在{@link #B1_MASK_LENGTH}指定的任何附加字节之后
     */
    static final int B1_FLAG_MASK = 0b10000000;
    /**
     * 有效负载长度的字节1掩码 如果这个值是{@link #PAYLOAD_SHORT}，接下来的两个字节表示长度 如果这个值是{@link #PAYLOAD_LONG}，接下来的8个字节表示长度
     */
    static final int B1_MASK_LENGTH = 0b01111111;

    static final int OPCODE_CONTINUATION = 0x0;
    static final int OPCODE_TEXT = 0x1;
    static final int OPCODE_BINARY = 0x2;

    static final int OPCODE_CONTROL_CLOSE = 0x8;
    static final int OPCODE_CONTROL_PING = 0x9;
    static final int OPCODE_CONTROL_PONG = 0xa;

    /**
     * 帧有效载荷的最大长度。如果框架类型支持较大的有效负载， 则可以使用特殊值{@link #PAYLOAD_SHORT}或{@link #PAYLOAD_LONG}
     */
    static final long PAYLOAD_BYTE_MAX = 125L;
    /**
     * 关闭消息的最大长度(以字节为单位)
     */
    static final long CLOSE_MESSAGE_MAX = PAYLOAD_BYTE_MAX - 2;
    /**
     * 表示后面两个字节是无符号长度的{@link #B1_MASK_LENGTH}的值
     */
    static final int PAYLOAD_SHORT = 126;
    /**
     * 帧有效载荷的最大长度表示为{@link #PAYLOAD_SHORT}
     */
    static final long PAYLOAD_SHORT_MAX = 0xffffL;
    /**
     * {@link #B1_MASK_LENGTH}的值，该值指示接下来的八个字节是无符号长度
     */
    static final int PAYLOAD_LONG = 127;
    /**
     * 在侦听器中抛出未检查异常时使用
     */
    static final int CLOSE_CLIENT_GOING_AWAY = 1001;
    /**
     * 当接收到一个空的关闭帧时使用,没有状态码)
     */
    static final int CLOSE_NO_STATUS_CODE = 1005;

    private WebSocketProtocol() {
        throw new AssertionError("No instances.");
    }

    static void toggleMask(Buffer.UnsafeCursor cursor, byte[] key) {
        int keyIndex = 0;
        int keyLength = key.length;
        do {
            byte[] buffer = cursor.data;
            for (int i = cursor.start, end = cursor.end; i < end; i++, keyIndex++) {
                keyIndex %= keyLength;
                buffer[i] = (byte) (buffer[i] ^ key[keyIndex]);
            }
        } while (cursor.next() != -1);
    }

    static String closeCodeExceptionMessage(int code) {
        if (code < 1000 || code >= 5000) {
            return "Code must be in range [1000,5000): " + code;
        } else if ((code >= 1004 && code <= 1006) || (code >= 1012 && code <= 2999)) {
            return "Code " + code + " is reserved and may not be used.";
        } else {
            return null;
        }
    }

    static void validateCloseCode(int code) {
        String message = closeCodeExceptionMessage(code);
        if (null != message)
            throw new IllegalArgumentException(message);
    }

    public static String acceptHeader(String key) {
        return ByteString.encodeUtf8(key + WebSocketProtocol.ACCEPT_MAGIC).sha1().base64();
    }

}
