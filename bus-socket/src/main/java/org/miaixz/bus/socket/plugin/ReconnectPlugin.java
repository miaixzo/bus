/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org sandao and other contributors.         ~
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
package org.miaixz.bus.socket.plugin;

import org.miaixz.bus.socket.Session;
import org.miaixz.bus.socket.Status;
import org.miaixz.bus.socket.accord.AioClient;

import java.nio.channels.AsynchronousChannelGroup;

/**
 * A plugin that provides automatic reconnection functionality for clients upon disconnection.
 *
 * @author Kimi Liu
 * @since Java 21+
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
