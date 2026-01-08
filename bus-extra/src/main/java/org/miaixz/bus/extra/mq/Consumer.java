/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.extra.mq;

import java.io.Closeable;

import org.miaixz.bus.core.xyz.ThreadKit;

/**
 * Represents a message consumer interface for Message Queue (MQ) systems. This interface defines methods for
 * subscribing to messages, either individually or continuously, and handling them with a {@link MessageHandler}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Consumer extends Closeable {

    /**
     * Subscribes to a single message and processes it using the provided message handler. This method is typically
     * blocking and waits for one message to arrive.
     *
     * @param messageHandler The {@link MessageHandler} to process the received message.
     */
    void subscribe(MessageHandler messageHandler);

    /**
     * Continuously subscribes to messages in an asynchronous manner. This method starts a new thread that constantly
     * listens for and processes messages using the given {@link MessageHandler}. The loop runs indefinitely until the
     * consumer is closed.
     *
     * @param messageHandler The {@link MessageHandler} to process the received messages.
     */
    default void listen(final MessageHandler messageHandler) {
        ThreadKit.execAsync(() -> {
            for (;;) {
                this.subscribe(messageHandler);
            }
        });
    }

}
