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
package org.miaixz.bus.socket.metric.handler;

import org.miaixz.bus.socket.Handler;
import org.miaixz.bus.socket.Monitor;
import org.miaixz.bus.socket.Plugin;
import org.miaixz.bus.socket.Session;
import org.miaixz.bus.socket.Status;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstract base class for message handlers, providing common functionality and integrating with plugins. This class
 * implements both {@link Handler} and {@link Monitor} interfaces.
 *
 * @param <T> the type of message handled by this processor
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class AbstractMessageHandler<T> implements Handler<T>, Monitor {

    /**
     * A list of plugins registered with this message handler.
     */
    private final List<Plugin<T>> plugins = new ArrayList<>();

    /**
     * Description inherited from parent class or interface.
     * <p>
     * This implementation notifies all registered plugins after a read operation completes.
     * </p>
     *
     * @param session  the communication session from which data was read
     * @param readSize the number of bytes read from the channel
     */
    @Override
    public final void afterRead(Session session, int readSize) {
        for (Plugin<T> plugin : plugins) {
            plugin.afterRead(session, readSize);
        }
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * This implementation notifies all registered plugins after a write operation completes.
     * </p>
     *
     * @param session   the communication session to which data was written
     * @param writeSize the number of bytes written to the channel
     */
    @Override
    public final void afterWrite(Session session, int writeSize) {
        for (Plugin<T> plugin : plugins) {
            plugin.afterWrite(session, writeSize);
        }
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * This implementation notifies all registered plugins before a read operation begins.
     * </p>
     *
     * @param session the communication session from which data will be read
     */
    @Override
    public final void beforeRead(Session session) {
        for (Plugin<T> plugin : plugins) {
            plugin.beforeRead(session);
        }
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * This implementation notifies all registered plugins before a write operation begins.
     * </p>
     *
     * @param session the communication session to which data will be written
     */
    @Override
    public final void beforeWrite(Session session) {
        for (Plugin<T> plugin : plugins) {
            plugin.beforeWrite(session);
        }
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * This implementation passes the channel through all registered plugins, allowing each to accept, reject, or wrap
     * the channel. If any plugin returns {@code null}, the connection is rejected.
     * </p>
     *
     * @param channel the asynchronous socket channel representing the incoming connection
     * @return the accepted channel (potentially wrapped by plugins), or {@code null} to reject the connection
     */
    @Override
    public final AsynchronousSocketChannel shouldAccept(AsynchronousSocketChannel channel) {
        AsynchronousSocketChannel acceptChannel = channel;
        for (Plugin<T> plugin : plugins) {
            acceptChannel = plugin.shouldAccept(acceptChannel);
            if (acceptChannel == null) {
                return null;
            }
        }
        return acceptChannel;
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * This implementation passes the message through all registered plugins. If all plugins return {@code true}, the
     * message is forwarded to {@link #process0(Session, Object)} for business logic processing.
     * </p>
     *
     * @param session the communication session through which the message was received
     * @param data    the business message to be processed
     */
    @Override
    public final void process(Session session, T data) {
        boolean flag = true;
        for (Plugin<T> plugin : plugins) {
            if (!plugin.process(session, data)) {
                flag = false;
            }
        }
        if (flag) {
            process0(session, data);
        }
    }

    /**
     * Processes the received message. This is the abstract method that concrete implementations must provide their
     * business logic in.
     *
     * @param session the communication session
     * @param data    the business message to be processed
     * @see Handler#process(Session, Object)
     */
    public abstract void process0(Session session, T data);

    /**
     * Handles state machine events. This method is invoked by the framework when a specific {@link Status} event
     * occurs. Plugins are notified first, then the abstract {@code stateEvent0} method is called.
     *
     * @param session   the {@link Session} object that triggered the state event
     * @param status    the {@link Status} enumeration indicating the type of event
     * @param throwable an optional {@link Throwable} object if an exception is associated with the event, otherwise
     *                  {@code null}
     */
    @Override
    public final void stateEvent(Session session, Status status, Throwable throwable) {
        for (Plugin<T> plugin : plugins) {
            plugin.stateEvent(status, session, throwable);
        }
        stateEvent0(session, status, throwable);
    }

    /**
     * Handles state machine events. This is the abstract method that concrete implementations must provide their custom
     * handling logic in.
     *
     * @param session   the communication session
     * @param status    the status of the event
     * @param throwable the exception associated with the event, if any
     * @see #stateEvent(Session, Status, Throwable)
     */
    public abstract void stateEvent0(Session session, Status status, Throwable throwable);

    /**
     * Adds a plugin to this message handler.
     *
     * @param plugin the {@link Plugin} to add
     */
    public final void addPlugin(Plugin<T> plugin) {
        this.plugins.add(plugin);
    }

}
