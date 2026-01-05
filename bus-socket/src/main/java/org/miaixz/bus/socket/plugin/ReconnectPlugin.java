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
package org.miaixz.bus.socket.plugin;

import org.miaixz.bus.socket.Session;
import org.miaixz.bus.socket.Status;
import org.miaixz.bus.socket.accord.AioClient;

import java.nio.channels.AsynchronousChannelGroup;

/**
 * A plugin that provides automatic reconnection functionality for clients upon disconnection.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
class ReconnectPlugin extends AbstractPlugin {

    /**
     * The asynchronous channel group to be used for reconnection, if provided.
     */
    private final AsynchronousChannelGroup asynchronousChannelGroup;
    /**
     * The AIO client instance that this plugin manages for reconnection.
     */
    private final AioClient client;
    /**
     * A flag indicating whether the plugin has been explicitly shut down.
     */
    private boolean shutdown = false;

    /**
     * Constructs a {@code ReconnectPlugin} for the given client.
     *
     * @param client the {@link AioClient} instance to manage
     */
    public ReconnectPlugin(AioClient client) {
        this(client, null);
    }

    /**
     * Constructs a {@code ReconnectPlugin} for the given client and an optional asynchronous channel group.
     *
     * @param client                   the {@link AioClient} instance to manage
     * @param asynchronousChannelGroup an optional {@link AsynchronousChannelGroup} to use for reconnection
     */
    public ReconnectPlugin(AioClient client, AsynchronousChannelGroup asynchronousChannelGroup) {
        this.client = client;
        this.asynchronousChannelGroup = asynchronousChannelGroup;
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public void stateEvent(Status status, Session session, Throwable throwable) {
        // Only attempt reconnection if the session is closed and the plugin is not shut down.
        if (status != Status.SESSION_CLOSED || shutdown) {
            return;
        }
        try {
            if (asynchronousChannelGroup == null) {
                client.start();
            } else {
                client.start(asynchronousChannelGroup);
            }
        } catch (Exception e) {
            shutdown = true;
            e.printStackTrace();
        }

    }

    /**
     * Shuts down the reconnection plugin, preventing further reconnection attempts.
     */
    public void shutdown() {
        shutdown = true;
    }

}
