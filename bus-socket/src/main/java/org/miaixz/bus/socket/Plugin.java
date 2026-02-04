/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
