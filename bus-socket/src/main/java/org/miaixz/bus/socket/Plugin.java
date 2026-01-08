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
package org.miaixz.bus.socket;

/**
 * Represents a plugin interface for extending socket communication functionality.
 * <p>
 * Plugins can intercept and modify the behavior of message processing and monitor various state events. This interface
 * extends {@link Monitor}, allowing plugins to also act as network monitors.
 * </p>
 *
 * @param <T> the type of message object entity handled by this plugin
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Plugin<T> extends Monitor {

    /**
     * Pre-processes the incoming request message and decides whether to proceed with subsequent {@link Handler}
     * processing.
     * <p>
     * If this method returns {@code false}, the current message will be ignored and not passed to the {@code Handler}.
     * If it returns {@code true}, the message will proceed normally to {@code Handler.process}.
     * </p>
     *
     * @param session the communication session
     * @param data    the business message to be processed
     * @return {@code true} if the message should be processed by the handler, {@code false} otherwise
     */
    boolean process(Session session, T data);

    /**
     * Listens to state machine events.
     * <p>
     * This method is triggered by the framework when a specific {@link Status} event occurs.
     * </p>
     *
     * @param status    the {@link Status} enumeration indicating the type of event
     * @param session   the {@link Session} object that triggered the state event
     * @param throwable an optional {@link Throwable} object if an exception is associated with the event, otherwise
     *                  {@code null}
     * @see Handler#stateEvent(Session, Status, Throwable)
     */
    void stateEvent(Status status, Session session, Throwable throwable);

}
