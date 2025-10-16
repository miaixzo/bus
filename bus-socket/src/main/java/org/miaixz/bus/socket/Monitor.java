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
 * @since Java 17+
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
