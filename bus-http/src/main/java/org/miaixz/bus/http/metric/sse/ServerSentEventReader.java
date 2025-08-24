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
package org.miaixz.bus.http.metric.sse;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.SegmentBuffer;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.BufferSource;

import java.io.IOException;

/**
 * 服务器推送事件（Server-Sent Events, SSE）读取器，负责解析事件流数据。 通过读取输入流，识别事件字段（id、event、data、retry 等），并触发回调处理事件。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class ServerSentEventReader {

    /**
     * 输入流，用于读取服务器推送事件数据
     */
    private final BufferSource source;

    /**
     * 事件回调接口，用于通知事件数据和重试时间变化
     */
    private final Callback callback;

    /**
     * 上一个事件的 ID，可能为 null
     */
    private String lastId;

    /**
     * 构造一个新的 {@code ServerSentEventReader} 实例。
     *
     * @param source   输入流，包含服务器推送事件数据
     * @param callback 事件回调接口，用于处理解析的事件
     */
    public ServerSentEventReader(BufferSource source, Callback callback) {
        this.source = source;
        this.callback = callback;
        this.lastId = null;
    }

    /**
     * 处理下一个事件。如果数据部分非空，将触发一次 {@link Callback#onEvent} 调用。 在处理事件期间，可能触发多次 {@link Callback#onRetryChange} 调用。
     *
     * @return 如果达到输入流末尾（EOF），返回 false；否则返回 true
     * @throws IOException 如果发生 I/O 错误
     */
    public boolean processNextEvent() throws IOException {
        String id = lastId;
        String type = null;
        Buffer data = new Buffer();

        while (true) {
            int option = source.select(options);
            switch (option) {
            case 0: // "\r\n"
            case 1: // "\r"
            case 2: // "\n"
                completeEvent(id, type, data);
                return true;

            case 3: // "data: "
            case 4: // "data:"
                readData(source, data);
                break;

            case 5: // "data\r\n"
            case 6: // "data\r"
            case 7: // "data\n"
                data.writeByte('\n');
                break;

            case 8: // "id: "
            case 9: // "id:"
                String idValue = source.readUtf8LineStrict();
                id = idValue.isEmpty() ? null : idValue;
                break;

            case 10: // "id\r\n"
            case 11: // "id\r"
            case 12: // "id\n"
                id = null;
                break;

            case 13: // "event: "
            case 14: // "event:"
                String typeValue = source.readUtf8LineStrict();
                type = typeValue.isEmpty() ? null : typeValue;
                break;

            case 15: // "event\r\n"
            case 16: // "event\r"
            case 17: // "event\n"
                type = null;
                break;

            case 18: // "retry: "
            case 19: // "retry:"
                long retryMs = readRetryMs(source);
                if (retryMs != -1L) {
                    callback.onRetryChange(retryMs);
                }
                break;

            case -1:
                long lineEnd = source.indexOfElement(CRLF);
                if (lineEnd != -1L) {
                    // 跳过当前行和换行符
                    source.skip(lineEnd);
                    source.select(options);
                } else {
                    return false; // 没有更多换行符
                }
                break;

            default:
                throw new AssertionError();
            }
        }
    }

    /**
     * 完成一个事件的处理，如果数据非空，则保存 ID 并触发回调。
     *
     * @param id   事件 ID，可能为 null
     * @param type 事件类型，可能为 null
     * @param data 事件数据缓冲区
     * @throws IOException 如果发生 I/O 错误
     */
    private void completeEvent(String id, String type, Buffer data) throws IOException {
        if (data.size() != 0L) {
            lastId = id;
            data.skip(1L); // 跳过开头的换行符
            callback.onEvent(id, type, data.readUtf8());
        }
    }

    /** SSE 事件字段的前缀和换行符选项，用于解析事件流 */
    private static final SegmentBuffer options = SegmentBuffer.of(/* 0 */ ByteString.encodeUtf8("\r\n"),
            /* 1 */ ByteString.encodeUtf8("\r"), /* 2 */ ByteString.encodeUtf8("\n"),
            /* 3 */ ByteString.encodeUtf8("data: "), /* 4 */ ByteString.encodeUtf8("data:"),
            /* 5 */ ByteString.encodeUtf8("data\r\n"), /* 6 */ ByteString.encodeUtf8("data\r"),
            /* 7 */ ByteString.encodeUtf8("data\n"), /* 8 */ ByteString.encodeUtf8("id: "),
            /* 9 */ ByteString.encodeUtf8("id:"), /* 10 */ ByteString.encodeUtf8("id\r\n"),
            /* 11 */ ByteString.encodeUtf8("id\r"), /* 12 */ ByteString.encodeUtf8("id\n"),
            /* 13 */ ByteString.encodeUtf8("event: "), /* 14 */ ByteString.encodeUtf8("event:"),
            /* 15 */ ByteString.encodeUtf8("event\r\n"), /* 16 */ ByteString.encodeUtf8("event\r"),
            /* 17 */ ByteString.encodeUtf8("event\n"), /* 18 */ ByteString.encodeUtf8("retry: "),
            /* 19 */ ByteString.encodeUtf8("retry:"));

    /** 换行符（CRLF），用于定位事件流中的行结束 */
    private static final ByteString CRLF = ByteString.encodeUtf8("\r\n");

    /**
     * 从输入流中读取数据字段内容，添加到数据缓冲区。
     *
     * @param source 输入流
     * @param data   数据缓冲区
     * @throws IOException 如果发生 I/O 错误
     */
    private static void readData(BufferSource source, Buffer data) throws IOException {
        data.writeByte('\n');
        source.readFully(data, source.indexOfElement(CRLF));
        source.select(options); // 跳过换行符
    }

    /**
     * 读取重试时间字段（retry），解析为毫秒数。
     *
     * @param source 输入流
     * @return 重试时间（毫秒），如果解析失败返回 -1
     * @throws IOException 如果发生 I/O 错误
     */
    private static long readRetryMs(BufferSource source) throws IOException {
        String retryString = source.readUtf8LineStrict();
        try {
            return Long.parseLong(retryString);
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    /**
     * 回调接口，用于处理服务器推送事件和重试时间变化。
     */
    public interface Callback {
        /**
         * 当接收到新的事件时调用。
         *
         * @param id   事件 ID，可能为 null
         * @param type 事件类型，可能为 null
         * @param data 事件数据
         */
        void onEvent(String id, String type, String data);

        /**
         * 当重试时间（retry 字段）发生变化时调用。
         *
         * @param timeMs 重试时间（毫秒）
         */
        void onRetryChange(long timeMs);
    }

}