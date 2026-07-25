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
package org.miaixz.bus.fabric;

/**
 * User callback contract for received session messages, failures, and close notifications.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Handler {

    /**
     * Handles a message delivered by an open session.
     *
     * @param session session that received the message
     * @param message protocol message delivered to the application
     */
    void message(Session session, Message message);

    /**
     * Handles a terminal or operation failure reported by a session.
     *
     * @param session session that reported the failure
     * @param cause   error associated with the failed session operation
     */
    default void failure(final Session session, final Throwable cause) {
        // Default failure handler intentionally performs no action.
    }

    /**
     * Handles notification that a session has closed.
     *
     * @param session session whose close lifecycle completed
     */
    default void closed(final Session session) {
        // Default close handler intentionally performs no action.
    }

}
