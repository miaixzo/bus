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

import java.util.Map;

/**
 * Long-lived fabric session contract for opened protocol connections.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Session {

    /**
     * Returns the session address.
     *
     * @return session address
     */
    Address address();

    /**
     * Returns the lifecycle state.
     *
     * @return lifecycle state
     */
    Status state();

    /**
     * Returns whether the session is opened.
     *
     * @return true when opened
     */
    boolean opened();

    /**
     * Sends a payload through the session.
     *
     * @param payload payload
     * @return send call
     */
    Call<Void> send(Payload payload);

    /**
     * Closes the session normally.
     *
     * @return true when this invocation changed the session to closed
     */
    boolean close();

    /**
     * Cancels the session forcefully.
     *
     * @return true when this invocation changed the session to cancelled
     */
    boolean cancel();

    /**
     * Returns session attributes.
     *
     * @return immutable attributes snapshot
     */
    Map<String, Object> attributes();

}
