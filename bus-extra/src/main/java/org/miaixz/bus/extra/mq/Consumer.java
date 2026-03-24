/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.extra.mq;

import java.io.Closeable;

import org.miaixz.bus.core.xyz.ThreadKit;

/**
 * Represents a message consumer interface for Message Queue (MQ) systems. This interface defines methods for
 * subscribing to messages, either individually or continuously, and handling them with a {@link MessageHandler}.
 *
 * @author Kimi Liu
 * @since Java 21+
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
