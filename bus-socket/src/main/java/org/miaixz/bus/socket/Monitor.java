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
package org.miaixz.bus.socket;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * Network monitor interface, providing communication-level monitoring capabilities.
 * <p>
 * This interface does not provide a separate configuration interface for monitoring services. Users only need to
 * implement this {@code Monitor} interface in their {@code Handler} implementation. When registering a message
 * processor, if the service detects that the processor also implements the {@code Monitor} interface, then this monitor
 * will become active.
 * </p>
 * 
 * <pre>
 * public class MessageProcessorImpl implements Handler, Monitor {
 *
 * }
 * </pre>
 * 
 * Note: When implementing this interface, pay attention to the return value of the {@code shouldAccept} method. If
 * there are no special requirements, return {@code true} directly; otherwise, returning {@code false} will reject the
 * connection.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Monitor {

    /**
     * Monitors an incoming connection.
     *
     * @param channel the {@link AsynchronousSocketChannel} object for the newly established connection
     * @return the {@link AsynchronousSocketChannel} if the connection should be accepted, or {@code null} if it should
     *         be rejected
     */
    AsynchronousSocketChannel shouldAccept(AsynchronousSocketChannel channel);

    /**
     * Monitors the number of bytes read by the session after a read callback is triggered.
     *
     * @param session  the {@link Session} object currently performing the read operation
     * @param readSize the number of bytes read
     */
    void afterRead(Session session, int readSize);

    /**
     * Called before data is read from the session.
     *
     * @param session the current {@link Session} object
     */
    void beforeRead(Session session);

    /**
     * Monitors the number of bytes written by the session after a write callback is triggered.
     *
     * @param session   the {@link Session} object currently performing the write callback
     * @param writeSize the number of bytes written in this operation
     */
    void afterWrite(Session session, int writeSize);

    /**
     * Called before data is written to the session.
     *
     * @param session the current {@link Session} object
     */
    void beforeWrite(Session session);

}
